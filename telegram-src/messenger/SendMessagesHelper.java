package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaCodecInfo;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.widget.Toast;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TimeToSampleBox.Entry;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;
import java.io.File;
import java.io.RandomAccessFile;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.QuickAckDelegate;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInlineMessage;
import org.telegram.tgnet.TLRPC.BotInlineResult;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.DecryptedMessage;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputDocument;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.InputMedia;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.ReplyMarkup;
import org.telegram.tgnet.TLRPC.TL_botInlineMediaResult;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaAuto;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaContact;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageText;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_decryptedMessage;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionAbortKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionAcceptKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionCommitKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionDeleteMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionFlushHistory;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionNoop;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionNotifyLayer;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionReadMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionRequestKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionResend;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionTyping;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAnimated;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo_layer65;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_game;
import org.telegram.tgnet.TLRPC.TL_geoPoint;
import org.telegram.tgnet.TLRPC.TL_inputDocument;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFile;
import org.telegram.tgnet.TLRPC.TL_inputMediaDocument;
import org.telegram.tgnet.TLRPC.TL_inputMediaGame;
import org.telegram.tgnet.TLRPC.TL_inputMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedDocument;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedPhoto;
import org.telegram.tgnet.TLRPC.TL_inputPeerChannel;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_inputPeerUser;
import org.telegram.tgnet.TLRPC.TL_inputPhoto;
import org.telegram.tgnet.TLRPC.TL_inputSingleMedia;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionScreenshotTaken;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageEntityBold;
import org.telegram.tgnet.TLRPC.TL_messageEntityCode;
import org.telegram.tgnet.TLRPC.TL_messageEntityItalic;
import org.telegram.tgnet.TLRPC.TL_messageEntityPre;
import org.telegram.tgnet.TLRPC.TL_messageEntityTextUrl;
import org.telegram.tgnet.TLRPC.TL_messageEntityUrl;
import org.telegram.tgnet.TLRPC.TL_messageFwdHeader;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_messages_botCallbackAnswer;
import org.telegram.tgnet.TLRPC.TL_messages_editMessage;
import org.telegram.tgnet.TLRPC.TL_messages_forwardMessages;
import org.telegram.tgnet.TLRPC.TL_messages_getBotCallbackAnswer;
import org.telegram.tgnet.TLRPC.TL_messages_messages;
import org.telegram.tgnet.TLRPC.TL_messages_sendBroadcast;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncryptedMultiMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendMessage;
import org.telegram.tgnet.TLRPC.TL_messages_sendMultiMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendScreenshotNotification;
import org.telegram.tgnet.TLRPC.TL_messages_uploadMedia;
import org.telegram.tgnet.TLRPC.TL_payments_getPaymentForm;
import org.telegram.tgnet.TLRPC.TL_payments_getPaymentReceipt;
import org.telegram.tgnet.TLRPC.TL_payments_paymentForm;
import org.telegram.tgnet.TLRPC.TL_payments_paymentReceipt;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photo;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_updateMessageID;
import org.telegram.tgnet.TLRPC.TL_updateNewChannelMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewMessage;
import org.telegram.tgnet.TLRPC.TL_updateShortSentMessage;
import org.telegram.tgnet.TLRPC.TL_user;
import org.telegram.tgnet.TLRPC.TL_userContact_old2;
import org.telegram.tgnet.TLRPC.TL_webPagePending;
import org.telegram.tgnet.TLRPC.Update;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebDocument;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.PaymentFormActivity;

public class SendMessagesHelper
  implements NotificationCenter.NotificationCenterDelegate
{
  private static volatile SendMessagesHelper[] Instance;
  private static DispatchQueue mediaSendQueue = new DispatchQueue("mediaSendQueue");
  private static ThreadPoolExecutor mediaSendThreadPool;
  private int currentAccount;
  private TLRPC.ChatFull currentChatInfo = null;
  private HashMap<String, ArrayList<DelayedMessage>> delayedMessages = new HashMap();
  private LocationProvider locationProvider = new LocationProvider(new SendMessagesHelper.LocationProvider.LocationProviderDelegate()
  {
    public void onLocationAcquired(Location paramAnonymousLocation)
    {
      SendMessagesHelper.this.sendLocation(paramAnonymousLocation);
      SendMessagesHelper.this.waitingForLocation.clear();
    }
    
    public void onUnableLocationAcquire()
    {
      HashMap localHashMap = new HashMap(SendMessagesHelper.this.waitingForLocation);
      NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.wasUnableToFindCurrentLocation, new Object[] { localHashMap });
      SendMessagesHelper.this.waitingForLocation.clear();
    }
  });
  private SparseArray<TLRPC.Message> sendingMessages = new SparseArray();
  private SparseArray<MessageObject> unsentMessages = new SparseArray();
  private HashMap<String, Boolean> waitingForCallback = new HashMap();
  private HashMap<String, MessageObject> waitingForLocation = new HashMap();
  
  static
  {
    if (Build.VERSION.SDK_INT >= 17) {}
    for (int i = Runtime.getRuntime().availableProcessors();; i = 2)
    {
      mediaSendThreadPool = new ThreadPoolExecutor(i, i, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue());
      Instance = new SendMessagesHelper[3];
      return;
    }
  }
  
  public SendMessagesHelper(int paramInt)
  {
    this.currentAccount = paramInt;
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileDidUpload);
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileDidFailUpload);
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FilePreparingStarted);
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileNewChunkAvailable);
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FilePreparingFailed);
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.httpFileDidFailedLoad);
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.httpFileDidLoaded);
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileDidFailedLoad);
      }
    });
  }
  
  private static VideoEditedInfo createCompressionSettings(String paramString)
  {
    Object localObject3 = null;
    int m = 0;
    int k = 0;
    float f1 = 0.0F;
    long l2 = 0L;
    long l4 = 0L;
    int n = 25;
    for (;;)
    {
      long l1;
      int i1;
      try
      {
        localObject1 = new IsoFile(paramString);
        localList = Path.getPaths((Container)localObject1, "/moov/trak/");
        if ((Path.getPath((Container)localObject1, "/moov/trak/mdia/minf/stbl/stsd/mp4a/") == null) && (BuildVars.LOGS_ENABLED)) {
          FileLog.d("video hasn't mp4a atom");
        }
        if (Path.getPath((Container)localObject1, "/moov/trak/mdia/minf/stbl/stsd/avc1/") != null) {
          break label1589;
        }
        if (!BuildVars.LOGS_ENABLED) {
          break label1587;
        }
        FileLog.d("video hasn't avc1 atom");
      }
      catch (Exception paramString)
      {
        List localList;
        TrackBox localTrackBox;
        long l6;
        Object localObject6;
        FileLog.e(paramString);
        return null;
      }
      if (j < localList.size())
      {
        localTrackBox = (TrackBox)localList.get(j);
        l1 = 0L;
        l6 = 0L;
        localObject2 = null;
        localObject6 = null;
        localObject1 = localObject6;
        long l3 = l1;
        long l5;
        try
        {
          localObject4 = localTrackBox.getMediaBox();
          localObject2 = localObject4;
          localObject1 = localObject6;
          l3 = l1;
          localObject6 = ((MediaBox)localObject4).getMediaHeaderBox();
          localObject2 = localObject4;
          localObject1 = localObject6;
          l3 = l1;
          long[] arrayOfLong = ((MediaBox)localObject4).getMediaInformationBox().getSampleTableBox().getSampleSizeBox().getSampleSizes();
          i = 0;
          localObject2 = localObject4;
          localObject1 = localObject6;
          l3 = l1;
          if (i < arrayOfLong.length)
          {
            l1 += arrayOfLong[i];
            i += 1;
            continue;
          }
          localObject2 = localObject4;
          localObject1 = localObject6;
          l3 = l1;
          f2 = (float)((MediaHeaderBox)localObject6).getDuration();
          localObject2 = localObject4;
          localObject1 = localObject6;
          l3 = l1;
          l5 = ((MediaHeaderBox)localObject6).getTimescale();
          f1 = f2 / (float)l5;
          l6 = (int)((float)(8L * l1) / f1);
          l5 = l1;
          localObject1 = localObject6;
          localObject2 = localObject4;
        }
        catch (Exception localException)
        {
          Object localObject4;
          FileLog.e(localException);
          l5 = l3;
          continue;
        }
        localObject6 = localTrackBox.getTrackHeaderBox();
        int i2;
        Object localObject5;
        if ((((TrackHeaderBox)localObject6).getWidth() != 0.0D) && (((TrackHeaderBox)localObject6).getHeight() != 0.0D)) {
          if ((localObject3 != null) && (((TrackHeaderBox)localObject3).getWidth() >= ((TrackHeaderBox)localObject6).getWidth()))
          {
            l1 = l4;
            i2 = k;
            localObject4 = localObject3;
            i1 = n;
            l3 = l2;
            if (((TrackHeaderBox)localObject3).getHeight() >= ((TrackHeaderBox)localObject6).getHeight()) {}
          }
          else
          {
            localObject3 = localObject6;
            m = (int)(l6 / 100000L * 100000L);
            k = m;
            i = m;
            if (m > 900000) {
              i = 900000;
            }
            l5 = l2 + l5;
            l1 = l4;
            i2 = i;
            m = k;
            localObject4 = localObject3;
            i1 = n;
            l3 = l5;
            if (localObject2 != null)
            {
              l1 = l4;
              i2 = i;
              m = k;
              localObject4 = localObject3;
              i1 = n;
              l3 = l5;
              if (localObject1 != null)
              {
                localObject2 = ((MediaBox)localObject2).getMediaInformationBox().getSampleTableBox().getTimeToSampleBox();
                l1 = l4;
                i2 = i;
                m = k;
                localObject4 = localObject3;
                i1 = n;
                l3 = l5;
                if (localObject2 != null)
                {
                  localObject2 = ((TimeToSampleBox)localObject2).getEntries();
                  l2 = 0L;
                  int i3 = Math.min(((List)localObject2).size(), 11);
                  m = 1;
                  if (m < i3)
                  {
                    l2 += ((TimeToSampleBox.Entry)((List)localObject2).get(m)).getDelta();
                    m += 1;
                    continue;
                  }
                  l1 = l4;
                  i2 = i;
                  m = k;
                  localObject5 = localObject3;
                  i1 = n;
                  l3 = l5;
                  if (l2 != 0L)
                  {
                    double d = ((MediaHeaderBox)localObject1).getTimescale();
                    l1 = l2 / (i3 - 1);
                    i1 = (int)(d / l1);
                    l3 = l5;
                    localObject5 = localObject3;
                    m = k;
                    i2 = i;
                    l1 = l4;
                  }
                }
              }
            }
          }
        }
        for (;;)
        {
          j += 1;
          l4 = l1;
          k = i2;
          localObject3 = localObject5;
          n = i1;
          l2 = l3;
          break;
          l1 = l4 + l5;
          i2 = k;
          localObject5 = localObject3;
          i1 = n;
          l3 = l2;
        }
      }
      if (localObject3 == null)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("video hasn't trackHeaderBox atom");
        }
        return null;
      }
      if (Build.VERSION.SDK_INT < 18) {
        try
        {
          localObject1 = MediaController.selectCodec("video/avc");
          if (localObject1 == null)
          {
            if (!BuildVars.LOGS_ENABLED) {
              break;
            }
            FileLog.d("no codec info for video/avc");
            break;
          }
          localObject2 = ((MediaCodecInfo)localObject1).getName();
          if ((((String)localObject2).equals("OMX.google.h264.encoder")) || (((String)localObject2).equals("OMX.ST.VFM.H264Enc")) || (((String)localObject2).equals("OMX.Exynos.avc.enc")) || (((String)localObject2).equals("OMX.MARVELL.VIDEO.HW.CODA7542ENCODER")) || (((String)localObject2).equals("OMX.MARVELL.VIDEO.H264ENCODER")) || (((String)localObject2).equals("OMX.k3.video.encoder.avc")) || (((String)localObject2).equals("OMX.TI.DUCATI1.VIDEO.H264E")))
          {
            if (!BuildVars.LOGS_ENABLED) {
              break label1597;
            }
            FileLog.d("unsupported encoder = " + (String)localObject2);
            break label1597;
          }
          if (MediaController.selectColorFormat((MediaCodecInfo)localObject1, "video/avc") == 0)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("no color format for video/avc");
            }
            return null;
          }
        }
        catch (Exception paramString)
        {
          return null;
        }
      }
      float f2 = f1 * 1000.0F;
      Object localObject1 = new VideoEditedInfo();
      ((VideoEditedInfo)localObject1).startTime = -1L;
      ((VideoEditedInfo)localObject1).endTime = -1L;
      ((VideoEditedInfo)localObject1).bitrate = k;
      ((VideoEditedInfo)localObject1).originalPath = paramString;
      ((VideoEditedInfo)localObject1).framerate = n;
      ((VideoEditedInfo)localObject1).estimatedDuration = (Math.ceil(f2));
      int i = (int)((TrackHeaderBox)localObject3).getWidth();
      ((VideoEditedInfo)localObject1).originalWidth = i;
      ((VideoEditedInfo)localObject1).resultWidth = i;
      i = (int)((TrackHeaderBox)localObject3).getHeight();
      ((VideoEditedInfo)localObject1).originalHeight = i;
      ((VideoEditedInfo)localObject1).resultHeight = i;
      Object localObject2 = ((TrackHeaderBox)localObject3).getMatrix();
      if (((Matrix)localObject2).equals(Matrix.ROTATE_90))
      {
        ((VideoEditedInfo)localObject1).rotationValue = 90;
        j = MessagesController.getGlobalMainSettings().getInt("compress_video2", 1);
        if ((((VideoEditedInfo)localObject1).originalWidth <= 1280) && (((VideoEditedInfo)localObject1).originalHeight <= 1280)) {
          break label1403;
        }
        i = 5;
        label1118:
        n = j;
        if (j >= i) {
          n = i - 1;
        }
        i1 = k;
        l1 = l2;
        if (n != i - 1) {
          switch (n)
          {
          default: 
            j = 2500000;
            f1 = 1280.0F;
            label1189:
            if (((VideoEditedInfo)localObject1).originalWidth <= ((VideoEditedInfo)localObject1).originalHeight) {
              break;
            }
          }
        }
      }
      for (f1 /= ((VideoEditedInfo)localObject1).originalWidth;; f1 /= ((VideoEditedInfo)localObject1).originalHeight)
      {
        ((VideoEditedInfo)localObject1).resultWidth = (Math.round(((VideoEditedInfo)localObject1).originalWidth * f1 / 2.0F) * 2);
        ((VideoEditedInfo)localObject1).resultHeight = (Math.round(((VideoEditedInfo)localObject1).originalHeight * f1 / 2.0F) * 2);
        i1 = k;
        l1 = l2;
        if (k != 0)
        {
          i1 = Math.min(j, (int)(m / f1));
          l1 = (i1 / 8 * f2 / 1000.0F);
        }
        if (n != i - 1) {
          break label1541;
        }
        ((VideoEditedInfo)localObject1).resultWidth = ((VideoEditedInfo)localObject1).originalWidth;
        ((VideoEditedInfo)localObject1).resultHeight = ((VideoEditedInfo)localObject1).originalHeight;
        ((VideoEditedInfo)localObject1).bitrate = m;
        ((VideoEditedInfo)localObject1).estimatedSize = ((int)new File(paramString).length());
        return (VideoEditedInfo)localObject1;
        if (((Matrix)localObject2).equals(Matrix.ROTATE_180))
        {
          ((VideoEditedInfo)localObject1).rotationValue = 180;
          break;
        }
        if (((Matrix)localObject2).equals(Matrix.ROTATE_270))
        {
          ((VideoEditedInfo)localObject1).rotationValue = 270;
          break;
        }
        ((VideoEditedInfo)localObject1).rotationValue = 0;
        break;
        label1403:
        if ((((VideoEditedInfo)localObject1).originalWidth > 848) || (((VideoEditedInfo)localObject1).originalHeight > 848))
        {
          i = 4;
          break label1118;
        }
        if ((((VideoEditedInfo)localObject1).originalWidth > 640) || (((VideoEditedInfo)localObject1).originalHeight > 640))
        {
          i = 3;
          break label1118;
        }
        if ((((VideoEditedInfo)localObject1).originalWidth > 480) || (((VideoEditedInfo)localObject1).originalHeight > 480))
        {
          i = 2;
          break label1118;
        }
        i = 1;
        break label1118;
        f1 = 432.0F;
        j = 400000;
        break label1189;
        f1 = 640.0F;
        j = 900000;
        break label1189;
        f1 = 848.0F;
        j = 1100000;
        break label1189;
      }
      label1541:
      ((VideoEditedInfo)localObject1).bitrate = i1;
      ((VideoEditedInfo)localObject1).estimatedSize = ((int)(l4 + l1));
      ((VideoEditedInfo)localObject1).estimatedSize += ((VideoEditedInfo)localObject1).estimatedSize / 32768L * 16L;
      return (VideoEditedInfo)localObject1;
      label1587:
      return null;
      label1589:
      int j = 0;
    }
    return null;
    label1597:
    return null;
  }
  
  /* Error */
  private static Bitmap createVideoThumbnail(String paramString, long paramLong)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 7
    //   3: new 627	android/media/MediaMetadataRetriever
    //   6: dup
    //   7: invokespecial 628	android/media/MediaMetadataRetriever:<init>	()V
    //   10: astore 8
    //   12: aload 8
    //   14: aload_0
    //   15: invokevirtual 631	android/media/MediaMetadataRetriever:setDataSource	(Ljava/lang/String;)V
    //   18: aload 8
    //   20: lload_1
    //   21: iconst_1
    //   22: invokevirtual 635	android/media/MediaMetadataRetriever:getFrameAtTime	(JI)Landroid/graphics/Bitmap;
    //   25: astore_0
    //   26: aload 8
    //   28: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   31: aload_0
    //   32: ifnonnull +32 -> 64
    //   35: aconst_null
    //   36: areturn
    //   37: astore_0
    //   38: aload 8
    //   40: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   43: aload 7
    //   45: astore_0
    //   46: goto -15 -> 31
    //   49: astore_0
    //   50: aload 7
    //   52: astore_0
    //   53: goto -22 -> 31
    //   56: astore_0
    //   57: aload 8
    //   59: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   62: aload_0
    //   63: athrow
    //   64: aload_0
    //   65: invokevirtual 642	android/graphics/Bitmap:getWidth	()I
    //   68: istore 4
    //   70: aload_0
    //   71: invokevirtual 644	android/graphics/Bitmap:getHeight	()I
    //   74: istore 5
    //   76: iload 4
    //   78: iload 5
    //   80: invokestatic 647	java/lang/Math:max	(II)I
    //   83: istore 6
    //   85: aload_0
    //   86: astore 7
    //   88: iload 6
    //   90: bipush 90
    //   92: if_icmple +51 -> 143
    //   95: ldc_w 648
    //   98: iload 6
    //   100: i2f
    //   101: fdiv
    //   102: fstore_3
    //   103: aload_0
    //   104: iload 4
    //   106: i2f
    //   107: fload_3
    //   108: fmul
    //   109: invokestatic 599	java/lang/Math:round	(F)I
    //   112: iload 5
    //   114: i2f
    //   115: fload_3
    //   116: fmul
    //   117: invokestatic 599	java/lang/Math:round	(F)I
    //   120: iconst_1
    //   121: invokestatic 654	org/telegram/messenger/Bitmaps:createScaledBitmap	(Landroid/graphics/Bitmap;IIZ)Landroid/graphics/Bitmap;
    //   124: astore 8
    //   126: aload_0
    //   127: astore 7
    //   129: aload 8
    //   131: aload_0
    //   132: if_acmpeq +11 -> 143
    //   135: aload_0
    //   136: invokevirtual 657	android/graphics/Bitmap:recycle	()V
    //   139: aload 8
    //   141: astore 7
    //   143: aload 7
    //   145: areturn
    //   146: astore 7
    //   148: goto -117 -> 31
    //   151: astore 7
    //   153: goto -91 -> 62
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	156	0	paramString	String
    //   0	156	1	paramLong	long
    //   102	14	3	f	float
    //   68	37	4	i	int
    //   74	39	5	j	int
    //   83	16	6	k	int
    //   1	143	7	localObject1	Object
    //   146	1	7	localRuntimeException1	RuntimeException
    //   151	1	7	localRuntimeException2	RuntimeException
    //   10	130	8	localObject2	Object
    // Exception table:
    //   from	to	target	type
    //   12	26	37	java/lang/Exception
    //   38	43	49	java/lang/RuntimeException
    //   12	26	56	finally
    //   26	31	146	java/lang/RuntimeException
    //   57	62	151	java/lang/RuntimeException
  }
  
  /* Error */
  private static void fillVideoAttribute(String paramString, TLRPC.TL_documentAttributeVideo paramTL_documentAttributeVideo, VideoEditedInfo paramVideoEditedInfo)
  {
    // Byte code:
    //   0: iconst_0
    //   1: istore 4
    //   3: aconst_null
    //   4: astore 5
    //   6: aconst_null
    //   7: astore 7
    //   9: new 627	android/media/MediaMetadataRetriever
    //   12: dup
    //   13: invokespecial 628	android/media/MediaMetadataRetriever:<init>	()V
    //   16: astore 6
    //   18: aload 6
    //   20: aload_0
    //   21: invokevirtual 631	android/media/MediaMetadataRetriever:setDataSource	(Ljava/lang/String;)V
    //   24: aload 6
    //   26: bipush 18
    //   28: invokevirtual 661	android/media/MediaMetadataRetriever:extractMetadata	(I)Ljava/lang/String;
    //   31: astore 5
    //   33: aload 5
    //   35: ifnull +12 -> 47
    //   38: aload_1
    //   39: aload 5
    //   41: invokestatic 667	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   44: putfield 672	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:w	I
    //   47: aload 6
    //   49: bipush 19
    //   51: invokevirtual 661	android/media/MediaMetadataRetriever:extractMetadata	(I)Ljava/lang/String;
    //   54: astore 5
    //   56: aload 5
    //   58: ifnull +12 -> 70
    //   61: aload_1
    //   62: aload 5
    //   64: invokestatic 667	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   67: putfield 675	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:h	I
    //   70: aload 6
    //   72: bipush 9
    //   74: invokevirtual 661	android/media/MediaMetadataRetriever:extractMetadata	(I)Ljava/lang/String;
    //   77: astore 5
    //   79: aload 5
    //   81: ifnull +22 -> 103
    //   84: aload_1
    //   85: aload 5
    //   87: invokestatic 681	java/lang/Long:parseLong	(Ljava/lang/String;)J
    //   90: l2f
    //   91: ldc_w 524
    //   94: fdiv
    //   95: f2d
    //   96: invokestatic 550	java/lang/Math:ceil	(D)D
    //   99: d2i
    //   100: putfield 684	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:duration	I
    //   103: getstatic 191	android/os/Build$VERSION:SDK_INT	I
    //   106: bipush 17
    //   108: if_icmplt +35 -> 143
    //   111: aload 6
    //   113: bipush 24
    //   115: invokevirtual 661	android/media/MediaMetadataRetriever:extractMetadata	(I)Ljava/lang/String;
    //   118: astore 5
    //   120: aload 5
    //   122: ifnull +21 -> 143
    //   125: aload 5
    //   127: invokestatic 689	org/telegram/messenger/Utilities:parseInt	(Ljava/lang/String;)Ljava/lang/Integer;
    //   130: invokevirtual 692	java/lang/Integer:intValue	()I
    //   133: istore_3
    //   134: aload_2
    //   135: ifnull +85 -> 220
    //   138: aload_2
    //   139: iload_3
    //   140: putfield 579	org/telegram/messenger/VideoEditedInfo:rotationValue	I
    //   143: iconst_1
    //   144: istore_3
    //   145: aload 6
    //   147: ifnull +8 -> 155
    //   150: aload 6
    //   152: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   155: iload_3
    //   156: ifne +63 -> 219
    //   159: getstatic 698	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   162: new 601	java/io/File
    //   165: dup
    //   166: aload_0
    //   167: invokespecial 602	java/io/File:<init>	(Ljava/lang/String;)V
    //   170: invokestatic 704	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   173: invokestatic 710	android/media/MediaPlayer:create	(Landroid/content/Context;Landroid/net/Uri;)Landroid/media/MediaPlayer;
    //   176: astore_0
    //   177: aload_0
    //   178: ifnull +41 -> 219
    //   181: aload_1
    //   182: aload_0
    //   183: invokevirtual 712	android/media/MediaPlayer:getDuration	()I
    //   186: i2f
    //   187: ldc_w 524
    //   190: fdiv
    //   191: f2d
    //   192: invokestatic 550	java/lang/Math:ceil	(D)D
    //   195: d2i
    //   196: putfield 684	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:duration	I
    //   199: aload_1
    //   200: aload_0
    //   201: invokevirtual 715	android/media/MediaPlayer:getVideoWidth	()I
    //   204: putfield 672	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:w	I
    //   207: aload_1
    //   208: aload_0
    //   209: invokevirtual 718	android/media/MediaPlayer:getVideoHeight	()I
    //   212: putfield 675	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:h	I
    //   215: aload_0
    //   216: invokevirtual 719	android/media/MediaPlayer:release	()V
    //   219: return
    //   220: iload_3
    //   221: bipush 90
    //   223: if_icmpeq +10 -> 233
    //   226: iload_3
    //   227: sipush 270
    //   230: if_icmpne -87 -> 143
    //   233: aload_1
    //   234: getfield 672	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:w	I
    //   237: istore_3
    //   238: aload_1
    //   239: aload_1
    //   240: getfield 675	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:h	I
    //   243: putfield 672	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:w	I
    //   246: aload_1
    //   247: iload_3
    //   248: putfield 675	org/telegram/tgnet/TLRPC$TL_documentAttributeVideo:h	I
    //   251: goto -108 -> 143
    //   254: astore 5
    //   256: aload 6
    //   258: astore_2
    //   259: aload 5
    //   261: astore 6
    //   263: aload_2
    //   264: astore 5
    //   266: aload 6
    //   268: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   271: iload 4
    //   273: istore_3
    //   274: aload_2
    //   275: ifnull -120 -> 155
    //   278: aload_2
    //   279: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   282: iload 4
    //   284: istore_3
    //   285: goto -130 -> 155
    //   288: astore_2
    //   289: aload_2
    //   290: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   293: iload 4
    //   295: istore_3
    //   296: goto -141 -> 155
    //   299: astore_2
    //   300: aload_2
    //   301: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   304: goto -149 -> 155
    //   307: astore_0
    //   308: aload 5
    //   310: ifnull +8 -> 318
    //   313: aload 5
    //   315: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   318: aload_0
    //   319: athrow
    //   320: astore_1
    //   321: aload_1
    //   322: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   325: goto -7 -> 318
    //   328: astore_0
    //   329: aload_0
    //   330: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   333: return
    //   334: astore_0
    //   335: aload 6
    //   337: astore 5
    //   339: goto -31 -> 308
    //   342: astore 6
    //   344: aload 7
    //   346: astore_2
    //   347: goto -84 -> 263
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	350	0	paramString	String
    //   0	350	1	paramTL_documentAttributeVideo	TLRPC.TL_documentAttributeVideo
    //   0	350	2	paramVideoEditedInfo	VideoEditedInfo
    //   133	163	3	i	int
    //   1	293	4	j	int
    //   4	122	5	str	String
    //   254	6	5	localException1	Exception
    //   264	74	5	localObject1	Object
    //   16	320	6	localObject2	Object
    //   342	1	6	localException2	Exception
    //   7	338	7	localObject3	Object
    // Exception table:
    //   from	to	target	type
    //   18	33	254	java/lang/Exception
    //   38	47	254	java/lang/Exception
    //   47	56	254	java/lang/Exception
    //   61	70	254	java/lang/Exception
    //   70	79	254	java/lang/Exception
    //   84	103	254	java/lang/Exception
    //   103	120	254	java/lang/Exception
    //   125	134	254	java/lang/Exception
    //   138	143	254	java/lang/Exception
    //   233	251	254	java/lang/Exception
    //   278	282	288	java/lang/Exception
    //   150	155	299	java/lang/Exception
    //   9	18	307	finally
    //   266	271	307	finally
    //   313	318	320	java/lang/Exception
    //   159	177	328	java/lang/Exception
    //   181	219	328	java/lang/Exception
    //   18	33	334	finally
    //   38	47	334	finally
    //   47	56	334	finally
    //   61	70	334	finally
    //   70	79	334	finally
    //   84	103	334	finally
    //   103	120	334	finally
    //   125	134	334	finally
    //   138	143	334	finally
    //   233	251	334	finally
    //   9	18	342	java/lang/Exception
  }
  
  private DelayedMessage findMaxDelayedMessageForMessageId(int paramInt, long paramLong)
  {
    Object localObject2 = null;
    int i = Integer.MIN_VALUE;
    Iterator localIterator = this.delayedMessages.entrySet().iterator();
    while (localIterator.hasNext())
    {
      ArrayList localArrayList = (ArrayList)((Map.Entry)localIterator.next()).getValue();
      int n = localArrayList.size();
      int j = 0;
      Object localObject1 = localObject2;
      int k = i;
      i = k;
      localObject2 = localObject1;
      if (j < n)
      {
        DelayedMessage localDelayedMessage = (DelayedMessage)localArrayList.get(j);
        int m;
        if (localDelayedMessage.type != 4)
        {
          m = k;
          localObject2 = localObject1;
          if (localDelayedMessage.type != 0) {}
        }
        else
        {
          m = k;
          localObject2 = localObject1;
          if (localDelayedMessage.peer == paramLong)
          {
            m = 0;
            if (localDelayedMessage.obj == null) {
              break label241;
            }
            i = localDelayedMessage.obj.getId();
          }
        }
        for (;;)
        {
          m = k;
          localObject2 = localObject1;
          if (i != 0)
          {
            m = k;
            localObject2 = localObject1;
            if (i > paramInt)
            {
              m = k;
              localObject2 = localObject1;
              if (localObject1 == null)
              {
                m = k;
                localObject2 = localObject1;
                if (k < i)
                {
                  localObject2 = localDelayedMessage;
                  m = i;
                }
              }
            }
          }
          j += 1;
          k = m;
          localObject1 = localObject2;
          break;
          label241:
          i = m;
          if (localDelayedMessage.messageObjects != null)
          {
            i = m;
            if (!localDelayedMessage.messageObjects.isEmpty()) {
              i = ((MessageObject)localDelayedMessage.messageObjects.get(localDelayedMessage.messageObjects.size() - 1)).getId();
            }
          }
        }
      }
    }
    return (DelayedMessage)localObject2;
  }
  
  public static SendMessagesHelper getInstance(int paramInt)
  {
    Object localObject1 = Instance[paramInt];
    if (localObject1 == null) {}
    try
    {
      Object localObject3 = Instance[paramInt];
      localObject1 = localObject3;
      if (localObject3 == null)
      {
        localObject3 = Instance;
        localObject1 = new SendMessagesHelper(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (SendMessagesHelper)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (SendMessagesHelper)localObject1;
  }
  
  private static String getTrimmedString(String paramString)
  {
    String str = paramString.trim();
    if (str.length() == 0) {
      return str;
    }
    for (;;)
    {
      str = paramString;
      if (!paramString.startsWith("\n")) {
        break;
      }
      paramString = paramString.substring(1);
    }
    while (str.endsWith("\n")) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }
  
  private void performSendDelayedMessage(DelayedMessage paramDelayedMessage)
  {
    performSendDelayedMessage(paramDelayedMessage, -1);
  }
  
  private void performSendDelayedMessage(DelayedMessage paramDelayedMessage, int paramInt)
  {
    if (paramDelayedMessage.type == 0) {
      if (paramDelayedMessage.httpLocation != null)
      {
        putToDelayedMessages(paramDelayedMessage.httpLocation, paramDelayedMessage);
        ImageLoader.getInstance().loadHttpFile(paramDelayedMessage.httpLocation, "file", this.currentAccount);
      }
    }
    Object localObject1;
    Object localObject3;
    Object localObject2;
    label409:
    label583:
    label695:
    label810:
    boolean bool1;
    label1151:
    label1355:
    do
    {
      do
      {
        return;
        if (paramDelayedMessage.sendRequest != null)
        {
          localObject1 = FileLoader.getPathToAttach(paramDelayedMessage.location).toString();
          putToDelayedMessages((String)localObject1, paramDelayedMessage);
          FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, false, true, 16777216);
          return;
        }
        localObject1 = FileLoader.getPathToAttach(paramDelayedMessage.location).toString();
        localObject3 = localObject1;
        if (paramDelayedMessage.sendEncryptedRequest != null)
        {
          localObject3 = localObject1;
          if (paramDelayedMessage.location.dc_id != 0)
          {
            localObject3 = new File((String)localObject1);
            localObject2 = localObject3;
            if (!((File)localObject3).exists())
            {
              localObject1 = FileLoader.getPathToAttach(paramDelayedMessage.location, true).toString();
              localObject2 = new File((String)localObject1);
            }
            localObject3 = localObject1;
            if (!((File)localObject2).exists())
            {
              putToDelayedMessages(FileLoader.getAttachFileName(paramDelayedMessage.location), paramDelayedMessage);
              FileLoader.getInstance(this.currentAccount).loadFile(paramDelayedMessage.location, "jpg", 0, 0);
              return;
            }
          }
        }
        putToDelayedMessages((String)localObject3, paramDelayedMessage);
        FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject3, true, true, 16777216);
        return;
        if (paramDelayedMessage.type == 1)
        {
          if ((paramDelayedMessage.videoEditedInfo != null) && (paramDelayedMessage.videoEditedInfo.needConvert()))
          {
            localObject2 = paramDelayedMessage.obj.messageOwner.attachPath;
            localObject3 = paramDelayedMessage.obj.getDocument();
            localObject1 = localObject2;
            if (localObject2 == null) {
              localObject1 = FileLoader.getDirectory(4) + "/" + ((TLRPC.Document)localObject3).id + ".mp4";
            }
            putToDelayedMessages((String)localObject1, paramDelayedMessage);
            MediaController.getInstance().scheduleVideoConvert(paramDelayedMessage.obj);
            return;
          }
          if (paramDelayedMessage.videoEditedInfo != null)
          {
            if (paramDelayedMessage.videoEditedInfo.file == null) {
              break label583;
            }
            if ((paramDelayedMessage.sendRequest instanceof TLRPC.TL_messages_sendMedia))
            {
              localObject1 = ((TLRPC.TL_messages_sendMedia)paramDelayedMessage.sendRequest).media;
              ((TLRPC.InputMedia)localObject1).file = paramDelayedMessage.videoEditedInfo.file;
              paramDelayedMessage.videoEditedInfo.file = null;
            }
          }
          else
          {
            if (paramDelayedMessage.sendRequest == null) {
              break label810;
            }
            if (!(paramDelayedMessage.sendRequest instanceof TLRPC.TL_messages_sendMedia)) {
              break label695;
            }
            localObject1 = ((TLRPC.TL_messages_sendMedia)paramDelayedMessage.sendRequest).media;
          }
          for (;;)
          {
            if (((TLRPC.InputMedia)localObject1).file == null)
            {
              localObject2 = paramDelayedMessage.obj.messageOwner.attachPath;
              localObject3 = paramDelayedMessage.obj.getDocument();
              localObject1 = localObject2;
              if (localObject2 == null) {
                localObject1 = FileLoader.getDirectory(4) + "/" + ((TLRPC.Document)localObject3).id + ".mp4";
              }
              putToDelayedMessages((String)localObject1, paramDelayedMessage);
              if ((paramDelayedMessage.obj.videoEditedInfo != null) && (paramDelayedMessage.obj.videoEditedInfo.needConvert()))
              {
                FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, false, false, ((TLRPC.Document)localObject3).size, 33554432);
                return;
                localObject1 = ((TLRPC.TL_messages_sendBroadcast)paramDelayedMessage.sendRequest).media;
                break;
                if (paramDelayedMessage.videoEditedInfo.encryptedFile == null) {
                  break label409;
                }
                localObject1 = (TLRPC.TL_decryptedMessage)paramDelayedMessage.sendEncryptedRequest;
                ((TLRPC.TL_decryptedMessage)localObject1).media.size = ((int)paramDelayedMessage.videoEditedInfo.estimatedSize);
                ((TLRPC.TL_decryptedMessage)localObject1).media.key = paramDelayedMessage.videoEditedInfo.key;
                ((TLRPC.TL_decryptedMessage)localObject1).media.iv = paramDelayedMessage.videoEditedInfo.iv;
                SecretChatHelper.getInstance(this.currentAccount).performSendEncryptedRequest((TLRPC.DecryptedMessage)localObject1, paramDelayedMessage.obj.messageOwner, paramDelayedMessage.encryptedChat, paramDelayedMessage.videoEditedInfo.encryptedFile, paramDelayedMessage.originalPath, paramDelayedMessage.obj);
                paramDelayedMessage.videoEditedInfo.encryptedFile = null;
                return;
                localObject1 = ((TLRPC.TL_messages_sendBroadcast)paramDelayedMessage.sendRequest).media;
                continue;
              }
              FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, false, false, 33554432);
              return;
            }
          }
          localObject1 = FileLoader.getDirectory(4) + "/" + paramDelayedMessage.location.volume_id + "_" + paramDelayedMessage.location.local_id + ".jpg";
          putToDelayedMessages((String)localObject1, paramDelayedMessage);
          FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, false, true, 16777216);
          return;
          localObject2 = paramDelayedMessage.obj.messageOwner.attachPath;
          localObject3 = paramDelayedMessage.obj.getDocument();
          localObject1 = localObject2;
          if (localObject2 == null) {
            localObject1 = FileLoader.getDirectory(4) + "/" + ((TLRPC.Document)localObject3).id + ".mp4";
          }
          if ((paramDelayedMessage.sendEncryptedRequest != null) && (((TLRPC.Document)localObject3).dc_id != 0) && (!new File((String)localObject1).exists()))
          {
            putToDelayedMessages(FileLoader.getAttachFileName((TLObject)localObject3), paramDelayedMessage);
            FileLoader.getInstance(this.currentAccount).loadFile((TLRPC.Document)localObject3, true, 0);
            return;
          }
          putToDelayedMessages((String)localObject1, paramDelayedMessage);
          if ((paramDelayedMessage.obj.videoEditedInfo != null) && (paramDelayedMessage.obj.videoEditedInfo.needConvert()))
          {
            FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, true, false, ((TLRPC.Document)localObject3).size, 33554432);
            return;
          }
          FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, true, false, 33554432);
          return;
        }
        if (paramDelayedMessage.type != 2) {
          break label1355;
        }
        if (paramDelayedMessage.httpLocation != null)
        {
          putToDelayedMessages(paramDelayedMessage.httpLocation, paramDelayedMessage);
          ImageLoader.getInstance().loadHttpFile(paramDelayedMessage.httpLocation, "gif", this.currentAccount);
          return;
        }
        if (paramDelayedMessage.sendRequest == null) {
          break;
        }
        if ((paramDelayedMessage.sendRequest instanceof TLRPC.TL_messages_sendMedia))
        {
          localObject1 = ((TLRPC.TL_messages_sendMedia)paramDelayedMessage.sendRequest).media;
          if (((TLRPC.InputMedia)localObject1).file != null) {
            continue;
          }
          localObject1 = paramDelayedMessage.obj.messageOwner.attachPath;
          putToDelayedMessages((String)localObject1, paramDelayedMessage);
          localObject2 = FileLoader.getInstance(this.currentAccount);
          if (paramDelayedMessage.sendRequest != null) {
            break label1151;
          }
        }
        for (bool1 = true;; bool1 = false)
        {
          ((FileLoader)localObject2).uploadFile((String)localObject1, bool1, false, 67108864);
          return;
          localObject1 = ((TLRPC.TL_messages_sendBroadcast)paramDelayedMessage.sendRequest).media;
          break;
        }
      } while ((((TLRPC.InputMedia)localObject1).thumb != null) || (paramDelayedMessage.location == null));
      localObject1 = FileLoader.getDirectory(4) + "/" + paramDelayedMessage.location.volume_id + "_" + paramDelayedMessage.location.local_id + ".jpg";
      putToDelayedMessages((String)localObject1, paramDelayedMessage);
      FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, false, true, 16777216);
      return;
      localObject1 = paramDelayedMessage.obj.messageOwner.attachPath;
      localObject2 = paramDelayedMessage.obj.getDocument();
      if ((paramDelayedMessage.sendEncryptedRequest != null) && (((TLRPC.Document)localObject2).dc_id != 0) && (!new File((String)localObject1).exists()))
      {
        putToDelayedMessages(FileLoader.getAttachFileName((TLObject)localObject2), paramDelayedMessage);
        FileLoader.getInstance(this.currentAccount).loadFile((TLRPC.Document)localObject2, true, 0);
        return;
      }
      putToDelayedMessages((String)localObject1, paramDelayedMessage);
      FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, true, false, 67108864);
      return;
      if (paramDelayedMessage.type == 3)
      {
        localObject1 = paramDelayedMessage.obj.messageOwner.attachPath;
        putToDelayedMessages((String)localObject1, paramDelayedMessage);
        localObject2 = FileLoader.getInstance(this.currentAccount);
        if (paramDelayedMessage.sendRequest == null) {}
        for (bool1 = true;; bool1 = false)
        {
          ((FileLoader)localObject2).uploadFile((String)localObject1, bool1, true, 50331648);
          return;
        }
      }
    } while (paramDelayedMessage.type != 4);
    int i;
    Object localObject4;
    if (paramInt < 0)
    {
      bool1 = true;
      if ((paramDelayedMessage.location == null) && (paramDelayedMessage.httpLocation == null) && (!paramDelayedMessage.upload) && (paramInt < 0)) {
        break label2541;
      }
      i = paramInt;
      if (paramInt < 0) {
        i = paramDelayedMessage.messageObjects.size() - 1;
      }
      localObject3 = (MessageObject)paramDelayedMessage.messageObjects.get(i);
      if (((MessageObject)localObject3).getDocument() == null) {
        break label2287;
      }
      if (paramDelayedMessage.videoEditedInfo == null) {
        break label1698;
      }
      localObject2 = ((MessageObject)localObject3).messageOwner.attachPath;
      localObject4 = ((MessageObject)localObject3).getDocument();
      localObject1 = localObject2;
      if (localObject2 == null) {
        localObject1 = FileLoader.getDirectory(4) + "/" + ((TLRPC.Document)localObject4).id + ".mp4";
      }
      putToDelayedMessages((String)localObject1, paramDelayedMessage);
      paramDelayedMessage.extraHashMap.put(localObject3, localObject1);
      paramDelayedMessage.extraHashMap.put((String)localObject1 + "_i", localObject3);
      if (paramDelayedMessage.location != null) {
        paramDelayedMessage.extraHashMap.put((String)localObject1 + "_t", paramDelayedMessage.location);
      }
      MediaController.getInstance().scheduleVideoConvert((MessageObject)localObject3);
      label1668:
      paramDelayedMessage.videoEditedInfo = null;
      paramDelayedMessage.location = null;
      label1678:
      paramDelayedMessage.upload = false;
    }
    for (;;)
    {
      sendReadyToSendGroup(paramDelayedMessage, bool1, true);
      return;
      bool1 = false;
      break;
      label1698:
      localObject4 = ((MessageObject)localObject3).getDocument();
      localObject2 = ((MessageObject)localObject3).messageOwner.attachPath;
      localObject1 = localObject2;
      if (localObject2 == null) {
        localObject1 = FileLoader.getDirectory(4) + "/" + ((TLRPC.Document)localObject4).id + ".mp4";
      }
      if (paramDelayedMessage.sendRequest != null)
      {
        localObject2 = ((TLRPC.TL_inputSingleMedia)((TLRPC.TL_messages_sendMultiMedia)paramDelayedMessage.sendRequest).multi_media.get(i)).media;
        if (((TLRPC.InputMedia)localObject2).file == null)
        {
          putToDelayedMessages((String)localObject1, paramDelayedMessage);
          paramDelayedMessage.extraHashMap.put(localObject3, localObject1);
          paramDelayedMessage.extraHashMap.put(localObject1, localObject2);
          paramDelayedMessage.extraHashMap.put((String)localObject1 + "_i", localObject3);
          if (paramDelayedMessage.location != null) {
            paramDelayedMessage.extraHashMap.put((String)localObject1 + "_t", paramDelayedMessage.location);
          }
          if ((((MessageObject)localObject3).videoEditedInfo != null) && (((MessageObject)localObject3).videoEditedInfo.needConvert()))
          {
            FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, false, false, ((TLRPC.Document)localObject4).size, 33554432);
            break label1668;
          }
          FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, false, false, 33554432);
          break label1668;
        }
        localObject4 = FileLoader.getDirectory(4) + "/" + paramDelayedMessage.location.volume_id + "_" + paramDelayedMessage.location.local_id + ".jpg";
        putToDelayedMessages((String)localObject4, paramDelayedMessage);
        paramDelayedMessage.extraHashMap.put((String)localObject4 + "_o", localObject1);
        paramDelayedMessage.extraHashMap.put(localObject3, localObject4);
        paramDelayedMessage.extraHashMap.put(localObject4, localObject2);
        FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject4, false, true, 16777216);
        break label1668;
      }
      localObject2 = (TLRPC.TL_messages_sendEncryptedMultiMedia)paramDelayedMessage.sendEncryptedRequest;
      putToDelayedMessages((String)localObject1, paramDelayedMessage);
      paramDelayedMessage.extraHashMap.put(localObject3, localObject1);
      paramDelayedMessage.extraHashMap.put(localObject1, ((TLRPC.TL_messages_sendEncryptedMultiMedia)localObject2).files.get(i));
      paramDelayedMessage.extraHashMap.put((String)localObject1 + "_i", localObject3);
      if (paramDelayedMessage.location != null) {
        paramDelayedMessage.extraHashMap.put((String)localObject1 + "_t", paramDelayedMessage.location);
      }
      if ((((MessageObject)localObject3).videoEditedInfo != null) && (((MessageObject)localObject3).videoEditedInfo.needConvert()))
      {
        FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, true, false, ((TLRPC.Document)localObject4).size, 33554432);
        break label1668;
      }
      FileLoader.getInstance(this.currentAccount).uploadFile((String)localObject1, true, false, 33554432);
      break label1668;
      label2287:
      if (paramDelayedMessage.httpLocation != null)
      {
        putToDelayedMessages(paramDelayedMessage.httpLocation, paramDelayedMessage);
        paramDelayedMessage.extraHashMap.put(localObject3, paramDelayedMessage.httpLocation);
        paramDelayedMessage.extraHashMap.put(paramDelayedMessage.httpLocation, localObject3);
        ImageLoader.getInstance().loadHttpFile(paramDelayedMessage.httpLocation, "file", this.currentAccount);
        paramDelayedMessage.httpLocation = null;
        break label1678;
      }
      if (paramDelayedMessage.sendRequest != null)
      {
        localObject1 = ((TLRPC.TL_inputSingleMedia)((TLRPC.TL_messages_sendMultiMedia)paramDelayedMessage.sendRequest).multi_media.get(i)).media;
        label2385:
        localObject2 = FileLoader.getDirectory(4) + "/" + paramDelayedMessage.location.volume_id + "_" + paramDelayedMessage.location.local_id + ".jpg";
        putToDelayedMessages((String)localObject2, paramDelayedMessage);
        paramDelayedMessage.extraHashMap.put(localObject2, localObject1);
        paramDelayedMessage.extraHashMap.put(localObject3, localObject2);
        localObject1 = FileLoader.getInstance(this.currentAccount);
        if (paramDelayedMessage.sendEncryptedRequest == null) {
          break label2535;
        }
      }
      label2535:
      for (boolean bool2 = true;; bool2 = false)
      {
        ((FileLoader)localObject1).uploadFile((String)localObject2, bool2, true, 16777216);
        paramDelayedMessage.location = null;
        break;
        localObject1 = (TLObject)((TLRPC.TL_messages_sendEncryptedMultiMedia)paramDelayedMessage.sendEncryptedRequest).files.get(i);
        break label2385;
      }
      label2541:
      if (!paramDelayedMessage.messageObjects.isEmpty()) {
        putToSendingMessages(((MessageObject)paramDelayedMessage.messageObjects.get(paramDelayedMessage.messageObjects.size() - 1)).messageOwner);
      }
    }
  }
  
  private void performSendMessageRequest(TLObject paramTLObject, MessageObject paramMessageObject, String paramString)
  {
    performSendMessageRequest(paramTLObject, paramMessageObject, paramString, null, false);
  }
  
  private void performSendMessageRequest(final TLObject paramTLObject, final MessageObject paramMessageObject, final String paramString, DelayedMessage paramDelayedMessage, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      localObject = findMaxDelayedMessageForMessageId(paramMessageObject.getId(), paramMessageObject.getDialogId());
      if (localObject != null)
      {
        ((DelayedMessage)localObject).addDelayedRequest(paramTLObject, paramMessageObject, paramString);
        if ((paramDelayedMessage != null) && (paramDelayedMessage.requests != null)) {
          ((DelayedMessage)localObject).requests.addAll(paramDelayedMessage.requests);
        }
        return;
      }
    }
    final TLRPC.Message localMessage = paramMessageObject.messageOwner;
    putToSendingMessages(localMessage);
    Object localObject = ConnectionsManager.getInstance(this.currentAccount);
    paramMessageObject = new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            int j = 0;
            int i = 0;
            final ArrayList localArrayList;
            final Object localObject1;
            final Object localObject2;
            Object localObject3;
            if (paramAnonymousTL_error == null)
            {
              final int k = SendMessagesHelper.13.this.val$newMsgObj.id;
              final boolean bool2 = SendMessagesHelper.13.this.val$req instanceof TLRPC.TL_messages_sendBroadcast;
              localArrayList = new ArrayList();
              final String str = SendMessagesHelper.13.this.val$newMsgObj.attachPath;
              if ((paramAnonymousTLObject instanceof TLRPC.TL_updateShortSentMessage))
              {
                localObject1 = (TLRPC.TL_updateShortSentMessage)paramAnonymousTLObject;
                localObject2 = SendMessagesHelper.13.this.val$newMsgObj;
                localObject3 = SendMessagesHelper.13.this.val$newMsgObj;
                j = ((TLRPC.TL_updateShortSentMessage)localObject1).id;
                ((TLRPC.Message)localObject3).id = j;
                ((TLRPC.Message)localObject2).local_id = j;
                SendMessagesHelper.13.this.val$newMsgObj.date = ((TLRPC.TL_updateShortSentMessage)localObject1).date;
                SendMessagesHelper.13.this.val$newMsgObj.entities = ((TLRPC.TL_updateShortSentMessage)localObject1).entities;
                SendMessagesHelper.13.this.val$newMsgObj.out = ((TLRPC.TL_updateShortSentMessage)localObject1).out;
                if (((TLRPC.TL_updateShortSentMessage)localObject1).media != null)
                {
                  SendMessagesHelper.13.this.val$newMsgObj.media = ((TLRPC.TL_updateShortSentMessage)localObject1).media;
                  localObject2 = SendMessagesHelper.13.this.val$newMsgObj;
                  ((TLRPC.Message)localObject2).flags |= 0x200;
                  ImageLoader.saveMessageThumbs(SendMessagesHelper.13.this.val$newMsgObj);
                }
                if (((((TLRPC.TL_updateShortSentMessage)localObject1).media instanceof TLRPC.TL_messageMediaGame)) && (!TextUtils.isEmpty(((TLRPC.TL_updateShortSentMessage)localObject1).message))) {
                  SendMessagesHelper.13.this.val$newMsgObj.message = ((TLRPC.TL_updateShortSentMessage)localObject1).message;
                }
                if (!SendMessagesHelper.13.this.val$newMsgObj.entities.isEmpty())
                {
                  localObject2 = SendMessagesHelper.13.this.val$newMsgObj;
                  ((TLRPC.Message)localObject2).flags |= 0x80;
                }
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewDifferenceParams(-1, localObject1.pts, localObject1.date, localObject1.pts_count);
                  }
                });
                localArrayList.add(SendMessagesHelper.13.this.val$newMsgObj);
                if (MessageObject.isLiveLocationMessage(SendMessagesHelper.13.this.val$newMsgObj)) {
                  LocationController.getInstance(SendMessagesHelper.this.currentAccount).addSharingLocation(SendMessagesHelper.13.this.val$newMsgObj.dialog_id, SendMessagesHelper.13.this.val$newMsgObj.id, SendMessagesHelper.13.this.val$newMsgObj.media.period, SendMessagesHelper.13.this.val$newMsgObj);
                }
                j = i;
                if (i == 0)
                {
                  StatsController.getInstance(SendMessagesHelper.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 1, 1);
                  SendMessagesHelper.13.this.val$newMsgObj.send_state = 0;
                  localObject1 = NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount);
                  int m = NotificationCenter.messageReceivedByServer;
                  if (!bool2) {
                    break label1137;
                  }
                  j = k;
                  label450:
                  ((NotificationCenter)localObject1).postNotificationName(m, new Object[] { Integer.valueOf(k), Integer.valueOf(j), SendMessagesHelper.13.this.val$newMsgObj, Long.valueOf(SendMessagesHelper.13.this.val$newMsgObj.dialog_id) });
                  MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                  {
                    public void run()
                    {
                      Object localObject = MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount);
                      long l = SendMessagesHelper.13.this.val$newMsgObj.random_id;
                      int j = k;
                      if (bool2) {}
                      for (int i = k;; i = SendMessagesHelper.13.this.val$newMsgObj.id)
                      {
                        ((MessagesStorage)localObject).updateMessageStateAndId(l, Integer.valueOf(j), i, 0, false, SendMessagesHelper.13.this.val$newMsgObj.to_id.channel_id);
                        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(localArrayList, true, false, bool2, 0);
                        if (bool2)
                        {
                          localObject = new ArrayList();
                          ((ArrayList)localObject).add(SendMessagesHelper.13.this.val$newMsgObj);
                          MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages((ArrayList)localObject, true, false, false, 0);
                        }
                        AndroidUtilities.runOnUIThread(new Runnable()
                        {
                          public void run()
                          {
                            if (SendMessagesHelper.13.1.5.this.val$isBroadcast)
                            {
                              i = 0;
                              while (i < SendMessagesHelper.13.1.5.this.val$sentMessages.size())
                              {
                                Object localObject2 = (TLRPC.Message)SendMessagesHelper.13.1.5.this.val$sentMessages.get(i);
                                localObject1 = new ArrayList();
                                localObject2 = new MessageObject(SendMessagesHelper.this.currentAccount, (TLRPC.Message)localObject2, false);
                                ((ArrayList)localObject1).add(localObject2);
                                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).updateInterfaceWithMessages(((MessageObject)localObject2).getDialogId(), (ArrayList)localObject1, true);
                                i += 1;
                              }
                              NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                            }
                            DataQuery.getInstance(SendMessagesHelper.this.currentAccount).increasePeerRaiting(SendMessagesHelper.13.this.val$newMsgObj.dialog_id);
                            Object localObject1 = NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount);
                            int j = NotificationCenter.messageReceivedByServer;
                            int k = SendMessagesHelper.13.1.5.this.val$oldId;
                            if (SendMessagesHelper.13.1.5.this.val$isBroadcast) {}
                            for (int i = SendMessagesHelper.13.1.5.this.val$oldId;; i = SendMessagesHelper.13.this.val$newMsgObj.id)
                            {
                              ((NotificationCenter)localObject1).postNotificationName(j, new Object[] { Integer.valueOf(k), Integer.valueOf(i), SendMessagesHelper.13.this.val$newMsgObj, Long.valueOf(SendMessagesHelper.13.this.val$newMsgObj.dialog_id) });
                              SendMessagesHelper.this.processSentMessage(SendMessagesHelper.13.1.5.this.val$oldId);
                              SendMessagesHelper.this.removeFromSendingMessages(SendMessagesHelper.13.1.5.this.val$oldId);
                              return;
                            }
                          }
                        });
                        if ((MessageObject.isVideoMessage(SendMessagesHelper.13.this.val$newMsgObj)) || (MessageObject.isRoundVideoMessage(SendMessagesHelper.13.this.val$newMsgObj)) || (MessageObject.isNewGifMessage(SendMessagesHelper.13.this.val$newMsgObj))) {
                          SendMessagesHelper.this.stopVideoService(str);
                        }
                        return;
                      }
                    }
                  });
                }
              }
            }
            for (j = i;; j = 1)
            {
              if (j != 0)
              {
                MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(SendMessagesHelper.13.this.val$newMsgObj);
                SendMessagesHelper.13.this.val$newMsgObj.send_state = 2;
                NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, new Object[] { Integer.valueOf(SendMessagesHelper.13.this.val$newMsgObj.id) });
                SendMessagesHelper.this.processSentMessage(SendMessagesHelper.13.this.val$newMsgObj.id);
                if ((MessageObject.isVideoMessage(SendMessagesHelper.13.this.val$newMsgObj)) || (MessageObject.isRoundVideoMessage(SendMessagesHelper.13.this.val$newMsgObj)) || (MessageObject.isNewGifMessage(SendMessagesHelper.13.this.val$newMsgObj))) {
                  SendMessagesHelper.this.stopVideoService(SendMessagesHelper.13.this.val$newMsgObj.attachPath);
                }
                SendMessagesHelper.this.removeFromSendingMessages(SendMessagesHelper.13.this.val$newMsgObj.id);
              }
              return;
              if (!(paramAnonymousTLObject instanceof TLRPC.Updates)) {
                break;
              }
              final TLRPC.Updates localUpdates = (TLRPC.Updates)paramAnonymousTLObject;
              localObject3 = ((TLRPC.Updates)paramAnonymousTLObject).updates;
              localObject2 = null;
              i = 0;
              label750:
              localObject1 = localObject2;
              label827:
              boolean bool1;
              if (i < ((ArrayList)localObject3).size())
              {
                localObject1 = (TLRPC.Update)((ArrayList)localObject3).get(i);
                if ((localObject1 instanceof TLRPC.TL_updateNewMessage))
                {
                  localObject2 = (TLRPC.TL_updateNewMessage)localObject1;
                  localObject1 = ((TLRPC.TL_updateNewMessage)localObject2).message;
                  localArrayList.add(localObject1);
                  Utilities.stageQueue.postRunnable(new Runnable()
                  {
                    public void run()
                    {
                      MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewDifferenceParams(-1, localObject2.pts, -1, localObject2.pts_count);
                    }
                  });
                  ((ArrayList)localObject3).remove(i);
                }
              }
              else
              {
                if (localObject1 == null) {
                  break label1132;
                }
                ImageLoader.saveMessageThumbs((TLRPC.Message)localObject1);
                localObject3 = (Integer)MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.get(Long.valueOf(((TLRPC.Message)localObject1).dialog_id));
                localObject2 = localObject3;
                if (localObject3 == null)
                {
                  localObject2 = Integer.valueOf(MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getDialogReadMax(((TLRPC.Message)localObject1).out, ((TLRPC.Message)localObject1).dialog_id));
                  MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.put(Long.valueOf(((TLRPC.Message)localObject1).dialog_id), localObject2);
                }
                if (((Integer)localObject2).intValue() >= ((TLRPC.Message)localObject1).id) {
                  break label1126;
                }
                bool1 = true;
                label955:
                ((TLRPC.Message)localObject1).unread = bool1;
                SendMessagesHelper.13.this.val$newMsgObj.id = ((TLRPC.Message)localObject1).id;
                SendMessagesHelper.this.updateMediaPaths(SendMessagesHelper.13.this.val$msgObj, (TLRPC.Message)localObject1, SendMessagesHelper.13.this.val$originalPath, false);
              }
              label1126:
              label1132:
              for (i = j;; i = 1)
              {
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates(localUpdates, false);
                  }
                });
                break;
                if ((localObject1 instanceof TLRPC.TL_updateNewChannelMessage))
                {
                  localObject2 = (TLRPC.TL_updateNewChannelMessage)localObject1;
                  localObject1 = ((TLRPC.TL_updateNewChannelMessage)localObject2).message;
                  localArrayList.add(localObject1);
                  if ((SendMessagesHelper.13.this.val$newMsgObj.flags & 0x80000000) != 0)
                  {
                    TLRPC.Message localMessage = ((TLRPC.TL_updateNewChannelMessage)localObject2).message;
                    localMessage.flags |= 0x80000000;
                  }
                  Utilities.stageQueue.postRunnable(new Runnable()
                  {
                    public void run()
                    {
                      MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewChannelDifferenceParams(localObject2.pts, localObject2.pts_count, localObject2.message.to_id.channel_id);
                    }
                  });
                  ((ArrayList)localObject3).remove(i);
                  break label827;
                }
                i += 1;
                break label750;
                bool1 = false;
                break label955;
              }
              label1137:
              j = SendMessagesHelper.13.this.val$newMsgObj.id;
              break label450;
              AlertsCreator.processError(SendMessagesHelper.this.currentAccount, paramAnonymousTL_error, null, SendMessagesHelper.13.this.val$req, new Object[0]);
            }
          }
        });
      }
    };
    paramString = new QuickAckDelegate()
    {
      public void run()
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            SendMessagesHelper.14.this.val$newMsgObj.send_state = 0;
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByAck, new Object[] { Integer.valueOf(this.val$msg_id) });
          }
        });
      }
    };
    if ((paramTLObject instanceof TLRPC.TL_messages_sendMessage)) {}
    for (int i = 128;; i = 0)
    {
      ((ConnectionsManager)localObject).sendRequest(paramTLObject, paramMessageObject, paramString, i | 0x44);
      if (paramDelayedMessage == null) {
        break;
      }
      paramDelayedMessage.sendDelayedRequests();
      return;
    }
  }
  
  private void performSendMessageRequestMulti(final TLRPC.TL_messages_sendMultiMedia paramTL_messages_sendMultiMedia, final ArrayList<MessageObject> paramArrayList, final ArrayList<String> paramArrayList1)
  {
    int i = 0;
    while (i < paramArrayList.size())
    {
      putToSendingMessages(((MessageObject)paramArrayList.get(i)).messageOwner);
      i += 1;
    }
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramTL_messages_sendMultiMedia, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            int k = 0;
            Object localObject1;
            final int i;
            if (paramAnonymousTL_error == null)
            {
              SparseArray localSparseArray = new SparseArray();
              LongSparseArray localLongSparseArray = new LongSparseArray();
              final TLRPC.Updates localUpdates = (TLRPC.Updates)paramAnonymousTLObject;
              localObject1 = ((TLRPC.Updates)paramAnonymousTLObject).updates;
              int j = 0;
              final Object localObject2;
              if (j < ((ArrayList)localObject1).size())
              {
                localObject2 = (TLRPC.Update)((ArrayList)localObject1).get(j);
                if ((localObject2 instanceof TLRPC.TL_updateMessageID))
                {
                  localObject2 = (TLRPC.TL_updateMessageID)localObject2;
                  localLongSparseArray.put(((TLRPC.TL_updateMessageID)localObject2).random_id, Integer.valueOf(((TLRPC.TL_updateMessageID)localObject2).id));
                  ((ArrayList)localObject1).remove(j);
                  i = j - 1;
                }
                for (;;)
                {
                  j = i + 1;
                  break;
                  if ((localObject2 instanceof TLRPC.TL_updateNewMessage))
                  {
                    localObject2 = (TLRPC.TL_updateNewMessage)localObject2;
                    localSparseArray.put(((TLRPC.TL_updateNewMessage)localObject2).message.id, ((TLRPC.TL_updateNewMessage)localObject2).message);
                    Utilities.stageQueue.postRunnable(new Runnable()
                    {
                      public void run()
                      {
                        MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewDifferenceParams(-1, localObject2.pts, -1, localObject2.pts_count);
                      }
                    });
                    ((ArrayList)localObject1).remove(j);
                    i = j - 1;
                  }
                  else
                  {
                    i = j;
                    if ((localObject2 instanceof TLRPC.TL_updateNewChannelMessage))
                    {
                      localObject2 = (TLRPC.TL_updateNewChannelMessage)localObject2;
                      localSparseArray.put(((TLRPC.TL_updateNewChannelMessage)localObject2).message.id, ((TLRPC.TL_updateNewChannelMessage)localObject2).message);
                      Utilities.stageQueue.postRunnable(new Runnable()
                      {
                        public void run()
                        {
                          MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewChannelDifferenceParams(localObject2.pts, localObject2.pts_count, localObject2.message.to_id.channel_id);
                        }
                      });
                      ((ArrayList)localObject1).remove(j);
                      i = j - 1;
                    }
                  }
                }
              }
              j = 0;
              i = k;
              if (j < SendMessagesHelper.12.this.val$msgObjs.size())
              {
                MessageObject localMessageObject = (MessageObject)SendMessagesHelper.12.this.val$msgObjs.get(j);
                String str = (String)SendMessagesHelper.12.this.val$originalPaths.get(j);
                final TLRPC.Message localMessage1 = localMessageObject.messageOwner;
                i = localMessage1.id;
                final ArrayList localArrayList = new ArrayList();
                localObject1 = localMessage1.attachPath;
                localObject1 = (Integer)localLongSparseArray.get(localMessage1.random_id);
                if (localObject1 != null)
                {
                  TLRPC.Message localMessage2 = (TLRPC.Message)localSparseArray.get(((Integer)localObject1).intValue());
                  if (localMessage2 != null)
                  {
                    localArrayList.add(localMessage2);
                    localMessage1.id = localMessage2.id;
                    if ((localMessage1.flags & 0x80000000) != 0) {
                      localMessage2.flags |= 0x80000000;
                    }
                    localObject2 = (Integer)MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.get(Long.valueOf(localMessage2.dialog_id));
                    localObject1 = localObject2;
                    if (localObject2 == null)
                    {
                      localObject1 = Integer.valueOf(MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getDialogReadMax(localMessage2.out, localMessage2.dialog_id));
                      MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.put(Long.valueOf(localMessage2.dialog_id), localObject1);
                    }
                    if (((Integer)localObject1).intValue() < localMessage2.id) {}
                    for (boolean bool = true;; bool = false)
                    {
                      localMessage2.unread = bool;
                      SendMessagesHelper.this.updateMediaPaths(localMessageObject, localMessage2, str, false);
                      if (0 == 0)
                      {
                        StatsController.getInstance(SendMessagesHelper.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 1, 1);
                        localMessage1.send_state = 0;
                        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, new Object[] { Integer.valueOf(i), Integer.valueOf(localMessage1.id), localMessage1, Long.valueOf(localMessage1.dialog_id) });
                        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                        {
                          public void run()
                          {
                            MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).updateMessageStateAndId(localMessage1.random_id, Integer.valueOf(i), localMessage1.id, 0, false, localMessage1.to_id.channel_id);
                            MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(localArrayList, true, false, false, 0);
                            AndroidUtilities.runOnUIThread(new Runnable()
                            {
                              public void run()
                              {
                                DataQuery.getInstance(SendMessagesHelper.this.currentAccount).increasePeerRaiting(SendMessagesHelper.12.1.3.this.val$newMsgObj.dialog_id);
                                NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, new Object[] { Integer.valueOf(SendMessagesHelper.12.1.3.this.val$oldId), Integer.valueOf(SendMessagesHelper.12.1.3.this.val$newMsgObj.id), SendMessagesHelper.12.1.3.this.val$newMsgObj, Long.valueOf(SendMessagesHelper.12.1.3.this.val$newMsgObj.dialog_id) });
                                SendMessagesHelper.this.processSentMessage(SendMessagesHelper.12.1.3.this.val$oldId);
                                SendMessagesHelper.this.removeFromSendingMessages(SendMessagesHelper.12.1.3.this.val$oldId);
                              }
                            });
                          }
                        });
                      }
                      j += 1;
                      break;
                    }
                  }
                  i = 1;
                }
              }
              else
              {
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates(localUpdates, false);
                  }
                });
              }
            }
            for (;;)
            {
              if (i == 0) {
                return;
              }
              i = 0;
              while (i < SendMessagesHelper.12.this.val$msgObjs.size())
              {
                localObject1 = ((MessageObject)SendMessagesHelper.12.this.val$msgObjs.get(i)).messageOwner;
                MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError((TLRPC.Message)localObject1);
                ((TLRPC.Message)localObject1).send_state = 2;
                NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, new Object[] { Integer.valueOf(((TLRPC.Message)localObject1).id) });
                SendMessagesHelper.this.processSentMessage(((TLRPC.Message)localObject1).id);
                SendMessagesHelper.this.removeFromSendingMessages(((TLRPC.Message)localObject1).id);
                i += 1;
              }
              i = 1;
              break;
              AlertsCreator.processError(SendMessagesHelper.this.currentAccount, paramAnonymousTL_error, null, SendMessagesHelper.12.this.val$req, new Object[0]);
              i = 1;
            }
          }
        });
      }
    }, null, 68);
  }
  
  public static void prepareSendingAudioDocuments(ArrayList<MessageObject> paramArrayList, final long paramLong, final MessageObject paramMessageObject)
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        int m = this.val$messageObjects.size();
        int i = 0;
        for (;;)
        {
          final MessageObject localMessageObject;
          final Object localObject3;
          int j;
          Object localObject2;
          if (i < m)
          {
            localMessageObject = (MessageObject)this.val$messageObjects.get(i);
            localObject1 = localMessageObject.messageOwner.attachPath;
            localObject3 = new File((String)localObject1);
            if ((int)paramLong != 0) {
              break label191;
            }
            j = 1;
            localObject2 = localObject1;
            if (localObject1 != null) {
              localObject2 = (String)localObject1 + "audio" + ((File)localObject3).length();
            }
            localObject1 = null;
            if (j == 0)
            {
              localObject1 = MessagesStorage.getInstance(paramMessageObject);
              if (j != 0) {
                break label196;
              }
            }
          }
          label191:
          label196:
          for (int k = 1;; k = 4)
          {
            localObject1 = (TLRPC.TL_document)((MessagesStorage)localObject1).getSentFile((String)localObject2, k);
            localObject3 = localObject1;
            if (localObject1 == null) {
              localObject3 = (TLRPC.TL_document)localMessageObject.messageOwner.media.document;
            }
            if (j == 0) {
              break label201;
            }
            j = (int)(paramLong >> 32);
            if (MessagesController.getInstance(paramMessageObject).getEncryptedChat(Integer.valueOf(j)) != null) {
              break label201;
            }
            return;
            j = 0;
            break;
          }
          label201:
          final Object localObject1 = new HashMap();
          if (localObject2 != null) {
            ((HashMap)localObject1).put("originalPath", localObject2);
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              SendMessagesHelper.getInstance(SendMessagesHelper.17.this.val$currentAccount).sendMessage(localObject3, null, localMessageObject.messageOwner.attachPath, SendMessagesHelper.17.this.val$dialog_id, SendMessagesHelper.17.this.val$reply_to_msg, null, null, null, localObject1, 0);
            }
          });
          i += 1;
        }
      }
    }).start();
  }
  
  public static void prepareSendingBotContextResult(TLRPC.BotInlineResult paramBotInlineResult, final HashMap<String, String> paramHashMap, final long paramLong, final MessageObject paramMessageObject)
  {
    if (paramBotInlineResult == null) {}
    int j;
    label213:
    do
    {
      return;
      j = UserConfig.selectedAccount;
      if ((paramBotInlineResult.send_message instanceof TLRPC.TL_botInlineMessageMediaAuto))
      {
        new Thread(new Runnable()
        {
          public void run()
          {
            String str = null;
            TLRPC.TL_document localTL_document = null;
            Object localObject8 = null;
            Object localObject6 = null;
            TLRPC.TL_game localTL_game2 = null;
            final Object localObject5;
            final Object localObject7;
            final TLRPC.TL_game localTL_game1;
            final Object localObject1;
            if ((this.val$result instanceof TLRPC.TL_botInlineMediaResult)) {
              if (this.val$result.type.equals("game"))
              {
                if ((int)paramLong == 0) {
                  return;
                }
                localTL_game2 = new TLRPC.TL_game();
                localTL_game2.title = this.val$result.title;
                localTL_game2.description = this.val$result.description;
                localTL_game2.short_name = this.val$result.id;
                localTL_game2.photo = this.val$result.photo;
                localObject5 = localTL_document;
                localObject7 = str;
                localTL_game1 = localTL_game2;
                localObject1 = localObject6;
                if ((this.val$result.document instanceof TLRPC.TL_document))
                {
                  localTL_game2.document = this.val$result.document;
                  localTL_game2.flags |= 0x1;
                  localObject1 = localObject6;
                  localTL_game1 = localTL_game2;
                  localObject7 = str;
                  localObject5 = localTL_document;
                }
              }
            }
            for (;;)
            {
              if ((paramMessageObject != null) && (this.val$result.content != null)) {
                paramMessageObject.put("originalPath", this.val$result.content.url);
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (localObject5 != null) {
                    SendMessagesHelper.getInstance(SendMessagesHelper.19.this.val$currentAccount).sendMessage(localObject5, null, localObject7, SendMessagesHelper.19.this.val$dialog_id, SendMessagesHelper.19.this.val$reply_to_msg, SendMessagesHelper.19.this.val$result.send_message.message, SendMessagesHelper.19.this.val$result.send_message.entities, SendMessagesHelper.19.this.val$result.send_message.reply_markup, SendMessagesHelper.19.this.val$params, 0);
                  }
                  do
                  {
                    return;
                    if (localObject1 != null)
                    {
                      SendMessagesHelper localSendMessagesHelper = SendMessagesHelper.getInstance(SendMessagesHelper.19.this.val$currentAccount);
                      TLRPC.TL_photo localTL_photo = localObject1;
                      if (SendMessagesHelper.19.this.val$result.content != null) {}
                      for (String str = SendMessagesHelper.19.this.val$result.content.url;; str = null)
                      {
                        localSendMessagesHelper.sendMessage(localTL_photo, str, SendMessagesHelper.19.this.val$dialog_id, SendMessagesHelper.19.this.val$reply_to_msg, SendMessagesHelper.19.this.val$result.send_message.message, SendMessagesHelper.19.this.val$result.send_message.entities, SendMessagesHelper.19.this.val$result.send_message.reply_markup, SendMessagesHelper.19.this.val$params, 0);
                        return;
                      }
                    }
                  } while (localTL_game1 == null);
                  SendMessagesHelper.getInstance(SendMessagesHelper.19.this.val$currentAccount).sendMessage(localTL_game1, SendMessagesHelper.19.this.val$dialog_id, SendMessagesHelper.19.this.val$result.send_message.reply_markup, SendMessagesHelper.19.this.val$params);
                }
              });
              return;
              if (this.val$result.document != null)
              {
                localObject5 = localTL_document;
                localObject7 = str;
                localTL_game1 = localTL_game2;
                localObject1 = localObject6;
                if ((this.val$result.document instanceof TLRPC.TL_document))
                {
                  localObject5 = (TLRPC.TL_document)this.val$result.document;
                  localObject7 = str;
                  localTL_game1 = localTL_game2;
                  localObject1 = localObject6;
                }
              }
              else
              {
                localObject5 = localTL_document;
                localObject7 = str;
                localTL_game1 = localTL_game2;
                localObject1 = localObject6;
                if (this.val$result.photo != null)
                {
                  localObject5 = localTL_document;
                  localObject7 = str;
                  localTL_game1 = localTL_game2;
                  localObject1 = localObject6;
                  if ((this.val$result.photo instanceof TLRPC.TL_photo))
                  {
                    localObject1 = (TLRPC.TL_photo)this.val$result.photo;
                    localObject5 = localTL_document;
                    localObject7 = str;
                    localTL_game1 = localTL_game2;
                    continue;
                    localObject5 = localTL_document;
                    localObject7 = str;
                    localTL_game1 = localTL_game2;
                    localObject1 = localObject6;
                    if (this.val$result.content != null)
                    {
                      localObject1 = new File(FileLoader.getDirectory(4), Utilities.MD5(this.val$result.content.url) + "." + ImageLoader.getHttpUrlExtension(this.val$result.content.url, "file"));
                      label461:
                      int i;
                      if (((File)localObject1).exists())
                      {
                        str = ((File)localObject1).getAbsolutePath();
                        localObject5 = this.val$result.type;
                        i = -1;
                        switch (((String)localObject5).hashCode())
                        {
                        default: 
                          switch (i)
                          {
                          default: 
                            localObject5 = localTL_document;
                            localObject7 = str;
                            localTL_game1 = localTL_game2;
                            localObject1 = localObject6;
                            break;
                          case 0: 
                          case 1: 
                          case 2: 
                          case 3: 
                          case 4: 
                          case 5: 
                            label544:
                            localTL_document = new TLRPC.TL_document();
                            localTL_document.id = 0L;
                            localTL_document.size = 0;
                            localTL_document.dc_id = 0;
                            localTL_document.mime_type = this.val$result.content.mime_type;
                            localTL_document.date = ConnectionsManager.getInstance(paramHashMap).getCurrentTime();
                            localObject5 = new TLRPC.TL_documentAttributeFilename();
                            localTL_document.attributes.add(localObject5);
                            localObject1 = this.val$result.type;
                            i = -1;
                            switch (((String)localObject1).hashCode())
                            {
                            default: 
                              label756:
                              switch (i)
                              {
                              }
                              break;
                            }
                            break;
                          }
                          break;
                        }
                      }
                      for (;;)
                      {
                        if (((TLRPC.TL_documentAttributeFilename)localObject5).file_name == null) {
                          ((TLRPC.TL_documentAttributeFilename)localObject5).file_name = "file";
                        }
                        if (localTL_document.mime_type == null) {
                          localTL_document.mime_type = "application/octet-stream";
                        }
                        localObject5 = localTL_document;
                        localObject7 = str;
                        localTL_game1 = localTL_game2;
                        localObject1 = localObject6;
                        if (localTL_document.thumb != null) {
                          break;
                        }
                        localTL_document.thumb = new TLRPC.TL_photoSize();
                        localObject1 = MessageObject.getInlineResultWidthAndHeight(this.val$result);
                        localTL_document.thumb.w = localObject1[0];
                        localTL_document.thumb.h = localObject1[1];
                        localTL_document.thumb.size = 0;
                        localTL_document.thumb.location = new TLRPC.TL_fileLocationUnavailable();
                        localTL_document.thumb.type = "x";
                        localObject5 = localTL_document;
                        localObject7 = str;
                        localTL_game1 = localTL_game2;
                        localObject1 = localObject6;
                        break;
                        str = this.val$result.content.url;
                        break label461;
                        if (!((String)localObject5).equals("audio")) {
                          break label544;
                        }
                        i = 0;
                        break label544;
                        if (!((String)localObject5).equals("voice")) {
                          break label544;
                        }
                        i = 1;
                        break label544;
                        if (!((String)localObject5).equals("file")) {
                          break label544;
                        }
                        i = 2;
                        break label544;
                        if (!((String)localObject5).equals("video")) {
                          break label544;
                        }
                        i = 3;
                        break label544;
                        if (!((String)localObject5).equals("sticker")) {
                          break label544;
                        }
                        i = 4;
                        break label544;
                        if (!((String)localObject5).equals("gif")) {
                          break label544;
                        }
                        i = 5;
                        break label544;
                        if (!((String)localObject5).equals("photo")) {
                          break label544;
                        }
                        i = 6;
                        break label544;
                        if (!((String)localObject1).equals("gif")) {
                          break label756;
                        }
                        i = 0;
                        break label756;
                        if (!((String)localObject1).equals("voice")) {
                          break label756;
                        }
                        i = 1;
                        break label756;
                        if (!((String)localObject1).equals("audio")) {
                          break label756;
                        }
                        i = 2;
                        break label756;
                        if (!((String)localObject1).equals("file")) {
                          break label756;
                        }
                        i = 3;
                        break label756;
                        if (!((String)localObject1).equals("video")) {
                          break label756;
                        }
                        i = 4;
                        break label756;
                        if (!((String)localObject1).equals("sticker")) {
                          break label756;
                        }
                        i = 5;
                        break label756;
                        ((TLRPC.TL_documentAttributeFilename)localObject5).file_name = "animation.gif";
                        if (str.endsWith("mp4"))
                        {
                          localTL_document.mime_type = "video/mp4";
                          localTL_document.attributes.add(new TLRPC.TL_documentAttributeAnimated());
                        }
                        for (;;)
                        {
                          try
                          {
                            if (!str.endsWith("mp4")) {
                              break label1250;
                            }
                            localObject1 = ThumbnailUtils.createVideoThumbnail(str, 1);
                            if (localObject1 == null) {
                              break;
                            }
                            localTL_document.thumb = ImageLoader.scaleAndSaveImage((Bitmap)localObject1, 90.0F, 90.0F, 55, false);
                            ((Bitmap)localObject1).recycle();
                          }
                          catch (Throwable localThrowable1)
                          {
                            FileLog.e(localThrowable1);
                          }
                          break;
                          localTL_document.mime_type = "image/gif";
                          continue;
                          label1250:
                          localObject2 = ImageLoader.loadBitmap(str, null, 90.0F, 90.0F, true);
                        }
                        Object localObject2 = new TLRPC.TL_documentAttributeAudio();
                        ((TLRPC.TL_documentAttributeAudio)localObject2).duration = MessageObject.getInlineResultDuration(this.val$result);
                        ((TLRPC.TL_documentAttributeAudio)localObject2).voice = true;
                        ((TLRPC.TL_documentAttributeFilename)localObject5).file_name = "audio.ogg";
                        localTL_document.attributes.add(localObject2);
                        localTL_document.thumb = new TLRPC.TL_photoSizeEmpty();
                        localTL_document.thumb.type = "s";
                        continue;
                        localObject2 = new TLRPC.TL_documentAttributeAudio();
                        ((TLRPC.TL_documentAttributeAudio)localObject2).duration = MessageObject.getInlineResultDuration(this.val$result);
                        ((TLRPC.TL_documentAttributeAudio)localObject2).title = this.val$result.title;
                        ((TLRPC.TL_documentAttributeAudio)localObject2).flags |= 0x1;
                        if (this.val$result.description != null)
                        {
                          ((TLRPC.TL_documentAttributeAudio)localObject2).performer = this.val$result.description;
                          ((TLRPC.TL_documentAttributeAudio)localObject2).flags |= 0x2;
                        }
                        ((TLRPC.TL_documentAttributeFilename)localObject5).file_name = "audio.mp3";
                        localTL_document.attributes.add(localObject2);
                        localTL_document.thumb = new TLRPC.TL_photoSizeEmpty();
                        localTL_document.thumb.type = "s";
                        continue;
                        i = this.val$result.content.mime_type.lastIndexOf('/');
                        if (i != -1)
                        {
                          ((TLRPC.TL_documentAttributeFilename)localObject5).file_name = ("file." + this.val$result.content.mime_type.substring(i + 1));
                        }
                        else
                        {
                          ((TLRPC.TL_documentAttributeFilename)localObject5).file_name = "file";
                          continue;
                          ((TLRPC.TL_documentAttributeFilename)localObject5).file_name = "video.mp4";
                          localObject2 = new TLRPC.TL_documentAttributeVideo();
                          localObject7 = MessageObject.getInlineResultWidthAndHeight(this.val$result);
                          ((TLRPC.TL_documentAttributeVideo)localObject2).w = localObject7[0];
                          ((TLRPC.TL_documentAttributeVideo)localObject2).h = localObject7[1];
                          ((TLRPC.TL_documentAttributeVideo)localObject2).duration = MessageObject.getInlineResultDuration(this.val$result);
                          ((TLRPC.TL_documentAttributeVideo)localObject2).supports_streaming = true;
                          localTL_document.attributes.add(localObject2);
                          try
                          {
                            if (this.val$result.thumb == null) {
                              continue;
                            }
                            localObject2 = ImageLoader.loadBitmap(new File(FileLoader.getDirectory(4), Utilities.MD5(this.val$result.thumb.url) + "." + ImageLoader.getHttpUrlExtension(this.val$result.thumb.url, "jpg")).getAbsolutePath(), null, 90.0F, 90.0F, true);
                            if (localObject2 == null) {
                              continue;
                            }
                            localTL_document.thumb = ImageLoader.scaleAndSaveImage((Bitmap)localObject2, 90.0F, 90.0F, 55, false);
                            ((Bitmap)localObject2).recycle();
                          }
                          catch (Throwable localThrowable2)
                          {
                            FileLog.e(localThrowable2);
                          }
                          continue;
                          Object localObject3 = new TLRPC.TL_documentAttributeSticker();
                          ((TLRPC.TL_documentAttributeSticker)localObject3).alt = "";
                          ((TLRPC.TL_documentAttributeSticker)localObject3).stickerset = new TLRPC.TL_inputStickerSetEmpty();
                          localTL_document.attributes.add(localObject3);
                          localObject3 = new TLRPC.TL_documentAttributeImageSize();
                          localObject7 = MessageObject.getInlineResultWidthAndHeight(this.val$result);
                          ((TLRPC.TL_documentAttributeImageSize)localObject3).w = localObject7[0];
                          ((TLRPC.TL_documentAttributeImageSize)localObject3).h = localObject7[1];
                          localTL_document.attributes.add(localObject3);
                          ((TLRPC.TL_documentAttributeFilename)localObject5).file_name = "sticker.webp";
                          try
                          {
                            if (this.val$result.thumb != null)
                            {
                              localObject3 = ImageLoader.loadBitmap(new File(FileLoader.getDirectory(4), Utilities.MD5(this.val$result.thumb.url) + "." + ImageLoader.getHttpUrlExtension(this.val$result.thumb.url, "webp")).getAbsolutePath(), null, 90.0F, 90.0F, true);
                              if (localObject3 != null)
                              {
                                localTL_document.thumb = ImageLoader.scaleAndSaveImage((Bitmap)localObject3, 90.0F, 90.0F, 55, false);
                                ((Bitmap)localObject3).recycle();
                              }
                            }
                          }
                          catch (Throwable localThrowable3)
                          {
                            FileLog.e(localThrowable3);
                          }
                        }
                      }
                      localObject6 = localObject8;
                      if (localThrowable3.exists()) {
                        localObject6 = SendMessagesHelper.getInstance(paramHashMap).generatePhotoSizes(str, null);
                      }
                      localObject5 = localTL_document;
                      localObject7 = str;
                      localTL_game1 = localTL_game2;
                      Object localObject4 = localObject6;
                      if (localObject6 == null)
                      {
                        localObject4 = new TLRPC.TL_photo();
                        ((TLRPC.TL_photo)localObject4).date = ConnectionsManager.getInstance(paramHashMap).getCurrentTime();
                        localObject5 = new TLRPC.TL_photoSize();
                        localObject6 = MessageObject.getInlineResultWidthAndHeight(this.val$result);
                        ((TLRPC.TL_photoSize)localObject5).w = localObject6[0];
                        ((TLRPC.TL_photoSize)localObject5).h = localObject6[1];
                        ((TLRPC.TL_photoSize)localObject5).size = 1;
                        ((TLRPC.TL_photoSize)localObject5).location = new TLRPC.TL_fileLocationUnavailable();
                        ((TLRPC.TL_photoSize)localObject5).type = "x";
                        ((TLRPC.TL_photo)localObject4).sizes.add(localObject5);
                        localObject5 = localTL_document;
                        localObject7 = str;
                        localTL_game1 = localTL_game2;
                      }
                    }
                  }
                }
              }
            }
          }
        }).run();
        return;
      }
      if ((paramBotInlineResult.send_message instanceof TLRPC.TL_botInlineMessageText))
      {
        SendMessagesHelper localSendMessagesHelper = null;
        localObject1 = localSendMessagesHelper;
        int i;
        if ((int)paramLong == 0)
        {
          i = 0;
          localObject1 = localSendMessagesHelper;
          if (i < paramBotInlineResult.send_message.entities.size())
          {
            localObject2 = (TLRPC.MessageEntity)paramBotInlineResult.send_message.entities.get(i);
            if (!(localObject2 instanceof TLRPC.TL_messageEntityUrl)) {
              break label213;
            }
            localObject1 = new TLRPC.TL_webPagePending();
            ((TLRPC.WebPage)localObject1).url = paramBotInlineResult.send_message.message.substring(((TLRPC.MessageEntity)localObject2).offset, ((TLRPC.MessageEntity)localObject2).offset + ((TLRPC.MessageEntity)localObject2).length);
          }
        }
        localSendMessagesHelper = getInstance(j);
        Object localObject2 = paramBotInlineResult.send_message.message;
        if (!paramBotInlineResult.send_message.no_webpage) {}
        for (boolean bool = true;; bool = false)
        {
          localSendMessagesHelper.sendMessage((String)localObject2, paramLong, paramMessageObject, (TLRPC.WebPage)localObject1, bool, paramBotInlineResult.send_message.entities, paramBotInlineResult.send_message.reply_markup, paramHashMap);
          return;
          i += 1;
          break;
        }
      }
      if ((paramBotInlineResult.send_message instanceof TLRPC.TL_botInlineMessageMediaVenue))
      {
        localObject1 = new TLRPC.TL_messageMediaVenue();
        ((TLRPC.TL_messageMediaVenue)localObject1).geo = paramBotInlineResult.send_message.geo;
        ((TLRPC.TL_messageMediaVenue)localObject1).address = paramBotInlineResult.send_message.address;
        ((TLRPC.TL_messageMediaVenue)localObject1).title = paramBotInlineResult.send_message.title;
        ((TLRPC.TL_messageMediaVenue)localObject1).provider = paramBotInlineResult.send_message.provider;
        ((TLRPC.TL_messageMediaVenue)localObject1).venue_id = paramBotInlineResult.send_message.venue_id;
        ((TLRPC.TL_messageMediaVenue)localObject1).venue_type = "";
        getInstance(j).sendMessage((TLRPC.MessageMedia)localObject1, paramLong, paramMessageObject, paramBotInlineResult.send_message.reply_markup, paramHashMap);
        return;
      }
      if ((paramBotInlineResult.send_message instanceof TLRPC.TL_botInlineMessageMediaGeo))
      {
        if (paramBotInlineResult.send_message.period != 0)
        {
          localObject1 = new TLRPC.TL_messageMediaGeoLive();
          ((TLRPC.TL_messageMediaGeoLive)localObject1).period = paramBotInlineResult.send_message.period;
          ((TLRPC.TL_messageMediaGeoLive)localObject1).geo = paramBotInlineResult.send_message.geo;
          getInstance(j).sendMessage((TLRPC.MessageMedia)localObject1, paramLong, paramMessageObject, paramBotInlineResult.send_message.reply_markup, paramHashMap);
          return;
        }
        localObject1 = new TLRPC.TL_messageMediaGeo();
        ((TLRPC.TL_messageMediaGeo)localObject1).geo = paramBotInlineResult.send_message.geo;
        getInstance(j).sendMessage((TLRPC.MessageMedia)localObject1, paramLong, paramMessageObject, paramBotInlineResult.send_message.reply_markup, paramHashMap);
        return;
      }
    } while (!(paramBotInlineResult.send_message instanceof TLRPC.TL_botInlineMessageMediaContact));
    Object localObject1 = new TLRPC.TL_user();
    ((TLRPC.User)localObject1).phone = paramBotInlineResult.send_message.phone_number;
    ((TLRPC.User)localObject1).first_name = paramBotInlineResult.send_message.first_name;
    ((TLRPC.User)localObject1).last_name = paramBotInlineResult.send_message.last_name;
    getInstance(j).sendMessage((TLRPC.User)localObject1, paramLong, paramMessageObject, paramBotInlineResult.send_message.reply_markup, paramHashMap);
  }
  
  public static void prepareSendingDocument(String paramString1, String paramString2, Uri paramUri, String paramString3, long paramLong, MessageObject paramMessageObject, InputContentInfoCompat paramInputContentInfoCompat)
  {
    if (((paramString1 == null) || (paramString2 == null)) && (paramUri == null)) {
      return;
    }
    ArrayList localArrayList2 = new ArrayList();
    ArrayList localArrayList3 = new ArrayList();
    ArrayList localArrayList1 = null;
    if (paramUri != null)
    {
      localArrayList1 = new ArrayList();
      localArrayList1.add(paramUri);
    }
    if (paramString1 != null)
    {
      localArrayList2.add(paramString1);
      localArrayList3.add(paramString2);
    }
    prepareSendingDocuments(localArrayList2, localArrayList3, localArrayList1, paramString3, paramLong, paramMessageObject, paramInputContentInfoCompat);
  }
  
  /* Error */
  private static boolean prepareSendingDocumentInternal(int paramInt, final String paramString1, final String paramString2, Uri paramUri, final String paramString3, final long paramLong, MessageObject paramMessageObject, CharSequence paramCharSequence, final ArrayList<TLRPC.MessageEntity> paramArrayList)
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnull +10 -> 11
    //   4: aload_1
    //   5: invokevirtual 782	java/lang/String:length	()I
    //   8: ifne +9 -> 17
    //   11: aload_3
    //   12: ifnonnull +5 -> 17
    //   15: iconst_0
    //   16: ireturn
    //   17: aload_3
    //   18: ifnull +12 -> 30
    //   21: aload_3
    //   22: invokestatic 1227	org/telegram/messenger/AndroidUtilities:isInternalUri	(Landroid/net/Uri;)Z
    //   25: ifeq +5 -> 30
    //   28: iconst_0
    //   29: ireturn
    //   30: aload_1
    //   31: ifnull +22 -> 53
    //   34: new 601	java/io/File
    //   37: dup
    //   38: aload_1
    //   39: invokespecial 602	java/io/File:<init>	(Ljava/lang/String;)V
    //   42: invokestatic 704	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   45: invokestatic 1227	org/telegram/messenger/AndroidUtilities:isInternalUri	(Landroid/net/Uri;)Z
    //   48: ifeq +5 -> 53
    //   51: iconst_0
    //   52: ireturn
    //   53: invokestatic 1233	android/webkit/MimeTypeMap:getSingleton	()Landroid/webkit/MimeTypeMap;
    //   56: astore 30
    //   58: aconst_null
    //   59: astore 25
    //   61: aconst_null
    //   62: astore 20
    //   64: aconst_null
    //   65: astore 21
    //   67: aload_1
    //   68: astore 23
    //   70: aload_3
    //   71: ifnull +65 -> 136
    //   74: iconst_0
    //   75: istore 10
    //   77: aload 21
    //   79: astore_1
    //   80: aload 4
    //   82: ifnull +11 -> 93
    //   85: aload 30
    //   87: aload 4
    //   89: invokevirtual 1236	android/webkit/MimeTypeMap:getExtensionFromMimeType	(Ljava/lang/String;)Ljava/lang/String;
    //   92: astore_1
    //   93: aload_1
    //   94: ifnonnull +19 -> 113
    //   97: ldc_w 1238
    //   100: astore_1
    //   101: aload_3
    //   102: aload_1
    //   103: invokestatic 1242	org/telegram/messenger/MediaController:copyFileToCache	(Landroid/net/Uri;Ljava/lang/String;)Ljava/lang/String;
    //   106: astore_3
    //   107: aload_3
    //   108: ifnonnull +11 -> 119
    //   111: iconst_0
    //   112: ireturn
    //   113: iconst_1
    //   114: istore 10
    //   116: goto -15 -> 101
    //   119: aload_1
    //   120: astore 20
    //   122: aload_3
    //   123: astore 23
    //   125: iload 10
    //   127: ifne +9 -> 136
    //   130: aconst_null
    //   131: astore 20
    //   133: aload_3
    //   134: astore 23
    //   136: new 601	java/io/File
    //   139: dup
    //   140: aload 23
    //   142: invokespecial 602	java/io/File:<init>	(Ljava/lang/String;)V
    //   145: astore 31
    //   147: aload 31
    //   149: invokevirtual 849	java/io/File:exists	()Z
    //   152: ifeq +13 -> 165
    //   155: aload 31
    //   157: invokevirtual 605	java/io/File:length	()J
    //   160: lconst_0
    //   161: lcmp
    //   162: ifne +5 -> 167
    //   165: iconst_0
    //   166: ireturn
    //   167: lload 5
    //   169: l2i
    //   170: ifne +976 -> 1146
    //   173: iconst_1
    //   174: istore 19
    //   176: iload 19
    //   178: ifne +974 -> 1152
    //   181: iconst_1
    //   182: istore 13
    //   184: aload 31
    //   186: invokevirtual 1243	java/io/File:getName	()Ljava/lang/String;
    //   189: astore 32
    //   191: ldc_w 1168
    //   194: astore 21
    //   196: aload 20
    //   198: ifnull +960 -> 1158
    //   201: aload 20
    //   203: astore 21
    //   205: aload 21
    //   207: invokevirtual 1246	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   210: astore 33
    //   212: aconst_null
    //   213: astore 4
    //   215: aconst_null
    //   216: astore 27
    //   218: aconst_null
    //   219: astore 24
    //   221: aconst_null
    //   222: astore 22
    //   224: aconst_null
    //   225: astore 20
    //   227: aconst_null
    //   228: astore 28
    //   230: aconst_null
    //   231: astore 26
    //   233: aconst_null
    //   234: astore 29
    //   236: iconst_0
    //   237: istore 16
    //   239: iconst_0
    //   240: istore 15
    //   242: iconst_0
    //   243: istore 10
    //   245: iconst_0
    //   246: istore 18
    //   248: iconst_0
    //   249: istore 12
    //   251: iconst_0
    //   252: istore 17
    //   254: aload 33
    //   256: ldc_w 1248
    //   259: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   262: ifne +14 -> 276
    //   265: aload 33
    //   267: ldc_w 1250
    //   270: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   273: ifeq +914 -> 1187
    //   276: aload 31
    //   278: invokestatic 1256	org/telegram/messenger/audioinfo/AudioInfo:getAudioInfo	(Ljava/io/File;)Lorg/telegram/messenger/audioinfo/AudioInfo;
    //   281: astore 4
    //   283: iload 17
    //   285: istore 11
    //   287: iload 15
    //   289: istore 14
    //   291: aload 22
    //   293: astore_3
    //   294: aload 29
    //   296: astore_1
    //   297: aload 4
    //   299: ifnull +47 -> 346
    //   302: iload 17
    //   304: istore 11
    //   306: iload 15
    //   308: istore 14
    //   310: aload 22
    //   312: astore_3
    //   313: aload 29
    //   315: astore_1
    //   316: aload 4
    //   318: invokevirtual 1257	org/telegram/messenger/audioinfo/AudioInfo:getDuration	()J
    //   321: lconst_0
    //   322: lcmp
    //   323: ifeq +23 -> 346
    //   326: aload 4
    //   328: invokevirtual 1260	org/telegram/messenger/audioinfo/AudioInfo:getArtist	()Ljava/lang/String;
    //   331: astore_3
    //   332: aload 4
    //   334: invokevirtual 1263	org/telegram/messenger/audioinfo/AudioInfo:getTitle	()Ljava/lang/String;
    //   337: astore_1
    //   338: iload 15
    //   340: istore 14
    //   342: iload 17
    //   344: istore 11
    //   346: aload 25
    //   348: astore 4
    //   350: iload 11
    //   352: ifeq +106 -> 458
    //   355: new 1265	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio
    //   358: dup
    //   359: invokespecial 1266	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:<init>	()V
    //   362: astore 20
    //   364: aload 20
    //   366: iload 11
    //   368: putfield 1267	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:duration	I
    //   371: aload 20
    //   373: aload_1
    //   374: putfield 1268	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:title	Ljava/lang/String;
    //   377: aload 20
    //   379: aload_3
    //   380: putfield 1271	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:performer	Ljava/lang/String;
    //   383: aload 20
    //   385: getfield 1268	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:title	Ljava/lang/String;
    //   388: ifnonnull +11 -> 399
    //   391: aload 20
    //   393: ldc_w 1168
    //   396: putfield 1268	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:title	Ljava/lang/String;
    //   399: aload 20
    //   401: aload 20
    //   403: getfield 1274	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:flags	I
    //   406: iconst_1
    //   407: ior
    //   408: putfield 1274	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:flags	I
    //   411: aload 20
    //   413: getfield 1271	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:performer	Ljava/lang/String;
    //   416: ifnonnull +11 -> 427
    //   419: aload 20
    //   421: ldc_w 1168
    //   424: putfield 1271	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:performer	Ljava/lang/String;
    //   427: aload 20
    //   429: aload 20
    //   431: getfield 1274	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:flags	I
    //   434: iconst_2
    //   435: ior
    //   436: putfield 1274	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:flags	I
    //   439: aload 20
    //   441: astore 4
    //   443: iload 14
    //   445: ifeq +13 -> 458
    //   448: aload 20
    //   450: iconst_1
    //   451: putfield 1277	org/telegram/tgnet/TLRPC$TL_documentAttributeAudio:voice	Z
    //   454: aload 20
    //   456: astore 4
    //   458: iconst_0
    //   459: istore 11
    //   461: iload 11
    //   463: istore 10
    //   465: aload_2
    //   466: astore_3
    //   467: aload_2
    //   468: ifnull +18 -> 486
    //   471: aload_2
    //   472: ldc_w 1279
    //   475: invokevirtual 794	java/lang/String:endsWith	(Ljava/lang/String;)Z
    //   478: ifeq +1109 -> 1587
    //   481: iconst_1
    //   482: istore 10
    //   484: aload_2
    //   485: astore_3
    //   486: aconst_null
    //   487: astore_2
    //   488: aload_2
    //   489: astore_1
    //   490: iload 10
    //   492: ifne +106 -> 598
    //   495: aload_2
    //   496: astore_1
    //   497: iload 19
    //   499: ifne +99 -> 598
    //   502: iload_0
    //   503: invokestatic 1284	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
    //   506: astore_1
    //   507: iload 19
    //   509: ifne +1155 -> 1664
    //   512: iconst_1
    //   513: istore 10
    //   515: aload_1
    //   516: aload_3
    //   517: iload 10
    //   519: invokevirtual 1288	org/telegram/messenger/MessagesStorage:getSentFile	(Ljava/lang/String;I)Lorg/telegram/tgnet/TLObject;
    //   522: checkcast 1290	org/telegram/tgnet/TLRPC$TL_document
    //   525: astore_2
    //   526: aload_2
    //   527: astore_1
    //   528: aload_2
    //   529: ifnonnull +69 -> 598
    //   532: aload_2
    //   533: astore_1
    //   534: aload 23
    //   536: aload_3
    //   537: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   540: ifne +58 -> 598
    //   543: aload_2
    //   544: astore_1
    //   545: iload 19
    //   547: ifne +51 -> 598
    //   550: iload_0
    //   551: invokestatic 1284	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
    //   554: astore_1
    //   555: new 507	java/lang/StringBuilder
    //   558: dup
    //   559: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   562: aload 23
    //   564: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   567: aload 31
    //   569: invokevirtual 605	java/io/File:length	()J
    //   572: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   575: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   578: astore_2
    //   579: iload 19
    //   581: ifne +1089 -> 1670
    //   584: iconst_1
    //   585: istore 10
    //   587: aload_1
    //   588: aload_2
    //   589: iload 10
    //   591: invokevirtual 1288	org/telegram/messenger/MessagesStorage:getSentFile	(Ljava/lang/String;I)Lorg/telegram/tgnet/TLObject;
    //   594: checkcast 1290	org/telegram/tgnet/TLRPC$TL_document
    //   597: astore_1
    //   598: aload_1
    //   599: astore_2
    //   600: aload_1
    //   601: ifnonnull +484 -> 1085
    //   604: new 1290	org/telegram/tgnet/TLRPC$TL_document
    //   607: dup
    //   608: invokespecial 1291	org/telegram/tgnet/TLRPC$TL_document:<init>	()V
    //   611: astore_1
    //   612: aload_1
    //   613: lconst_0
    //   614: putfield 1292	org/telegram/tgnet/TLRPC$TL_document:id	J
    //   617: aload_1
    //   618: iload_0
    //   619: invokestatic 1047	org/telegram/tgnet/ConnectionsManager:getInstance	(I)Lorg/telegram/tgnet/ConnectionsManager;
    //   622: invokevirtual 1295	org/telegram/tgnet/ConnectionsManager:getCurrentTime	()I
    //   625: putfield 1298	org/telegram/tgnet/TLRPC$TL_document:date	I
    //   628: new 1300	org/telegram/tgnet/TLRPC$TL_documentAttributeFilename
    //   631: dup
    //   632: invokespecial 1301	org/telegram/tgnet/TLRPC$TL_documentAttributeFilename:<init>	()V
    //   635: astore_2
    //   636: aload_2
    //   637: aload 32
    //   639: putfield 1304	org/telegram/tgnet/TLRPC$TL_documentAttributeFilename:file_name	Ljava/lang/String;
    //   642: aload_1
    //   643: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   646: aload_2
    //   647: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   650: pop
    //   651: aload_1
    //   652: aload 31
    //   654: invokevirtual 605	java/io/File:length	()J
    //   657: l2i
    //   658: putfield 1308	org/telegram/tgnet/TLRPC$TL_document:size	I
    //   661: aload_1
    //   662: iconst_0
    //   663: putfield 1309	org/telegram/tgnet/TLRPC$TL_document:dc_id	I
    //   666: aload 4
    //   668: ifnull +13 -> 681
    //   671: aload_1
    //   672: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   675: aload 4
    //   677: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   680: pop
    //   681: aload 21
    //   683: invokevirtual 782	java/lang/String:length	()I
    //   686: ifeq +1108 -> 1794
    //   689: iconst_m1
    //   690: istore 10
    //   692: aload 33
    //   694: invokevirtual 1312	java/lang/String:hashCode	()I
    //   697: lookupswitch	default:+43->740, 109967:+1013->1710, 3145576:+1030->1727, 3418175:+996->1693, 3645340:+979->1676
    //   740: iload 10
    //   742: tableswitch	default:+30->772, 0:+1002->1744, 1:+1012->1754, 2:+1022->1764, 3:+1032->1774
    //   772: aload 30
    //   774: aload 33
    //   776: invokevirtual 1315	android/webkit/MimeTypeMap:getMimeTypeFromExtension	(Ljava/lang/String;)Ljava/lang/String;
    //   779: astore 4
    //   781: aload 4
    //   783: ifnull +1001 -> 1784
    //   786: aload_1
    //   787: aload 4
    //   789: putfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   792: aload_1
    //   793: getfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   796: ldc_w 1320
    //   799: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   802: ifeq +57 -> 859
    //   805: aload 31
    //   807: invokevirtual 1323	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   810: aconst_null
    //   811: ldc_w 648
    //   814: ldc_w 648
    //   817: iconst_1
    //   818: invokestatic 1327	org/telegram/messenger/ImageLoader:loadBitmap	(Ljava/lang/String;Landroid/net/Uri;FFZ)Landroid/graphics/Bitmap;
    //   821: astore 4
    //   823: aload 4
    //   825: ifnull +34 -> 859
    //   828: aload_2
    //   829: ldc_w 1329
    //   832: putfield 1304	org/telegram/tgnet/TLRPC$TL_documentAttributeFilename:file_name	Ljava/lang/String;
    //   835: aload_1
    //   836: aload 4
    //   838: ldc_w 648
    //   841: ldc_w 648
    //   844: bipush 55
    //   846: iload 19
    //   848: invokestatic 1333	org/telegram/messenger/ImageLoader:scaleAndSaveImage	(Landroid/graphics/Bitmap;FFIZ)Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   851: putfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   854: aload 4
    //   856: invokevirtual 657	android/graphics/Bitmap:recycle	()V
    //   859: aload_1
    //   860: getfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   863: ldc_w 1338
    //   866: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   869: ifeq +184 -> 1053
    //   872: iload 13
    //   874: ifeq +179 -> 1053
    //   877: new 1340	android/graphics/BitmapFactory$Options
    //   880: dup
    //   881: invokespecial 1341	android/graphics/BitmapFactory$Options:<init>	()V
    //   884: astore_2
    //   885: aload_2
    //   886: iconst_1
    //   887: putfield 1344	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
    //   890: new 1346	java/io/RandomAccessFile
    //   893: dup
    //   894: aload 23
    //   896: ldc_w 1348
    //   899: invokespecial 1351	java/io/RandomAccessFile:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   902: astore 4
    //   904: aload 4
    //   906: invokevirtual 1355	java/io/RandomAccessFile:getChannel	()Ljava/nio/channels/FileChannel;
    //   909: getstatic 1361	java/nio/channels/FileChannel$MapMode:READ_ONLY	Ljava/nio/channels/FileChannel$MapMode;
    //   912: lconst_0
    //   913: aload 23
    //   915: invokevirtual 782	java/lang/String:length	()I
    //   918: i2l
    //   919: invokevirtual 1367	java/nio/channels/FileChannel:map	(Ljava/nio/channels/FileChannel$MapMode;JJ)Ljava/nio/MappedByteBuffer;
    //   922: astore 20
    //   924: aconst_null
    //   925: aload 20
    //   927: aload 20
    //   929: invokevirtual 1372	java/nio/ByteBuffer:limit	()I
    //   932: aload_2
    //   933: iconst_1
    //   934: invokestatic 1376	org/telegram/messenger/Utilities:loadWebpImage	(Landroid/graphics/Bitmap;Ljava/nio/ByteBuffer;ILandroid/graphics/BitmapFactory$Options;Z)Z
    //   937: pop
    //   938: aload 4
    //   940: invokevirtual 1379	java/io/RandomAccessFile:close	()V
    //   943: aload_2
    //   944: getfield 1382	android/graphics/BitmapFactory$Options:outWidth	I
    //   947: ifeq +106 -> 1053
    //   950: aload_2
    //   951: getfield 1385	android/graphics/BitmapFactory$Options:outHeight	I
    //   954: ifeq +99 -> 1053
    //   957: aload_2
    //   958: getfield 1382	android/graphics/BitmapFactory$Options:outWidth	I
    //   961: sipush 800
    //   964: if_icmpgt +89 -> 1053
    //   967: aload_2
    //   968: getfield 1385	android/graphics/BitmapFactory$Options:outHeight	I
    //   971: sipush 800
    //   974: if_icmpgt +79 -> 1053
    //   977: new 1387	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker
    //   980: dup
    //   981: invokespecial 1388	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker:<init>	()V
    //   984: astore 4
    //   986: aload 4
    //   988: ldc_w 1168
    //   991: putfield 1391	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker:alt	Ljava/lang/String;
    //   994: aload 4
    //   996: new 1393	org/telegram/tgnet/TLRPC$TL_inputStickerSetEmpty
    //   999: dup
    //   1000: invokespecial 1394	org/telegram/tgnet/TLRPC$TL_inputStickerSetEmpty:<init>	()V
    //   1003: putfield 1398	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   1006: aload_1
    //   1007: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   1010: aload 4
    //   1012: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   1015: pop
    //   1016: new 1400	org/telegram/tgnet/TLRPC$TL_documentAttributeImageSize
    //   1019: dup
    //   1020: invokespecial 1401	org/telegram/tgnet/TLRPC$TL_documentAttributeImageSize:<init>	()V
    //   1023: astore 4
    //   1025: aload 4
    //   1027: aload_2
    //   1028: getfield 1382	android/graphics/BitmapFactory$Options:outWidth	I
    //   1031: putfield 1402	org/telegram/tgnet/TLRPC$TL_documentAttributeImageSize:w	I
    //   1034: aload 4
    //   1036: aload_2
    //   1037: getfield 1385	android/graphics/BitmapFactory$Options:outHeight	I
    //   1040: putfield 1403	org/telegram/tgnet/TLRPC$TL_documentAttributeImageSize:h	I
    //   1043: aload_1
    //   1044: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   1047: aload 4
    //   1049: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   1052: pop
    //   1053: aload_1
    //   1054: astore_2
    //   1055: aload_1
    //   1056: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   1059: ifnonnull +26 -> 1085
    //   1062: aload_1
    //   1063: new 1405	org/telegram/tgnet/TLRPC$TL_photoSizeEmpty
    //   1066: dup
    //   1067: invokespecial 1406	org/telegram/tgnet/TLRPC$TL_photoSizeEmpty:<init>	()V
    //   1070: putfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   1073: aload_1
    //   1074: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   1077: ldc_w 1408
    //   1080: putfield 1412	org/telegram/tgnet/TLRPC$PhotoSize:type	Ljava/lang/String;
    //   1083: aload_1
    //   1084: astore_2
    //   1085: aload 8
    //   1087: ifnull +735 -> 1822
    //   1090: aload 8
    //   1092: invokeinterface 1415 1 0
    //   1097: astore_1
    //   1098: new 229	java/util/HashMap
    //   1101: dup
    //   1102: invokespecial 230	java/util/HashMap:<init>	()V
    //   1105: astore 4
    //   1107: aload_3
    //   1108: ifnull +13 -> 1121
    //   1111: aload 4
    //   1113: ldc_w 1416
    //   1116: aload_3
    //   1117: invokevirtual 999	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   1120: pop
    //   1121: new 54	org/telegram/messenger/SendMessagesHelper$16
    //   1124: dup
    //   1125: iload_0
    //   1126: aload_2
    //   1127: aload 23
    //   1129: lload 5
    //   1131: aload 7
    //   1133: aload_1
    //   1134: aload 9
    //   1136: aload 4
    //   1138: invokespecial 1419	org/telegram/messenger/SendMessagesHelper$16:<init>	(ILorg/telegram/tgnet/TLRPC$TL_document;Ljava/lang/String;JLorg/telegram/messenger/MessageObject;Ljava/lang/String;Ljava/util/ArrayList;Ljava/util/HashMap;)V
    //   1141: invokestatic 260	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
    //   1144: iconst_1
    //   1145: ireturn
    //   1146: iconst_0
    //   1147: istore 19
    //   1149: goto -973 -> 176
    //   1152: iconst_0
    //   1153: istore 13
    //   1155: goto -971 -> 184
    //   1158: aload 23
    //   1160: bipush 46
    //   1162: invokevirtual 1423	java/lang/String:lastIndexOf	(I)I
    //   1165: istore 10
    //   1167: iload 10
    //   1169: iconst_m1
    //   1170: if_icmpeq -965 -> 205
    //   1173: aload 23
    //   1175: iload 10
    //   1177: iconst_1
    //   1178: iadd
    //   1179: invokevirtual 791	java/lang/String:substring	(I)Ljava/lang/String;
    //   1182: astore 21
    //   1184: goto -979 -> 205
    //   1187: aload 33
    //   1189: ldc_w 1425
    //   1192: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1195: ifne +39 -> 1234
    //   1198: aload 33
    //   1200: ldc_w 1427
    //   1203: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1206: ifne +28 -> 1234
    //   1209: iload 17
    //   1211: istore 11
    //   1213: iload 15
    //   1215: istore 14
    //   1217: aload 22
    //   1219: astore_3
    //   1220: aload 29
    //   1222: astore_1
    //   1223: aload 33
    //   1225: ldc_w 1429
    //   1228: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1231: ifeq -885 -> 346
    //   1234: aconst_null
    //   1235: astore_1
    //   1236: aconst_null
    //   1237: astore_3
    //   1238: new 627	android/media/MediaMetadataRetriever
    //   1241: dup
    //   1242: invokespecial 628	android/media/MediaMetadataRetriever:<init>	()V
    //   1245: astore 22
    //   1247: iload 12
    //   1249: istore 11
    //   1251: aload 24
    //   1253: astore_3
    //   1254: aload 26
    //   1256: astore_1
    //   1257: aload 22
    //   1259: aload 31
    //   1261: invokevirtual 1323	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   1264: invokevirtual 631	android/media/MediaMetadataRetriever:setDataSource	(Ljava/lang/String;)V
    //   1267: iload 12
    //   1269: istore 11
    //   1271: aload 24
    //   1273: astore_3
    //   1274: aload 26
    //   1276: astore_1
    //   1277: aload 22
    //   1279: bipush 9
    //   1281: invokevirtual 661	android/media/MediaMetadataRetriever:extractMetadata	(I)Ljava/lang/String;
    //   1284: astore 27
    //   1286: aload 27
    //   1288: ifnull +67 -> 1355
    //   1291: iload 12
    //   1293: istore 11
    //   1295: aload 24
    //   1297: astore_3
    //   1298: aload 26
    //   1300: astore_1
    //   1301: aload 27
    //   1303: invokestatic 681	java/lang/Long:parseLong	(Ljava/lang/String;)J
    //   1306: l2f
    //   1307: ldc_w 524
    //   1310: fdiv
    //   1311: f2d
    //   1312: invokestatic 550	java/lang/Math:ceil	(D)D
    //   1315: d2i
    //   1316: istore 10
    //   1318: iload 10
    //   1320: istore 11
    //   1322: aload 24
    //   1324: astore_3
    //   1325: aload 26
    //   1327: astore_1
    //   1328: aload 22
    //   1330: bipush 7
    //   1332: invokevirtual 661	android/media/MediaMetadataRetriever:extractMetadata	(I)Ljava/lang/String;
    //   1335: astore 20
    //   1337: iload 10
    //   1339: istore 11
    //   1341: aload 24
    //   1343: astore_3
    //   1344: aload 20
    //   1346: astore_1
    //   1347: aload 22
    //   1349: iconst_2
    //   1350: invokevirtual 661	android/media/MediaMetadataRetriever:extractMetadata	(I)Ljava/lang/String;
    //   1353: astore 4
    //   1355: iload 16
    //   1357: istore 12
    //   1359: iload 10
    //   1361: istore 11
    //   1363: aload 4
    //   1365: astore_3
    //   1366: aload 20
    //   1368: astore_1
    //   1369: aload 33
    //   1371: ldc_w 1427
    //   1374: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1377: ifeq +36 -> 1413
    //   1380: iload 10
    //   1382: istore 11
    //   1384: aload 4
    //   1386: astore_3
    //   1387: aload 20
    //   1389: astore_1
    //   1390: aload 31
    //   1392: invokevirtual 1323	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   1395: invokestatic 1432	org/telegram/messenger/MediaController:isOpusFile	(Ljava/lang/String;)I
    //   1398: istore 14
    //   1400: iload 16
    //   1402: istore 12
    //   1404: iload 14
    //   1406: iconst_1
    //   1407: if_icmpne +6 -> 1413
    //   1410: iconst_1
    //   1411: istore 12
    //   1413: iload 10
    //   1415: istore 11
    //   1417: iload 12
    //   1419: istore 14
    //   1421: aload 4
    //   1423: astore_3
    //   1424: aload 20
    //   1426: astore_1
    //   1427: aload 22
    //   1429: ifnull -1083 -> 346
    //   1432: aload 22
    //   1434: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   1437: iload 10
    //   1439: istore 11
    //   1441: iload 12
    //   1443: istore 14
    //   1445: aload 4
    //   1447: astore_3
    //   1448: aload 20
    //   1450: astore_1
    //   1451: goto -1105 -> 346
    //   1454: astore_1
    //   1455: aload_1
    //   1456: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1459: iload 10
    //   1461: istore 11
    //   1463: iload 12
    //   1465: istore 14
    //   1467: aload 4
    //   1469: astore_3
    //   1470: aload 20
    //   1472: astore_1
    //   1473: goto -1127 -> 346
    //   1476: astore 24
    //   1478: aload 28
    //   1480: astore 20
    //   1482: aload 27
    //   1484: astore 4
    //   1486: aload_3
    //   1487: astore 22
    //   1489: iload 18
    //   1491: istore 10
    //   1493: aload 22
    //   1495: astore_1
    //   1496: aload 24
    //   1498: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1501: iload 10
    //   1503: istore 11
    //   1505: iload 15
    //   1507: istore 14
    //   1509: aload 4
    //   1511: astore_3
    //   1512: aload 20
    //   1514: astore_1
    //   1515: aload 22
    //   1517: ifnull -1171 -> 346
    //   1520: aload 22
    //   1522: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   1525: iload 10
    //   1527: istore 11
    //   1529: iload 15
    //   1531: istore 14
    //   1533: aload 4
    //   1535: astore_3
    //   1536: aload 20
    //   1538: astore_1
    //   1539: goto -1193 -> 346
    //   1542: astore_1
    //   1543: aload_1
    //   1544: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1547: iload 10
    //   1549: istore 11
    //   1551: iload 15
    //   1553: istore 14
    //   1555: aload 4
    //   1557: astore_3
    //   1558: aload 20
    //   1560: astore_1
    //   1561: goto -1215 -> 346
    //   1564: astore_3
    //   1565: aload_1
    //   1566: astore_2
    //   1567: aload_3
    //   1568: astore_1
    //   1569: aload_2
    //   1570: ifnull +7 -> 1577
    //   1573: aload_2
    //   1574: invokevirtual 638	android/media/MediaMetadataRetriever:release	()V
    //   1577: aload_1
    //   1578: athrow
    //   1579: astore_2
    //   1580: aload_2
    //   1581: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1584: goto -7 -> 1577
    //   1587: aload 4
    //   1589: ifnull +39 -> 1628
    //   1592: new 507	java/lang/StringBuilder
    //   1595: dup
    //   1596: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   1599: aload_2
    //   1600: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1603: ldc_w 1434
    //   1606: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1609: aload 31
    //   1611: invokevirtual 605	java/io/File:length	()J
    //   1614: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   1617: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1620: astore_3
    //   1621: iload 11
    //   1623: istore 10
    //   1625: goto -1139 -> 486
    //   1628: new 507	java/lang/StringBuilder
    //   1631: dup
    //   1632: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   1635: aload_2
    //   1636: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1639: ldc_w 1168
    //   1642: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1645: aload 31
    //   1647: invokevirtual 605	java/io/File:length	()J
    //   1650: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   1653: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1656: astore_3
    //   1657: iload 11
    //   1659: istore 10
    //   1661: goto -1175 -> 486
    //   1664: iconst_4
    //   1665: istore 10
    //   1667: goto -1152 -> 515
    //   1670: iconst_4
    //   1671: istore 10
    //   1673: goto -1086 -> 587
    //   1676: aload 33
    //   1678: ldc_w 1436
    //   1681: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1684: ifeq -944 -> 740
    //   1687: iconst_0
    //   1688: istore 10
    //   1690: goto -950 -> 740
    //   1693: aload 33
    //   1695: ldc_w 1425
    //   1698: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1701: ifeq -961 -> 740
    //   1704: iconst_1
    //   1705: istore 10
    //   1707: goto -967 -> 740
    //   1710: aload 33
    //   1712: ldc_w 1427
    //   1715: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1718: ifeq -978 -> 740
    //   1721: iconst_2
    //   1722: istore 10
    //   1724: goto -984 -> 740
    //   1727: aload 33
    //   1729: ldc_w 1429
    //   1732: invokevirtual 493	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1735: ifeq -995 -> 740
    //   1738: iconst_3
    //   1739: istore 10
    //   1741: goto -1001 -> 740
    //   1744: aload_1
    //   1745: ldc_w 1338
    //   1748: putfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   1751: goto -959 -> 792
    //   1754: aload_1
    //   1755: ldc_w 1438
    //   1758: putfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   1761: goto -969 -> 792
    //   1764: aload_1
    //   1765: ldc_w 1440
    //   1768: putfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   1771: goto -979 -> 792
    //   1774: aload_1
    //   1775: ldc_w 1442
    //   1778: putfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   1781: goto -989 -> 792
    //   1784: aload_1
    //   1785: ldc_w 1444
    //   1788: putfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   1791: goto -999 -> 792
    //   1794: aload_1
    //   1795: ldc_w 1444
    //   1798: putfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   1801: goto -1009 -> 792
    //   1804: astore_2
    //   1805: aload_2
    //   1806: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1809: goto -950 -> 859
    //   1812: astore 4
    //   1814: aload 4
    //   1816: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1819: goto -876 -> 943
    //   1822: ldc_w 1168
    //   1825: astore_1
    //   1826: goto -728 -> 1098
    //   1829: astore_1
    //   1830: aload 22
    //   1832: astore_2
    //   1833: goto -264 -> 1569
    //   1836: astore 24
    //   1838: iload 11
    //   1840: istore 10
    //   1842: aload_3
    //   1843: astore 4
    //   1845: aload_1
    //   1846: astore 20
    //   1848: goto -355 -> 1493
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1851	0	paramInt	int
    //   0	1851	1	paramString1	String
    //   0	1851	2	paramString2	String
    //   0	1851	3	paramUri	Uri
    //   0	1851	4	paramString3	String
    //   0	1851	5	paramLong	long
    //   0	1851	7	paramMessageObject	MessageObject
    //   0	1851	8	paramCharSequence	CharSequence
    //   0	1851	9	paramArrayList	ArrayList<TLRPC.MessageEntity>
    //   75	1766	10	i	int
    //   285	1554	11	j	int
    //   249	1215	12	k	int
    //   182	972	13	m	int
    //   289	1265	14	n	int
    //   240	1312	15	i1	int
    //   237	1164	16	i2	int
    //   252	958	17	i3	int
    //   246	1244	18	i4	int
    //   174	974	19	bool	boolean
    //   62	1785	20	localObject1	Object
    //   65	1118	21	localObject2	Object
    //   222	1609	22	localObject3	Object
    //   68	1106	23	localObject4	Object
    //   219	1123	24	localObject5	Object
    //   1476	21	24	localException1	Exception
    //   1836	1	24	localException2	Exception
    //   59	288	25	localObject6	Object
    //   231	1095	26	localObject7	Object
    //   216	1267	27	str1	String
    //   228	1251	28	localObject8	Object
    //   234	987	29	localObject9	Object
    //   56	717	30	localMimeTypeMap	android.webkit.MimeTypeMap
    //   145	1501	31	localFile	File
    //   189	449	32	str2	String
    //   210	1518	33	str3	String
    // Exception table:
    //   from	to	target	type
    //   1432	1437	1454	java/lang/Exception
    //   1238	1247	1476	java/lang/Exception
    //   1520	1525	1542	java/lang/Exception
    //   1238	1247	1564	finally
    //   1496	1501	1564	finally
    //   1573	1577	1579	java/lang/Exception
    //   805	823	1804	java/lang/Exception
    //   828	859	1804	java/lang/Exception
    //   885	943	1812	java/lang/Exception
    //   1257	1267	1829	finally
    //   1277	1286	1829	finally
    //   1301	1318	1829	finally
    //   1328	1337	1829	finally
    //   1347	1355	1829	finally
    //   1369	1380	1829	finally
    //   1390	1400	1829	finally
    //   1257	1267	1836	java/lang/Exception
    //   1277	1286	1836	java/lang/Exception
    //   1301	1318	1836	java/lang/Exception
    //   1328	1337	1836	java/lang/Exception
    //   1347	1355	1836	java/lang/Exception
    //   1369	1380	1836	java/lang/Exception
    //   1390	1400	1836	java/lang/Exception
  }
  
  public static void prepareSendingDocuments(ArrayList<String> paramArrayList1, final ArrayList<String> paramArrayList2, final ArrayList<Uri> paramArrayList, final String paramString, final long paramLong, MessageObject paramMessageObject, final InputContentInfoCompat paramInputContentInfoCompat)
  {
    if (((paramArrayList1 == null) && (paramArrayList2 == null) && (paramArrayList == null)) || ((paramArrayList1 != null) && (paramArrayList2 != null) && (paramArrayList1.size() != paramArrayList2.size()))) {
      return;
    }
    new Thread(new Runnable()
    {
      public void run()
      {
        int i = 0;
        int j = 0;
        if (this.val$paths != null)
        {
          k = 0;
          for (;;)
          {
            i = j;
            if (k >= this.val$paths.size()) {
              break;
            }
            if (!SendMessagesHelper.prepareSendingDocumentInternal(this.val$currentAccount, (String)this.val$paths.get(k), (String)paramArrayList2.get(k), null, paramString, paramLong, paramArrayList, null, null)) {
              j = 1;
            }
            k += 1;
          }
        }
        int k = i;
        if (paramInputContentInfoCompat != null)
        {
          j = 0;
          for (;;)
          {
            k = i;
            if (j >= paramInputContentInfoCompat.size()) {
              break;
            }
            if (!SendMessagesHelper.prepareSendingDocumentInternal(this.val$currentAccount, null, null, (Uri)paramInputContentInfoCompat.get(j), paramString, paramLong, paramArrayList, null, null)) {
              i = 1;
            }
            j += 1;
          }
        }
        if (this.val$inputContent != null) {
          this.val$inputContent.releasePermission();
        }
        if (k != 0) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              try
              {
                Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("UnsupportedAttachment", 2131494517), 0).show();
                return;
              }
              catch (Exception localException)
              {
                FileLog.e(localException);
              }
            }
          });
        }
      }
    }).start();
  }
  
  public static void prepareSendingMedia(ArrayList<SendingMediaInfo> paramArrayList, final long paramLong, final MessageObject paramMessageObject, final InputContentInfoCompat paramInputContentInfoCompat, final boolean paramBoolean1, final boolean paramBoolean2)
  {
    if (paramArrayList.isEmpty()) {
      return;
    }
    int i = UserConfig.selectedAccount;
    mediaSendQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        long l5 = System.currentTimeMillis();
        int i1 = this.val$media.size();
        boolean bool2;
        int i;
        int k;
        final Object localObject1;
        label111:
        final Object localObject6;
        if ((int)paramLong == 0)
        {
          bool2 = true;
          i = 0;
          k = i;
          if (bool2)
          {
            j = (int)(paramLong >> 32);
            localObject1 = MessagesController.getInstance(paramBoolean1).getEncryptedChat(Integer.valueOf(j));
            k = i;
            if (localObject1 != null) {
              k = AndroidUtilities.getPeerLayerVersion(((TLRPC.EncryptedChat)localObject1).layer);
            }
          }
          if (((bool2) && (k < 73)) || (paramBoolean2) || (!paramMessageObject)) {
            break label538;
          }
          localObject3 = new HashMap();
          i = 0;
          localObject13 = localObject3;
          if (i >= i1) {
            break label541;
          }
          localObject6 = (SendMessagesHelper.SendingMediaInfo)this.val$media.get(i);
          if ((((SendMessagesHelper.SendingMediaInfo)localObject6).searchImage == null) && (!((SendMessagesHelper.SendingMediaInfo)localObject6).isVideo))
          {
            localObject4 = ((SendMessagesHelper.SendingMediaInfo)localObject6).path;
            localObject5 = ((SendMessagesHelper.SendingMediaInfo)localObject6).path;
            localObject1 = localObject4;
            localObject2 = localObject5;
            if (localObject5 == null)
            {
              localObject1 = localObject4;
              localObject2 = localObject5;
              if (((SendMessagesHelper.SendingMediaInfo)localObject6).uri != null)
              {
                localObject2 = AndroidUtilities.getPath(((SendMessagesHelper.SendingMediaInfo)localObject6).uri);
                localObject1 = ((SendMessagesHelper.SendingMediaInfo)localObject6).uri.toString();
              }
            }
            if ((localObject2 == null) || ((!((String)localObject2).endsWith(".gif")) && (!((String)localObject2).endsWith(".webp")))) {
              break label251;
            }
          }
        }
        for (;;)
        {
          i += 1;
          break label111;
          bool2 = false;
          break;
          label251:
          if ((localObject2 != null) || (((SendMessagesHelper.SendingMediaInfo)localObject6).uri == null) || ((!MediaController.isGif(((SendMessagesHelper.SendingMediaInfo)localObject6).uri)) && (!MediaController.isWebp(((SendMessagesHelper.SendingMediaInfo)localObject6).uri))))
          {
            if (localObject2 != null)
            {
              localObject2 = new File((String)localObject2);
              localObject2 = (String)localObject1 + ((File)localObject2).length() + "_" + ((File)localObject2).lastModified();
              label340:
              localObject4 = null;
              localObject1 = localObject4;
              if (!bool2)
              {
                localObject1 = localObject4;
                if (((SendMessagesHelper.SendingMediaInfo)localObject6).ttl == 0)
                {
                  localObject1 = MessagesStorage.getInstance(paramBoolean1);
                  if (bool2) {
                    break label494;
                  }
                  j = 0;
                  label380:
                  localObject2 = (TLRPC.TL_photo)((MessagesStorage)localObject1).getSentFile((String)localObject2, j);
                  localObject1 = localObject2;
                  if (localObject2 == null)
                  {
                    localObject1 = localObject2;
                    if (((SendMessagesHelper.SendingMediaInfo)localObject6).uri != null)
                    {
                      localObject1 = MessagesStorage.getInstance(paramBoolean1);
                      localObject2 = AndroidUtilities.getPath(((SendMessagesHelper.SendingMediaInfo)localObject6).uri);
                      if (bool2) {
                        break label499;
                      }
                    }
                  }
                }
              }
            }
            label494:
            label499:
            for (j = 0;; j = 3)
            {
              localObject1 = (TLRPC.TL_photo)((MessagesStorage)localObject1).getSentFile((String)localObject2, j);
              localObject2 = new SendMessagesHelper.MediaSendPrepareWorker(null);
              ((HashMap)localObject3).put(localObject6, localObject2);
              if (localObject1 == null) {
                break label504;
              }
              ((SendMessagesHelper.MediaSendPrepareWorker)localObject2).photo = ((TLRPC.TL_photo)localObject1);
              break;
              localObject2 = null;
              break label340;
              j = 3;
              break label380;
            }
            label504:
            ((SendMessagesHelper.MediaSendPrepareWorker)localObject2).sync = new CountDownLatch(1);
            SendMessagesHelper.mediaSendThreadPool.execute(new Runnable()
            {
              public void run()
              {
                localObject2.photo = SendMessagesHelper.getInstance(SendMessagesHelper.21.this.val$currentAccount).generatePhotoSizes(localObject6.path, localObject6.uri);
                localObject2.sync.countDown();
              }
            });
          }
        }
        label538:
        Object localObject13 = null;
        label541:
        long l4 = 0L;
        final long l2 = 0L;
        Object localObject3 = null;
        final Object localObject2 = null;
        Object localObject5 = null;
        Object localObject4 = null;
        Object localObject10 = null;
        int j = 0;
        int m = 0;
        if (m < i1)
        {
          final SendMessagesHelper.SendingMediaInfo localSendingMediaInfo = (SendMessagesHelper.SendingMediaInfo)this.val$media.get(m);
          long l3 = l4;
          long l1 = l2;
          i = j;
          if (paramMessageObject) {
            if (bool2)
            {
              l3 = l4;
              l1 = l2;
              i = j;
              if (k < 73) {}
            }
            else
            {
              l3 = l4;
              l1 = l2;
              i = j;
              if (i1 > 1)
              {
                l3 = l4;
                l1 = l2;
                i = j;
                if (j % 10 == 0)
                {
                  l3 = Utilities.random.nextLong();
                  l1 = l3;
                  i = 0;
                }
              }
            }
          }
          final Object localObject14;
          Object localObject11;
          final Object localObject9;
          final Object localObject12;
          if (localSendingMediaInfo.searchImage != null) {
            if (localSendingMediaInfo.searchImage.type == 1)
            {
              localObject14 = new HashMap();
              localObject1 = null;
              if ((localSendingMediaInfo.searchImage.document instanceof TLRPC.TL_document))
              {
                localObject6 = (TLRPC.TL_document)localSendingMediaInfo.searchImage.document;
                localObject1 = FileLoader.getPathToAttach((TLObject)localObject6, true);
                localObject11 = localObject1;
                localObject9 = localObject6;
                if (localObject6 == null)
                {
                  if (localSendingMediaInfo.searchImage.localUrl != null) {
                    ((HashMap)localObject14).put("url", localSendingMediaInfo.searchImage.localUrl);
                  }
                  localObject6 = null;
                  localObject12 = new TLRPC.TL_document();
                  ((TLRPC.TL_document)localObject12).id = 0L;
                  ((TLRPC.TL_document)localObject12).date = ConnectionsManager.getInstance(paramBoolean1).getCurrentTime();
                  localObject9 = new TLRPC.TL_documentAttributeFilename();
                  ((TLRPC.TL_documentAttributeFilename)localObject9).file_name = "animation.gif";
                  ((TLRPC.TL_document)localObject12).attributes.add(localObject9);
                  ((TLRPC.TL_document)localObject12).size = localSendingMediaInfo.searchImage.size;
                  ((TLRPC.TL_document)localObject12).dc_id = 0;
                  if (!((File)localObject1).toString().endsWith("mp4")) {
                    break label1437;
                  }
                  ((TLRPC.TL_document)localObject12).mime_type = "video/mp4";
                  ((TLRPC.TL_document)localObject12).attributes.add(new TLRPC.TL_documentAttributeAnimated());
                  label899:
                  if (!((File)localObject1).exists()) {
                    break label1448;
                  }
                  localObject6 = localObject1;
                  label911:
                  localObject9 = localObject6;
                  if (localObject6 == null)
                  {
                    localObject6 = Utilities.MD5(localSendingMediaInfo.searchImage.thumbUrl) + "." + ImageLoader.getHttpUrlExtension(localSendingMediaInfo.searchImage.thumbUrl, "jpg");
                    localObject6 = new File(FileLoader.getDirectory(4), (String)localObject6);
                    localObject9 = localObject6;
                    if (!((File)localObject6).exists()) {
                      localObject9 = null;
                    }
                  }
                  if (localObject9 == null) {}
                }
              }
              try
              {
                if (!((File)localObject9).getAbsolutePath().endsWith("mp4")) {
                  break label1454;
                }
                localObject6 = ThumbnailUtils.createVideoThumbnail(((File)localObject9).getAbsolutePath(), 1);
                label1029:
                if (localObject6 != null)
                {
                  ((TLRPC.TL_document)localObject12).thumb = ImageLoader.scaleAndSaveImage((Bitmap)localObject6, 90.0F, 90.0F, 55, bool2);
                  ((Bitmap)localObject6).recycle();
                }
              }
              catch (Exception localException1)
              {
                for (;;)
                {
                  FileLog.e(localException1);
                  continue;
                  localObject1 = ((File)localObject11).toString();
                }
              }
              localObject11 = localObject1;
              localObject9 = localObject12;
              if (((TLRPC.TL_document)localObject12).thumb == null)
              {
                ((TLRPC.TL_document)localObject12).thumb = new TLRPC.TL_photoSize();
                ((TLRPC.TL_document)localObject12).thumb.w = localSendingMediaInfo.searchImage.width;
                ((TLRPC.TL_document)localObject12).thumb.h = localSendingMediaInfo.searchImage.height;
                ((TLRPC.TL_document)localObject12).thumb.size = 0;
                ((TLRPC.TL_document)localObject12).thumb.location = new TLRPC.TL_fileLocationUnavailable();
                ((TLRPC.TL_document)localObject12).thumb.type = "x";
                localObject9 = localObject12;
                localObject11 = localObject1;
              }
              localObject1 = localSendingMediaInfo.searchImage.imageUrl;
              if (localObject11 == null)
              {
                localObject1 = localSendingMediaInfo.searchImage.imageUrl;
                if ((localObject14 != null) && (localSendingMediaInfo.searchImage.imageUrl != null)) {
                  ((HashMap)localObject14).put("originalPath", localSendingMediaInfo.searchImage.imageUrl);
                }
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    SendMessagesHelper.getInstance(SendMessagesHelper.21.this.val$currentAccount).sendMessage(localObject9, null, localObject1, SendMessagesHelper.21.this.val$dialog_id, SendMessagesHelper.21.this.val$reply_to_msg, localSendingMediaInfo.caption, localSendingMediaInfo.entities, null, localObject14, 0);
                  }
                });
                localObject12 = localObject2;
                localObject11 = localObject4;
                localObject9 = localObject5;
                localObject6 = localObject3;
                j = i;
                l2 = l1;
                localObject14 = localObject10;
              }
            }
          }
          for (;;)
          {
            m += 1;
            localObject10 = localObject14;
            l4 = l3;
            localObject3 = localObject6;
            localObject5 = localObject9;
            localObject4 = localObject11;
            localObject2 = localObject12;
            break;
            localObject6 = localObject1;
            if (!bool2)
            {
              localObject6 = MessagesStorage.getInstance(paramBoolean1);
              localObject9 = localSendingMediaInfo.searchImage.imageUrl;
              if (bool2) {
                break label1432;
              }
            }
            label1432:
            for (j = 1;; j = 4)
            {
              localObject9 = (TLRPC.Document)((MessagesStorage)localObject6).getSentFile((String)localObject9, j);
              localObject6 = localObject1;
              if ((localObject9 instanceof TLRPC.TL_document)) {
                localObject6 = (TLRPC.TL_document)localObject9;
              }
              localObject1 = Utilities.MD5(localSendingMediaInfo.searchImage.imageUrl) + "." + ImageLoader.getHttpUrlExtension(localSendingMediaInfo.searchImage.imageUrl, "jpg");
              localObject1 = new File(FileLoader.getDirectory(4), (String)localObject1);
              break;
            }
            label1437:
            ((TLRPC.TL_document)localObject12).mime_type = "image/gif";
            break label899;
            label1448:
            localObject1 = null;
            break label911;
            label1454:
            localObject6 = ImageLoader.loadBitmap(((File)localObject9).getAbsolutePath(), null, 90.0F, 90.0F, true);
            break label1029;
            final boolean bool3 = true;
            boolean bool4 = true;
            localObject1 = null;
            localObject9 = localObject1;
            final Object localObject7;
            if (!bool2)
            {
              localObject9 = localObject1;
              if (localSendingMediaInfo.ttl == 0)
              {
                localObject1 = MessagesStorage.getInstance(paramBoolean1);
                localObject7 = localSendingMediaInfo.searchImage.imageUrl;
                if (bool2) {
                  break label2120;
                }
              }
            }
            boolean bool1;
            label2120:
            for (j = 0;; j = 3)
            {
              localObject9 = (TLRPC.TL_photo)((MessagesStorage)localObject1).getSentFile((String)localObject7, j);
              localObject1 = localObject9;
              if (localObject9 == null)
              {
                localObject1 = Utilities.MD5(localSendingMediaInfo.searchImage.imageUrl) + "." + ImageLoader.getHttpUrlExtension(localSendingMediaInfo.searchImage.imageUrl, "jpg");
                localObject1 = new File(FileLoader.getDirectory(4), (String)localObject1);
                bool1 = bool4;
                localObject7 = localObject9;
                if (((File)localObject1).exists())
                {
                  bool1 = bool4;
                  localObject7 = localObject9;
                  if (((File)localObject1).length() != 0L)
                  {
                    localObject1 = SendMessagesHelper.getInstance(paramBoolean1).generatePhotoSizes(((File)localObject1).toString(), null);
                    bool1 = bool4;
                    localObject7 = localObject1;
                    if (localObject1 != null)
                    {
                      bool1 = false;
                      localObject7 = localObject1;
                    }
                  }
                }
                bool3 = bool1;
                localObject1 = localObject7;
                if (localObject7 == null)
                {
                  localObject1 = Utilities.MD5(localSendingMediaInfo.searchImage.thumbUrl) + "." + ImageLoader.getHttpUrlExtension(localSendingMediaInfo.searchImage.thumbUrl, "jpg");
                  localObject1 = new File(FileLoader.getDirectory(4), (String)localObject1);
                  if (((File)localObject1).exists()) {
                    localObject7 = SendMessagesHelper.getInstance(paramBoolean1).generatePhotoSizes(((File)localObject1).toString(), null);
                  }
                  bool3 = bool1;
                  localObject1 = localObject7;
                  if (localObject7 == null)
                  {
                    localObject1 = new TLRPC.TL_photo();
                    ((TLRPC.TL_photo)localObject1).date = ConnectionsManager.getInstance(paramBoolean1).getCurrentTime();
                    localObject7 = new TLRPC.TL_photoSize();
                    ((TLRPC.TL_photoSize)localObject7).w = localSendingMediaInfo.searchImage.width;
                    ((TLRPC.TL_photoSize)localObject7).h = localSendingMediaInfo.searchImage.height;
                    ((TLRPC.TL_photoSize)localObject7).size = 0;
                    ((TLRPC.TL_photoSize)localObject7).location = new TLRPC.TL_fileLocationUnavailable();
                    ((TLRPC.TL_photoSize)localObject7).type = "x";
                    ((TLRPC.TL_photo)localObject1).sizes.add(localObject7);
                    bool3 = bool1;
                  }
                }
              }
              localObject14 = localObject10;
              l2 = l1;
              j = i;
              localObject7 = localObject3;
              localObject9 = localObject5;
              localObject11 = localObject4;
              localObject12 = localObject2;
              if (localObject1 == null) {
                break;
              }
              localObject7 = new HashMap();
              if (localSendingMediaInfo.searchImage.imageUrl != null) {
                ((HashMap)localObject7).put("originalPath", localSendingMediaInfo.searchImage.imageUrl);
              }
              l2 = l1;
              j = i;
              if (paramMessageObject)
              {
                i += 1;
                ((HashMap)localObject7).put("groupId", "" + l3);
                if (i != 10)
                {
                  l2 = l1;
                  j = i;
                  if (m != i1 - 1) {}
                }
                else
                {
                  ((HashMap)localObject7).put("final", "1");
                  l2 = 0L;
                  j = i;
                }
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  SendMessagesHelper localSendMessagesHelper = SendMessagesHelper.getInstance(SendMessagesHelper.21.this.val$currentAccount);
                  TLRPC.TL_photo localTL_photo = localObject1;
                  if (bool3) {}
                  for (String str = localSendingMediaInfo.searchImage.imageUrl;; str = null)
                  {
                    localSendMessagesHelper.sendMessage(localTL_photo, str, SendMessagesHelper.21.this.val$dialog_id, SendMessagesHelper.21.this.val$reply_to_msg, localSendingMediaInfo.caption, localSendingMediaInfo.entities, null, localObject7, localSendingMediaInfo.ttl);
                    return;
                  }
                }
              });
              localObject14 = localObject10;
              localObject7 = localObject3;
              localObject9 = localObject5;
              localObject11 = localObject4;
              localObject12 = localObject2;
              break;
            }
            if (localSendingMediaInfo.isVideo)
            {
              localObject14 = null;
              String str;
              File localFile;
              if (paramBoolean2)
              {
                localObject9 = null;
                if ((paramBoolean2) || ((localObject9 == null) && (!localSendingMediaInfo.path.endsWith("mp4")))) {
                  break label3183;
                }
                str = localSendingMediaInfo.path;
                localObject1 = localSendingMediaInfo.path;
                localFile = new File((String)localObject1);
                l2 = 0L;
                bool1 = false;
                localObject1 = (String)localObject1 + localFile.length() + "_" + localFile.lastModified();
                localObject11 = localObject1;
                if (localObject9 != null)
                {
                  bool1 = ((VideoEditedInfo)localObject9).muted;
                  localObject7 = new StringBuilder().append((String)localObject1).append(((VideoEditedInfo)localObject9).estimatedDuration).append("_").append(((VideoEditedInfo)localObject9).startTime).append("_").append(((VideoEditedInfo)localObject9).endTime);
                  if (!((VideoEditedInfo)localObject9).muted) {
                    break label2993;
                  }
                  localObject1 = "_m";
                  label2318:
                  localObject7 = (String)localObject1;
                  localObject1 = localObject7;
                  if (((VideoEditedInfo)localObject9).resultWidth != ((VideoEditedInfo)localObject9).originalWidth) {
                    localObject1 = (String)localObject7 + "_" + ((VideoEditedInfo)localObject9).resultWidth;
                  }
                  if (((VideoEditedInfo)localObject9).startTime < 0L) {
                    break label3001;
                  }
                  l2 = ((VideoEditedInfo)localObject9).startTime;
                  localObject11 = localObject1;
                }
                label2398:
                localObject7 = null;
                localObject1 = localObject7;
                if (!bool2)
                {
                  localObject1 = localObject7;
                  if (localSendingMediaInfo.ttl == 0)
                  {
                    localObject1 = MessagesStorage.getInstance(paramBoolean1);
                    if (bool2) {
                      break label3011;
                    }
                    j = 2;
                    label2438:
                    localObject1 = (TLRPC.TL_document)((MessagesStorage)localObject1).getSentFile((String)localObject11, j);
                  }
                }
                localObject12 = localObject1;
                localObject7 = str;
                if (localObject1 == null)
                {
                  localObject1 = SendMessagesHelper.createVideoThumbnail(localSendingMediaInfo.path, l2);
                  localObject7 = localObject1;
                  if (localObject1 == null) {
                    localObject7 = ThumbnailUtils.createVideoThumbnail(localSendingMediaInfo.path, 1);
                  }
                  localObject14 = ImageLoader.scaleAndSaveImage((Bitmap)localObject7, 90.0F, 90.0F, 55, bool2);
                  localObject1 = localObject7;
                  if (localObject7 != null)
                  {
                    localObject1 = localObject7;
                    if (localObject14 != null) {
                      localObject1 = null;
                    }
                  }
                  localObject12 = new TLRPC.TL_document();
                  ((TLRPC.TL_document)localObject12).thumb = ((TLRPC.PhotoSize)localObject14);
                  if (((TLRPC.TL_document)localObject12).thumb != null) {
                    break label3016;
                  }
                  ((TLRPC.TL_document)localObject12).thumb = new TLRPC.TL_photoSizeEmpty();
                  ((TLRPC.TL_document)localObject12).thumb.type = "s";
                  label2581:
                  ((TLRPC.TL_document)localObject12).mime_type = "video/mp4";
                  UserConfig.getInstance(paramBoolean1).saveConfig(false);
                  if (!bool2) {
                    break label3042;
                  }
                  if (k < 66) {
                    break label3030;
                  }
                  localObject7 = new TLRPC.TL_documentAttributeVideo();
                  label2620:
                  ((TLRPC.TL_document)localObject12).attributes.add(localObject7);
                  if ((localObject9 == null) || (!((VideoEditedInfo)localObject9).needConvert())) {
                    break label3142;
                  }
                  if (!((VideoEditedInfo)localObject9).muted) {
                    break label3060;
                  }
                  ((TLRPC.TL_document)localObject12).attributes.add(new TLRPC.TL_documentAttributeAnimated());
                  SendMessagesHelper.fillVideoAttribute(localSendingMediaInfo.path, (TLRPC.TL_documentAttributeVideo)localObject7, (VideoEditedInfo)localObject9);
                  ((VideoEditedInfo)localObject9).originalWidth = ((TLRPC.TL_documentAttributeVideo)localObject7).w;
                  ((VideoEditedInfo)localObject9).originalHeight = ((TLRPC.TL_documentAttributeVideo)localObject7).h;
                  ((TLRPC.TL_documentAttributeVideo)localObject7).w = ((VideoEditedInfo)localObject9).resultWidth;
                  ((TLRPC.TL_documentAttributeVideo)localObject7).h = ((VideoEditedInfo)localObject9).resultHeight;
                  label2720:
                  ((TLRPC.TL_document)localObject12).size = ((int)((VideoEditedInfo)localObject9).estimatedSize);
                  localObject7 = "-2147483648_" + SharedConfig.getLastLocalId() + ".mp4";
                  localObject7 = new File(FileLoader.getDirectory(4), (String)localObject7);
                  SharedConfig.saveConfig();
                  localObject7 = ((File)localObject7).getAbsolutePath();
                }
              }
              for (localObject14 = localObject1;; localObject14 = localObject1)
              {
                localObject1 = new HashMap();
                if (localObject11 != null) {
                  ((HashMap)localObject1).put("originalPath", localObject11);
                }
                l2 = l1;
                j = i;
                if (!bool1)
                {
                  l2 = l1;
                  j = i;
                  if (paramMessageObject)
                  {
                    i += 1;
                    ((HashMap)localObject1).put("groupId", "" + l3);
                    if (i != 10)
                    {
                      l2 = l1;
                      j = i;
                      if (m != i1 - 1) {}
                    }
                    else
                    {
                      ((HashMap)localObject1).put("final", "1");
                      l2 = 0L;
                      j = i;
                    }
                  }
                }
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    if ((localObject14 != null) && (this.val$thumbKeyFinal != null)) {
                      ImageLoader.getInstance().putImageToCache(new BitmapDrawable(localObject14), this.val$thumbKeyFinal);
                    }
                    SendMessagesHelper.getInstance(SendMessagesHelper.21.this.val$currentAccount).sendMessage(localObject12, localObject9, localObject7, SendMessagesHelper.21.this.val$dialog_id, SendMessagesHelper.21.this.val$reply_to_msg, localSendingMediaInfo.caption, localSendingMediaInfo.entities, null, localObject1, localSendingMediaInfo.ttl);
                  }
                });
                localObject14 = localObject10;
                localObject7 = localObject3;
                localObject9 = localObject5;
                localObject11 = localObject4;
                localObject12 = localObject2;
                break;
                if (localSendingMediaInfo.videoEditedInfo != null) {}
                for (localObject1 = localSendingMediaInfo.videoEditedInfo;; localObject1 = SendMessagesHelper.createCompressionSettings(localSendingMediaInfo.path))
                {
                  localObject9 = localObject1;
                  break;
                }
                label2993:
                localObject1 = "";
                break label2318;
                label3001:
                l2 = 0L;
                localObject11 = localObject1;
                break label2398;
                label3011:
                j = 5;
                break label2438;
                label3016:
                ((TLRPC.TL_document)localObject12).thumb.type = "s";
                break label2581;
                label3030:
                localObject7 = new TLRPC.TL_documentAttributeVideo_layer65();
                break label2620;
                label3042:
                localObject7 = new TLRPC.TL_documentAttributeVideo();
                ((TLRPC.TL_documentAttributeVideo)localObject7).supports_streaming = true;
                break label2620;
                label3060:
                ((TLRPC.TL_documentAttributeVideo)localObject7).duration = ((int)(((VideoEditedInfo)localObject9).estimatedDuration / 1000L));
                if ((((VideoEditedInfo)localObject9).rotationValue == 90) || (((VideoEditedInfo)localObject9).rotationValue == 270))
                {
                  ((TLRPC.TL_documentAttributeVideo)localObject7).w = ((VideoEditedInfo)localObject9).resultHeight;
                  ((TLRPC.TL_documentAttributeVideo)localObject7).h = ((VideoEditedInfo)localObject9).resultWidth;
                  break label2720;
                }
                ((TLRPC.TL_documentAttributeVideo)localObject7).w = ((VideoEditedInfo)localObject9).resultWidth;
                ((TLRPC.TL_documentAttributeVideo)localObject7).h = ((VideoEditedInfo)localObject9).resultHeight;
                break label2720;
                label3142:
                if (localFile.exists()) {
                  ((TLRPC.TL_document)localObject12).size = ((int)localFile.length());
                }
                SendMessagesHelper.fillVideoAttribute(localSendingMediaInfo.path, (TLRPC.TL_documentAttributeVideo)localObject7, null);
                localObject7 = str;
              }
              label3183:
              SendMessagesHelper.prepareSendingDocumentInternal(paramBoolean1, localSendingMediaInfo.path, localSendingMediaInfo.path, null, null, paramLong, paramInputContentInfoCompat, localSendingMediaInfo.caption, localSendingMediaInfo.entities);
              localObject14 = localObject10;
              l2 = l1;
              j = i;
              localObject7 = localObject3;
              localObject9 = localObject5;
              localObject11 = localObject4;
              localObject12 = localObject2;
            }
            else
            {
              localObject1 = localSendingMediaInfo.path;
              localObject7 = localSendingMediaInfo.path;
              localObject12 = localObject1;
              localObject9 = localObject7;
              if (localObject7 == null)
              {
                localObject12 = localObject1;
                localObject9 = localObject7;
                if (localSendingMediaInfo.uri != null)
                {
                  localObject9 = AndroidUtilities.getPath(localSendingMediaInfo.uri);
                  localObject12 = localSendingMediaInfo.uri.toString();
                }
              }
              int n = 0;
              if (paramBoolean2)
              {
                j = 1;
                localObject1 = FileLoader.getFileExtension(new File((String)localObject9));
                localObject11 = localObject9;
                localObject7 = localObject12;
              }
              for (;;)
              {
                if (j == 0) {
                  break label3670;
                }
                localObject9 = localObject3;
                if (localObject3 == null)
                {
                  localObject9 = new ArrayList();
                  localObject2 = new ArrayList();
                  localObject5 = new ArrayList();
                  localObject4 = new ArrayList();
                }
                ((ArrayList)localObject9).add(localObject11);
                ((ArrayList)localObject2).add(localObject7);
                ((ArrayList)localObject5).add(localSendingMediaInfo.caption);
                ((ArrayList)localObject4).add(localSendingMediaInfo.entities);
                localObject14 = localObject1;
                l2 = l1;
                j = i;
                localObject7 = localObject9;
                localObject9 = localObject5;
                localObject11 = localObject4;
                localObject12 = localObject2;
                break;
                if ((localObject9 != null) && ((((String)localObject9).endsWith(".gif")) || (((String)localObject9).endsWith(".webp"))))
                {
                  if (((String)localObject9).endsWith(".gif")) {}
                  for (localObject1 = "gif";; localObject1 = "webp")
                  {
                    j = 1;
                    localObject7 = localObject12;
                    localObject11 = localObject9;
                    break;
                  }
                }
                localObject1 = localObject10;
                j = n;
                localObject7 = localObject12;
                localObject11 = localObject9;
                if (localObject9 == null)
                {
                  localObject1 = localObject10;
                  j = n;
                  localObject7 = localObject12;
                  localObject11 = localObject9;
                  if (localSendingMediaInfo.uri != null) {
                    if (MediaController.isGif(localSendingMediaInfo.uri))
                    {
                      j = 1;
                      localObject7 = localSendingMediaInfo.uri.toString();
                      localObject11 = MediaController.copyFileToCache(localSendingMediaInfo.uri, "gif");
                      localObject1 = "gif";
                    }
                    else
                    {
                      localObject1 = localObject10;
                      j = n;
                      localObject7 = localObject12;
                      localObject11 = localObject9;
                      if (MediaController.isWebp(localSendingMediaInfo.uri))
                      {
                        j = 1;
                        localObject7 = localSendingMediaInfo.uri.toString();
                        localObject11 = MediaController.copyFileToCache(localSendingMediaInfo.uri, "webp");
                        localObject1 = "webp";
                      }
                    }
                  }
                }
              }
              label3670:
              if (localObject11 != null)
              {
                localObject9 = new File((String)localObject11);
                localObject10 = (String)localObject7 + ((File)localObject9).length() + "_" + ((File)localObject9).lastModified();
                localObject7 = null;
                if (localObject13 == null) {
                  break label3912;
                }
                localObject12 = (SendMessagesHelper.MediaSendPrepareWorker)((HashMap)localObject13).get(localSendingMediaInfo);
                localObject9 = ((SendMessagesHelper.MediaSendPrepareWorker)localObject12).photo;
                localObject7 = localObject9;
                if (localObject9 != null) {}
              }
              for (;;)
              {
                try
                {
                  ((SendMessagesHelper.MediaSendPrepareWorker)localObject12).sync.await();
                  localObject7 = ((SendMessagesHelper.MediaSendPrepareWorker)localObject12).photo;
                  if (localObject7 == null) {
                    break label4230;
                  }
                  localObject9 = new HashMap();
                  if ((localSendingMediaInfo.masks == null) || (localSendingMediaInfo.masks.isEmpty())) {
                    break label4066;
                  }
                  bool1 = true;
                  ((TLRPC.TL_photo)localObject7).has_stickers = bool1;
                  if (!bool1) {
                    break label4089;
                  }
                  localObject11 = new SerializedData(localSendingMediaInfo.masks.size() * 20 + 4);
                  ((SerializedData)localObject11).writeInt32(localSendingMediaInfo.masks.size());
                  j = 0;
                  if (j >= localSendingMediaInfo.masks.size()) {
                    break label4072;
                  }
                  ((TLRPC.InputDocument)localSendingMediaInfo.masks.get(j)).serializeToStream((AbstractSerializedData)localObject11);
                  j += 1;
                  continue;
                  localObject10 = null;
                }
                catch (Exception localException2)
                {
                  FileLog.e(localException2);
                  continue;
                }
                label3912:
                localObject9 = localException2;
                if (!bool2)
                {
                  localObject9 = localException2;
                  if (localSendingMediaInfo.ttl == 0)
                  {
                    localObject8 = MessagesStorage.getInstance(paramBoolean1);
                    if (bool2) {
                      break label4056;
                    }
                    j = 0;
                    label3949:
                    localObject8 = (TLRPC.TL_photo)((MessagesStorage)localObject8).getSentFile((String)localObject10, j);
                    localObject9 = localObject8;
                    if (localObject8 == null)
                    {
                      localObject9 = localObject8;
                      if (localSendingMediaInfo.uri != null)
                      {
                        localObject8 = MessagesStorage.getInstance(paramBoolean1);
                        localObject9 = AndroidUtilities.getPath(localSendingMediaInfo.uri);
                        if (bool2) {
                          break label4061;
                        }
                      }
                    }
                  }
                }
                label4056:
                label4061:
                for (j = 0;; j = 3)
                {
                  localObject9 = (TLRPC.TL_photo)((MessagesStorage)localObject8).getSentFile((String)localObject9, j);
                  localObject8 = localObject9;
                  if (localObject9 != null) {
                    break;
                  }
                  localObject8 = SendMessagesHelper.getInstance(paramBoolean1).generatePhotoSizes(localSendingMediaInfo.path, localSendingMediaInfo.uri);
                  break;
                  j = 3;
                  break label3949;
                }
                label4066:
                bool1 = false;
              }
              label4072:
              ((HashMap)localObject9).put("masks", Utilities.bytesToHex(((SerializedData)localObject11).toByteArray()));
              label4089:
              if (localObject10 != null) {
                ((HashMap)localObject9).put("originalPath", localObject10);
              }
              l2 = l1;
              j = i;
              if (paramMessageObject)
              {
                i += 1;
                ((HashMap)localObject9).put("groupId", "" + l3);
                if (i != 10)
                {
                  l2 = l1;
                  j = i;
                  if (m != i1 - 1) {}
                }
                else
                {
                  ((HashMap)localObject9).put("final", "1");
                  l2 = 0L;
                  j = i;
                }
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  SendMessagesHelper.getInstance(SendMessagesHelper.21.this.val$currentAccount).sendMessage(localObject8, null, SendMessagesHelper.21.this.val$dialog_id, SendMessagesHelper.21.this.val$reply_to_msg, localSendingMediaInfo.caption, localSendingMediaInfo.entities, null, localObject9, localSendingMediaInfo.ttl);
                }
              });
              localObject14 = localObject1;
              final Object localObject8 = localObject3;
              localObject9 = localObject5;
              localObject11 = localObject4;
              localObject12 = localObject2;
              continue;
              label4230:
              localObject8 = localObject3;
              if (localObject3 == null)
              {
                localObject8 = new ArrayList();
                localObject2 = new ArrayList();
                localObject5 = new ArrayList();
                localObject4 = new ArrayList();
              }
              ((ArrayList)localObject8).add(localObject11);
              ((ArrayList)localObject2).add(localObject10);
              ((ArrayList)localObject5).add(localSendingMediaInfo.caption);
              ((ArrayList)localObject4).add(localSendingMediaInfo.entities);
              localObject14 = localObject1;
              l2 = l1;
              j = i;
              localObject9 = localObject5;
              localObject11 = localObject4;
              localObject12 = localObject2;
            }
          }
        }
        if (l2 != 0L) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              SendMessagesHelper localSendMessagesHelper = SendMessagesHelper.getInstance(SendMessagesHelper.21.this.val$currentAccount);
              Object localObject = (ArrayList)localSendMessagesHelper.delayedMessages.get("group_" + l2);
              if ((localObject != null) && (!((ArrayList)localObject).isEmpty()))
              {
                localObject = (SendMessagesHelper.DelayedMessage)((ArrayList)localObject).get(0);
                MessageObject localMessageObject = (MessageObject)((SendMessagesHelper.DelayedMessage)localObject).messageObjects.get(((SendMessagesHelper.DelayedMessage)localObject).messageObjects.size() - 1);
                ((SendMessagesHelper.DelayedMessage)localObject).finalGroupMessage = localMessageObject.getId();
                localMessageObject.messageOwner.params.put("final", "1");
                TLRPC.TL_messages_messages localTL_messages_messages = new TLRPC.TL_messages_messages();
                localTL_messages_messages.messages.add(localMessageObject.messageOwner);
                MessagesStorage.getInstance(SendMessagesHelper.21.this.val$currentAccount).putMessages(localTL_messages_messages, ((SendMessagesHelper.DelayedMessage)localObject).peer, -2, 0, false);
                localSendMessagesHelper.sendReadyToSendGroup((SendMessagesHelper.DelayedMessage)localObject, true, true);
              }
            }
          });
        }
        if (this.val$inputContent != null) {
          this.val$inputContent.releasePermission();
        }
        if ((localObject3 != null) && (!((ArrayList)localObject3).isEmpty()))
        {
          i = 0;
          while (i < ((ArrayList)localObject3).size())
          {
            SendMessagesHelper.prepareSendingDocumentInternal(paramBoolean1, (String)((ArrayList)localObject3).get(i), (String)((ArrayList)localObject2).get(i), null, (String)localObject10, paramLong, paramInputContentInfoCompat, (CharSequence)((ArrayList)localObject5).get(i), (ArrayList)((ArrayList)localObject4).get(i));
            i += 1;
          }
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("total send time = " + (System.currentTimeMillis() - l5));
        }
      }
    });
  }
  
  public static void prepareSendingPhoto(String paramString, Uri paramUri, long paramLong, MessageObject paramMessageObject, CharSequence paramCharSequence, ArrayList<TLRPC.MessageEntity> paramArrayList, ArrayList<TLRPC.InputDocument> paramArrayList1, InputContentInfoCompat paramInputContentInfoCompat, int paramInt)
  {
    SendingMediaInfo localSendingMediaInfo = new SendingMediaInfo();
    localSendingMediaInfo.path = paramString;
    localSendingMediaInfo.uri = paramUri;
    if (paramCharSequence != null) {
      localSendingMediaInfo.caption = paramCharSequence.toString();
    }
    localSendingMediaInfo.entities = paramArrayList;
    localSendingMediaInfo.ttl = paramInt;
    if ((paramArrayList1 != null) && (!paramArrayList1.isEmpty())) {
      localSendingMediaInfo.masks = new ArrayList(paramArrayList1);
    }
    paramString = new ArrayList();
    paramString.add(localSendingMediaInfo);
    prepareSendingMedia(paramString, paramLong, paramMessageObject, paramInputContentInfoCompat, false, false);
  }
  
  public static void prepareSendingText(String paramString, final long paramLong)
  {
    final int i = UserConfig.selectedAccount;
    MessagesStorage.getInstance(i).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                String str1 = SendMessagesHelper.getTrimmedString(SendMessagesHelper.20.this.val$text);
                if (str1.length() != 0)
                {
                  int j = (int)Math.ceil(str1.length() / 4096.0F);
                  int i = 0;
                  while (i < j)
                  {
                    String str2 = str1.substring(i * 4096, Math.min((i + 1) * 4096, str1.length()));
                    SendMessagesHelper.getInstance(SendMessagesHelper.20.this.val$currentAccount).sendMessage(str2, SendMessagesHelper.20.this.val$dialog_id, null, null, true, null, null, null);
                    i += 1;
                  }
                }
              }
            });
          }
        });
      }
    });
  }
  
  public static void prepareSendingVideo(final String paramString, final long paramLong1, long paramLong2, final int paramInt1, final int paramInt2, VideoEditedInfo paramVideoEditedInfo, final long paramLong3, final MessageObject paramMessageObject, final CharSequence paramCharSequence, ArrayList<TLRPC.MessageEntity> paramArrayList, final int paramInt3)
  {
    if ((paramString == null) || (paramString.length() == 0)) {
      return;
    }
    new Thread(new Runnable()
    {
      public void run()
      {
        final VideoEditedInfo localVideoEditedInfo;
        boolean bool1;
        label23:
        boolean bool2;
        label38:
        final Object localObject3;
        final Object localObject6;
        Object localObject8;
        final Object localObject7;
        File localFile;
        long l;
        final Object localObject2;
        Object localObject4;
        label208:
        label288:
        int i;
        label325:
        final Object localObject5;
        if (this.val$info != null)
        {
          localVideoEditedInfo = this.val$info;
          if ((int)paramLong3 != 0) {
            break label662;
          }
          bool1 = true;
          if ((localVideoEditedInfo == null) || (!localVideoEditedInfo.roundVideo)) {
            break label667;
          }
          bool2 = true;
          localObject3 = null;
          localObject6 = null;
          localObject8 = null;
          if ((localVideoEditedInfo == null) && (!paramString.endsWith("mp4")) && (!bool2)) {
            break label1267;
          }
          localObject7 = paramString;
          localObject1 = paramString;
          localFile = new File((String)localObject1);
          l = 0L;
          localObject2 = (String)localObject1 + localFile.length() + "_" + localFile.lastModified();
          localObject4 = localObject2;
          if (localVideoEditedInfo != null)
          {
            localObject1 = localObject2;
            if (!bool2)
            {
              localObject2 = new StringBuilder().append((String)localObject2).append(paramInt3).append("_").append(localVideoEditedInfo.startTime).append("_").append(localVideoEditedInfo.endTime);
              if (!localVideoEditedInfo.muted) {
                break label672;
              }
              localObject1 = "_m";
              localObject2 = (String)localObject1;
              localObject1 = localObject2;
              if (localVideoEditedInfo.resultWidth != localVideoEditedInfo.originalWidth) {
                localObject1 = (String)localObject2 + "_" + localVideoEditedInfo.resultWidth;
              }
            }
            if (localVideoEditedInfo.startTime < 0L) {
              break label679;
            }
            l = localVideoEditedInfo.startTime;
            localObject4 = localObject1;
          }
          localObject2 = null;
          localObject1 = localObject2;
          if (!bool1)
          {
            localObject1 = localObject2;
            if (paramInt2 == 0)
            {
              localObject1 = MessagesStorage.getInstance(paramInt1);
              if (bool1) {
                break label689;
              }
              i = 2;
              localObject1 = (TLRPC.TL_document)((MessagesStorage)localObject1).getSentFile((String)localObject4, i);
            }
          }
          localObject2 = localObject3;
          localObject5 = localObject1;
          localObject3 = localObject7;
          if (localObject1 != null) {
            break label1044;
          }
          localObject1 = SendMessagesHelper.createVideoThumbnail(paramString, l);
          localObject3 = localObject1;
          if (localObject1 == null) {
            localObject3 = ThumbnailUtils.createVideoThumbnail(paramString, 1);
          }
          localObject6 = ImageLoader.scaleAndSaveImage((Bitmap)localObject3, 90.0F, 90.0F, 55, bool1);
          localObject2 = localObject3;
          localObject1 = localObject8;
          if (localObject3 != null)
          {
            localObject2 = localObject3;
            localObject1 = localObject8;
            if (localObject6 != null)
            {
              if (!bool2) {
                break label825;
              }
              if (!bool1) {
                break label699;
              }
              if (Build.VERSION.SDK_INT >= 21) {
                break label694;
              }
              i = 0;
              label443:
              Utilities.blurBitmap(localObject3, 7, i, ((Bitmap)localObject3).getWidth(), ((Bitmap)localObject3).getHeight(), ((Bitmap)localObject3).getRowBytes());
              localObject1 = String.format(((TLRPC.PhotoSize)localObject6).location.volume_id + "_" + ((TLRPC.PhotoSize)localObject6).location.local_id + "@%d_%d_b2", new Object[] { Integer.valueOf((int)(AndroidUtilities.roundMessageSize / AndroidUtilities.density)), Integer.valueOf((int)(AndroidUtilities.roundMessageSize / AndroidUtilities.density)) });
              localObject2 = localObject3;
            }
          }
          label551:
          localObject5 = new TLRPC.TL_document();
          ((TLRPC.TL_document)localObject5).thumb = ((TLRPC.PhotoSize)localObject6);
          if (((TLRPC.TL_document)localObject5).thumb != null) {
            break label835;
          }
          ((TLRPC.TL_document)localObject5).thumb = new TLRPC.TL_photoSizeEmpty();
        }
        label662:
        label667:
        label672:
        label679:
        label689:
        label694:
        label699:
        label825:
        label835:
        for (((TLRPC.TL_document)localObject5).thumb.type = "s";; ((TLRPC.TL_document)localObject5).thumb.type = "s")
        {
          ((TLRPC.TL_document)localObject5).mime_type = "video/mp4";
          UserConfig.getInstance(paramInt1).saveConfig(false);
          if (!bool1) {
            break label1125;
          }
          i = (int)(paramLong3 >> 32);
          localObject3 = MessagesController.getInstance(paramInt1).getEncryptedChat(Integer.valueOf(i));
          if (localObject3 != null) {
            break label848;
          }
          return;
          localVideoEditedInfo = SendMessagesHelper.createCompressionSettings(paramString);
          break;
          bool1 = false;
          break label23;
          bool2 = false;
          break label38;
          localObject1 = "";
          break label208;
          l = 0L;
          localObject4 = localObject1;
          break label288;
          i = 5;
          break label325;
          i = 1;
          break label443;
          if (Build.VERSION.SDK_INT < 21) {}
          for (i = 0;; i = 1)
          {
            Utilities.blurBitmap(localObject3, 3, i, ((Bitmap)localObject3).getWidth(), ((Bitmap)localObject3).getHeight(), ((Bitmap)localObject3).getRowBytes());
            localObject1 = String.format(((TLRPC.PhotoSize)localObject6).location.volume_id + "_" + ((TLRPC.PhotoSize)localObject6).location.local_id + "@%d_%d_b", new Object[] { Integer.valueOf((int)(AndroidUtilities.roundMessageSize / AndroidUtilities.density)), Integer.valueOf((int)(AndroidUtilities.roundMessageSize / AndroidUtilities.density)) });
            localObject2 = localObject3;
            break;
          }
          localObject2 = null;
          localObject1 = localObject8;
          break label551;
        }
        label848:
        if (AndroidUtilities.getPeerLayerVersion(((TLRPC.EncryptedChat)localObject3).layer) >= 66)
        {
          localObject3 = new TLRPC.TL_documentAttributeVideo();
          ((TLRPC.TL_documentAttributeVideo)localObject3).round_message = bool2;
          ((TLRPC.TL_document)localObject5).attributes.add(localObject3);
          if ((localVideoEditedInfo == null) || (!localVideoEditedInfo.needConvert())) {
            break label1220;
          }
          if (!localVideoEditedInfo.muted) {
            break label1143;
          }
          ((TLRPC.TL_document)localObject5).attributes.add(new TLRPC.TL_documentAttributeAnimated());
          SendMessagesHelper.fillVideoAttribute(paramString, (TLRPC.TL_documentAttributeVideo)localObject3, localVideoEditedInfo);
          localVideoEditedInfo.originalWidth = ((TLRPC.TL_documentAttributeVideo)localObject3).w;
          localVideoEditedInfo.originalHeight = ((TLRPC.TL_documentAttributeVideo)localObject3).h;
          ((TLRPC.TL_documentAttributeVideo)localObject3).w = localVideoEditedInfo.resultWidth;
          ((TLRPC.TL_documentAttributeVideo)localObject3).h = localVideoEditedInfo.resultHeight;
          label975:
          ((TLRPC.TL_document)localObject5).size = ((int)paramMessageObject);
          localObject3 = "-2147483648_" + SharedConfig.getLastLocalId() + ".mp4";
          localObject3 = new File(FileLoader.getDirectory(4), (String)localObject3);
          SharedConfig.saveConfig();
          localObject3 = ((File)localObject3).getAbsolutePath();
          localObject6 = localObject1;
          label1044:
          localObject7 = new HashMap();
          if (this.val$caption == null) {
            break label1260;
          }
        }
        label1125:
        label1143:
        label1220:
        label1260:
        for (final Object localObject1 = this.val$caption.toString();; localObject1 = "")
        {
          if (localObject4 != null) {
            ((HashMap)localObject7).put("originalPath", localObject4);
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if ((localObject2 != null) && (localObject6 != null)) {
                ImageLoader.getInstance().putImageToCache(new BitmapDrawable(localObject2), localObject6);
              }
              SendMessagesHelper.getInstance(SendMessagesHelper.22.this.val$currentAccount).sendMessage(localObject5, localVideoEditedInfo, localObject3, SendMessagesHelper.22.this.val$dialog_id, SendMessagesHelper.22.this.val$reply_to_msg, localObject1, SendMessagesHelper.22.this.val$entities, null, localObject7, SendMessagesHelper.22.this.val$ttl);
            }
          });
          return;
          localObject3 = new TLRPC.TL_documentAttributeVideo_layer65();
          break;
          localObject3 = new TLRPC.TL_documentAttributeVideo();
          ((TLRPC.TL_documentAttributeVideo)localObject3).supports_streaming = true;
          break;
          ((TLRPC.TL_documentAttributeVideo)localObject3).duration = ((int)(paramInt3 / 1000L));
          if ((localVideoEditedInfo.rotationValue == 90) || (localVideoEditedInfo.rotationValue == 270))
          {
            ((TLRPC.TL_documentAttributeVideo)localObject3).w = paramLong1;
            ((TLRPC.TL_documentAttributeVideo)localObject3).h = paramCharSequence;
            break label975;
          }
          ((TLRPC.TL_documentAttributeVideo)localObject3).w = paramCharSequence;
          ((TLRPC.TL_documentAttributeVideo)localObject3).h = paramLong1;
          break label975;
          if (localFile.exists()) {
            ((TLRPC.TL_document)localObject5).size = ((int)localFile.length());
          }
          SendMessagesHelper.fillVideoAttribute(paramString, (TLRPC.TL_documentAttributeVideo)localObject3, null);
          localObject3 = localObject7;
          localObject6 = localObject1;
          break label1044;
        }
        label1267:
        SendMessagesHelper.prepareSendingDocumentInternal(paramInt1, paramString, paramString, null, null, paramLong3, this.val$reply_to_msg, this.val$caption, this.val$entities);
      }
    }).start();
  }
  
  private void putToDelayedMessages(String paramString, DelayedMessage paramDelayedMessage)
  {
    ArrayList localArrayList2 = (ArrayList)this.delayedMessages.get(paramString);
    ArrayList localArrayList1 = localArrayList2;
    if (localArrayList2 == null)
    {
      localArrayList1 = new ArrayList();
      this.delayedMessages.put(paramString, localArrayList1);
    }
    localArrayList1.add(paramDelayedMessage);
  }
  
  private void sendLocation(Location paramLocation)
  {
    TLRPC.TL_messageMediaGeo localTL_messageMediaGeo = new TLRPC.TL_messageMediaGeo();
    localTL_messageMediaGeo.geo = new TLRPC.TL_geoPoint();
    localTL_messageMediaGeo.geo.lat = paramLocation.getLatitude();
    localTL_messageMediaGeo.geo._long = paramLocation.getLongitude();
    paramLocation = this.waitingForLocation.entrySet().iterator();
    while (paramLocation.hasNext())
    {
      MessageObject localMessageObject = (MessageObject)((Map.Entry)paramLocation.next()).getValue();
      sendMessage(localTL_messageMediaGeo, localMessageObject.getDialogId(), localMessageObject, null, null);
    }
  }
  
  /* Error */
  private void sendMessage(String paramString1, String paramString2, TLRPC.MessageMedia paramMessageMedia, TLRPC.TL_photo paramTL_photo, VideoEditedInfo paramVideoEditedInfo, TLRPC.User paramUser, TLRPC.TL_document paramTL_document, TLRPC.TL_game paramTL_game, long paramLong, String paramString3, MessageObject paramMessageObject1, TLRPC.WebPage paramWebPage, boolean paramBoolean, MessageObject paramMessageObject2, ArrayList<TLRPC.MessageEntity> paramArrayList, TLRPC.ReplyMarkup paramReplyMarkup, HashMap<String, String> paramHashMap, int paramInt)
  {
    // Byte code:
    //   0: lload 9
    //   2: lconst_0
    //   3: lcmp
    //   4: ifne +4 -> 8
    //   7: return
    //   8: aload_2
    //   9: astore 37
    //   11: aload_1
    //   12: ifnonnull +15 -> 27
    //   15: aload_2
    //   16: astore 37
    //   18: aload_2
    //   19: ifnonnull +8 -> 27
    //   22: ldc_w 1168
    //   25: astore 37
    //   27: aconst_null
    //   28: astore_2
    //   29: aload_2
    //   30: astore 36
    //   32: aload 18
    //   34: ifnull +30 -> 64
    //   37: aload_2
    //   38: astore 36
    //   40: aload 18
    //   42: ldc_w 1416
    //   45: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   48: ifeq +16 -> 64
    //   51: aload 18
    //   53: ldc_w 1416
    //   56: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   59: checkcast 489	java/lang/String
    //   62: astore 36
    //   64: aconst_null
    //   65: astore 31
    //   67: aconst_null
    //   68: astore_2
    //   69: iconst_m1
    //   70: istore 20
    //   72: lload 9
    //   74: l2i
    //   75: istore 25
    //   77: lload 9
    //   79: bipush 32
    //   81: lshr
    //   82: l2i
    //   83: istore 24
    //   85: iconst_0
    //   86: istore 22
    //   88: aconst_null
    //   89: astore 32
    //   91: iload 25
    //   93: ifeq +118 -> 211
    //   96: aload_0
    //   97: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   100: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   103: iload 25
    //   105: invokevirtual 1533	org/telegram/messenger/MessagesController:getInputPeer	(I)Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   108: astore 38
    //   110: aconst_null
    //   111: astore 45
    //   113: iload 25
    //   115: ifne +102 -> 217
    //   118: aload_0
    //   119: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   122: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   125: iload 24
    //   127: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   130: invokevirtual 1541	org/telegram/messenger/MessagesController:getEncryptedChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   133: astore 32
    //   135: aload 32
    //   137: astore 39
    //   139: aload 32
    //   141: ifnonnull +128 -> 269
    //   144: aload 15
    //   146: ifnull -139 -> 7
    //   149: aload_0
    //   150: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   153: invokestatic 1284	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
    //   156: aload 15
    //   158: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   161: invokevirtual 1544	org/telegram/messenger/MessagesStorage:markMessageAsSendError	(Lorg/telegram/tgnet/TLRPC$Message;)V
    //   164: aload 15
    //   166: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   169: iconst_2
    //   170: putfield 1547	org/telegram/tgnet/TLRPC$Message:send_state	I
    //   173: aload_0
    //   174: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   177: invokestatic 1552	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   180: getstatic 1555	org/telegram/messenger/NotificationCenter:messageSendError	I
    //   183: iconst_1
    //   184: anewarray 4	java/lang/Object
    //   187: dup
    //   188: iconst_0
    //   189: aload 15
    //   191: invokevirtual 766	org/telegram/messenger/MessageObject:getId	()I
    //   194: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   197: aastore
    //   198: invokevirtual 1559	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   201: aload_0
    //   202: aload 15
    //   204: invokevirtual 766	org/telegram/messenger/MessageObject:getId	()I
    //   207: invokevirtual 1562	org/telegram/messenger/SendMessagesHelper:processSentMessage	(I)V
    //   210: return
    //   211: aconst_null
    //   212: astore 38
    //   214: goto -104 -> 110
    //   217: aload 32
    //   219: astore 39
    //   221: aload 38
    //   223: instanceof 1564
    //   226: ifeq +43 -> 269
    //   229: aload_0
    //   230: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   233: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   236: aload 38
    //   238: getfield 1569	org/telegram/tgnet/TLRPC$InputPeer:channel_id	I
    //   241: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   244: invokevirtual 1573	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   247: astore 33
    //   249: aload 33
    //   251: ifnull +609 -> 860
    //   254: aload 33
    //   256: getfield 1578	org/telegram/tgnet/TLRPC$Chat:megagroup	Z
    //   259: ifne +601 -> 860
    //   262: iconst_1
    //   263: istore 22
    //   265: aload 32
    //   267: astore 39
    //   269: aload 15
    //   271: ifnull +1166 -> 1437
    //   274: aload 15
    //   276: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   279: astore 31
    //   281: aload 31
    //   283: astore_2
    //   284: aload 15
    //   286: invokevirtual 1581	org/telegram/messenger/MessageObject:isForwarded	()Z
    //   289: ifeq +581 -> 870
    //   292: iconst_4
    //   293: istore 21
    //   295: aload 18
    //   297: astore 41
    //   299: aload 13
    //   301: astore 44
    //   303: aload 7
    //   305: astore 40
    //   307: aload_3
    //   308: astore 42
    //   310: aload_1
    //   311: astore 43
    //   313: aload 31
    //   315: astore 8
    //   317: aload 8
    //   319: astore_2
    //   320: aload 8
    //   322: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   325: lconst_0
    //   326: lcmp
    //   327: ifne +15 -> 342
    //   330: aload 8
    //   332: astore_2
    //   333: aload 8
    //   335: aload_0
    //   336: invokevirtual 1587	org/telegram/messenger/SendMessagesHelper:getNextRandomId	()J
    //   339: putfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   342: aload 41
    //   344: ifnull +80 -> 424
    //   347: aload 8
    //   349: astore_2
    //   350: aload 41
    //   352: ldc_w 1589
    //   355: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   358: ifeq +66 -> 424
    //   361: aload 39
    //   363: ifnull +2836 -> 3199
    //   366: aload 8
    //   368: astore_2
    //   369: aload 8
    //   371: aload 41
    //   373: ldc_w 1591
    //   376: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   379: checkcast 489	java/lang/String
    //   382: putfield 1594	org/telegram/tgnet/TLRPC$Message:via_bot_name	Ljava/lang/String;
    //   385: aload 8
    //   387: astore_2
    //   388: aload 8
    //   390: getfield 1594	org/telegram/tgnet/TLRPC$Message:via_bot_name	Ljava/lang/String;
    //   393: ifnonnull +14 -> 407
    //   396: aload 8
    //   398: astore_2
    //   399: aload 8
    //   401: ldc_w 1168
    //   404: putfield 1594	org/telegram/tgnet/TLRPC$Message:via_bot_name	Ljava/lang/String;
    //   407: aload 8
    //   409: astore_2
    //   410: aload 8
    //   412: aload 8
    //   414: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   417: sipush 2048
    //   420: ior
    //   421: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   424: aload 8
    //   426: astore_2
    //   427: aload 8
    //   429: aload 41
    //   431: putfield 1598	org/telegram/tgnet/TLRPC$Message:params	Ljava/util/HashMap;
    //   434: aload 15
    //   436: ifnull +14 -> 450
    //   439: aload 8
    //   441: astore_2
    //   442: aload 15
    //   444: getfield 1601	org/telegram/messenger/MessageObject:resendAsIs	Z
    //   447: ifne +125 -> 572
    //   450: aload 8
    //   452: astore_2
    //   453: aload 8
    //   455: aload_0
    //   456: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   459: invokestatic 1047	org/telegram/tgnet/ConnectionsManager:getInstance	(I)Lorg/telegram/tgnet/ConnectionsManager;
    //   462: invokevirtual 1295	org/telegram/tgnet/ConnectionsManager:getCurrentTime	()I
    //   465: putfield 1602	org/telegram/tgnet/TLRPC$Message:date	I
    //   468: aload 8
    //   470: astore_2
    //   471: aload 38
    //   473: instanceof 1564
    //   476: ifeq +2791 -> 3267
    //   479: iload 22
    //   481: ifeq +29 -> 510
    //   484: aload 8
    //   486: astore_2
    //   487: aload 8
    //   489: iconst_1
    //   490: putfield 1605	org/telegram/tgnet/TLRPC$Message:views	I
    //   493: aload 8
    //   495: astore_2
    //   496: aload 8
    //   498: aload 8
    //   500: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   503: sipush 1024
    //   506: ior
    //   507: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   510: aload 8
    //   512: astore_2
    //   513: aload_0
    //   514: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   517: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   520: aload 38
    //   522: getfield 1569	org/telegram/tgnet/TLRPC$InputPeer:channel_id	I
    //   525: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   528: invokevirtual 1573	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   531: astore_1
    //   532: aload_1
    //   533: ifnull +39 -> 572
    //   536: aload 8
    //   538: astore_2
    //   539: aload_1
    //   540: getfield 1578	org/telegram/tgnet/TLRPC$Chat:megagroup	Z
    //   543: ifeq +2684 -> 3227
    //   546: aload 8
    //   548: astore_2
    //   549: aload 8
    //   551: aload 8
    //   553: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   556: ldc_w 722
    //   559: ior
    //   560: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   563: aload 8
    //   565: astore_2
    //   566: aload 8
    //   568: iconst_1
    //   569: putfield 1608	org/telegram/tgnet/TLRPC$Message:unread	Z
    //   572: aload 8
    //   574: astore_2
    //   575: aload 8
    //   577: aload 8
    //   579: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   582: sipush 512
    //   585: ior
    //   586: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   589: aload 8
    //   591: astore_2
    //   592: aload 8
    //   594: lload 9
    //   596: putfield 1611	org/telegram/tgnet/TLRPC$Message:dialog_id	J
    //   599: aload 12
    //   601: ifnull +69 -> 670
    //   604: aload 39
    //   606: ifnull +2673 -> 3279
    //   609: aload 8
    //   611: astore_2
    //   612: aload 12
    //   614: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   617: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   620: lconst_0
    //   621: lcmp
    //   622: ifeq +2657 -> 3279
    //   625: aload 8
    //   627: astore_2
    //   628: aload 8
    //   630: aload 12
    //   632: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   635: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   638: putfield 1614	org/telegram/tgnet/TLRPC$Message:reply_to_random_id	J
    //   641: aload 8
    //   643: astore_2
    //   644: aload 8
    //   646: aload 8
    //   648: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   651: bipush 8
    //   653: ior
    //   654: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   657: aload 8
    //   659: astore_2
    //   660: aload 8
    //   662: aload 12
    //   664: invokevirtual 766	org/telegram/messenger/MessageObject:getId	()I
    //   667: putfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   670: aload 17
    //   672: ifnull +34 -> 706
    //   675: aload 39
    //   677: ifnonnull +29 -> 706
    //   680: aload 8
    //   682: astore_2
    //   683: aload 8
    //   685: aload 8
    //   687: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   690: bipush 64
    //   692: ior
    //   693: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   696: aload 8
    //   698: astore_2
    //   699: aload 8
    //   701: aload 17
    //   703: putfield 1618	org/telegram/tgnet/TLRPC$Message:reply_markup	Lorg/telegram/tgnet/TLRPC$ReplyMarkup;
    //   706: iload 25
    //   708: ifeq +3265 -> 3973
    //   711: iload 24
    //   713: iconst_1
    //   714: if_icmpne +3167 -> 3881
    //   717: aload 8
    //   719: astore_2
    //   720: aload_0
    //   721: getfield 227	org/telegram/messenger/SendMessagesHelper:currentChatInfo	Lorg/telegram/tgnet/TLRPC$ChatFull;
    //   724: ifnonnull +2574 -> 3298
    //   727: aload 8
    //   729: astore_2
    //   730: aload_0
    //   731: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   734: invokestatic 1284	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
    //   737: aload 8
    //   739: invokevirtual 1544	org/telegram/messenger/MessagesStorage:markMessageAsSendError	(Lorg/telegram/tgnet/TLRPC$Message;)V
    //   742: aload 8
    //   744: astore_2
    //   745: aload_0
    //   746: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   749: invokestatic 1552	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   752: getstatic 1555	org/telegram/messenger/NotificationCenter:messageSendError	I
    //   755: iconst_1
    //   756: anewarray 4	java/lang/Object
    //   759: dup
    //   760: iconst_0
    //   761: aload 8
    //   763: getfield 1620	org/telegram/tgnet/TLRPC$Message:id	I
    //   766: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   769: aastore
    //   770: invokevirtual 1559	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   773: aload 8
    //   775: astore_2
    //   776: aload_0
    //   777: aload 8
    //   779: getfield 1620	org/telegram/tgnet/TLRPC$Message:id	I
    //   782: invokevirtual 1562	org/telegram/messenger/SendMessagesHelper:processSentMessage	(I)V
    //   785: return
    //   786: astore_1
    //   787: aconst_null
    //   788: astore_3
    //   789: aload_2
    //   790: astore 8
    //   792: aload_3
    //   793: astore_2
    //   794: aload_1
    //   795: invokestatic 467	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   798: aload_0
    //   799: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   802: invokestatic 1284	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
    //   805: aload 8
    //   807: invokevirtual 1544	org/telegram/messenger/MessagesStorage:markMessageAsSendError	(Lorg/telegram/tgnet/TLRPC$Message;)V
    //   810: aload_2
    //   811: ifnull +11 -> 822
    //   814: aload_2
    //   815: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   818: iconst_2
    //   819: putfield 1547	org/telegram/tgnet/TLRPC$Message:send_state	I
    //   822: aload_0
    //   823: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   826: invokestatic 1552	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   829: getstatic 1555	org/telegram/messenger/NotificationCenter:messageSendError	I
    //   832: iconst_1
    //   833: anewarray 4	java/lang/Object
    //   836: dup
    //   837: iconst_0
    //   838: aload 8
    //   840: getfield 1620	org/telegram/tgnet/TLRPC$Message:id	I
    //   843: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   846: aastore
    //   847: invokevirtual 1559	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   850: aload_0
    //   851: aload 8
    //   853: getfield 1620	org/telegram/tgnet/TLRPC$Message:id	I
    //   856: invokevirtual 1562	org/telegram/messenger/SendMessagesHelper:processSentMessage	(I)V
    //   859: return
    //   860: iconst_0
    //   861: istore 22
    //   863: aload 32
    //   865: astore 39
    //   867: goto -598 -> 269
    //   870: aload 31
    //   872: astore_2
    //   873: aload 15
    //   875: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   878: ifne +165 -> 1043
    //   881: aload 31
    //   883: astore_2
    //   884: aload 15
    //   886: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   889: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   892: instanceof 1626
    //   895: ifeq +136 -> 1031
    //   898: goto +9396 -> 10294
    //   901: aload 31
    //   903: astore 8
    //   905: iload 20
    //   907: istore 21
    //   909: aload 32
    //   911: astore 43
    //   913: aload 33
    //   915: astore 42
    //   917: aload 34
    //   919: astore 4
    //   921: aload 35
    //   923: astore 6
    //   925: aload 7
    //   927: astore 40
    //   929: aload 13
    //   931: astore 44
    //   933: aload 18
    //   935: astore 41
    //   937: aload 18
    //   939: ifnull -622 -> 317
    //   942: aload 31
    //   944: astore 8
    //   946: iload 20
    //   948: istore 21
    //   950: aload 32
    //   952: astore 43
    //   954: aload 33
    //   956: astore 42
    //   958: aload 34
    //   960: astore 4
    //   962: aload 35
    //   964: astore 6
    //   966: aload 7
    //   968: astore 40
    //   970: aload 13
    //   972: astore 44
    //   974: aload 18
    //   976: astore 41
    //   978: aload 31
    //   980: astore_2
    //   981: aload 18
    //   983: ldc_w 1628
    //   986: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   989: ifeq -672 -> 317
    //   992: bipush 9
    //   994: istore 21
    //   996: aload 31
    //   998: astore 8
    //   1000: aload 32
    //   1002: astore 43
    //   1004: aload 33
    //   1006: astore 42
    //   1008: aload 34
    //   1010: astore 4
    //   1012: aload 35
    //   1014: astore 6
    //   1016: aload 7
    //   1018: astore 40
    //   1020: aload 13
    //   1022: astore 44
    //   1024: aload 18
    //   1026: astore 41
    //   1028: goto -711 -> 317
    //   1031: aload 31
    //   1033: astore_2
    //   1034: aload 31
    //   1036: getfield 1629	org/telegram/tgnet/TLRPC$Message:message	Ljava/lang/String;
    //   1039: astore_1
    //   1040: goto +9254 -> 10294
    //   1043: aload 31
    //   1045: astore_2
    //   1046: aload 15
    //   1048: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1051: iconst_4
    //   1052: if_icmpne +30 -> 1082
    //   1055: aload 31
    //   1057: astore_2
    //   1058: aload 31
    //   1060: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1063: astore 33
    //   1065: iconst_1
    //   1066: istore 20
    //   1068: aload_1
    //   1069: astore 32
    //   1071: aload 4
    //   1073: astore 34
    //   1075: aload 6
    //   1077: astore 35
    //   1079: goto -178 -> 901
    //   1082: aload 31
    //   1084: astore_2
    //   1085: aload 15
    //   1087: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1090: iconst_1
    //   1091: if_icmpne +35 -> 1126
    //   1094: aload 31
    //   1096: astore_2
    //   1097: aload 31
    //   1099: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1102: getfield 1635	org/telegram/tgnet/TLRPC$MessageMedia:photo	Lorg/telegram/tgnet/TLRPC$Photo;
    //   1105: checkcast 1637	org/telegram/tgnet/TLRPC$TL_photo
    //   1108: astore 34
    //   1110: iconst_2
    //   1111: istore 20
    //   1113: aload_1
    //   1114: astore 32
    //   1116: aload_3
    //   1117: astore 33
    //   1119: aload 6
    //   1121: astore 35
    //   1123: goto -222 -> 901
    //   1126: aload 31
    //   1128: astore_2
    //   1129: aload 15
    //   1131: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1134: iconst_3
    //   1135: if_icmpeq +20 -> 1155
    //   1138: aload 31
    //   1140: astore_2
    //   1141: aload 15
    //   1143: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1146: iconst_5
    //   1147: if_icmpeq +8 -> 1155
    //   1150: aload 5
    //   1152: ifnull +39 -> 1191
    //   1155: iconst_3
    //   1156: istore 20
    //   1158: aload 31
    //   1160: astore_2
    //   1161: aload 31
    //   1163: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1166: getfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   1169: checkcast 1290	org/telegram/tgnet/TLRPC$TL_document
    //   1172: astore 7
    //   1174: aload_1
    //   1175: astore 32
    //   1177: aload_3
    //   1178: astore 33
    //   1180: aload 4
    //   1182: astore 34
    //   1184: aload 6
    //   1186: astore 35
    //   1188: goto -287 -> 901
    //   1191: aload 31
    //   1193: astore_2
    //   1194: aload 15
    //   1196: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1199: bipush 12
    //   1201: if_icmpne +84 -> 1285
    //   1204: aload 31
    //   1206: astore_2
    //   1207: new 1643	org/telegram/tgnet/TLRPC$TL_userRequest_old2
    //   1210: dup
    //   1211: invokespecial 1644	org/telegram/tgnet/TLRPC$TL_userRequest_old2:<init>	()V
    //   1214: astore 35
    //   1216: aload 35
    //   1218: aload 31
    //   1220: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1223: getfield 1645	org/telegram/tgnet/TLRPC$MessageMedia:phone_number	Ljava/lang/String;
    //   1226: putfield 1201	org/telegram/tgnet/TLRPC$User:phone	Ljava/lang/String;
    //   1229: aload 35
    //   1231: aload 31
    //   1233: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1236: getfield 1646	org/telegram/tgnet/TLRPC$MessageMedia:first_name	Ljava/lang/String;
    //   1239: putfield 1205	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   1242: aload 35
    //   1244: aload 31
    //   1246: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1249: getfield 1647	org/telegram/tgnet/TLRPC$MessageMedia:last_name	Ljava/lang/String;
    //   1252: putfield 1209	org/telegram/tgnet/TLRPC$User:last_name	Ljava/lang/String;
    //   1255: aload 35
    //   1257: aload 31
    //   1259: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1262: getfield 1650	org/telegram/tgnet/TLRPC$MessageMedia:user_id	I
    //   1265: putfield 1651	org/telegram/tgnet/TLRPC$User:id	I
    //   1268: bipush 6
    //   1270: istore 20
    //   1272: aload_1
    //   1273: astore 32
    //   1275: aload_3
    //   1276: astore 33
    //   1278: aload 4
    //   1280: astore 34
    //   1282: goto -381 -> 901
    //   1285: aload 31
    //   1287: astore_2
    //   1288: aload 15
    //   1290: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1293: bipush 8
    //   1295: if_icmpeq +42 -> 1337
    //   1298: aload 31
    //   1300: astore_2
    //   1301: aload 15
    //   1303: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1306: bipush 9
    //   1308: if_icmpeq +29 -> 1337
    //   1311: aload 31
    //   1313: astore_2
    //   1314: aload 15
    //   1316: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1319: bipush 13
    //   1321: if_icmpeq +16 -> 1337
    //   1324: aload 31
    //   1326: astore_2
    //   1327: aload 15
    //   1329: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1332: bipush 14
    //   1334: if_icmpne +40 -> 1374
    //   1337: aload 31
    //   1339: astore_2
    //   1340: aload 31
    //   1342: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1345: getfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   1348: checkcast 1290	org/telegram/tgnet/TLRPC$TL_document
    //   1351: astore 7
    //   1353: bipush 7
    //   1355: istore 20
    //   1357: aload_1
    //   1358: astore 32
    //   1360: aload_3
    //   1361: astore 33
    //   1363: aload 4
    //   1365: astore 34
    //   1367: aload 6
    //   1369: astore 35
    //   1371: goto -470 -> 901
    //   1374: aload 31
    //   1376: astore_2
    //   1377: aload_1
    //   1378: astore 32
    //   1380: aload_3
    //   1381: astore 33
    //   1383: aload 4
    //   1385: astore 34
    //   1387: aload 6
    //   1389: astore 35
    //   1391: aload 15
    //   1393: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   1396: iconst_2
    //   1397: if_icmpne -496 -> 901
    //   1400: aload 31
    //   1402: astore_2
    //   1403: aload 31
    //   1405: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1408: getfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   1411: checkcast 1290	org/telegram/tgnet/TLRPC$TL_document
    //   1414: astore 7
    //   1416: bipush 8
    //   1418: istore 20
    //   1420: aload_1
    //   1421: astore 32
    //   1423: aload_3
    //   1424: astore 33
    //   1426: aload 4
    //   1428: astore 34
    //   1430: aload 6
    //   1432: astore 35
    //   1434: goto -533 -> 901
    //   1437: aload_1
    //   1438: ifnull +367 -> 1805
    //   1441: aload 39
    //   1443: ifnull +319 -> 1762
    //   1446: new 1653	org/telegram/tgnet/TLRPC$TL_message_secret
    //   1449: dup
    //   1450: invokespecial 1654	org/telegram/tgnet/TLRPC$TL_message_secret:<init>	()V
    //   1453: astore 8
    //   1455: aload 13
    //   1457: astore 31
    //   1459: aload 39
    //   1461: ifnull +54 -> 1515
    //   1464: aload 8
    //   1466: astore_2
    //   1467: aload 13
    //   1469: astore 31
    //   1471: aload 13
    //   1473: instanceof 1113
    //   1476: ifeq +39 -> 1515
    //   1479: aload 8
    //   1481: astore_2
    //   1482: aload 13
    //   1484: getfield 1127	org/telegram/tgnet/TLRPC$WebPage:url	Ljava/lang/String;
    //   1487: ifnull +8827 -> 10314
    //   1490: aload 8
    //   1492: astore_2
    //   1493: new 1656	org/telegram/tgnet/TLRPC$TL_webPageUrlPending
    //   1496: dup
    //   1497: invokespecial 1657	org/telegram/tgnet/TLRPC$TL_webPageUrlPending:<init>	()V
    //   1500: astore 31
    //   1502: aload 8
    //   1504: astore_2
    //   1505: aload 31
    //   1507: aload 13
    //   1509: getfield 1127	org/telegram/tgnet/TLRPC$WebPage:url	Ljava/lang/String;
    //   1512: putfield 1127	org/telegram/tgnet/TLRPC$WebPage:url	Ljava/lang/String;
    //   1515: aload 31
    //   1517: ifnonnull +257 -> 1774
    //   1520: aload 8
    //   1522: astore_2
    //   1523: aload 8
    //   1525: new 1659	org/telegram/tgnet/TLRPC$TL_messageMediaEmpty
    //   1528: dup
    //   1529: invokespecial 1660	org/telegram/tgnet/TLRPC$TL_messageMediaEmpty:<init>	()V
    //   1532: putfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1535: aload 18
    //   1537: ifnull +8783 -> 10320
    //   1540: aload 8
    //   1542: astore_2
    //   1543: aload 18
    //   1545: ldc_w 1628
    //   1548: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   1551: ifeq +8769 -> 10320
    //   1554: bipush 9
    //   1556: istore 20
    //   1558: aload 8
    //   1560: astore_2
    //   1561: aload 8
    //   1563: aload_1
    //   1564: putfield 1629	org/telegram/tgnet/TLRPC$Message:message	Ljava/lang/String;
    //   1567: aload 18
    //   1569: astore 32
    //   1571: aload 31
    //   1573: astore 33
    //   1575: aload 16
    //   1577: ifnull +41 -> 1618
    //   1580: aload 8
    //   1582: astore_2
    //   1583: aload 16
    //   1585: invokevirtual 773	java/util/ArrayList:isEmpty	()Z
    //   1588: ifne +30 -> 1618
    //   1591: aload 8
    //   1593: astore_2
    //   1594: aload 8
    //   1596: aload 16
    //   1598: putfield 1661	org/telegram/tgnet/TLRPC$Message:entities	Ljava/util/ArrayList;
    //   1601: aload 8
    //   1603: astore_2
    //   1604: aload 8
    //   1606: aload 8
    //   1608: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   1611: sipush 128
    //   1614: ior
    //   1615: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   1618: aload 37
    //   1620: ifnull +1516 -> 3136
    //   1623: aload 8
    //   1625: astore_2
    //   1626: aload 8
    //   1628: aload 37
    //   1630: putfield 1629	org/telegram/tgnet/TLRPC$Message:message	Ljava/lang/String;
    //   1633: aload 8
    //   1635: astore_2
    //   1636: aload 8
    //   1638: getfield 878	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   1641: ifnonnull +14 -> 1655
    //   1644: aload 8
    //   1646: astore_2
    //   1647: aload 8
    //   1649: ldc_w 1168
    //   1652: putfield 878	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   1655: aload 8
    //   1657: astore_2
    //   1658: aload_0
    //   1659: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   1662: invokestatic 1664	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   1665: invokevirtual 1667	org/telegram/messenger/UserConfig:getNewMessageId	()I
    //   1668: istore 21
    //   1670: aload 8
    //   1672: astore_2
    //   1673: aload 8
    //   1675: iload 21
    //   1677: putfield 1620	org/telegram/tgnet/TLRPC$Message:id	I
    //   1680: aload 8
    //   1682: astore_2
    //   1683: aload 8
    //   1685: iload 21
    //   1687: putfield 1668	org/telegram/tgnet/TLRPC$Message:local_id	I
    //   1690: aload 8
    //   1692: astore_2
    //   1693: aload 8
    //   1695: iconst_1
    //   1696: putfield 1671	org/telegram/tgnet/TLRPC$Message:out	Z
    //   1699: iload 22
    //   1701: ifeq +1460 -> 3161
    //   1704: aload 38
    //   1706: ifnull +1455 -> 3161
    //   1709: aload 8
    //   1711: astore_2
    //   1712: aload 8
    //   1714: aload 38
    //   1716: getfield 1569	org/telegram/tgnet/TLRPC$InputPeer:channel_id	I
    //   1719: ineg
    //   1720: putfield 1674	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   1723: aload 8
    //   1725: astore_2
    //   1726: aload_0
    //   1727: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   1730: invokestatic 1664	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   1733: iconst_0
    //   1734: invokevirtual 1678	org/telegram/messenger/UserConfig:saveConfig	(Z)V
    //   1737: iload 20
    //   1739: istore 21
    //   1741: aload_1
    //   1742: astore 43
    //   1744: aload_3
    //   1745: astore 42
    //   1747: aload 7
    //   1749: astore 40
    //   1751: aload 33
    //   1753: astore 44
    //   1755: aload 32
    //   1757: astore 41
    //   1759: goto -1442 -> 317
    //   1762: new 1680	org/telegram/tgnet/TLRPC$TL_message
    //   1765: dup
    //   1766: invokespecial 1681	org/telegram/tgnet/TLRPC$TL_message:<init>	()V
    //   1769: astore 8
    //   1771: goto -316 -> 1455
    //   1774: aload 8
    //   1776: astore_2
    //   1777: aload 8
    //   1779: new 1683	org/telegram/tgnet/TLRPC$TL_messageMediaWebPage
    //   1782: dup
    //   1783: invokespecial 1684	org/telegram/tgnet/TLRPC$TL_messageMediaWebPage:<init>	()V
    //   1786: putfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1789: aload 8
    //   1791: astore_2
    //   1792: aload 8
    //   1794: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1797: aload 31
    //   1799: putfield 1688	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   1802: goto -267 -> 1535
    //   1805: aload_3
    //   1806: ifnull +72 -> 1878
    //   1809: aload 39
    //   1811: ifnull +55 -> 1866
    //   1814: new 1653	org/telegram/tgnet/TLRPC$TL_message_secret
    //   1817: dup
    //   1818: invokespecial 1654	org/telegram/tgnet/TLRPC$TL_message_secret:<init>	()V
    //   1821: astore 8
    //   1823: aload 8
    //   1825: astore_2
    //   1826: aload 8
    //   1828: aload_3
    //   1829: putfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1832: aload 18
    //   1834: ifnull +8492 -> 10326
    //   1837: aload 8
    //   1839: astore_2
    //   1840: aload 18
    //   1842: ldc_w 1628
    //   1845: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   1848: ifeq +8478 -> 10326
    //   1851: bipush 9
    //   1853: istore 20
    //   1855: aload 13
    //   1857: astore 33
    //   1859: aload 18
    //   1861: astore 32
    //   1863: goto -288 -> 1575
    //   1866: new 1680	org/telegram/tgnet/TLRPC$TL_message
    //   1869: dup
    //   1870: invokespecial 1681	org/telegram/tgnet/TLRPC$TL_message:<init>	()V
    //   1873: astore 8
    //   1875: goto -52 -> 1823
    //   1878: aload 4
    //   1880: ifnull +274 -> 2154
    //   1883: aload 39
    //   1885: ifnull +207 -> 2092
    //   1888: new 1653	org/telegram/tgnet/TLRPC$TL_message_secret
    //   1891: dup
    //   1892: invokespecial 1654	org/telegram/tgnet/TLRPC$TL_message_secret:<init>	()V
    //   1895: astore 8
    //   1897: aload 8
    //   1899: astore_2
    //   1900: aload 8
    //   1902: new 1690	org/telegram/tgnet/TLRPC$TL_messageMediaPhoto
    //   1905: dup
    //   1906: invokespecial 1691	org/telegram/tgnet/TLRPC$TL_messageMediaPhoto:<init>	()V
    //   1909: putfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1912: aload 8
    //   1914: astore_2
    //   1915: aload 8
    //   1917: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1920: astore 31
    //   1922: aload 8
    //   1924: astore_2
    //   1925: aload 31
    //   1927: aload 31
    //   1929: getfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   1932: iconst_3
    //   1933: ior
    //   1934: putfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   1937: aload 16
    //   1939: ifnull +13 -> 1952
    //   1942: aload 8
    //   1944: astore_2
    //   1945: aload 8
    //   1947: aload 16
    //   1949: putfield 1661	org/telegram/tgnet/TLRPC$Message:entities	Ljava/util/ArrayList;
    //   1952: iload 19
    //   1954: ifeq +51 -> 2005
    //   1957: aload 8
    //   1959: astore_2
    //   1960: aload 8
    //   1962: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1965: iload 19
    //   1967: putfield 1695	org/telegram/tgnet/TLRPC$MessageMedia:ttl_seconds	I
    //   1970: aload 8
    //   1972: astore_2
    //   1973: aload 8
    //   1975: iload 19
    //   1977: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   1980: aload 8
    //   1982: astore_2
    //   1983: aload 8
    //   1985: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1988: astore 31
    //   1990: aload 8
    //   1992: astore_2
    //   1993: aload 31
    //   1995: aload 31
    //   1997: getfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   2000: iconst_4
    //   2001: ior
    //   2002: putfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   2005: aload 8
    //   2007: astore_2
    //   2008: aload 8
    //   2010: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2013: aload 4
    //   2015: putfield 1635	org/telegram/tgnet/TLRPC$MessageMedia:photo	Lorg/telegram/tgnet/TLRPC$Photo;
    //   2018: aload 18
    //   2020: ifnull +8320 -> 10340
    //   2023: aload 8
    //   2025: astore_2
    //   2026: aload 18
    //   2028: ldc_w 1628
    //   2031: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   2034: ifeq +8306 -> 10340
    //   2037: bipush 9
    //   2039: istore 20
    //   2041: aload 11
    //   2043: ifnull +61 -> 2104
    //   2046: aload 8
    //   2048: astore_2
    //   2049: aload 11
    //   2051: invokevirtual 782	java/lang/String:length	()I
    //   2054: ifle +50 -> 2104
    //   2057: aload 8
    //   2059: astore_2
    //   2060: aload 11
    //   2062: ldc_w 1698
    //   2065: invokevirtual 788	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   2068: ifeq +36 -> 2104
    //   2071: aload 8
    //   2073: astore_2
    //   2074: aload 8
    //   2076: aload 11
    //   2078: putfield 878	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   2081: aload 13
    //   2083: astore 33
    //   2085: aload 18
    //   2087: astore 32
    //   2089: goto -514 -> 1575
    //   2092: new 1680	org/telegram/tgnet/TLRPC$TL_message
    //   2095: dup
    //   2096: invokespecial 1681	org/telegram/tgnet/TLRPC$TL_message:<init>	()V
    //   2099: astore 8
    //   2101: goto -204 -> 1897
    //   2104: aload 8
    //   2106: astore_2
    //   2107: aload 8
    //   2109: aload 4
    //   2111: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   2114: aload 4
    //   2116: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   2119: invokevirtual 750	java/util/ArrayList:size	()I
    //   2122: iconst_1
    //   2123: isub
    //   2124: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   2127: checkcast 1410	org/telegram/tgnet/TLRPC$PhotoSize
    //   2130: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2133: iconst_1
    //   2134: invokestatic 852	org/telegram/messenger/FileLoader:getPathToAttach	(Lorg/telegram/tgnet/TLObject;Z)Ljava/io/File;
    //   2137: invokevirtual 830	java/io/File:toString	()Ljava/lang/String;
    //   2140: putfield 878	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   2143: aload 13
    //   2145: astore 33
    //   2147: aload 18
    //   2149: astore 32
    //   2151: goto -576 -> 1575
    //   2154: aload 8
    //   2156: ifnull +73 -> 2229
    //   2159: new 1680	org/telegram/tgnet/TLRPC$TL_message
    //   2162: dup
    //   2163: invokespecial 1681	org/telegram/tgnet/TLRPC$TL_message:<init>	()V
    //   2166: astore 31
    //   2168: aload 31
    //   2170: new 1626	org/telegram/tgnet/TLRPC$TL_messageMediaGame
    //   2173: dup
    //   2174: invokespecial 1703	org/telegram/tgnet/TLRPC$TL_messageMediaGame:<init>	()V
    //   2177: putfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2180: aload 31
    //   2182: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2185: aload 8
    //   2187: putfield 1707	org/telegram/tgnet/TLRPC$MessageMedia:game	Lorg/telegram/tgnet/TLRPC$TL_game;
    //   2190: aload 18
    //   2192: ifnull +8087 -> 10279
    //   2195: aload 18
    //   2197: ldc_w 1628
    //   2200: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   2203: istore 30
    //   2205: iload 30
    //   2207: ifeq +8072 -> 10279
    //   2210: bipush 9
    //   2212: istore 20
    //   2214: aload 31
    //   2216: astore 8
    //   2218: aload 13
    //   2220: astore 33
    //   2222: aload 18
    //   2224: astore 32
    //   2226: goto -651 -> 1575
    //   2229: aload 6
    //   2231: ifnull +220 -> 2451
    //   2234: aload 39
    //   2236: ifnull +203 -> 2439
    //   2239: new 1653	org/telegram/tgnet/TLRPC$TL_message_secret
    //   2242: dup
    //   2243: invokespecial 1654	org/telegram/tgnet/TLRPC$TL_message_secret:<init>	()V
    //   2246: astore 8
    //   2248: aload 8
    //   2250: astore_2
    //   2251: aload 8
    //   2253: new 1709	org/telegram/tgnet/TLRPC$TL_messageMediaContact
    //   2256: dup
    //   2257: invokespecial 1710	org/telegram/tgnet/TLRPC$TL_messageMediaContact:<init>	()V
    //   2260: putfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2263: aload 8
    //   2265: astore_2
    //   2266: aload 8
    //   2268: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2271: aload 6
    //   2273: getfield 1201	org/telegram/tgnet/TLRPC$User:phone	Ljava/lang/String;
    //   2276: putfield 1645	org/telegram/tgnet/TLRPC$MessageMedia:phone_number	Ljava/lang/String;
    //   2279: aload 8
    //   2281: astore_2
    //   2282: aload 8
    //   2284: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2287: aload 6
    //   2289: getfield 1205	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   2292: putfield 1646	org/telegram/tgnet/TLRPC$MessageMedia:first_name	Ljava/lang/String;
    //   2295: aload 8
    //   2297: astore_2
    //   2298: aload 8
    //   2300: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2303: aload 6
    //   2305: getfield 1209	org/telegram/tgnet/TLRPC$User:last_name	Ljava/lang/String;
    //   2308: putfield 1647	org/telegram/tgnet/TLRPC$MessageMedia:last_name	Ljava/lang/String;
    //   2311: aload 8
    //   2313: astore_2
    //   2314: aload 8
    //   2316: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2319: aload 6
    //   2321: getfield 1651	org/telegram/tgnet/TLRPC$User:id	I
    //   2324: putfield 1650	org/telegram/tgnet/TLRPC$MessageMedia:user_id	I
    //   2327: aload 8
    //   2329: astore_2
    //   2330: aload 8
    //   2332: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2335: getfield 1646	org/telegram/tgnet/TLRPC$MessageMedia:first_name	Ljava/lang/String;
    //   2338: ifnonnull +28 -> 2366
    //   2341: aload 8
    //   2343: astore_2
    //   2344: aload 8
    //   2346: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2349: ldc_w 1168
    //   2352: putfield 1646	org/telegram/tgnet/TLRPC$MessageMedia:first_name	Ljava/lang/String;
    //   2355: aload 8
    //   2357: astore_2
    //   2358: aload 6
    //   2360: ldc_w 1168
    //   2363: putfield 1205	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   2366: aload 8
    //   2368: astore_2
    //   2369: aload 8
    //   2371: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2374: getfield 1647	org/telegram/tgnet/TLRPC$MessageMedia:last_name	Ljava/lang/String;
    //   2377: ifnonnull +28 -> 2405
    //   2380: aload 8
    //   2382: astore_2
    //   2383: aload 8
    //   2385: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2388: ldc_w 1168
    //   2391: putfield 1647	org/telegram/tgnet/TLRPC$MessageMedia:last_name	Ljava/lang/String;
    //   2394: aload 8
    //   2396: astore_2
    //   2397: aload 6
    //   2399: ldc_w 1168
    //   2402: putfield 1209	org/telegram/tgnet/TLRPC$User:last_name	Ljava/lang/String;
    //   2405: aload 18
    //   2407: ifnull +7939 -> 10346
    //   2410: aload 8
    //   2412: astore_2
    //   2413: aload 18
    //   2415: ldc_w 1628
    //   2418: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   2421: ifeq +7925 -> 10346
    //   2424: bipush 9
    //   2426: istore 20
    //   2428: aload 13
    //   2430: astore 33
    //   2432: aload 18
    //   2434: astore 32
    //   2436: goto -861 -> 1575
    //   2439: new 1680	org/telegram/tgnet/TLRPC$TL_message
    //   2442: dup
    //   2443: invokespecial 1681	org/telegram/tgnet/TLRPC$TL_message:<init>	()V
    //   2446: astore 8
    //   2448: goto -200 -> 2248
    //   2451: aload 31
    //   2453: astore 8
    //   2455: aload 13
    //   2457: astore 33
    //   2459: aload 18
    //   2461: astore 32
    //   2463: aload 7
    //   2465: ifnull -890 -> 1575
    //   2468: aload 39
    //   2470: ifnull +499 -> 2969
    //   2473: new 1653	org/telegram/tgnet/TLRPC$TL_message_secret
    //   2476: dup
    //   2477: invokespecial 1654	org/telegram/tgnet/TLRPC$TL_message_secret:<init>	()V
    //   2480: astore 31
    //   2482: aload 31
    //   2484: astore_2
    //   2485: aload 31
    //   2487: new 1712	org/telegram/tgnet/TLRPC$TL_messageMediaDocument
    //   2490: dup
    //   2491: invokespecial 1713	org/telegram/tgnet/TLRPC$TL_messageMediaDocument:<init>	()V
    //   2494: putfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2497: aload 31
    //   2499: astore_2
    //   2500: aload 31
    //   2502: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2505: astore 8
    //   2507: aload 31
    //   2509: astore_2
    //   2510: aload 8
    //   2512: aload 8
    //   2514: getfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   2517: iconst_3
    //   2518: ior
    //   2519: putfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   2522: iload 19
    //   2524: ifeq +51 -> 2575
    //   2527: aload 31
    //   2529: astore_2
    //   2530: aload 31
    //   2532: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2535: iload 19
    //   2537: putfield 1695	org/telegram/tgnet/TLRPC$MessageMedia:ttl_seconds	I
    //   2540: aload 31
    //   2542: astore_2
    //   2543: aload 31
    //   2545: iload 19
    //   2547: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   2550: aload 31
    //   2552: astore_2
    //   2553: aload 31
    //   2555: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2558: astore 8
    //   2560: aload 31
    //   2562: astore_2
    //   2563: aload 8
    //   2565: aload 8
    //   2567: getfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   2570: iconst_4
    //   2571: ior
    //   2572: putfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   2575: aload 31
    //   2577: astore_2
    //   2578: aload 31
    //   2580: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2583: aload 7
    //   2585: putfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   2588: aload 18
    //   2590: ifnull +391 -> 2981
    //   2593: aload 31
    //   2595: astore_2
    //   2596: aload 18
    //   2598: ldc_w 1628
    //   2601: invokevirtual 1526	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   2604: ifeq +377 -> 2981
    //   2607: bipush 9
    //   2609: istore 21
    //   2611: aload 18
    //   2613: astore 34
    //   2615: aload 5
    //   2617: ifnull +48 -> 2665
    //   2620: aload 31
    //   2622: astore_2
    //   2623: aload 5
    //   2625: invokevirtual 1716	org/telegram/messenger/VideoEditedInfo:getString	()Ljava/lang/String;
    //   2628: astore 8
    //   2630: aload 18
    //   2632: astore 34
    //   2634: aload 18
    //   2636: ifnonnull +15 -> 2651
    //   2639: aload 31
    //   2641: astore_2
    //   2642: new 229	java/util/HashMap
    //   2645: dup
    //   2646: invokespecial 230	java/util/HashMap:<init>	()V
    //   2649: astore 34
    //   2651: aload 31
    //   2653: astore_2
    //   2654: aload 34
    //   2656: ldc_w 1718
    //   2659: aload 8
    //   2661: invokevirtual 999	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   2664: pop
    //   2665: aload 39
    //   2667: ifnull +362 -> 3029
    //   2670: aload 31
    //   2672: astore_2
    //   2673: aload 7
    //   2675: getfield 1309	org/telegram/tgnet/TLRPC$TL_document:dc_id	I
    //   2678: ifle +351 -> 3029
    //   2681: aload 31
    //   2683: astore_2
    //   2684: aload 7
    //   2686: invokestatic 1722	org/telegram/messenger/MessageObject:isStickerDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   2689: ifne +340 -> 3029
    //   2692: aload 31
    //   2694: astore_2
    //   2695: aload 31
    //   2697: aload 7
    //   2699: invokestatic 829	org/telegram/messenger/FileLoader:getPathToAttach	(Lorg/telegram/tgnet/TLObject;)Ljava/io/File;
    //   2702: invokevirtual 830	java/io/File:toString	()Ljava/lang/String;
    //   2705: putfield 878	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   2708: aload 31
    //   2710: astore 8
    //   2712: iload 21
    //   2714: istore 20
    //   2716: aload 13
    //   2718: astore 33
    //   2720: aload 34
    //   2722: astore 32
    //   2724: aload 39
    //   2726: ifnull -1151 -> 1575
    //   2729: aload 31
    //   2731: astore_2
    //   2732: aload 31
    //   2734: astore 8
    //   2736: iload 21
    //   2738: istore 20
    //   2740: aload 13
    //   2742: astore 33
    //   2744: aload 34
    //   2746: astore 32
    //   2748: aload 7
    //   2750: invokestatic 1722	org/telegram/messenger/MessageObject:isStickerDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   2753: ifeq -1178 -> 1575
    //   2756: iconst_0
    //   2757: istore 23
    //   2759: aload 31
    //   2761: astore_2
    //   2762: aload 31
    //   2764: astore 8
    //   2766: iload 21
    //   2768: istore 20
    //   2770: aload 13
    //   2772: astore 33
    //   2774: aload 34
    //   2776: astore 32
    //   2778: iload 23
    //   2780: aload 7
    //   2782: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   2785: invokevirtual 750	java/util/ArrayList:size	()I
    //   2788: if_icmpge -1213 -> 1575
    //   2791: aload 31
    //   2793: astore_2
    //   2794: aload 7
    //   2796: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   2799: iload 23
    //   2801: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   2804: checkcast 1724	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   2807: astore 8
    //   2809: aload 31
    //   2811: astore_2
    //   2812: aload 8
    //   2814: instanceof 1387
    //   2817: ifeq +7557 -> 10374
    //   2820: aload 31
    //   2822: astore_2
    //   2823: aload 7
    //   2825: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   2828: iload 23
    //   2830: invokevirtual 1727	java/util/ArrayList:remove	(I)Ljava/lang/Object;
    //   2833: pop
    //   2834: aload 31
    //   2836: astore_2
    //   2837: new 1729	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker_layer55
    //   2840: dup
    //   2841: invokespecial 1730	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker_layer55:<init>	()V
    //   2844: astore 18
    //   2846: aload 31
    //   2848: astore_2
    //   2849: aload 7
    //   2851: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   2854: aload 18
    //   2856: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   2859: pop
    //   2860: aload 31
    //   2862: astore_2
    //   2863: aload 18
    //   2865: aload 8
    //   2867: getfield 1731	org/telegram/tgnet/TLRPC$DocumentAttribute:alt	Ljava/lang/String;
    //   2870: putfield 1732	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker_layer55:alt	Ljava/lang/String;
    //   2873: aload 31
    //   2875: astore_2
    //   2876: aload 8
    //   2878: getfield 1733	org/telegram/tgnet/TLRPC$DocumentAttribute:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   2881: ifnull +221 -> 3102
    //   2884: aload 31
    //   2886: astore_2
    //   2887: aload 8
    //   2889: getfield 1733	org/telegram/tgnet/TLRPC$DocumentAttribute:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   2892: instanceof 1735
    //   2895: ifeq +147 -> 3042
    //   2898: aload 31
    //   2900: astore_2
    //   2901: aload 8
    //   2903: getfield 1733	org/telegram/tgnet/TLRPC$DocumentAttribute:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   2906: getfield 1740	org/telegram/tgnet/TLRPC$InputStickerSet:short_name	Ljava/lang/String;
    //   2909: astore 8
    //   2911: aload 31
    //   2913: astore_2
    //   2914: aload 8
    //   2916: invokestatic 1745	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   2919: ifne +149 -> 3068
    //   2922: aload 31
    //   2924: astore_2
    //   2925: aload 18
    //   2927: new 1735	org/telegram/tgnet/TLRPC$TL_inputStickerSetShortName
    //   2930: dup
    //   2931: invokespecial 1746	org/telegram/tgnet/TLRPC$TL_inputStickerSetShortName:<init>	()V
    //   2934: putfield 1747	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker_layer55:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   2937: aload 31
    //   2939: astore_2
    //   2940: aload 18
    //   2942: getfield 1747	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker_layer55:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   2945: aload 8
    //   2947: putfield 1740	org/telegram/tgnet/TLRPC$InputStickerSet:short_name	Ljava/lang/String;
    //   2950: aload 31
    //   2952: astore 8
    //   2954: iload 21
    //   2956: istore 20
    //   2958: aload 13
    //   2960: astore 33
    //   2962: aload 34
    //   2964: astore 32
    //   2966: goto -1391 -> 1575
    //   2969: new 1680	org/telegram/tgnet/TLRPC$TL_message
    //   2972: dup
    //   2973: invokespecial 1681	org/telegram/tgnet/TLRPC$TL_message:<init>	()V
    //   2976: astore 31
    //   2978: goto -496 -> 2482
    //   2981: aload 31
    //   2983: astore_2
    //   2984: aload 7
    //   2986: invokestatic 1750	org/telegram/messenger/MessageObject:isVideoDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   2989: ifne +7372 -> 10361
    //   2992: aload 31
    //   2994: astore_2
    //   2995: aload 7
    //   2997: invokestatic 1753	org/telegram/messenger/MessageObject:isRoundVideoDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   3000: ifne +7361 -> 10361
    //   3003: aload 5
    //   3005: ifnull +6 -> 3011
    //   3008: goto +7353 -> 10361
    //   3011: aload 31
    //   3013: astore_2
    //   3014: aload 7
    //   3016: invokestatic 1756	org/telegram/messenger/MessageObject:isVoiceDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   3019: ifeq +7348 -> 10367
    //   3022: bipush 8
    //   3024: istore 21
    //   3026: goto -415 -> 2611
    //   3029: aload 31
    //   3031: astore_2
    //   3032: aload 31
    //   3034: aload 11
    //   3036: putfield 878	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   3039: goto -331 -> 2708
    //   3042: aload 31
    //   3044: astore_2
    //   3045: aload_0
    //   3046: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3049: invokestatic 1761	org/telegram/messenger/DataQuery:getInstance	(I)Lorg/telegram/messenger/DataQuery;
    //   3052: aload 8
    //   3054: getfield 1733	org/telegram/tgnet/TLRPC$DocumentAttribute:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   3057: getfield 1762	org/telegram/tgnet/TLRPC$InputStickerSet:id	J
    //   3060: invokevirtual 1766	org/telegram/messenger/DataQuery:getStickerSetName	(J)Ljava/lang/String;
    //   3063: astore 8
    //   3065: goto -154 -> 2911
    //   3068: aload 31
    //   3070: astore_2
    //   3071: aload 18
    //   3073: new 1393	org/telegram/tgnet/TLRPC$TL_inputStickerSetEmpty
    //   3076: dup
    //   3077: invokespecial 1394	org/telegram/tgnet/TLRPC$TL_inputStickerSetEmpty:<init>	()V
    //   3080: putfield 1747	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker_layer55:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   3083: aload 31
    //   3085: astore 8
    //   3087: iload 21
    //   3089: istore 20
    //   3091: aload 13
    //   3093: astore 33
    //   3095: aload 34
    //   3097: astore 32
    //   3099: goto -1524 -> 1575
    //   3102: aload 31
    //   3104: astore_2
    //   3105: aload 18
    //   3107: new 1393	org/telegram/tgnet/TLRPC$TL_inputStickerSetEmpty
    //   3110: dup
    //   3111: invokespecial 1394	org/telegram/tgnet/TLRPC$TL_inputStickerSetEmpty:<init>	()V
    //   3114: putfield 1747	org/telegram/tgnet/TLRPC$TL_documentAttributeSticker_layer55:stickerset	Lorg/telegram/tgnet/TLRPC$InputStickerSet;
    //   3117: aload 31
    //   3119: astore 8
    //   3121: iload 21
    //   3123: istore 20
    //   3125: aload 13
    //   3127: astore 33
    //   3129: aload 34
    //   3131: astore 32
    //   3133: goto -1558 -> 1575
    //   3136: aload 8
    //   3138: astore_2
    //   3139: aload 8
    //   3141: getfield 1629	org/telegram/tgnet/TLRPC$Message:message	Ljava/lang/String;
    //   3144: ifnonnull -1511 -> 1633
    //   3147: aload 8
    //   3149: astore_2
    //   3150: aload 8
    //   3152: ldc_w 1168
    //   3155: putfield 1629	org/telegram/tgnet/TLRPC$Message:message	Ljava/lang/String;
    //   3158: goto -1525 -> 1633
    //   3161: aload 8
    //   3163: astore_2
    //   3164: aload 8
    //   3166: aload_0
    //   3167: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3170: invokestatic 1664	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   3173: invokevirtual 1769	org/telegram/messenger/UserConfig:getClientUserId	()I
    //   3176: putfield 1674	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   3179: aload 8
    //   3181: astore_2
    //   3182: aload 8
    //   3184: aload 8
    //   3186: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   3189: sipush 256
    //   3192: ior
    //   3193: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   3196: goto -1473 -> 1723
    //   3199: aload 8
    //   3201: astore_2
    //   3202: aload 8
    //   3204: aload 41
    //   3206: ldc_w 1589
    //   3209: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   3212: checkcast 489	java/lang/String
    //   3215: invokestatic 689	org/telegram/messenger/Utilities:parseInt	(Ljava/lang/String;)Ljava/lang/Integer;
    //   3218: invokevirtual 692	java/lang/Integer:intValue	()I
    //   3221: putfield 1772	org/telegram/tgnet/TLRPC$Message:via_bot_id	I
    //   3224: goto -2817 -> 407
    //   3227: aload 8
    //   3229: astore_2
    //   3230: aload 8
    //   3232: iconst_1
    //   3233: putfield 1775	org/telegram/tgnet/TLRPC$Message:post	Z
    //   3236: aload 8
    //   3238: astore_2
    //   3239: aload_1
    //   3240: getfield 1778	org/telegram/tgnet/TLRPC$Chat:signatures	Z
    //   3243: ifeq -2671 -> 572
    //   3246: aload 8
    //   3248: astore_2
    //   3249: aload 8
    //   3251: aload_0
    //   3252: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3255: invokestatic 1664	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   3258: invokevirtual 1769	org/telegram/messenger/UserConfig:getClientUserId	()I
    //   3261: putfield 1674	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   3264: goto -2692 -> 572
    //   3267: aload 8
    //   3269: astore_2
    //   3270: aload 8
    //   3272: iconst_1
    //   3273: putfield 1608	org/telegram/tgnet/TLRPC$Message:unread	Z
    //   3276: goto -2704 -> 572
    //   3279: aload 8
    //   3281: astore_2
    //   3282: aload 8
    //   3284: aload 8
    //   3286: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   3289: bipush 8
    //   3291: ior
    //   3292: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   3295: goto -2638 -> 657
    //   3298: aload 8
    //   3300: astore_2
    //   3301: new 749	java/util/ArrayList
    //   3304: dup
    //   3305: invokespecial 1216	java/util/ArrayList:<init>	()V
    //   3308: astore_3
    //   3309: aload_0
    //   3310: getfield 227	org/telegram/messenger/SendMessagesHelper:currentChatInfo	Lorg/telegram/tgnet/TLRPC$ChatFull;
    //   3313: getfield 1784	org/telegram/tgnet/TLRPC$ChatFull:participants	Lorg/telegram/tgnet/TLRPC$ChatParticipants;
    //   3316: getfield 1788	org/telegram/tgnet/TLRPC$ChatParticipants:participants	Ljava/util/ArrayList;
    //   3319: invokevirtual 1789	java/util/ArrayList:iterator	()Ljava/util/Iterator;
    //   3322: astore_1
    //   3323: aload_1
    //   3324: invokeinterface 738 1 0
    //   3329: ifeq +56 -> 3385
    //   3332: aload_1
    //   3333: invokeinterface 742 1 0
    //   3338: checkcast 1791	org/telegram/tgnet/TLRPC$ChatParticipant
    //   3341: astore_2
    //   3342: aload_0
    //   3343: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3346: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   3349: aload_2
    //   3350: getfield 1792	org/telegram/tgnet/TLRPC$ChatParticipant:user_id	I
    //   3353: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   3356: invokevirtual 1796	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   3359: astore_2
    //   3360: aload_0
    //   3361: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3364: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   3367: aload_2
    //   3368: invokevirtual 1800	org/telegram/messenger/MessagesController:getInputUser	(Lorg/telegram/tgnet/TLRPC$User;)Lorg/telegram/tgnet/TLRPC$InputUser;
    //   3371: astore_2
    //   3372: aload_2
    //   3373: ifnull -50 -> 3323
    //   3376: aload_3
    //   3377: aload_2
    //   3378: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   3381: pop
    //   3382: goto -59 -> 3323
    //   3385: aload 8
    //   3387: new 1802	org/telegram/tgnet/TLRPC$TL_peerChat
    //   3390: dup
    //   3391: invokespecial 1803	org/telegram/tgnet/TLRPC$TL_peerChat:<init>	()V
    //   3394: putfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   3397: aload 8
    //   3399: getfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   3402: iload 25
    //   3404: putfield 1812	org/telegram/tgnet/TLRPC$Peer:chat_id	I
    //   3407: iload 24
    //   3409: iconst_1
    //   3410: if_icmpeq +34 -> 3444
    //   3413: aload 8
    //   3415: astore_2
    //   3416: aload 8
    //   3418: invokestatic 1816	org/telegram/messenger/MessageObject:isVoiceMessage	(Lorg/telegram/tgnet/TLRPC$Message;)Z
    //   3421: ifne +14 -> 3435
    //   3424: aload 8
    //   3426: astore_2
    //   3427: aload 8
    //   3429: invokestatic 1819	org/telegram/messenger/MessageObject:isRoundVideoMessage	(Lorg/telegram/tgnet/TLRPC$Message;)Z
    //   3432: ifeq +12 -> 3444
    //   3435: aload 8
    //   3437: astore_2
    //   3438: aload 8
    //   3440: iconst_1
    //   3441: putfield 1822	org/telegram/tgnet/TLRPC$Message:media_unread	Z
    //   3444: aload 8
    //   3446: astore_2
    //   3447: aload 8
    //   3449: iconst_1
    //   3450: putfield 1547	org/telegram/tgnet/TLRPC$Message:send_state	I
    //   3453: aload 8
    //   3455: astore_2
    //   3456: new 763	org/telegram/messenger/MessageObject
    //   3459: dup
    //   3460: aload_0
    //   3461: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3464: aload 8
    //   3466: iconst_1
    //   3467: invokespecial 1825	org/telegram/messenger/MessageObject:<init>	(ILorg/telegram/tgnet/TLRPC$Message;Z)V
    //   3470: astore 13
    //   3472: aload 13
    //   3474: aload 12
    //   3476: putfield 1828	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   3479: aload 13
    //   3481: invokevirtual 1581	org/telegram/messenger/MessageObject:isForwarded	()Z
    //   3484: ifne +43 -> 3527
    //   3487: aload 13
    //   3489: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   3492: iconst_3
    //   3493: if_icmpeq +17 -> 3510
    //   3496: aload 5
    //   3498: ifnonnull +12 -> 3510
    //   3501: aload 13
    //   3503: getfield 1621	org/telegram/messenger/MessageObject:type	I
    //   3506: iconst_2
    //   3507: if_icmpne +20 -> 3527
    //   3510: aload 8
    //   3512: getfield 878	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   3515: invokestatic 1745	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   3518: ifne +9 -> 3527
    //   3521: aload 13
    //   3523: iconst_1
    //   3524: putfield 1831	org/telegram/messenger/MessageObject:attachPathExists	Z
    //   3527: aload 5
    //   3529: astore 7
    //   3531: aload 13
    //   3533: getfield 921	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   3536: ifnull +19 -> 3555
    //   3539: aload 5
    //   3541: astore 7
    //   3543: aload 5
    //   3545: ifnonnull +10 -> 3555
    //   3548: aload 13
    //   3550: getfield 921	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   3553: astore 7
    //   3555: lconst_0
    //   3556: lstore 26
    //   3558: iconst_0
    //   3559: istore 20
    //   3561: lload 26
    //   3563: lstore 28
    //   3565: aload 41
    //   3567: ifnull +67 -> 3634
    //   3570: aload 41
    //   3572: ldc_w 1833
    //   3575: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   3578: checkcast 489	java/lang/String
    //   3581: astore_1
    //   3582: aload_1
    //   3583: ifnull +33 -> 3616
    //   3586: aload_1
    //   3587: invokestatic 1836	org/telegram/messenger/Utilities:parseLong	(Ljava/lang/String;)Ljava/lang/Long;
    //   3590: invokevirtual 1839	java/lang/Long:longValue	()J
    //   3593: lstore 26
    //   3595: aload 8
    //   3597: lload 26
    //   3599: putfield 1842	org/telegram/tgnet/TLRPC$Message:grouped_id	J
    //   3602: aload 8
    //   3604: aload 8
    //   3606: getfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   3609: ldc_w 1843
    //   3612: ior
    //   3613: putfield 1595	org/telegram/tgnet/TLRPC$Message:flags	I
    //   3616: aload 41
    //   3618: ldc_w 1845
    //   3621: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   3624: ifnull +784 -> 4408
    //   3627: iconst_1
    //   3628: istore 20
    //   3630: lload 26
    //   3632: lstore 28
    //   3634: lload 28
    //   3636: lconst_0
    //   3637: lcmp
    //   3638: ifne +780 -> 4418
    //   3641: new 749	java/util/ArrayList
    //   3644: dup
    //   3645: invokespecial 1216	java/util/ArrayList:<init>	()V
    //   3648: astore_1
    //   3649: aload_1
    //   3650: aload 13
    //   3652: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   3655: pop
    //   3656: new 749	java/util/ArrayList
    //   3659: dup
    //   3660: invokespecial 1216	java/util/ArrayList:<init>	()V
    //   3663: astore_2
    //   3664: aload_2
    //   3665: aload 8
    //   3667: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   3670: pop
    //   3671: aload_0
    //   3672: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3675: invokestatic 1284	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
    //   3678: aload_2
    //   3679: iconst_0
    //   3680: iconst_1
    //   3681: iconst_0
    //   3682: iconst_0
    //   3683: invokevirtual 1849	org/telegram/messenger/MessagesStorage:putMessages	(Ljava/util/ArrayList;ZZZI)V
    //   3686: aload_0
    //   3687: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3690: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   3693: lload 9
    //   3695: aload_1
    //   3696: invokevirtual 1853	org/telegram/messenger/MessagesController:updateInterfaceWithMessages	(JLjava/util/ArrayList;)V
    //   3699: aload_0
    //   3700: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3703: invokestatic 1552	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   3706: getstatic 1856	org/telegram/messenger/NotificationCenter:dialogsNeedReload	I
    //   3709: iconst_0
    //   3710: anewarray 4	java/lang/Object
    //   3713: invokevirtual 1559	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   3716: aconst_null
    //   3717: astore_1
    //   3718: aload_1
    //   3719: astore 5
    //   3721: getstatic 359	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   3724: ifeq +6665 -> 10389
    //   3727: aload 38
    //   3729: ifnull +6660 -> 10389
    //   3732: aload_1
    //   3733: astore 5
    //   3735: new 507	java/lang/StringBuilder
    //   3738: dup
    //   3739: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   3742: ldc_w 1858
    //   3745: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   3748: aload 38
    //   3750: getfield 1859	org/telegram/tgnet/TLRPC$InputPeer:user_id	I
    //   3753: invokevirtual 976	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   3756: ldc_w 1861
    //   3759: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   3762: aload 38
    //   3764: getfield 1862	org/telegram/tgnet/TLRPC$InputPeer:chat_id	I
    //   3767: invokevirtual 976	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   3770: ldc_w 1864
    //   3773: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   3776: aload 38
    //   3778: getfield 1569	org/telegram/tgnet/TLRPC$InputPeer:channel_id	I
    //   3781: invokevirtual 976	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   3784: ldc_w 1866
    //   3787: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   3790: aload 38
    //   3792: getfield 1869	org/telegram/tgnet/TLRPC$InputPeer:access_hash	J
    //   3795: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   3798: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   3801: invokestatic 366	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   3804: goto +6585 -> 10389
    //   3807: aload 39
    //   3809: ifnonnull +1040 -> 4849
    //   3812: aload_3
    //   3813: ifnull +800 -> 4613
    //   3816: aload_1
    //   3817: astore 5
    //   3819: new 929	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast
    //   3822: dup
    //   3823: invokespecial 1870	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:<init>	()V
    //   3826: astore_2
    //   3827: aload_1
    //   3828: astore 5
    //   3830: new 749	java/util/ArrayList
    //   3833: dup
    //   3834: invokespecial 1216	java/util/ArrayList:<init>	()V
    //   3837: astore 4
    //   3839: iconst_0
    //   3840: istore 19
    //   3842: aload_1
    //   3843: astore 5
    //   3845: iload 19
    //   3847: aload_3
    //   3848: invokevirtual 750	java/util/ArrayList:size	()I
    //   3851: if_icmpge +710 -> 4561
    //   3854: aload_1
    //   3855: astore 5
    //   3857: aload 4
    //   3859: getstatic 1874	org/telegram/messenger/Utilities:random	Ljava/security/SecureRandom;
    //   3862: invokevirtual 1879	java/security/SecureRandom:nextLong	()J
    //   3865: invokestatic 1882	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   3868: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   3871: pop
    //   3872: iload 19
    //   3874: iconst_1
    //   3875: iadd
    //   3876: istore 19
    //   3878: goto -36 -> 3842
    //   3881: aload 8
    //   3883: astore_2
    //   3884: aload 8
    //   3886: aload_0
    //   3887: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3890: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   3893: iload 25
    //   3895: invokevirtual 1886	org/telegram/messenger/MessagesController:getPeer	(I)Lorg/telegram/tgnet/TLRPC$Peer;
    //   3898: putfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   3901: aload 45
    //   3903: astore_3
    //   3904: iload 25
    //   3906: ifle -499 -> 3407
    //   3909: aload 8
    //   3911: astore_2
    //   3912: aload_0
    //   3913: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   3916: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   3919: iload 25
    //   3921: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   3924: invokevirtual 1796	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   3927: astore_1
    //   3928: aload_1
    //   3929: ifnonnull +16 -> 3945
    //   3932: aload 8
    //   3934: astore_2
    //   3935: aload_0
    //   3936: aload 8
    //   3938: getfield 1620	org/telegram/tgnet/TLRPC$Message:id	I
    //   3941: invokevirtual 1562	org/telegram/messenger/SendMessagesHelper:processSentMessage	(I)V
    //   3944: return
    //   3945: aload 8
    //   3947: astore_2
    //   3948: aload 45
    //   3950: astore_3
    //   3951: aload_1
    //   3952: getfield 1888	org/telegram/tgnet/TLRPC$User:bot	Z
    //   3955: ifeq -548 -> 3407
    //   3958: aload 8
    //   3960: astore_2
    //   3961: aload 8
    //   3963: iconst_0
    //   3964: putfield 1608	org/telegram/tgnet/TLRPC$Message:unread	Z
    //   3967: aload 45
    //   3969: astore_3
    //   3970: goto -563 -> 3407
    //   3973: aload 8
    //   3975: astore_2
    //   3976: aload 8
    //   3978: new 1890	org/telegram/tgnet/TLRPC$TL_peerUser
    //   3981: dup
    //   3982: invokespecial 1891	org/telegram/tgnet/TLRPC$TL_peerUser:<init>	()V
    //   3985: putfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   3988: aload 8
    //   3990: astore_2
    //   3991: aload 39
    //   3993: getfield 1896	org/telegram/tgnet/TLRPC$EncryptedChat:participant_id	I
    //   3996: aload_0
    //   3997: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   4000: invokestatic 1664	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   4003: invokevirtual 1769	org/telegram/messenger/UserConfig:getClientUserId	()I
    //   4006: if_icmpne +176 -> 4182
    //   4009: aload 8
    //   4011: astore_2
    //   4012: aload 8
    //   4014: getfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   4017: aload 39
    //   4019: getfield 1899	org/telegram/tgnet/TLRPC$EncryptedChat:admin_id	I
    //   4022: putfield 1900	org/telegram/tgnet/TLRPC$Peer:user_id	I
    //   4025: iload 19
    //   4027: ifeq +174 -> 4201
    //   4030: aload 8
    //   4032: astore_2
    //   4033: aload 8
    //   4035: iload 19
    //   4037: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4040: aload 8
    //   4042: astore_2
    //   4043: aload 45
    //   4045: astore_3
    //   4046: aload 8
    //   4048: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4051: ifeq -644 -> 3407
    //   4054: aload 8
    //   4056: astore_2
    //   4057: aload 45
    //   4059: astore_3
    //   4060: aload 8
    //   4062: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4065: getfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   4068: ifnull -661 -> 3407
    //   4071: aload 8
    //   4073: astore_2
    //   4074: aload 8
    //   4076: invokestatic 1816	org/telegram/messenger/MessageObject:isVoiceMessage	(Lorg/telegram/tgnet/TLRPC$Message;)Z
    //   4079: ifeq +198 -> 4277
    //   4082: iconst_0
    //   4083: istore 23
    //   4085: iconst_0
    //   4086: istore 20
    //   4088: aload 8
    //   4090: astore_2
    //   4091: iload 23
    //   4093: istore 22
    //   4095: iload 20
    //   4097: aload 8
    //   4099: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4102: getfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   4105: getfield 1901	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   4108: invokevirtual 750	java/util/ArrayList:size	()I
    //   4111: if_icmpge +45 -> 4156
    //   4114: aload 8
    //   4116: astore_2
    //   4117: aload 8
    //   4119: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4122: getfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   4125: getfield 1901	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   4128: iload 20
    //   4130: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   4133: checkcast 1724	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   4136: astore_1
    //   4137: aload 8
    //   4139: astore_2
    //   4140: aload_1
    //   4141: instanceof 1265
    //   4144: ifeq +6270 -> 10414
    //   4147: aload 8
    //   4149: astore_2
    //   4150: aload_1
    //   4151: getfield 1902	org/telegram/tgnet/TLRPC$DocumentAttribute:duration	I
    //   4154: istore 22
    //   4156: aload 8
    //   4158: astore_2
    //   4159: aload 8
    //   4161: aload 8
    //   4163: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4166: iload 22
    //   4168: iconst_1
    //   4169: iadd
    //   4170: invokestatic 647	java/lang/Math:max	(II)I
    //   4173: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4176: aload 45
    //   4178: astore_3
    //   4179: goto -772 -> 3407
    //   4182: aload 8
    //   4184: astore_2
    //   4185: aload 8
    //   4187: getfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   4190: aload 39
    //   4192: getfield 1896	org/telegram/tgnet/TLRPC$EncryptedChat:participant_id	I
    //   4195: putfield 1900	org/telegram/tgnet/TLRPC$Peer:user_id	I
    //   4198: goto -173 -> 4025
    //   4201: aload 8
    //   4203: astore_2
    //   4204: aload 8
    //   4206: aload 39
    //   4208: getfield 1903	org/telegram/tgnet/TLRPC$EncryptedChat:ttl	I
    //   4211: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4214: aload 8
    //   4216: astore_2
    //   4217: aload 8
    //   4219: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4222: ifeq -182 -> 4040
    //   4225: aload 8
    //   4227: astore_2
    //   4228: aload 8
    //   4230: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4233: ifnull -193 -> 4040
    //   4236: aload 8
    //   4238: astore_2
    //   4239: aload 8
    //   4241: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4244: aload 8
    //   4246: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4249: putfield 1695	org/telegram/tgnet/TLRPC$MessageMedia:ttl_seconds	I
    //   4252: aload 8
    //   4254: astore_2
    //   4255: aload 8
    //   4257: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4260: astore_1
    //   4261: aload 8
    //   4263: astore_2
    //   4264: aload_1
    //   4265: aload_1
    //   4266: getfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   4269: iconst_4
    //   4270: ior
    //   4271: putfield 1692	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   4274: goto -234 -> 4040
    //   4277: aload 8
    //   4279: astore_2
    //   4280: aload 8
    //   4282: invokestatic 1906	org/telegram/messenger/MessageObject:isVideoMessage	(Lorg/telegram/tgnet/TLRPC$Message;)Z
    //   4285: ifne +6138 -> 10423
    //   4288: aload 8
    //   4290: astore_2
    //   4291: aload 45
    //   4293: astore_3
    //   4294: aload 8
    //   4296: invokestatic 1819	org/telegram/messenger/MessageObject:isRoundVideoMessage	(Lorg/telegram/tgnet/TLRPC$Message;)Z
    //   4299: ifeq -892 -> 3407
    //   4302: goto +6121 -> 10423
    //   4305: aload 8
    //   4307: astore_2
    //   4308: iload 23
    //   4310: istore 22
    //   4312: iload 20
    //   4314: aload 8
    //   4316: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4319: getfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   4322: getfield 1901	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   4325: invokevirtual 750	java/util/ArrayList:size	()I
    //   4328: if_icmpge +45 -> 4373
    //   4331: aload 8
    //   4333: astore_2
    //   4334: aload 8
    //   4336: getfield 1624	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4339: getfield 1641	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   4342: getfield 1901	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   4345: iload 20
    //   4347: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   4350: checkcast 1724	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   4353: astore_1
    //   4354: aload 8
    //   4356: astore_2
    //   4357: aload_1
    //   4358: instanceof 669
    //   4361: ifeq +38 -> 4399
    //   4364: aload 8
    //   4366: astore_2
    //   4367: aload_1
    //   4368: getfield 1902	org/telegram/tgnet/TLRPC$DocumentAttribute:duration	I
    //   4371: istore 22
    //   4373: aload 8
    //   4375: astore_2
    //   4376: aload 8
    //   4378: aload 8
    //   4380: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4383: iload 22
    //   4385: iconst_1
    //   4386: iadd
    //   4387: invokestatic 647	java/lang/Math:max	(II)I
    //   4390: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4393: aload 45
    //   4395: astore_3
    //   4396: goto -989 -> 3407
    //   4399: iload 20
    //   4401: iconst_1
    //   4402: iadd
    //   4403: istore 20
    //   4405: goto -100 -> 4305
    //   4408: iconst_0
    //   4409: istore 20
    //   4411: lload 26
    //   4413: lstore 28
    //   4415: goto -781 -> 3634
    //   4418: new 507	java/lang/StringBuilder
    //   4421: dup
    //   4422: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   4425: ldc_w 1908
    //   4428: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   4431: lload 28
    //   4433: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   4436: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   4439: astore_1
    //   4440: aload_0
    //   4441: getfield 232	org/telegram/messenger/SendMessagesHelper:delayedMessages	Ljava/util/HashMap;
    //   4444: aload_1
    //   4445: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   4448: checkcast 749	java/util/ArrayList
    //   4451: astore_1
    //   4452: aload_1
    //   4453: ifnull +5821 -> 10274
    //   4456: aload_1
    //   4457: iconst_0
    //   4458: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   4461: checkcast 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   4464: astore_1
    //   4465: aload_1
    //   4466: ifnonnull +5805 -> 10271
    //   4469: aload_1
    //   4470: astore 5
    //   4472: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   4475: dup
    //   4476: aload_0
    //   4477: lload 9
    //   4479: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   4482: astore_1
    //   4483: aload_1
    //   4484: iconst_4
    //   4485: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   4488: aload_1
    //   4489: lload 28
    //   4491: putfield 1913	org/telegram/messenger/SendMessagesHelper$DelayedMessage:groupId	J
    //   4494: aload_1
    //   4495: new 749	java/util/ArrayList
    //   4498: dup
    //   4499: invokespecial 1216	java/util/ArrayList:<init>	()V
    //   4502: putfield 770	org/telegram/messenger/SendMessagesHelper$DelayedMessage:messageObjects	Ljava/util/ArrayList;
    //   4505: aload_1
    //   4506: new 749	java/util/ArrayList
    //   4509: dup
    //   4510: invokespecial 1216	java/util/ArrayList:<init>	()V
    //   4513: putfield 1916	org/telegram/messenger/SendMessagesHelper$DelayedMessage:messages	Ljava/util/ArrayList;
    //   4516: aload_1
    //   4517: new 749	java/util/ArrayList
    //   4520: dup
    //   4521: invokespecial 1216	java/util/ArrayList:<init>	()V
    //   4524: putfield 1919	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPaths	Ljava/util/ArrayList;
    //   4527: aload_1
    //   4528: new 229	java/util/HashMap
    //   4531: dup
    //   4532: invokespecial 230	java/util/HashMap:<init>	()V
    //   4535: putfield 995	org/telegram/messenger/SendMessagesHelper$DelayedMessage:extraHashMap	Ljava/util/HashMap;
    //   4538: aload_1
    //   4539: aload 39
    //   4541: putfield 960	org/telegram/messenger/SendMessagesHelper$DelayedMessage:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   4544: iload 20
    //   4546: ifeq +12 -> 4558
    //   4549: aload_1
    //   4550: aload 8
    //   4552: getfield 1620	org/telegram/tgnet/TLRPC$Message:id	I
    //   4555: putfield 1922	org/telegram/messenger/SendMessagesHelper$DelayedMessage:finalGroupMessage	I
    //   4558: goto -840 -> 3718
    //   4561: aload_1
    //   4562: astore 5
    //   4564: aload_2
    //   4565: aload 43
    //   4567: putfield 1923	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:message	Ljava/lang/String;
    //   4570: aload_1
    //   4571: astore 5
    //   4573: aload_2
    //   4574: aload_3
    //   4575: putfield 1926	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:contacts	Ljava/util/ArrayList;
    //   4578: aload_1
    //   4579: astore 5
    //   4581: aload_2
    //   4582: new 1928	org/telegram/tgnet/TLRPC$TL_inputMediaEmpty
    //   4585: dup
    //   4586: invokespecial 1929	org/telegram/tgnet/TLRPC$TL_inputMediaEmpty:<init>	()V
    //   4589: putfield 930	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:media	Lorg/telegram/tgnet/TLRPC$InputMedia;
    //   4592: aload_1
    //   4593: astore 5
    //   4595: aload_2
    //   4596: aload 4
    //   4598: putfield 1931	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:random_id	Ljava/util/ArrayList;
    //   4601: aload_1
    //   4602: astore 5
    //   4604: aload_0
    //   4605: aload_2
    //   4606: aload 13
    //   4608: aconst_null
    //   4609: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   4612: return
    //   4613: aload_1
    //   4614: astore 5
    //   4616: new 1055	org/telegram/tgnet/TLRPC$TL_messages_sendMessage
    //   4619: dup
    //   4620: invokespecial 1932	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:<init>	()V
    //   4623: astore_2
    //   4624: aload_1
    //   4625: astore 5
    //   4627: aload_2
    //   4628: aload 43
    //   4630: putfield 1933	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:message	Ljava/lang/String;
    //   4633: aload 15
    //   4635: ifnonnull +5798 -> 10433
    //   4638: iconst_1
    //   4639: istore 30
    //   4641: aload_1
    //   4642: astore 5
    //   4644: aload_2
    //   4645: iload 30
    //   4647: putfield 1936	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:clear_draft	Z
    //   4650: aload_1
    //   4651: astore 5
    //   4653: aload 8
    //   4655: getfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   4658: instanceof 1938
    //   4661: ifeq +44 -> 4705
    //   4664: aload_1
    //   4665: astore 5
    //   4667: aload_2
    //   4668: aload_0
    //   4669: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   4672: invokestatic 1942	org/telegram/messenger/MessagesController:getNotificationsSettings	(I)Landroid/content/SharedPreferences;
    //   4675: new 507	java/lang/StringBuilder
    //   4678: dup
    //   4679: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   4682: ldc_w 1944
    //   4685: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   4688: lload 9
    //   4690: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   4693: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   4696: iconst_0
    //   4697: invokeinterface 1948 3 0
    //   4702: putfield 1951	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:silent	Z
    //   4705: aload_1
    //   4706: astore 5
    //   4708: aload_2
    //   4709: aload 38
    //   4711: putfield 1954	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   4714: aload_1
    //   4715: astore 5
    //   4717: aload_2
    //   4718: aload 8
    //   4720: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   4723: putfield 1955	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:random_id	J
    //   4726: aload_1
    //   4727: astore 5
    //   4729: aload 8
    //   4731: getfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   4734: ifeq +28 -> 4762
    //   4737: aload_1
    //   4738: astore 5
    //   4740: aload_2
    //   4741: aload_2
    //   4742: getfield 1956	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:flags	I
    //   4745: iconst_1
    //   4746: ior
    //   4747: putfield 1956	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:flags	I
    //   4750: aload_1
    //   4751: astore 5
    //   4753: aload_2
    //   4754: aload 8
    //   4756: getfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   4759: putfield 1957	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:reply_to_msg_id	I
    //   4762: iload 14
    //   4764: ifne +11 -> 4775
    //   4767: aload_1
    //   4768: astore 5
    //   4770: aload_2
    //   4771: iconst_1
    //   4772: putfield 1958	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:no_webpage	Z
    //   4775: aload 16
    //   4777: ifnull +37 -> 4814
    //   4780: aload_1
    //   4781: astore 5
    //   4783: aload 16
    //   4785: invokevirtual 773	java/util/ArrayList:isEmpty	()Z
    //   4788: ifne +26 -> 4814
    //   4791: aload_1
    //   4792: astore 5
    //   4794: aload_2
    //   4795: aload 16
    //   4797: putfield 1959	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:entities	Ljava/util/ArrayList;
    //   4800: aload_1
    //   4801: astore 5
    //   4803: aload_2
    //   4804: aload_2
    //   4805: getfield 1956	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:flags	I
    //   4808: bipush 8
    //   4810: ior
    //   4811: putfield 1956	org/telegram/tgnet/TLRPC$TL_messages_sendMessage:flags	I
    //   4814: aload_1
    //   4815: astore 5
    //   4817: aload_0
    //   4818: aload_2
    //   4819: aload 13
    //   4821: aconst_null
    //   4822: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   4825: aload 15
    //   4827: ifnonnull +5605 -> 10432
    //   4830: aload_1
    //   4831: astore 5
    //   4833: aload_0
    //   4834: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   4837: invokestatic 1761	org/telegram/messenger/DataQuery:getInstance	(I)Lorg/telegram/messenger/DataQuery;
    //   4840: lload 9
    //   4842: iconst_0
    //   4843: invokevirtual 1963	org/telegram/messenger/DataQuery:cleanDraft	(JZ)V
    //   4846: goto +5586 -> 10432
    //   4849: aload_1
    //   4850: astore 5
    //   4852: aload 39
    //   4854: getfield 1966	org/telegram/tgnet/TLRPC$EncryptedChat:layer	I
    //   4857: invokestatic 1969	org/telegram/messenger/AndroidUtilities:getPeerLayerVersion	(I)I
    //   4860: bipush 73
    //   4862: if_icmplt +287 -> 5149
    //   4865: aload_1
    //   4866: astore 5
    //   4868: new 936	org/telegram/tgnet/TLRPC$TL_decryptedMessage
    //   4871: dup
    //   4872: invokespecial 1970	org/telegram/tgnet/TLRPC$TL_decryptedMessage:<init>	()V
    //   4875: astore_2
    //   4876: aload_1
    //   4877: astore 5
    //   4879: aload_2
    //   4880: aload 8
    //   4882: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   4885: putfield 1971	org/telegram/tgnet/TLRPC$TL_decryptedMessage:ttl	I
    //   4888: aload 16
    //   4890: ifnull +38 -> 4928
    //   4893: aload_1
    //   4894: astore 5
    //   4896: aload 16
    //   4898: invokevirtual 773	java/util/ArrayList:isEmpty	()Z
    //   4901: ifne +27 -> 4928
    //   4904: aload_1
    //   4905: astore 5
    //   4907: aload_2
    //   4908: aload 16
    //   4910: putfield 1972	org/telegram/tgnet/TLRPC$TL_decryptedMessage:entities	Ljava/util/ArrayList;
    //   4913: aload_1
    //   4914: astore 5
    //   4916: aload_2
    //   4917: aload_2
    //   4918: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   4921: sipush 128
    //   4924: ior
    //   4925: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   4928: aload_1
    //   4929: astore 5
    //   4931: aload 8
    //   4933: getfield 1614	org/telegram/tgnet/TLRPC$Message:reply_to_random_id	J
    //   4936: lconst_0
    //   4937: lcmp
    //   4938: ifeq +29 -> 4967
    //   4941: aload_1
    //   4942: astore 5
    //   4944: aload_2
    //   4945: aload 8
    //   4947: getfield 1614	org/telegram/tgnet/TLRPC$Message:reply_to_random_id	J
    //   4950: putfield 1974	org/telegram/tgnet/TLRPC$TL_decryptedMessage:reply_to_random_id	J
    //   4953: aload_1
    //   4954: astore 5
    //   4956: aload_2
    //   4957: aload_2
    //   4958: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   4961: bipush 8
    //   4963: ior
    //   4964: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   4967: aload 41
    //   4969: ifnull +50 -> 5019
    //   4972: aload_1
    //   4973: astore 5
    //   4975: aload 41
    //   4977: ldc_w 1591
    //   4980: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   4983: ifnull +36 -> 5019
    //   4986: aload_1
    //   4987: astore 5
    //   4989: aload_2
    //   4990: aload 41
    //   4992: ldc_w 1591
    //   4995: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   4998: checkcast 489	java/lang/String
    //   5001: putfield 1975	org/telegram/tgnet/TLRPC$TL_decryptedMessage:via_bot_name	Ljava/lang/String;
    //   5004: aload_1
    //   5005: astore 5
    //   5007: aload_2
    //   5008: aload_2
    //   5009: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   5012: sipush 2048
    //   5015: ior
    //   5016: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   5019: aload_1
    //   5020: astore 5
    //   5022: aload_2
    //   5023: aload 8
    //   5025: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   5028: putfield 1976	org/telegram/tgnet/TLRPC$TL_decryptedMessage:random_id	J
    //   5031: aload_1
    //   5032: astore 5
    //   5034: aload_2
    //   5035: aload 43
    //   5037: putfield 1977	org/telegram/tgnet/TLRPC$TL_decryptedMessage:message	Ljava/lang/String;
    //   5040: aload 44
    //   5042: ifnull +121 -> 5163
    //   5045: aload_1
    //   5046: astore 5
    //   5048: aload 44
    //   5050: getfield 1127	org/telegram/tgnet/TLRPC$WebPage:url	Ljava/lang/String;
    //   5053: ifnull +110 -> 5163
    //   5056: aload_1
    //   5057: astore 5
    //   5059: aload_2
    //   5060: new 1979	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaWebPage
    //   5063: dup
    //   5064: invokespecial 1980	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaWebPage:<init>	()V
    //   5067: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   5070: aload_1
    //   5071: astore 5
    //   5073: aload_2
    //   5074: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   5077: aload 44
    //   5079: getfield 1127	org/telegram/tgnet/TLRPC$WebPage:url	Ljava/lang/String;
    //   5082: putfield 1981	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:url	Ljava/lang/String;
    //   5085: aload_1
    //   5086: astore 5
    //   5088: aload_2
    //   5089: aload_2
    //   5090: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   5093: sipush 512
    //   5096: ior
    //   5097: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   5100: aload_1
    //   5101: astore 5
    //   5103: aload_0
    //   5104: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   5107: invokestatic 956	org/telegram/messenger/SecretChatHelper:getInstance	(I)Lorg/telegram/messenger/SecretChatHelper;
    //   5110: aload_2
    //   5111: aload 13
    //   5113: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   5116: aload 39
    //   5118: aconst_null
    //   5119: aconst_null
    //   5120: aload 13
    //   5122: invokevirtual 965	org/telegram/messenger/SecretChatHelper:performSendEncryptedRequest	(Lorg/telegram/tgnet/TLRPC$DecryptedMessage;Lorg/telegram/tgnet/TLRPC$Message;Lorg/telegram/tgnet/TLRPC$EncryptedChat;Lorg/telegram/tgnet/TLRPC$InputEncryptedFile;Ljava/lang/String;Lorg/telegram/messenger/MessageObject;)V
    //   5125: aload 15
    //   5127: ifnonnull +5312 -> 10439
    //   5130: aload_1
    //   5131: astore 5
    //   5133: aload_0
    //   5134: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   5137: invokestatic 1761	org/telegram/messenger/DataQuery:getInstance	(I)Lorg/telegram/messenger/DataQuery;
    //   5140: lload 9
    //   5142: iconst_0
    //   5143: invokevirtual 1963	org/telegram/messenger/DataQuery:cleanDraft	(JZ)V
    //   5146: goto +5293 -> 10439
    //   5149: aload_1
    //   5150: astore 5
    //   5152: new 1983	org/telegram/tgnet/TLRPC$TL_decryptedMessage_layer45
    //   5155: dup
    //   5156: invokespecial 1984	org/telegram/tgnet/TLRPC$TL_decryptedMessage_layer45:<init>	()V
    //   5159: astore_2
    //   5160: goto -284 -> 4876
    //   5163: aload_1
    //   5164: astore 5
    //   5166: aload_2
    //   5167: new 1986	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaEmpty
    //   5170: dup
    //   5171: invokespecial 1987	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaEmpty:<init>	()V
    //   5174: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   5177: goto -77 -> 5100
    //   5180: aload 39
    //   5182: ifnonnull +1977 -> 7159
    //   5185: aconst_null
    //   5186: astore_2
    //   5187: iload 21
    //   5189: iconst_1
    //   5190: if_icmpne +5297 -> 10487
    //   5193: aload_1
    //   5194: astore 5
    //   5196: aload 42
    //   5198: instanceof 1144
    //   5201: ifeq +180 -> 5381
    //   5204: aload_1
    //   5205: astore 5
    //   5207: new 1989	org/telegram/tgnet/TLRPC$TL_inputMediaVenue
    //   5210: dup
    //   5211: invokespecial 1990	org/telegram/tgnet/TLRPC$TL_inputMediaVenue:<init>	()V
    //   5214: astore_2
    //   5215: aload_1
    //   5216: astore 5
    //   5218: aload_2
    //   5219: aload 42
    //   5221: getfield 1991	org/telegram/tgnet/TLRPC$MessageMedia:address	Ljava/lang/String;
    //   5224: putfield 1992	org/telegram/tgnet/TLRPC$InputMedia:address	Ljava/lang/String;
    //   5227: aload_1
    //   5228: astore 5
    //   5230: aload_2
    //   5231: aload 42
    //   5233: getfield 1993	org/telegram/tgnet/TLRPC$MessageMedia:title	Ljava/lang/String;
    //   5236: putfield 1994	org/telegram/tgnet/TLRPC$InputMedia:title	Ljava/lang/String;
    //   5239: aload_1
    //   5240: astore 5
    //   5242: aload_2
    //   5243: aload 42
    //   5245: getfield 1995	org/telegram/tgnet/TLRPC$MessageMedia:provider	Ljava/lang/String;
    //   5248: putfield 1996	org/telegram/tgnet/TLRPC$InputMedia:provider	Ljava/lang/String;
    //   5251: aload_1
    //   5252: astore 5
    //   5254: aload_2
    //   5255: aload 42
    //   5257: getfield 1997	org/telegram/tgnet/TLRPC$MessageMedia:venue_id	Ljava/lang/String;
    //   5260: putfield 1998	org/telegram/tgnet/TLRPC$InputMedia:venue_id	Ljava/lang/String;
    //   5263: aload_1
    //   5264: astore 5
    //   5266: aload_2
    //   5267: ldc_w 1168
    //   5270: putfield 1999	org/telegram/tgnet/TLRPC$InputMedia:venue_type	Ljava/lang/String;
    //   5273: aload_1
    //   5274: astore 5
    //   5276: aload_2
    //   5277: new 2001	org/telegram/tgnet/TLRPC$TL_inputGeoPoint
    //   5280: dup
    //   5281: invokespecial 2002	org/telegram/tgnet/TLRPC$TL_inputGeoPoint:<init>	()V
    //   5284: putfield 2006	org/telegram/tgnet/TLRPC$InputMedia:geo_point	Lorg/telegram/tgnet/TLRPC$InputGeoPoint;
    //   5287: aload_1
    //   5288: astore 5
    //   5290: aload_2
    //   5291: getfield 2006	org/telegram/tgnet/TLRPC$InputMedia:geo_point	Lorg/telegram/tgnet/TLRPC$InputGeoPoint;
    //   5294: aload 42
    //   5296: getfield 2007	org/telegram/tgnet/TLRPC$MessageMedia:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
    //   5299: getfield 1516	org/telegram/tgnet/TLRPC$GeoPoint:lat	D
    //   5302: putfield 2010	org/telegram/tgnet/TLRPC$InputGeoPoint:lat	D
    //   5305: aload_1
    //   5306: astore 5
    //   5308: aload_2
    //   5309: getfield 2006	org/telegram/tgnet/TLRPC$InputMedia:geo_point	Lorg/telegram/tgnet/TLRPC$InputGeoPoint;
    //   5312: aload 42
    //   5314: getfield 2007	org/telegram/tgnet/TLRPC$MessageMedia:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
    //   5317: getfield 1522	org/telegram/tgnet/TLRPC$GeoPoint:_long	D
    //   5320: putfield 2011	org/telegram/tgnet/TLRPC$InputGeoPoint:_long	D
    //   5323: aload_3
    //   5324: ifnull +1290 -> 6614
    //   5327: new 929	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast
    //   5330: dup
    //   5331: invokespecial 1870	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:<init>	()V
    //   5334: astore 5
    //   5336: new 749	java/util/ArrayList
    //   5339: dup
    //   5340: invokespecial 1216	java/util/ArrayList:<init>	()V
    //   5343: astore 6
    //   5345: iconst_0
    //   5346: istore 19
    //   5348: iload 19
    //   5350: aload_3
    //   5351: invokevirtual 750	java/util/ArrayList:size	()I
    //   5354: if_icmpge +1185 -> 6539
    //   5357: aload 6
    //   5359: getstatic 1874	org/telegram/messenger/Utilities:random	Ljava/security/SecureRandom;
    //   5362: invokevirtual 1879	java/security/SecureRandom:nextLong	()J
    //   5365: invokestatic 1882	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   5368: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   5371: pop
    //   5372: iload 19
    //   5374: iconst_1
    //   5375: iadd
    //   5376: istore 19
    //   5378: goto -30 -> 5348
    //   5381: aload_1
    //   5382: astore 5
    //   5384: aload 42
    //   5386: instanceof 1181
    //   5389: ifeq +29 -> 5418
    //   5392: aload_1
    //   5393: astore 5
    //   5395: new 2013	org/telegram/tgnet/TLRPC$TL_inputMediaGeoLive
    //   5398: dup
    //   5399: invokespecial 2014	org/telegram/tgnet/TLRPC$TL_inputMediaGeoLive:<init>	()V
    //   5402: astore_2
    //   5403: aload_1
    //   5404: astore 5
    //   5406: aload_2
    //   5407: aload 42
    //   5409: getfield 2015	org/telegram/tgnet/TLRPC$MessageMedia:period	I
    //   5412: putfield 2016	org/telegram/tgnet/TLRPC$InputMedia:period	I
    //   5415: goto -142 -> 5273
    //   5418: aload_1
    //   5419: astore 5
    //   5421: new 2018	org/telegram/tgnet/TLRPC$TL_inputMediaGeoPoint
    //   5424: dup
    //   5425: invokespecial 2019	org/telegram/tgnet/TLRPC$TL_inputMediaGeoPoint:<init>	()V
    //   5428: astore_2
    //   5429: goto -156 -> 5273
    //   5432: aload_1
    //   5433: astore 5
    //   5435: aload 4
    //   5437: getfield 2020	org/telegram/tgnet/TLRPC$TL_photo:access_hash	J
    //   5440: lconst_0
    //   5441: lcmp
    //   5442: ifne +259 -> 5701
    //   5445: aload_1
    //   5446: astore 5
    //   5448: new 2022	org/telegram/tgnet/TLRPC$TL_inputMediaUploadedPhoto
    //   5451: dup
    //   5452: invokespecial 2023	org/telegram/tgnet/TLRPC$TL_inputMediaUploadedPhoto:<init>	()V
    //   5455: astore_2
    //   5456: iload 19
    //   5458: ifeq +35 -> 5493
    //   5461: aload_1
    //   5462: astore 5
    //   5464: aload_2
    //   5465: iload 19
    //   5467: putfield 2024	org/telegram/tgnet/TLRPC$InputMedia:ttl_seconds	I
    //   5470: aload_1
    //   5471: astore 5
    //   5473: aload 8
    //   5475: iload 19
    //   5477: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   5480: aload_1
    //   5481: astore 5
    //   5483: aload_2
    //   5484: aload_2
    //   5485: getfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   5488: iconst_2
    //   5489: ior
    //   5490: putfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   5493: aload 41
    //   5495: ifnull +107 -> 5602
    //   5498: aload_1
    //   5499: astore 5
    //   5501: aload 41
    //   5503: ldc_w 2026
    //   5506: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   5509: checkcast 489	java/lang/String
    //   5512: astore 6
    //   5514: aload 6
    //   5516: ifnull +86 -> 5602
    //   5519: aload_1
    //   5520: astore 5
    //   5522: new 2028	org/telegram/tgnet/SerializedData
    //   5525: dup
    //   5526: aload 6
    //   5528: invokestatic 2032	org/telegram/messenger/Utilities:hexToBytes	(Ljava/lang/String;)[B
    //   5531: invokespecial 2035	org/telegram/tgnet/SerializedData:<init>	([B)V
    //   5534: astore 6
    //   5536: aload_1
    //   5537: astore 5
    //   5539: aload 6
    //   5541: iconst_0
    //   5542: invokevirtual 2039	org/telegram/tgnet/SerializedData:readInt32	(Z)I
    //   5545: istore 20
    //   5547: iconst_0
    //   5548: istore 19
    //   5550: iload 19
    //   5552: iload 20
    //   5554: if_icmpge +35 -> 5589
    //   5557: aload_1
    //   5558: astore 5
    //   5560: aload_2
    //   5561: getfield 2042	org/telegram/tgnet/TLRPC$InputMedia:stickers	Ljava/util/ArrayList;
    //   5564: aload 6
    //   5566: aload 6
    //   5568: iconst_0
    //   5569: invokevirtual 2039	org/telegram/tgnet/SerializedData:readInt32	(Z)I
    //   5572: iconst_0
    //   5573: invokestatic 2048	org/telegram/tgnet/TLRPC$InputDocument:TLdeserialize	(Lorg/telegram/tgnet/AbstractSerializedData;IZ)Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   5576: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   5579: pop
    //   5580: iload 19
    //   5582: iconst_1
    //   5583: iadd
    //   5584: istore 19
    //   5586: goto -36 -> 5550
    //   5589: aload_1
    //   5590: astore 5
    //   5592: aload_2
    //   5593: aload_2
    //   5594: getfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   5597: iconst_1
    //   5598: ior
    //   5599: putfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   5602: aload_1
    //   5603: ifnonnull +4665 -> 10268
    //   5606: aload_1
    //   5607: astore 5
    //   5609: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   5612: dup
    //   5613: aload_0
    //   5614: lload 9
    //   5616: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   5619: astore_1
    //   5620: aload_1
    //   5621: iconst_0
    //   5622: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   5625: aload_1
    //   5626: aload 13
    //   5628: putfield 761	org/telegram/messenger/SendMessagesHelper$DelayedMessage:obj	Lorg/telegram/messenger/MessageObject;
    //   5631: aload_1
    //   5632: aload 36
    //   5634: putfield 961	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPath	Ljava/lang/String;
    //   5637: aload 11
    //   5639: ifnull +31 -> 5670
    //   5642: aload 11
    //   5644: invokevirtual 782	java/lang/String:length	()I
    //   5647: ifle +23 -> 5670
    //   5650: aload 11
    //   5652: ldc_w 1698
    //   5655: invokevirtual 788	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   5658: ifeq +12 -> 5670
    //   5661: aload_1
    //   5662: aload 11
    //   5664: putfield 800	org/telegram/messenger/SendMessagesHelper$DelayedMessage:httpLocation	Ljava/lang/String;
    //   5667: goto -344 -> 5323
    //   5670: aload_1
    //   5671: aload 4
    //   5673: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   5676: aload 4
    //   5678: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   5681: invokevirtual 750	java/util/ArrayList:size	()I
    //   5684: iconst_1
    //   5685: isub
    //   5686: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   5689: checkcast 1410	org/telegram/tgnet/TLRPC$PhotoSize
    //   5692: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   5695: putfield 823	org/telegram/messenger/SendMessagesHelper$DelayedMessage:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   5698: goto -375 -> 5323
    //   5701: aload_1
    //   5702: astore 5
    //   5704: new 2050	org/telegram/tgnet/TLRPC$TL_inputMediaPhoto
    //   5707: dup
    //   5708: invokespecial 2051	org/telegram/tgnet/TLRPC$TL_inputMediaPhoto:<init>	()V
    //   5711: astore_2
    //   5712: aload_1
    //   5713: astore 5
    //   5715: aload_2
    //   5716: new 2053	org/telegram/tgnet/TLRPC$TL_inputPhoto
    //   5719: dup
    //   5720: invokespecial 2054	org/telegram/tgnet/TLRPC$TL_inputPhoto:<init>	()V
    //   5723: putfield 2057	org/telegram/tgnet/TLRPC$TL_inputMediaPhoto:id	Lorg/telegram/tgnet/TLRPC$InputPhoto;
    //   5726: aload_1
    //   5727: astore 5
    //   5729: aload_2
    //   5730: getfield 2057	org/telegram/tgnet/TLRPC$TL_inputMediaPhoto:id	Lorg/telegram/tgnet/TLRPC$InputPhoto;
    //   5733: aload 4
    //   5735: getfield 2058	org/telegram/tgnet/TLRPC$TL_photo:id	J
    //   5738: putfield 2061	org/telegram/tgnet/TLRPC$InputPhoto:id	J
    //   5741: aload_1
    //   5742: astore 5
    //   5744: aload_2
    //   5745: getfield 2057	org/telegram/tgnet/TLRPC$TL_inputMediaPhoto:id	Lorg/telegram/tgnet/TLRPC$InputPhoto;
    //   5748: aload 4
    //   5750: getfield 2020	org/telegram/tgnet/TLRPC$TL_photo:access_hash	J
    //   5753: putfield 2062	org/telegram/tgnet/TLRPC$InputPhoto:access_hash	J
    //   5756: goto -433 -> 5323
    //   5759: iload 21
    //   5761: iconst_3
    //   5762: if_icmpne +248 -> 6010
    //   5765: aload_1
    //   5766: astore 5
    //   5768: aload 40
    //   5770: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   5773: lconst_0
    //   5774: lcmp
    //   5775: ifne +177 -> 5952
    //   5778: aload_1
    //   5779: astore 5
    //   5781: new 2065	org/telegram/tgnet/TLRPC$TL_inputMediaUploadedDocument
    //   5784: dup
    //   5785: invokespecial 2066	org/telegram/tgnet/TLRPC$TL_inputMediaUploadedDocument:<init>	()V
    //   5788: astore_2
    //   5789: aload_1
    //   5790: astore 5
    //   5792: aload_2
    //   5793: aload 40
    //   5795: getfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   5798: putfield 2067	org/telegram/tgnet/TLRPC$InputMedia:mime_type	Ljava/lang/String;
    //   5801: aload_1
    //   5802: astore 5
    //   5804: aload_2
    //   5805: aload 40
    //   5807: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   5810: putfield 2068	org/telegram/tgnet/TLRPC$InputMedia:attributes	Ljava/util/ArrayList;
    //   5813: aload_1
    //   5814: astore 5
    //   5816: aload 40
    //   5818: invokestatic 1753	org/telegram/messenger/MessageObject:isRoundVideoDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   5821: ifne +38 -> 5859
    //   5824: aload 7
    //   5826: ifnull +25 -> 5851
    //   5829: aload_1
    //   5830: astore 5
    //   5832: aload 7
    //   5834: getfield 2071	org/telegram/messenger/VideoEditedInfo:muted	Z
    //   5837: ifne +22 -> 5859
    //   5840: aload_1
    //   5841: astore 5
    //   5843: aload 7
    //   5845: getfield 2074	org/telegram/messenger/VideoEditedInfo:roundVideo	Z
    //   5848: ifne +11 -> 5859
    //   5851: aload_1
    //   5852: astore 5
    //   5854: aload_2
    //   5855: iconst_1
    //   5856: putfield 2077	org/telegram/tgnet/TLRPC$InputMedia:nosound_video	Z
    //   5859: iload 19
    //   5861: ifeq +35 -> 5896
    //   5864: aload_1
    //   5865: astore 5
    //   5867: aload_2
    //   5868: iload 19
    //   5870: putfield 2024	org/telegram/tgnet/TLRPC$InputMedia:ttl_seconds	I
    //   5873: aload_1
    //   5874: astore 5
    //   5876: aload 8
    //   5878: iload 19
    //   5880: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   5883: aload_1
    //   5884: astore 5
    //   5886: aload_2
    //   5887: aload_2
    //   5888: getfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   5891: iconst_2
    //   5892: ior
    //   5893: putfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   5896: aload_1
    //   5897: ifnonnull +4368 -> 10265
    //   5900: aload_1
    //   5901: astore 5
    //   5903: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   5906: dup
    //   5907: aload_0
    //   5908: lload 9
    //   5910: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   5913: astore_1
    //   5914: aload_1
    //   5915: iconst_1
    //   5916: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   5919: aload_1
    //   5920: aload 13
    //   5922: putfield 761	org/telegram/messenger/SendMessagesHelper$DelayedMessage:obj	Lorg/telegram/messenger/MessageObject;
    //   5925: aload_1
    //   5926: aload 36
    //   5928: putfield 961	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPath	Ljava/lang/String;
    //   5931: aload_1
    //   5932: aload 40
    //   5934: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   5937: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   5940: putfield 823	org/telegram/messenger/SendMessagesHelper$DelayedMessage:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   5943: aload_1
    //   5944: aload 7
    //   5946: putfield 866	org/telegram/messenger/SendMessagesHelper$DelayedMessage:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   5949: goto -626 -> 5323
    //   5952: aload_1
    //   5953: astore 5
    //   5955: new 2079	org/telegram/tgnet/TLRPC$TL_inputMediaDocument
    //   5958: dup
    //   5959: invokespecial 2080	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:<init>	()V
    //   5962: astore_2
    //   5963: aload_1
    //   5964: astore 5
    //   5966: aload_2
    //   5967: new 2082	org/telegram/tgnet/TLRPC$TL_inputDocument
    //   5970: dup
    //   5971: invokespecial 2083	org/telegram/tgnet/TLRPC$TL_inputDocument:<init>	()V
    //   5974: putfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   5977: aload_1
    //   5978: astore 5
    //   5980: aload_2
    //   5981: getfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   5984: aload 40
    //   5986: getfield 1292	org/telegram/tgnet/TLRPC$TL_document:id	J
    //   5989: putfield 2087	org/telegram/tgnet/TLRPC$InputDocument:id	J
    //   5992: aload_1
    //   5993: astore 5
    //   5995: aload_2
    //   5996: getfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   5999: aload 40
    //   6001: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   6004: putfield 2088	org/telegram/tgnet/TLRPC$InputDocument:access_hash	J
    //   6007: goto -684 -> 5323
    //   6010: iload 21
    //   6012: bipush 6
    //   6014: if_icmpne +4501 -> 10515
    //   6017: aload_1
    //   6018: astore 5
    //   6020: new 2090	org/telegram/tgnet/TLRPC$TL_inputMediaContact
    //   6023: dup
    //   6024: invokespecial 2091	org/telegram/tgnet/TLRPC$TL_inputMediaContact:<init>	()V
    //   6027: astore_2
    //   6028: aload_1
    //   6029: astore 5
    //   6031: aload_2
    //   6032: aload 6
    //   6034: getfield 1201	org/telegram/tgnet/TLRPC$User:phone	Ljava/lang/String;
    //   6037: putfield 2092	org/telegram/tgnet/TLRPC$InputMedia:phone_number	Ljava/lang/String;
    //   6040: aload_1
    //   6041: astore 5
    //   6043: aload_2
    //   6044: aload 6
    //   6046: getfield 1205	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   6049: putfield 2093	org/telegram/tgnet/TLRPC$InputMedia:first_name	Ljava/lang/String;
    //   6052: aload_1
    //   6053: astore 5
    //   6055: aload_2
    //   6056: aload 6
    //   6058: getfield 1209	org/telegram/tgnet/TLRPC$User:last_name	Ljava/lang/String;
    //   6061: putfield 2094	org/telegram/tgnet/TLRPC$InputMedia:last_name	Ljava/lang/String;
    //   6064: goto -741 -> 5323
    //   6067: aload_1
    //   6068: astore 5
    //   6070: aload 40
    //   6072: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   6075: lconst_0
    //   6076: lcmp
    //   6077: ifne +226 -> 6303
    //   6080: aload 39
    //   6082: ifnonnull +127 -> 6209
    //   6085: aload 36
    //   6087: ifnull +122 -> 6209
    //   6090: aload_1
    //   6091: astore 5
    //   6093: aload 36
    //   6095: invokevirtual 782	java/lang/String:length	()I
    //   6098: ifle +111 -> 6209
    //   6101: aload_1
    //   6102: astore 5
    //   6104: aload 36
    //   6106: ldc_w 1698
    //   6109: invokevirtual 788	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   6112: ifeq +97 -> 6209
    //   6115: aload 41
    //   6117: ifnull +92 -> 6209
    //   6120: aload_1
    //   6121: astore 5
    //   6123: new 2096	org/telegram/tgnet/TLRPC$TL_inputMediaGifExternal
    //   6126: dup
    //   6127: invokespecial 2097	org/telegram/tgnet/TLRPC$TL_inputMediaGifExternal:<init>	()V
    //   6130: astore_2
    //   6131: aload_1
    //   6132: astore 5
    //   6134: aload 41
    //   6136: ldc_w 2098
    //   6139: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   6142: checkcast 489	java/lang/String
    //   6145: ldc_w 2100
    //   6148: invokevirtual 2104	java/lang/String:split	(Ljava/lang/String;)[Ljava/lang/String;
    //   6151: astore 6
    //   6153: aload_1
    //   6154: astore 5
    //   6156: aload 6
    //   6158: arraylength
    //   6159: iconst_2
    //   6160: if_icmpne +28 -> 6188
    //   6163: aload_1
    //   6164: astore 5
    //   6166: aload_2
    //   6167: checkcast 2096	org/telegram/tgnet/TLRPC$TL_inputMediaGifExternal
    //   6170: aload 6
    //   6172: iconst_0
    //   6173: aaload
    //   6174: putfield 2105	org/telegram/tgnet/TLRPC$TL_inputMediaGifExternal:url	Ljava/lang/String;
    //   6177: aload_1
    //   6178: astore 5
    //   6180: aload_2
    //   6181: aload 6
    //   6183: iconst_1
    //   6184: aaload
    //   6185: putfield 2108	org/telegram/tgnet/TLRPC$InputMedia:q	Ljava/lang/String;
    //   6188: aload_2
    //   6189: aload 40
    //   6191: getfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   6194: putfield 2067	org/telegram/tgnet/TLRPC$InputMedia:mime_type	Ljava/lang/String;
    //   6197: aload_2
    //   6198: aload 40
    //   6200: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   6203: putfield 2068	org/telegram/tgnet/TLRPC$InputMedia:attributes	Ljava/util/ArrayList;
    //   6206: goto -883 -> 5323
    //   6209: aload_1
    //   6210: astore 5
    //   6212: new 2065	org/telegram/tgnet/TLRPC$TL_inputMediaUploadedDocument
    //   6215: dup
    //   6216: invokespecial 2066	org/telegram/tgnet/TLRPC$TL_inputMediaUploadedDocument:<init>	()V
    //   6219: astore_2
    //   6220: iload 19
    //   6222: ifeq +35 -> 6257
    //   6225: aload_1
    //   6226: astore 5
    //   6228: aload_2
    //   6229: iload 19
    //   6231: putfield 2024	org/telegram/tgnet/TLRPC$InputMedia:ttl_seconds	I
    //   6234: aload_1
    //   6235: astore 5
    //   6237: aload 8
    //   6239: iload 19
    //   6241: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   6244: aload_1
    //   6245: astore 5
    //   6247: aload_2
    //   6248: aload_2
    //   6249: getfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   6252: iconst_2
    //   6253: ior
    //   6254: putfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   6257: aload_1
    //   6258: astore 5
    //   6260: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   6263: dup
    //   6264: aload_0
    //   6265: lload 9
    //   6267: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   6270: astore_1
    //   6271: aload_1
    //   6272: aload 36
    //   6274: putfield 961	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPath	Ljava/lang/String;
    //   6277: aload_1
    //   6278: iconst_2
    //   6279: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   6282: aload_1
    //   6283: aload 13
    //   6285: putfield 761	org/telegram/messenger/SendMessagesHelper$DelayedMessage:obj	Lorg/telegram/messenger/MessageObject;
    //   6288: aload_1
    //   6289: aload 40
    //   6291: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6294: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   6297: putfield 823	org/telegram/messenger/SendMessagesHelper$DelayedMessage:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   6300: goto -112 -> 6188
    //   6303: aload_1
    //   6304: astore 5
    //   6306: new 2079	org/telegram/tgnet/TLRPC$TL_inputMediaDocument
    //   6309: dup
    //   6310: invokespecial 2080	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:<init>	()V
    //   6313: astore_2
    //   6314: aload_1
    //   6315: astore 5
    //   6317: aload_2
    //   6318: new 2082	org/telegram/tgnet/TLRPC$TL_inputDocument
    //   6321: dup
    //   6322: invokespecial 2083	org/telegram/tgnet/TLRPC$TL_inputDocument:<init>	()V
    //   6325: putfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   6328: aload_1
    //   6329: astore 5
    //   6331: aload_2
    //   6332: getfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   6335: aload 40
    //   6337: getfield 1292	org/telegram/tgnet/TLRPC$TL_document:id	J
    //   6340: putfield 2087	org/telegram/tgnet/TLRPC$InputDocument:id	J
    //   6343: aload_1
    //   6344: astore 5
    //   6346: aload_2
    //   6347: getfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   6350: aload 40
    //   6352: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   6355: putfield 2088	org/telegram/tgnet/TLRPC$InputDocument:access_hash	J
    //   6358: goto -1035 -> 5323
    //   6361: iload 21
    //   6363: bipush 8
    //   6365: if_icmpne +3897 -> 10262
    //   6368: aload_1
    //   6369: astore 5
    //   6371: aload 40
    //   6373: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   6376: lconst_0
    //   6377: lcmp
    //   6378: ifne +103 -> 6481
    //   6381: aload_1
    //   6382: astore 5
    //   6384: new 2065	org/telegram/tgnet/TLRPC$TL_inputMediaUploadedDocument
    //   6387: dup
    //   6388: invokespecial 2066	org/telegram/tgnet/TLRPC$TL_inputMediaUploadedDocument:<init>	()V
    //   6391: astore_2
    //   6392: aload_1
    //   6393: astore 5
    //   6395: aload_2
    //   6396: aload 40
    //   6398: getfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   6401: putfield 2067	org/telegram/tgnet/TLRPC$InputMedia:mime_type	Ljava/lang/String;
    //   6404: aload_1
    //   6405: astore 5
    //   6407: aload_2
    //   6408: aload 40
    //   6410: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   6413: putfield 2068	org/telegram/tgnet/TLRPC$InputMedia:attributes	Ljava/util/ArrayList;
    //   6416: iload 19
    //   6418: ifeq +35 -> 6453
    //   6421: aload_1
    //   6422: astore 5
    //   6424: aload_2
    //   6425: iload 19
    //   6427: putfield 2024	org/telegram/tgnet/TLRPC$InputMedia:ttl_seconds	I
    //   6430: aload_1
    //   6431: astore 5
    //   6433: aload 8
    //   6435: iload 19
    //   6437: putfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   6440: aload_1
    //   6441: astore 5
    //   6443: aload_2
    //   6444: aload_2
    //   6445: getfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   6448: iconst_2
    //   6449: ior
    //   6450: putfield 2025	org/telegram/tgnet/TLRPC$InputMedia:flags	I
    //   6453: aload_1
    //   6454: astore 5
    //   6456: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   6459: dup
    //   6460: aload_0
    //   6461: lload 9
    //   6463: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   6466: astore_1
    //   6467: aload_1
    //   6468: iconst_3
    //   6469: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   6472: aload_1
    //   6473: aload 13
    //   6475: putfield 761	org/telegram/messenger/SendMessagesHelper$DelayedMessage:obj	Lorg/telegram/messenger/MessageObject;
    //   6478: goto -1155 -> 5323
    //   6481: aload_1
    //   6482: astore 5
    //   6484: new 2079	org/telegram/tgnet/TLRPC$TL_inputMediaDocument
    //   6487: dup
    //   6488: invokespecial 2080	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:<init>	()V
    //   6491: astore_2
    //   6492: aload_1
    //   6493: astore 5
    //   6495: aload_2
    //   6496: new 2082	org/telegram/tgnet/TLRPC$TL_inputDocument
    //   6499: dup
    //   6500: invokespecial 2083	org/telegram/tgnet/TLRPC$TL_inputDocument:<init>	()V
    //   6503: putfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   6506: aload_1
    //   6507: astore 5
    //   6509: aload_2
    //   6510: getfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   6513: aload 40
    //   6515: getfield 1292	org/telegram/tgnet/TLRPC$TL_document:id	J
    //   6518: putfield 2087	org/telegram/tgnet/TLRPC$InputDocument:id	J
    //   6521: aload_1
    //   6522: astore 5
    //   6524: aload_2
    //   6525: getfield 2086	org/telegram/tgnet/TLRPC$TL_inputMediaDocument:id	Lorg/telegram/tgnet/TLRPC$InputDocument;
    //   6528: aload 40
    //   6530: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   6533: putfield 2088	org/telegram/tgnet/TLRPC$InputDocument:access_hash	J
    //   6536: goto -1213 -> 5323
    //   6539: aload 5
    //   6541: aload_3
    //   6542: putfield 1926	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:contacts	Ljava/util/ArrayList;
    //   6545: aload 5
    //   6547: aload_2
    //   6548: putfield 930	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:media	Lorg/telegram/tgnet/TLRPC$InputMedia;
    //   6551: aload 5
    //   6553: aload 6
    //   6555: putfield 1931	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:random_id	Ljava/util/ArrayList;
    //   6558: aload 5
    //   6560: ldc_w 1168
    //   6563: putfield 1923	org/telegram/tgnet/TLRPC$TL_messages_sendBroadcast:message	Ljava/lang/String;
    //   6566: aload_1
    //   6567: ifnull +9 -> 6576
    //   6570: aload_1
    //   6571: aload 5
    //   6573: putfield 819	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendRequest	Lorg/telegram/tgnet/TLObject;
    //   6576: aload 5
    //   6578: astore_3
    //   6579: aload_3
    //   6580: astore_2
    //   6581: aload 15
    //   6583: ifnonnull +18 -> 6601
    //   6586: aload_0
    //   6587: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   6590: invokestatic 1761	org/telegram/messenger/DataQuery:getInstance	(I)Lorg/telegram/messenger/DataQuery;
    //   6593: lload 9
    //   6595: iconst_0
    //   6596: invokevirtual 1963	org/telegram/messenger/DataQuery:cleanDraft	(JZ)V
    //   6599: aload_3
    //   6600: astore_2
    //   6601: lload 28
    //   6603: lconst_0
    //   6604: lcmp
    //   6605: ifeq +390 -> 6995
    //   6608: aload_0
    //   6609: aload_1
    //   6610: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   6613: return
    //   6614: lload 28
    //   6616: lconst_0
    //   6617: lcmp
    //   6618: ifeq +225 -> 6843
    //   6621: aload_1
    //   6622: getfield 819	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendRequest	Lorg/telegram/tgnet/TLObject;
    //   6625: ifnull +120 -> 6745
    //   6628: aload_1
    //   6629: getfield 819	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendRequest	Lorg/telegram/tgnet/TLObject;
    //   6632: checkcast 1005	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia
    //   6635: astore_3
    //   6636: aload_1
    //   6637: getfield 770	org/telegram/messenger/SendMessagesHelper$DelayedMessage:messageObjects	Ljava/util/ArrayList;
    //   6640: aload 13
    //   6642: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   6645: pop
    //   6646: aload_1
    //   6647: getfield 1916	org/telegram/messenger/SendMessagesHelper$DelayedMessage:messages	Ljava/util/ArrayList;
    //   6650: aload 8
    //   6652: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   6655: pop
    //   6656: aload_1
    //   6657: getfield 1919	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPaths	Ljava/util/ArrayList;
    //   6660: aload 36
    //   6662: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   6665: pop
    //   6666: new 1010	org/telegram/tgnet/TLRPC$TL_inputSingleMedia
    //   6669: dup
    //   6670: invokespecial 2109	org/telegram/tgnet/TLRPC$TL_inputSingleMedia:<init>	()V
    //   6673: astore 5
    //   6675: aload 5
    //   6677: aload 8
    //   6679: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   6682: putfield 2110	org/telegram/tgnet/TLRPC$TL_inputSingleMedia:random_id	J
    //   6685: aload 5
    //   6687: aload_2
    //   6688: putfield 1011	org/telegram/tgnet/TLRPC$TL_inputSingleMedia:media	Lorg/telegram/tgnet/TLRPC$InputMedia;
    //   6691: aload 5
    //   6693: aload 37
    //   6695: putfield 2111	org/telegram/tgnet/TLRPC$TL_inputSingleMedia:message	Ljava/lang/String;
    //   6698: aload 16
    //   6700: ifnull +30 -> 6730
    //   6703: aload 16
    //   6705: invokevirtual 773	java/util/ArrayList:isEmpty	()Z
    //   6708: ifne +22 -> 6730
    //   6711: aload 5
    //   6713: aload 16
    //   6715: putfield 2112	org/telegram/tgnet/TLRPC$TL_inputSingleMedia:entities	Ljava/util/ArrayList;
    //   6718: aload 5
    //   6720: aload 5
    //   6722: getfield 2113	org/telegram/tgnet/TLRPC$TL_inputSingleMedia:flags	I
    //   6725: iconst_1
    //   6726: ior
    //   6727: putfield 2113	org/telegram/tgnet/TLRPC$TL_inputSingleMedia:flags	I
    //   6730: aload_3
    //   6731: getfield 1008	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia:multi_media	Ljava/util/ArrayList;
    //   6734: aload 5
    //   6736: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   6739: pop
    //   6740: aload_3
    //   6741: astore_2
    //   6742: goto -141 -> 6601
    //   6745: new 1005	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia
    //   6748: dup
    //   6749: invokespecial 2114	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia:<init>	()V
    //   6752: astore_3
    //   6753: aload_3
    //   6754: aload 38
    //   6756: putfield 2115	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia:peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   6759: aload 8
    //   6761: getfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   6764: instanceof 1938
    //   6767: ifeq +41 -> 6808
    //   6770: aload_3
    //   6771: aload_0
    //   6772: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   6775: invokestatic 1942	org/telegram/messenger/MessagesController:getNotificationsSettings	(I)Landroid/content/SharedPreferences;
    //   6778: new 507	java/lang/StringBuilder
    //   6781: dup
    //   6782: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   6785: ldc_w 1944
    //   6788: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   6791: lload 9
    //   6793: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   6796: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   6799: iconst_0
    //   6800: invokeinterface 1948 3 0
    //   6805: putfield 2116	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia:silent	Z
    //   6808: aload 8
    //   6810: getfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   6813: ifeq +22 -> 6835
    //   6816: aload_3
    //   6817: aload_3
    //   6818: getfield 2117	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia:flags	I
    //   6821: iconst_1
    //   6822: ior
    //   6823: putfield 2117	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia:flags	I
    //   6826: aload_3
    //   6827: aload 8
    //   6829: getfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   6832: putfield 2118	org/telegram/tgnet/TLRPC$TL_messages_sendMultiMedia:reply_to_msg_id	I
    //   6835: aload_1
    //   6836: aload_3
    //   6837: putfield 819	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendRequest	Lorg/telegram/tgnet/TLObject;
    //   6840: goto -204 -> 6636
    //   6843: new 913	org/telegram/tgnet/TLRPC$TL_messages_sendMedia
    //   6846: dup
    //   6847: invokespecial 2119	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:<init>	()V
    //   6850: astore_3
    //   6851: aload_3
    //   6852: aload 38
    //   6854: putfield 2120	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   6857: aload 8
    //   6859: getfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   6862: instanceof 1938
    //   6865: ifeq +41 -> 6906
    //   6868: aload_3
    //   6869: aload_0
    //   6870: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   6873: invokestatic 1942	org/telegram/messenger/MessagesController:getNotificationsSettings	(I)Landroid/content/SharedPreferences;
    //   6876: new 507	java/lang/StringBuilder
    //   6879: dup
    //   6880: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   6883: ldc_w 1944
    //   6886: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   6889: lload 9
    //   6891: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   6894: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   6897: iconst_0
    //   6898: invokeinterface 1948 3 0
    //   6903: putfield 2121	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:silent	Z
    //   6906: aload 8
    //   6908: getfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   6911: ifeq +22 -> 6933
    //   6914: aload_3
    //   6915: aload_3
    //   6916: getfield 2122	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:flags	I
    //   6919: iconst_1
    //   6920: ior
    //   6921: putfield 2122	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:flags	I
    //   6924: aload_3
    //   6925: aload 8
    //   6927: getfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   6930: putfield 2123	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:reply_to_msg_id	I
    //   6933: aload_3
    //   6934: aload 8
    //   6936: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   6939: putfield 2124	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:random_id	J
    //   6942: aload_3
    //   6943: aload_2
    //   6944: putfield 917	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:media	Lorg/telegram/tgnet/TLRPC$InputMedia;
    //   6947: aload_3
    //   6948: aload 37
    //   6950: putfield 2125	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:message	Ljava/lang/String;
    //   6953: aload 16
    //   6955: ifnull +28 -> 6983
    //   6958: aload 16
    //   6960: invokevirtual 773	java/util/ArrayList:isEmpty	()Z
    //   6963: ifne +20 -> 6983
    //   6966: aload_3
    //   6967: aload 16
    //   6969: putfield 2126	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:entities	Ljava/util/ArrayList;
    //   6972: aload_3
    //   6973: aload_3
    //   6974: getfield 2122	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:flags	I
    //   6977: bipush 8
    //   6979: ior
    //   6980: putfield 2122	org/telegram/tgnet/TLRPC$TL_messages_sendMedia:flags	I
    //   6983: aload_1
    //   6984: ifnull +3548 -> 10532
    //   6987: aload_1
    //   6988: aload_3
    //   6989: putfield 819	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendRequest	Lorg/telegram/tgnet/TLObject;
    //   6992: goto +3540 -> 10532
    //   6995: iload 21
    //   6997: iconst_1
    //   6998: if_icmpne +12 -> 7010
    //   7001: aload_0
    //   7002: aload_2
    //   7003: aload 13
    //   7005: aconst_null
    //   7006: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   7009: return
    //   7010: iload 21
    //   7012: iconst_2
    //   7013: if_icmpne +30 -> 7043
    //   7016: aload 4
    //   7018: getfield 2020	org/telegram/tgnet/TLRPC$TL_photo:access_hash	J
    //   7021: lconst_0
    //   7022: lcmp
    //   7023: ifne +9 -> 7032
    //   7026: aload_0
    //   7027: aload_1
    //   7028: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   7031: return
    //   7032: aload_0
    //   7033: aload_2
    //   7034: aload 13
    //   7036: aconst_null
    //   7037: aconst_null
    //   7038: iconst_1
    //   7039: invokespecial 1027	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;Z)V
    //   7042: return
    //   7043: iload 21
    //   7045: iconst_3
    //   7046: if_icmpne +28 -> 7074
    //   7049: aload 40
    //   7051: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   7054: lconst_0
    //   7055: lcmp
    //   7056: ifne +9 -> 7065
    //   7059: aload_0
    //   7060: aload_1
    //   7061: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   7064: return
    //   7065: aload_0
    //   7066: aload_2
    //   7067: aload 13
    //   7069: aconst_null
    //   7070: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   7073: return
    //   7074: iload 21
    //   7076: bipush 6
    //   7078: if_icmpne +12 -> 7090
    //   7081: aload_0
    //   7082: aload_2
    //   7083: aload 13
    //   7085: aconst_null
    //   7086: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   7089: return
    //   7090: iload 21
    //   7092: bipush 7
    //   7094: if_icmpne +33 -> 7127
    //   7097: aload 40
    //   7099: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   7102: lconst_0
    //   7103: lcmp
    //   7104: ifne +13 -> 7117
    //   7107: aload_1
    //   7108: ifnull +9 -> 7117
    //   7111: aload_0
    //   7112: aload_1
    //   7113: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   7116: return
    //   7117: aload_0
    //   7118: aload_2
    //   7119: aload 13
    //   7121: aload 36
    //   7123: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   7126: return
    //   7127: iload 21
    //   7129: bipush 8
    //   7131: if_icmpne -7124 -> 7
    //   7134: aload 40
    //   7136: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   7139: lconst_0
    //   7140: lcmp
    //   7141: ifne +9 -> 7150
    //   7144: aload_0
    //   7145: aload_1
    //   7146: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   7149: return
    //   7150: aload_0
    //   7151: aload_2
    //   7152: aload 13
    //   7154: aconst_null
    //   7155: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   7158: return
    //   7159: aload_1
    //   7160: astore 5
    //   7162: aload 39
    //   7164: getfield 1966	org/telegram/tgnet/TLRPC$EncryptedChat:layer	I
    //   7167: invokestatic 1969	org/telegram/messenger/AndroidUtilities:getPeerLayerVersion	(I)I
    //   7170: bipush 73
    //   7172: if_icmplt +503 -> 7675
    //   7175: aload_1
    //   7176: astore 5
    //   7178: new 936	org/telegram/tgnet/TLRPC$TL_decryptedMessage
    //   7181: dup
    //   7182: invokespecial 1970	org/telegram/tgnet/TLRPC$TL_decryptedMessage:<init>	()V
    //   7185: astore_2
    //   7186: aload_2
    //   7187: astore_3
    //   7188: lload 28
    //   7190: lconst_0
    //   7191: lcmp
    //   7192: ifeq +29 -> 7221
    //   7195: aload_1
    //   7196: astore 5
    //   7198: aload_2
    //   7199: lload 28
    //   7201: putfield 2127	org/telegram/tgnet/TLRPC$TL_decryptedMessage:grouped_id	J
    //   7204: aload_1
    //   7205: astore 5
    //   7207: aload_2
    //   7208: aload_2
    //   7209: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7212: ldc_w 1843
    //   7215: ior
    //   7216: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7219: aload_2
    //   7220: astore_3
    //   7221: aload_1
    //   7222: astore 5
    //   7224: aload_3
    //   7225: aload 8
    //   7227: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   7230: putfield 1971	org/telegram/tgnet/TLRPC$TL_decryptedMessage:ttl	I
    //   7233: aload 16
    //   7235: ifnull +38 -> 7273
    //   7238: aload_1
    //   7239: astore 5
    //   7241: aload 16
    //   7243: invokevirtual 773	java/util/ArrayList:isEmpty	()Z
    //   7246: ifne +27 -> 7273
    //   7249: aload_1
    //   7250: astore 5
    //   7252: aload_3
    //   7253: aload 16
    //   7255: putfield 1972	org/telegram/tgnet/TLRPC$TL_decryptedMessage:entities	Ljava/util/ArrayList;
    //   7258: aload_1
    //   7259: astore 5
    //   7261: aload_3
    //   7262: aload_3
    //   7263: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7266: sipush 128
    //   7269: ior
    //   7270: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7273: aload_1
    //   7274: astore 5
    //   7276: aload 8
    //   7278: getfield 1614	org/telegram/tgnet/TLRPC$Message:reply_to_random_id	J
    //   7281: lconst_0
    //   7282: lcmp
    //   7283: ifeq +29 -> 7312
    //   7286: aload_1
    //   7287: astore 5
    //   7289: aload_3
    //   7290: aload 8
    //   7292: getfield 1614	org/telegram/tgnet/TLRPC$Message:reply_to_random_id	J
    //   7295: putfield 1974	org/telegram/tgnet/TLRPC$TL_decryptedMessage:reply_to_random_id	J
    //   7298: aload_1
    //   7299: astore 5
    //   7301: aload_3
    //   7302: aload_3
    //   7303: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7306: bipush 8
    //   7308: ior
    //   7309: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7312: aload_1
    //   7313: astore 5
    //   7315: aload_3
    //   7316: aload_3
    //   7317: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7320: sipush 512
    //   7323: ior
    //   7324: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7327: aload 41
    //   7329: ifnull +50 -> 7379
    //   7332: aload_1
    //   7333: astore 5
    //   7335: aload 41
    //   7337: ldc_w 1591
    //   7340: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   7343: ifnull +36 -> 7379
    //   7346: aload_1
    //   7347: astore 5
    //   7349: aload_3
    //   7350: aload 41
    //   7352: ldc_w 1591
    //   7355: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   7358: checkcast 489	java/lang/String
    //   7361: putfield 1975	org/telegram/tgnet/TLRPC$TL_decryptedMessage:via_bot_name	Ljava/lang/String;
    //   7364: aload_1
    //   7365: astore 5
    //   7367: aload_3
    //   7368: aload_3
    //   7369: getfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7372: sipush 2048
    //   7375: ior
    //   7376: putfield 1973	org/telegram/tgnet/TLRPC$TL_decryptedMessage:flags	I
    //   7379: aload_1
    //   7380: astore 5
    //   7382: aload_3
    //   7383: aload 8
    //   7385: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   7388: putfield 1976	org/telegram/tgnet/TLRPC$TL_decryptedMessage:random_id	J
    //   7391: aload_1
    //   7392: astore 5
    //   7394: aload_3
    //   7395: ldc_w 1168
    //   7398: putfield 1977	org/telegram/tgnet/TLRPC$TL_decryptedMessage:message	Ljava/lang/String;
    //   7401: iload 21
    //   7403: iconst_1
    //   7404: if_icmpne +3133 -> 10537
    //   7407: aload_1
    //   7408: astore 5
    //   7410: aload 42
    //   7412: instanceof 1144
    //   7415: ifeq +274 -> 7689
    //   7418: aload_1
    //   7419: astore 5
    //   7421: aload_3
    //   7422: new 2129	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaVenue
    //   7425: dup
    //   7426: invokespecial 2130	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaVenue:<init>	()V
    //   7429: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7432: aload_1
    //   7433: astore 5
    //   7435: aload_3
    //   7436: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7439: aload 42
    //   7441: getfield 1991	org/telegram/tgnet/TLRPC$MessageMedia:address	Ljava/lang/String;
    //   7444: putfield 2131	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:address	Ljava/lang/String;
    //   7447: aload_1
    //   7448: astore 5
    //   7450: aload_3
    //   7451: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7454: aload 42
    //   7456: getfield 1993	org/telegram/tgnet/TLRPC$MessageMedia:title	Ljava/lang/String;
    //   7459: putfield 2132	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:title	Ljava/lang/String;
    //   7462: aload_1
    //   7463: astore 5
    //   7465: aload_3
    //   7466: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7469: aload 42
    //   7471: getfield 1995	org/telegram/tgnet/TLRPC$MessageMedia:provider	Ljava/lang/String;
    //   7474: putfield 2133	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:provider	Ljava/lang/String;
    //   7477: aload_1
    //   7478: astore 5
    //   7480: aload_3
    //   7481: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7484: aload 42
    //   7486: getfield 1997	org/telegram/tgnet/TLRPC$MessageMedia:venue_id	Ljava/lang/String;
    //   7489: putfield 2134	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:venue_id	Ljava/lang/String;
    //   7492: aload_1
    //   7493: astore 5
    //   7495: aload_3
    //   7496: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7499: aload 42
    //   7501: getfield 2007	org/telegram/tgnet/TLRPC$MessageMedia:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
    //   7504: getfield 1516	org/telegram/tgnet/TLRPC$GeoPoint:lat	D
    //   7507: putfield 2135	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:lat	D
    //   7510: aload_1
    //   7511: astore 5
    //   7513: aload_3
    //   7514: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7517: aload 42
    //   7519: getfield 2007	org/telegram/tgnet/TLRPC$MessageMedia:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
    //   7522: getfield 1522	org/telegram/tgnet/TLRPC$GeoPoint:_long	D
    //   7525: putfield 2136	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:_long	D
    //   7528: aload_1
    //   7529: astore 5
    //   7531: aload_0
    //   7532: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   7535: invokestatic 956	org/telegram/messenger/SecretChatHelper:getInstance	(I)Lorg/telegram/messenger/SecretChatHelper;
    //   7538: aload_3
    //   7539: aload 13
    //   7541: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   7544: aload 39
    //   7546: aconst_null
    //   7547: aconst_null
    //   7548: aload 13
    //   7550: invokevirtual 965	org/telegram/messenger/SecretChatHelper:performSendEncryptedRequest	(Lorg/telegram/tgnet/TLRPC$DecryptedMessage;Lorg/telegram/tgnet/TLRPC$Message;Lorg/telegram/tgnet/TLRPC$EncryptedChat;Lorg/telegram/tgnet/TLRPC$InputEncryptedFile;Ljava/lang/String;Lorg/telegram/messenger/MessageObject;)V
    //   7553: lload 28
    //   7555: lconst_0
    //   7556: lcmp
    //   7557: ifeq +99 -> 7656
    //   7560: aload_1
    //   7561: getfield 841	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendEncryptedRequest	Lorg/telegram/tgnet/TLObject;
    //   7564: ifnull +2084 -> 9648
    //   7567: aload_1
    //   7568: getfield 841	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendEncryptedRequest	Lorg/telegram/tgnet/TLObject;
    //   7571: checkcast 1015	org/telegram/tgnet/TLRPC$TL_messages_sendEncryptedMultiMedia
    //   7574: astore_2
    //   7575: aload_1
    //   7576: getfield 770	org/telegram/messenger/SendMessagesHelper$DelayedMessage:messageObjects	Ljava/util/ArrayList;
    //   7579: aload 13
    //   7581: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   7584: pop
    //   7585: aload_1
    //   7586: getfield 1916	org/telegram/messenger/SendMessagesHelper$DelayedMessage:messages	Ljava/util/ArrayList;
    //   7589: aload 8
    //   7591: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   7594: pop
    //   7595: aload_1
    //   7596: getfield 1919	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPaths	Ljava/util/ArrayList;
    //   7599: aload 36
    //   7601: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   7604: pop
    //   7605: aload_1
    //   7606: iconst_1
    //   7607: putfield 992	org/telegram/messenger/SendMessagesHelper$DelayedMessage:upload	Z
    //   7610: aload_2
    //   7611: getfield 2137	org/telegram/tgnet/TLRPC$TL_messages_sendEncryptedMultiMedia:messages	Ljava/util/ArrayList;
    //   7614: aload_3
    //   7615: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   7618: pop
    //   7619: new 2139	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile
    //   7622: dup
    //   7623: invokespecial 2140	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:<init>	()V
    //   7626: astore_3
    //   7627: iload 21
    //   7629: iconst_3
    //   7630: if_icmpne +2034 -> 9664
    //   7633: lconst_1
    //   7634: lstore 26
    //   7636: aload_3
    //   7637: lload 26
    //   7639: putfield 2141	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:id	J
    //   7642: aload_2
    //   7643: getfield 1018	org/telegram/tgnet/TLRPC$TL_messages_sendEncryptedMultiMedia:files	Ljava/util/ArrayList;
    //   7646: aload_3
    //   7647: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   7650: pop
    //   7651: aload_0
    //   7652: aload_1
    //   7653: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   7656: aload 15
    //   7658: ifnonnull -7651 -> 7
    //   7661: aload_0
    //   7662: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   7665: invokestatic 1761	org/telegram/messenger/DataQuery:getInstance	(I)Lorg/telegram/messenger/DataQuery;
    //   7668: lload 9
    //   7670: iconst_0
    //   7671: invokevirtual 1963	org/telegram/messenger/DataQuery:cleanDraft	(JZ)V
    //   7674: return
    //   7675: aload_1
    //   7676: astore 5
    //   7678: new 1983	org/telegram/tgnet/TLRPC$TL_decryptedMessage_layer45
    //   7681: dup
    //   7682: invokespecial 1984	org/telegram/tgnet/TLRPC$TL_decryptedMessage_layer45:<init>	()V
    //   7685: astore_3
    //   7686: goto -465 -> 7221
    //   7689: aload_1
    //   7690: astore 5
    //   7692: aload_3
    //   7693: new 2143	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaGeoPoint
    //   7696: dup
    //   7697: invokespecial 2144	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaGeoPoint:<init>	()V
    //   7700: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7703: goto -211 -> 7492
    //   7706: aload_1
    //   7707: astore 5
    //   7709: aload 4
    //   7711: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   7714: iconst_0
    //   7715: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   7718: checkcast 1410	org/telegram/tgnet/TLRPC$PhotoSize
    //   7721: astore 6
    //   7723: aload_1
    //   7724: astore 5
    //   7726: aload 4
    //   7728: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   7731: aload 4
    //   7733: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   7736: invokevirtual 750	java/util/ArrayList:size	()I
    //   7739: iconst_1
    //   7740: isub
    //   7741: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   7744: checkcast 1410	org/telegram/tgnet/TLRPC$PhotoSize
    //   7747: astore_2
    //   7748: aload_1
    //   7749: astore 5
    //   7751: aload 6
    //   7753: invokestatic 2148	org/telegram/messenger/ImageLoader:fillPhotoSizeWithBytes	(Lorg/telegram/tgnet/TLRPC$PhotoSize;)V
    //   7756: aload_1
    //   7757: astore 5
    //   7759: aload_3
    //   7760: new 2150	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaPhoto
    //   7763: dup
    //   7764: invokespecial 2151	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaPhoto:<init>	()V
    //   7767: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7770: aload_1
    //   7771: astore 5
    //   7773: aload_3
    //   7774: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7777: aload 37
    //   7779: putfield 2152	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:caption	Ljava/lang/String;
    //   7782: aload_1
    //   7783: astore 5
    //   7785: aload 6
    //   7787: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   7790: ifnull +203 -> 7993
    //   7793: aload_1
    //   7794: astore 5
    //   7796: aload_3
    //   7797: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7800: checkcast 2150	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaPhoto
    //   7803: aload 6
    //   7805: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   7808: putfield 2157	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaPhoto:thumb	[B
    //   7811: aload_1
    //   7812: astore 5
    //   7814: aload_3
    //   7815: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7818: aload 6
    //   7820: getfield 2158	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7823: putfield 2161	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_h	I
    //   7826: aload_1
    //   7827: astore 5
    //   7829: aload_3
    //   7830: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7833: aload 6
    //   7835: getfield 2162	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7838: putfield 2165	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_w	I
    //   7841: aload_1
    //   7842: astore 5
    //   7844: aload_3
    //   7845: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7848: aload_2
    //   7849: getfield 2162	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7852: putfield 2166	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:w	I
    //   7855: aload_1
    //   7856: astore 5
    //   7858: aload_3
    //   7859: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7862: aload_2
    //   7863: getfield 2158	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7866: putfield 2167	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:h	I
    //   7869: aload_1
    //   7870: astore 5
    //   7872: aload_3
    //   7873: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   7876: aload_2
    //   7877: getfield 2168	org/telegram/tgnet/TLRPC$PhotoSize:size	I
    //   7880: putfield 942	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:size	I
    //   7883: aload_1
    //   7884: astore 5
    //   7886: aload_2
    //   7887: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   7890: getfield 2169	org/telegram/tgnet/TLRPC$FileLocation:key	[B
    //   7893: ifnull +10 -> 7903
    //   7896: lload 28
    //   7898: lconst_0
    //   7899: lcmp
    //   7900: ifeq +143 -> 8043
    //   7903: aload_1
    //   7904: ifnonnull +2353 -> 10257
    //   7907: aload_1
    //   7908: astore 5
    //   7910: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   7913: dup
    //   7914: aload_0
    //   7915: lload 9
    //   7917: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   7920: astore_2
    //   7921: aload_2
    //   7922: aload 39
    //   7924: putfield 960	org/telegram/messenger/SendMessagesHelper$DelayedMessage:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   7927: aload_2
    //   7928: iconst_0
    //   7929: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   7932: aload_2
    //   7933: aload 36
    //   7935: putfield 961	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPath	Ljava/lang/String;
    //   7938: aload_2
    //   7939: aload_3
    //   7940: putfield 841	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendEncryptedRequest	Lorg/telegram/tgnet/TLObject;
    //   7943: aload_2
    //   7944: aload 13
    //   7946: putfield 761	org/telegram/messenger/SendMessagesHelper$DelayedMessage:obj	Lorg/telegram/messenger/MessageObject;
    //   7949: aload 11
    //   7951: invokestatic 1745	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   7954: ifne +58 -> 8012
    //   7957: aload 11
    //   7959: ldc_w 1698
    //   7962: invokevirtual 788	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   7965: ifeq +47 -> 8012
    //   7968: aload_2
    //   7969: aload 11
    //   7971: putfield 800	org/telegram/messenger/SendMessagesHelper$DelayedMessage:httpLocation	Ljava/lang/String;
    //   7974: aload_2
    //   7975: astore_1
    //   7976: lload 28
    //   7978: lconst_0
    //   7979: lcmp
    //   7980: ifne -427 -> 7553
    //   7983: aload_0
    //   7984: aload_2
    //   7985: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   7988: aload_2
    //   7989: astore_1
    //   7990: goto -437 -> 7553
    //   7993: aload_1
    //   7994: astore 5
    //   7996: aload_3
    //   7997: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8000: checkcast 2150	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaPhoto
    //   8003: iconst_0
    //   8004: newarray <illegal type>
    //   8006: putfield 2157	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaPhoto:thumb	[B
    //   8009: goto -198 -> 7811
    //   8012: aload_2
    //   8013: aload 4
    //   8015: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   8018: aload 4
    //   8020: getfield 1701	org/telegram/tgnet/TLRPC$TL_photo:sizes	Ljava/util/ArrayList;
    //   8023: invokevirtual 750	java/util/ArrayList:size	()I
    //   8026: iconst_1
    //   8027: isub
    //   8028: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   8031: checkcast 1410	org/telegram/tgnet/TLRPC$PhotoSize
    //   8034: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8037: putfield 823	org/telegram/messenger/SendMessagesHelper$DelayedMessage:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8040: goto -66 -> 7974
    //   8043: aload_1
    //   8044: astore 5
    //   8046: new 2139	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile
    //   8049: dup
    //   8050: invokespecial 2140	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:<init>	()V
    //   8053: astore 4
    //   8055: aload_1
    //   8056: astore 5
    //   8058: aload 4
    //   8060: aload_2
    //   8061: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8064: getfield 968	org/telegram/tgnet/TLRPC$FileLocation:volume_id	J
    //   8067: putfield 2141	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:id	J
    //   8070: aload_1
    //   8071: astore 5
    //   8073: aload 4
    //   8075: aload_2
    //   8076: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8079: getfield 2172	org/telegram/tgnet/TLRPC$FileLocation:secret	J
    //   8082: putfield 2173	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:access_hash	J
    //   8085: aload_1
    //   8086: astore 5
    //   8088: aload_3
    //   8089: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8092: aload_2
    //   8093: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8096: getfield 2169	org/telegram/tgnet/TLRPC$FileLocation:key	[B
    //   8099: putfield 947	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:key	[B
    //   8102: aload_1
    //   8103: astore 5
    //   8105: aload_3
    //   8106: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8109: aload_2
    //   8110: getfield 1702	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8113: getfield 2174	org/telegram/tgnet/TLRPC$FileLocation:iv	[B
    //   8116: putfield 951	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:iv	[B
    //   8119: aload_1
    //   8120: astore 5
    //   8122: aload_0
    //   8123: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   8126: invokestatic 956	org/telegram/messenger/SecretChatHelper:getInstance	(I)Lorg/telegram/messenger/SecretChatHelper;
    //   8129: aload_3
    //   8130: aload 13
    //   8132: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   8135: aload 39
    //   8137: aload 4
    //   8139: aconst_null
    //   8140: aload 13
    //   8142: invokevirtual 965	org/telegram/messenger/SecretChatHelper:performSendEncryptedRequest	(Lorg/telegram/tgnet/TLRPC$DecryptedMessage;Lorg/telegram/tgnet/TLRPC$Message;Lorg/telegram/tgnet/TLRPC$EncryptedChat;Lorg/telegram/tgnet/TLRPC$InputEncryptedFile;Ljava/lang/String;Lorg/telegram/messenger/MessageObject;)V
    //   8145: goto +2413 -> 10558
    //   8148: iload 21
    //   8150: iconst_3
    //   8151: if_icmpne +558 -> 8709
    //   8154: aload_1
    //   8155: astore 5
    //   8157: aload 40
    //   8159: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8162: invokestatic 2148	org/telegram/messenger/ImageLoader:fillPhotoSizeWithBytes	(Lorg/telegram/tgnet/TLRPC$PhotoSize;)V
    //   8165: aload_1
    //   8166: astore 5
    //   8168: aload 40
    //   8170: invokestatic 2177	org/telegram/messenger/MessageObject:isNewGifDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   8173: ifne +14 -> 8187
    //   8176: aload_1
    //   8177: astore 5
    //   8179: aload 40
    //   8181: invokestatic 1753	org/telegram/messenger/MessageObject:isRoundVideoDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   8184: ifeq +350 -> 8534
    //   8187: aload_1
    //   8188: astore 5
    //   8190: aload_3
    //   8191: new 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   8194: dup
    //   8195: invokespecial 2180	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:<init>	()V
    //   8198: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8201: aload_1
    //   8202: astore 5
    //   8204: aload_3
    //   8205: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8208: aload 40
    //   8210: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   8213: putfield 2181	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:attributes	Ljava/util/ArrayList;
    //   8216: aload_1
    //   8217: astore 5
    //   8219: aload 40
    //   8221: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8224: ifnull +291 -> 8515
    //   8227: aload_1
    //   8228: astore 5
    //   8230: aload 40
    //   8232: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8235: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   8238: ifnull +277 -> 8515
    //   8241: aload_1
    //   8242: astore 5
    //   8244: aload_3
    //   8245: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8248: checkcast 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   8251: aload 40
    //   8253: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8256: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   8259: putfield 2182	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:thumb	[B
    //   8262: aload_1
    //   8263: astore 5
    //   8265: aload_3
    //   8266: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8269: aload 37
    //   8271: putfield 2152	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:caption	Ljava/lang/String;
    //   8274: aload_1
    //   8275: astore 5
    //   8277: aload_3
    //   8278: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8281: ldc_w 2184
    //   8284: putfield 2185	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:mime_type	Ljava/lang/String;
    //   8287: aload_1
    //   8288: astore 5
    //   8290: aload_3
    //   8291: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8294: aload 40
    //   8296: getfield 1308	org/telegram/tgnet/TLRPC$TL_document:size	I
    //   8299: putfield 942	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:size	I
    //   8302: iconst_0
    //   8303: istore 19
    //   8305: aload_1
    //   8306: astore 5
    //   8308: iload 19
    //   8310: aload 40
    //   8312: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   8315: invokevirtual 750	java/util/ArrayList:size	()I
    //   8318: if_icmpge +72 -> 8390
    //   8321: aload_1
    //   8322: astore 5
    //   8324: aload 40
    //   8326: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   8329: iload 19
    //   8331: invokevirtual 751	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   8334: checkcast 1724	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   8337: astore_2
    //   8338: aload_1
    //   8339: astore 5
    //   8341: aload_2
    //   8342: instanceof 669
    //   8345: ifeq +2216 -> 10561
    //   8348: aload_1
    //   8349: astore 5
    //   8351: aload_3
    //   8352: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8355: aload_2
    //   8356: getfield 2186	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   8359: putfield 2166	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:w	I
    //   8362: aload_1
    //   8363: astore 5
    //   8365: aload_3
    //   8366: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8369: aload_2
    //   8370: getfield 2187	org/telegram/tgnet/TLRPC$DocumentAttribute:h	I
    //   8373: putfield 2167	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:h	I
    //   8376: aload_1
    //   8377: astore 5
    //   8379: aload_3
    //   8380: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8383: aload_2
    //   8384: getfield 1902	org/telegram/tgnet/TLRPC$DocumentAttribute:duration	I
    //   8387: putfield 2188	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:duration	I
    //   8390: aload_1
    //   8391: astore 5
    //   8393: aload_3
    //   8394: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8397: aload 40
    //   8399: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8402: getfield 2158	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   8405: putfield 2161	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_h	I
    //   8408: aload_1
    //   8409: astore 5
    //   8411: aload_3
    //   8412: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8415: aload 40
    //   8417: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8420: getfield 2162	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   8423: putfield 2165	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_w	I
    //   8426: aload_1
    //   8427: astore 5
    //   8429: aload 40
    //   8431: getfield 2189	org/telegram/tgnet/TLRPC$TL_document:key	[B
    //   8434: ifnull +10 -> 8444
    //   8437: lload 28
    //   8439: lconst_0
    //   8440: lcmp
    //   8441: ifeq +175 -> 8616
    //   8444: aload_1
    //   8445: ifnonnull +1807 -> 10252
    //   8448: aload_1
    //   8449: astore 5
    //   8451: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   8454: dup
    //   8455: aload_0
    //   8456: lload 9
    //   8458: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   8461: astore_2
    //   8462: aload_2
    //   8463: aload 39
    //   8465: putfield 960	org/telegram/messenger/SendMessagesHelper$DelayedMessage:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   8468: aload_2
    //   8469: iconst_1
    //   8470: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   8473: aload_2
    //   8474: aload_3
    //   8475: putfield 841	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendEncryptedRequest	Lorg/telegram/tgnet/TLObject;
    //   8478: aload_2
    //   8479: aload 36
    //   8481: putfield 961	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPath	Ljava/lang/String;
    //   8484: aload_2
    //   8485: aload 13
    //   8487: putfield 761	org/telegram/messenger/SendMessagesHelper$DelayedMessage:obj	Lorg/telegram/messenger/MessageObject;
    //   8490: aload_2
    //   8491: aload 7
    //   8493: putfield 866	org/telegram/messenger/SendMessagesHelper$DelayedMessage:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   8496: aload_2
    //   8497: astore_1
    //   8498: lload 28
    //   8500: lconst_0
    //   8501: lcmp
    //   8502: ifne -949 -> 7553
    //   8505: aload_0
    //   8506: aload_2
    //   8507: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   8510: aload_2
    //   8511: astore_1
    //   8512: goto -959 -> 7553
    //   8515: aload_1
    //   8516: astore 5
    //   8518: aload_3
    //   8519: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8522: checkcast 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   8525: iconst_0
    //   8526: newarray <illegal type>
    //   8528: putfield 2182	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:thumb	[B
    //   8531: goto -269 -> 8262
    //   8534: aload_1
    //   8535: astore 5
    //   8537: aload_3
    //   8538: new 2191	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaVideo
    //   8541: dup
    //   8542: invokespecial 2192	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaVideo:<init>	()V
    //   8545: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8548: aload_1
    //   8549: astore 5
    //   8551: aload 40
    //   8553: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8556: ifnull +41 -> 8597
    //   8559: aload_1
    //   8560: astore 5
    //   8562: aload 40
    //   8564: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8567: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   8570: ifnull +27 -> 8597
    //   8573: aload_1
    //   8574: astore 5
    //   8576: aload_3
    //   8577: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8580: checkcast 2191	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaVideo
    //   8583: aload 40
    //   8585: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8588: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   8591: putfield 2193	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaVideo:thumb	[B
    //   8594: goto -332 -> 8262
    //   8597: aload_1
    //   8598: astore 5
    //   8600: aload_3
    //   8601: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8604: checkcast 2191	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaVideo
    //   8607: iconst_0
    //   8608: newarray <illegal type>
    //   8610: putfield 2193	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaVideo:thumb	[B
    //   8613: goto -351 -> 8262
    //   8616: aload_1
    //   8617: astore 5
    //   8619: new 2139	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile
    //   8622: dup
    //   8623: invokespecial 2140	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:<init>	()V
    //   8626: astore_2
    //   8627: aload_1
    //   8628: astore 5
    //   8630: aload_2
    //   8631: aload 40
    //   8633: getfield 1292	org/telegram/tgnet/TLRPC$TL_document:id	J
    //   8636: putfield 2141	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:id	J
    //   8639: aload_1
    //   8640: astore 5
    //   8642: aload_2
    //   8643: aload 40
    //   8645: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   8648: putfield 2173	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:access_hash	J
    //   8651: aload_1
    //   8652: astore 5
    //   8654: aload_3
    //   8655: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8658: aload 40
    //   8660: getfield 2189	org/telegram/tgnet/TLRPC$TL_document:key	[B
    //   8663: putfield 947	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:key	[B
    //   8666: aload_1
    //   8667: astore 5
    //   8669: aload_3
    //   8670: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8673: aload 40
    //   8675: getfield 2194	org/telegram/tgnet/TLRPC$TL_document:iv	[B
    //   8678: putfield 951	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:iv	[B
    //   8681: aload_1
    //   8682: astore 5
    //   8684: aload_0
    //   8685: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   8688: invokestatic 956	org/telegram/messenger/SecretChatHelper:getInstance	(I)Lorg/telegram/messenger/SecretChatHelper;
    //   8691: aload_3
    //   8692: aload 13
    //   8694: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   8697: aload 39
    //   8699: aload_2
    //   8700: aconst_null
    //   8701: aload 13
    //   8703: invokevirtual 965	org/telegram/messenger/SecretChatHelper:performSendEncryptedRequest	(Lorg/telegram/tgnet/TLRPC$DecryptedMessage;Lorg/telegram/tgnet/TLRPC$Message;Lorg/telegram/tgnet/TLRPC$EncryptedChat;Lorg/telegram/tgnet/TLRPC$InputEncryptedFile;Ljava/lang/String;Lorg/telegram/messenger/MessageObject;)V
    //   8706: goto -1153 -> 7553
    //   8709: iload 21
    //   8711: bipush 6
    //   8713: if_icmpne +1857 -> 10570
    //   8716: aload_1
    //   8717: astore 5
    //   8719: aload_3
    //   8720: new 2196	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaContact
    //   8723: dup
    //   8724: invokespecial 2197	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaContact:<init>	()V
    //   8727: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8730: aload_1
    //   8731: astore 5
    //   8733: aload_3
    //   8734: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8737: aload 6
    //   8739: getfield 1201	org/telegram/tgnet/TLRPC$User:phone	Ljava/lang/String;
    //   8742: putfield 2198	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:phone_number	Ljava/lang/String;
    //   8745: aload_1
    //   8746: astore 5
    //   8748: aload_3
    //   8749: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8752: aload 6
    //   8754: getfield 1205	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   8757: putfield 2199	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:first_name	Ljava/lang/String;
    //   8760: aload_1
    //   8761: astore 5
    //   8763: aload_3
    //   8764: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8767: aload 6
    //   8769: getfield 1209	org/telegram/tgnet/TLRPC$User:last_name	Ljava/lang/String;
    //   8772: putfield 2200	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:last_name	Ljava/lang/String;
    //   8775: aload_1
    //   8776: astore 5
    //   8778: aload_3
    //   8779: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8782: aload 6
    //   8784: getfield 1651	org/telegram/tgnet/TLRPC$User:id	I
    //   8787: putfield 2201	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:user_id	I
    //   8790: aload_1
    //   8791: astore 5
    //   8793: aload_0
    //   8794: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   8797: invokestatic 956	org/telegram/messenger/SecretChatHelper:getInstance	(I)Lorg/telegram/messenger/SecretChatHelper;
    //   8800: aload_3
    //   8801: aload 13
    //   8803: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   8806: aload 39
    //   8808: aconst_null
    //   8809: aconst_null
    //   8810: aload 13
    //   8812: invokevirtual 965	org/telegram/messenger/SecretChatHelper:performSendEncryptedRequest	(Lorg/telegram/tgnet/TLRPC$DecryptedMessage;Lorg/telegram/tgnet/TLRPC$Message;Lorg/telegram/tgnet/TLRPC$EncryptedChat;Lorg/telegram/tgnet/TLRPC$InputEncryptedFile;Ljava/lang/String;Lorg/telegram/messenger/MessageObject;)V
    //   8815: goto -1262 -> 7553
    //   8818: aload_1
    //   8819: astore 5
    //   8821: aload 40
    //   8823: invokestatic 1722	org/telegram/messenger/MessageObject:isStickerDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   8826: ifeq +221 -> 9047
    //   8829: aload_1
    //   8830: astore 5
    //   8832: aload_3
    //   8833: new 2203	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaExternalDocument
    //   8836: dup
    //   8837: invokespecial 2204	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaExternalDocument:<init>	()V
    //   8840: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8843: aload_1
    //   8844: astore 5
    //   8846: aload_3
    //   8847: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8850: aload 40
    //   8852: getfield 1292	org/telegram/tgnet/TLRPC$TL_document:id	J
    //   8855: putfield 2205	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:id	J
    //   8858: aload_1
    //   8859: astore 5
    //   8861: aload_3
    //   8862: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8865: aload 40
    //   8867: getfield 1298	org/telegram/tgnet/TLRPC$TL_document:date	I
    //   8870: putfield 2206	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:date	I
    //   8873: aload_1
    //   8874: astore 5
    //   8876: aload_3
    //   8877: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8880: aload 40
    //   8882: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   8885: putfield 2207	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:access_hash	J
    //   8888: aload_1
    //   8889: astore 5
    //   8891: aload_3
    //   8892: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8895: aload 40
    //   8897: getfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   8900: putfield 2185	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:mime_type	Ljava/lang/String;
    //   8903: aload_1
    //   8904: astore 5
    //   8906: aload_3
    //   8907: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8910: aload 40
    //   8912: getfield 1308	org/telegram/tgnet/TLRPC$TL_document:size	I
    //   8915: putfield 942	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:size	I
    //   8918: aload_1
    //   8919: astore 5
    //   8921: aload_3
    //   8922: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8925: aload 40
    //   8927: getfield 1309	org/telegram/tgnet/TLRPC$TL_document:dc_id	I
    //   8930: putfield 2208	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:dc_id	I
    //   8933: aload_1
    //   8934: astore 5
    //   8936: aload_3
    //   8937: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8940: aload 40
    //   8942: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   8945: putfield 2181	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:attributes	Ljava/util/ArrayList;
    //   8948: aload_1
    //   8949: astore 5
    //   8951: aload 40
    //   8953: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8956: ifnonnull +70 -> 9026
    //   8959: aload_1
    //   8960: astore 5
    //   8962: aload_3
    //   8963: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8966: checkcast 2203	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaExternalDocument
    //   8969: new 1405	org/telegram/tgnet/TLRPC$TL_photoSizeEmpty
    //   8972: dup
    //   8973: invokespecial 1406	org/telegram/tgnet/TLRPC$TL_photoSizeEmpty:<init>	()V
    //   8976: putfield 2209	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaExternalDocument:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8979: aload_1
    //   8980: astore 5
    //   8982: aload_3
    //   8983: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   8986: checkcast 2203	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaExternalDocument
    //   8989: getfield 2209	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaExternalDocument:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8992: ldc_w 1408
    //   8995: putfield 1412	org/telegram/tgnet/TLRPC$PhotoSize:type	Ljava/lang/String;
    //   8998: aload_1
    //   8999: astore 5
    //   9001: aload_0
    //   9002: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   9005: invokestatic 956	org/telegram/messenger/SecretChatHelper:getInstance	(I)Lorg/telegram/messenger/SecretChatHelper;
    //   9008: aload_3
    //   9009: aload 13
    //   9011: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9014: aload 39
    //   9016: aconst_null
    //   9017: aconst_null
    //   9018: aload 13
    //   9020: invokevirtual 965	org/telegram/messenger/SecretChatHelper:performSendEncryptedRequest	(Lorg/telegram/tgnet/TLRPC$DecryptedMessage;Lorg/telegram/tgnet/TLRPC$Message;Lorg/telegram/tgnet/TLRPC$EncryptedChat;Lorg/telegram/tgnet/TLRPC$InputEncryptedFile;Ljava/lang/String;Lorg/telegram/messenger/MessageObject;)V
    //   9023: goto -1470 -> 7553
    //   9026: aload_1
    //   9027: astore 5
    //   9029: aload_3
    //   9030: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9033: checkcast 2203	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaExternalDocument
    //   9036: aload 40
    //   9038: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9041: putfield 2209	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaExternalDocument:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9044: goto -46 -> 8998
    //   9047: aload_1
    //   9048: astore 5
    //   9050: aload 40
    //   9052: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9055: invokestatic 2148	org/telegram/messenger/ImageLoader:fillPhotoSizeWithBytes	(Lorg/telegram/tgnet/TLRPC$PhotoSize;)V
    //   9058: aload_1
    //   9059: astore 5
    //   9061: aload_3
    //   9062: new 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   9065: dup
    //   9066: invokespecial 2180	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:<init>	()V
    //   9069: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9072: aload_1
    //   9073: astore 5
    //   9075: aload_3
    //   9076: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9079: aload 40
    //   9081: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   9084: putfield 2181	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:attributes	Ljava/util/ArrayList;
    //   9087: aload_1
    //   9088: astore 5
    //   9090: aload_3
    //   9091: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9094: aload 37
    //   9096: putfield 2152	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:caption	Ljava/lang/String;
    //   9099: aload_1
    //   9100: astore 5
    //   9102: aload 40
    //   9104: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9107: ifnull +195 -> 9302
    //   9110: aload_1
    //   9111: astore 5
    //   9113: aload 40
    //   9115: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9118: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   9121: ifnull +181 -> 9302
    //   9124: aload_1
    //   9125: astore 5
    //   9127: aload_3
    //   9128: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9131: checkcast 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   9134: aload 40
    //   9136: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9139: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   9142: putfield 2182	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:thumb	[B
    //   9145: aload_1
    //   9146: astore 5
    //   9148: aload_3
    //   9149: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9152: aload 40
    //   9154: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9157: getfield 2158	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   9160: putfield 2161	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_h	I
    //   9163: aload_1
    //   9164: astore 5
    //   9166: aload_3
    //   9167: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9170: aload 40
    //   9172: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9175: getfield 2162	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   9178: putfield 2165	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_w	I
    //   9181: aload_1
    //   9182: astore 5
    //   9184: aload_3
    //   9185: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9188: aload 40
    //   9190: getfield 1308	org/telegram/tgnet/TLRPC$TL_document:size	I
    //   9193: putfield 942	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:size	I
    //   9196: aload_1
    //   9197: astore 5
    //   9199: aload_3
    //   9200: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9203: aload 40
    //   9205: getfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   9208: putfield 2185	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:mime_type	Ljava/lang/String;
    //   9211: aload_1
    //   9212: astore 5
    //   9214: aload 40
    //   9216: getfield 2189	org/telegram/tgnet/TLRPC$TL_document:key	[B
    //   9219: ifnonnull +124 -> 9343
    //   9222: aload_1
    //   9223: astore 5
    //   9225: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   9228: dup
    //   9229: aload_0
    //   9230: lload 9
    //   9232: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   9235: astore_1
    //   9236: aload_1
    //   9237: aload 36
    //   9239: putfield 961	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPath	Ljava/lang/String;
    //   9242: aload_1
    //   9243: aload_3
    //   9244: putfield 841	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendEncryptedRequest	Lorg/telegram/tgnet/TLObject;
    //   9247: aload_1
    //   9248: iconst_2
    //   9249: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   9252: aload_1
    //   9253: aload 13
    //   9255: putfield 761	org/telegram/messenger/SendMessagesHelper$DelayedMessage:obj	Lorg/telegram/messenger/MessageObject;
    //   9258: aload_1
    //   9259: aload 39
    //   9261: putfield 960	org/telegram/messenger/SendMessagesHelper$DelayedMessage:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   9264: aload 11
    //   9266: ifnull +28 -> 9294
    //   9269: aload 11
    //   9271: invokevirtual 782	java/lang/String:length	()I
    //   9274: ifle +20 -> 9294
    //   9277: aload 11
    //   9279: ldc_w 1698
    //   9282: invokevirtual 788	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   9285: ifeq +9 -> 9294
    //   9288: aload_1
    //   9289: aload 11
    //   9291: putfield 800	org/telegram/messenger/SendMessagesHelper$DelayedMessage:httpLocation	Ljava/lang/String;
    //   9294: aload_0
    //   9295: aload_1
    //   9296: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   9299: goto -1746 -> 7553
    //   9302: aload_1
    //   9303: astore 5
    //   9305: aload_3
    //   9306: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9309: checkcast 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   9312: iconst_0
    //   9313: newarray <illegal type>
    //   9315: putfield 2182	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:thumb	[B
    //   9318: aload_1
    //   9319: astore 5
    //   9321: aload_3
    //   9322: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9325: iconst_0
    //   9326: putfield 2161	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_h	I
    //   9329: aload_1
    //   9330: astore 5
    //   9332: aload_3
    //   9333: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9336: iconst_0
    //   9337: putfield 2165	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_w	I
    //   9340: goto -159 -> 9181
    //   9343: aload_1
    //   9344: astore 5
    //   9346: new 2139	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile
    //   9349: dup
    //   9350: invokespecial 2140	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:<init>	()V
    //   9353: astore_2
    //   9354: aload_1
    //   9355: astore 5
    //   9357: aload_2
    //   9358: aload 40
    //   9360: getfield 1292	org/telegram/tgnet/TLRPC$TL_document:id	J
    //   9363: putfield 2141	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:id	J
    //   9366: aload_1
    //   9367: astore 5
    //   9369: aload_2
    //   9370: aload 40
    //   9372: getfield 2063	org/telegram/tgnet/TLRPC$TL_document:access_hash	J
    //   9375: putfield 2173	org/telegram/tgnet/TLRPC$TL_inputEncryptedFile:access_hash	J
    //   9378: aload_1
    //   9379: astore 5
    //   9381: aload_3
    //   9382: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9385: aload 40
    //   9387: getfield 2189	org/telegram/tgnet/TLRPC$TL_document:key	[B
    //   9390: putfield 947	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:key	[B
    //   9393: aload_1
    //   9394: astore 5
    //   9396: aload_3
    //   9397: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9400: aload 40
    //   9402: getfield 2194	org/telegram/tgnet/TLRPC$TL_document:iv	[B
    //   9405: putfield 951	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:iv	[B
    //   9408: aload_1
    //   9409: astore 5
    //   9411: aload_0
    //   9412: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   9415: invokestatic 956	org/telegram/messenger/SecretChatHelper:getInstance	(I)Lorg/telegram/messenger/SecretChatHelper;
    //   9418: aload_3
    //   9419: aload 13
    //   9421: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9424: aload 39
    //   9426: aload_2
    //   9427: aconst_null
    //   9428: aload 13
    //   9430: invokevirtual 965	org/telegram/messenger/SecretChatHelper:performSendEncryptedRequest	(Lorg/telegram/tgnet/TLRPC$DecryptedMessage;Lorg/telegram/tgnet/TLRPC$Message;Lorg/telegram/tgnet/TLRPC$EncryptedChat;Lorg/telegram/tgnet/TLRPC$InputEncryptedFile;Ljava/lang/String;Lorg/telegram/messenger/MessageObject;)V
    //   9433: goto -1880 -> 7553
    //   9436: iload 21
    //   9438: bipush 8
    //   9440: if_icmpne +1118 -> 10558
    //   9443: aload_1
    //   9444: astore 5
    //   9446: new 130	org/telegram/messenger/SendMessagesHelper$DelayedMessage
    //   9449: dup
    //   9450: aload_0
    //   9451: lload 9
    //   9453: invokespecial 1911	org/telegram/messenger/SendMessagesHelper$DelayedMessage:<init>	(Lorg/telegram/messenger/SendMessagesHelper;J)V
    //   9456: astore_1
    //   9457: aload_1
    //   9458: aload 39
    //   9460: putfield 960	org/telegram/messenger/SendMessagesHelper$DelayedMessage:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   9463: aload_1
    //   9464: aload_3
    //   9465: putfield 841	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendEncryptedRequest	Lorg/telegram/tgnet/TLObject;
    //   9468: aload_1
    //   9469: aload 13
    //   9471: putfield 761	org/telegram/messenger/SendMessagesHelper$DelayedMessage:obj	Lorg/telegram/messenger/MessageObject;
    //   9474: aload_1
    //   9475: iconst_3
    //   9476: putfield 754	org/telegram/messenger/SendMessagesHelper$DelayedMessage:type	I
    //   9479: aload_3
    //   9480: new 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   9483: dup
    //   9484: invokespecial 2180	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:<init>	()V
    //   9487: putfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9490: aload_3
    //   9491: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9494: aload 40
    //   9496: getfield 1307	org/telegram/tgnet/TLRPC$TL_document:attributes	Ljava/util/ArrayList;
    //   9499: putfield 2181	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:attributes	Ljava/util/ArrayList;
    //   9502: aload_3
    //   9503: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9506: aload 37
    //   9508: putfield 2152	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:caption	Ljava/lang/String;
    //   9511: aload 40
    //   9513: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9516: ifnull +100 -> 9616
    //   9519: aload 40
    //   9521: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9524: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   9527: ifnull +89 -> 9616
    //   9530: aload_3
    //   9531: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9534: checkcast 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   9537: aload 40
    //   9539: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9542: getfield 2155	org/telegram/tgnet/TLRPC$PhotoSize:bytes	[B
    //   9545: putfield 2182	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:thumb	[B
    //   9548: aload_3
    //   9549: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9552: aload 40
    //   9554: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9557: getfield 2158	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   9560: putfield 2161	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_h	I
    //   9563: aload_3
    //   9564: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9567: aload 40
    //   9569: getfield 1336	org/telegram/tgnet/TLRPC$TL_document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9572: getfield 2162	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   9575: putfield 2165	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_w	I
    //   9578: aload_3
    //   9579: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9582: aload 40
    //   9584: getfield 1318	org/telegram/tgnet/TLRPC$TL_document:mime_type	Ljava/lang/String;
    //   9587: putfield 2185	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:mime_type	Ljava/lang/String;
    //   9590: aload_3
    //   9591: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9594: aload 40
    //   9596: getfield 1308	org/telegram/tgnet/TLRPC$TL_document:size	I
    //   9599: putfield 942	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:size	I
    //   9602: aload_1
    //   9603: aload 36
    //   9605: putfield 961	org/telegram/messenger/SendMessagesHelper$DelayedMessage:originalPath	Ljava/lang/String;
    //   9608: aload_0
    //   9609: aload_1
    //   9610: invokespecial 280	org/telegram/messenger/SendMessagesHelper:performSendDelayedMessage	(Lorg/telegram/messenger/SendMessagesHelper$DelayedMessage;)V
    //   9613: goto -2060 -> 7553
    //   9616: aload_3
    //   9617: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9620: checkcast 2179	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument
    //   9623: iconst_0
    //   9624: newarray <illegal type>
    //   9626: putfield 2182	org/telegram/tgnet/TLRPC$TL_decryptedMessageMediaDocument:thumb	[B
    //   9629: aload_3
    //   9630: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9633: iconst_0
    //   9634: putfield 2161	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_h	I
    //   9637: aload_3
    //   9638: getfield 939	org/telegram/tgnet/TLRPC$TL_decryptedMessage:media	Lorg/telegram/tgnet/TLRPC$DecryptedMessageMedia;
    //   9641: iconst_0
    //   9642: putfield 2165	org/telegram/tgnet/TLRPC$DecryptedMessageMedia:thumb_w	I
    //   9645: goto -67 -> 9578
    //   9648: new 1015	org/telegram/tgnet/TLRPC$TL_messages_sendEncryptedMultiMedia
    //   9651: dup
    //   9652: invokespecial 2210	org/telegram/tgnet/TLRPC$TL_messages_sendEncryptedMultiMedia:<init>	()V
    //   9655: astore_2
    //   9656: aload_1
    //   9657: aload_2
    //   9658: putfield 841	org/telegram/messenger/SendMessagesHelper$DelayedMessage:sendEncryptedRequest	Lorg/telegram/tgnet/TLObject;
    //   9661: goto -2086 -> 7575
    //   9664: lconst_0
    //   9665: lstore 26
    //   9667: goto -2031 -> 7636
    //   9670: iload 21
    //   9672: iconst_4
    //   9673: if_icmpne +346 -> 10019
    //   9676: aload_1
    //   9677: astore 5
    //   9679: new 2212	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages
    //   9682: dup
    //   9683: invokespecial 2213	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:<init>	()V
    //   9686: astore_2
    //   9687: aload_1
    //   9688: astore 5
    //   9690: aload_2
    //   9691: aload 38
    //   9693: putfield 2216	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:to_peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   9696: aload_1
    //   9697: astore 5
    //   9699: aload_2
    //   9700: aload 15
    //   9702: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9705: getfield 2219	org/telegram/tgnet/TLRPC$Message:with_my_score	Z
    //   9708: putfield 2220	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:with_my_score	Z
    //   9711: aload_1
    //   9712: astore 5
    //   9714: aload 15
    //   9716: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9719: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   9722: ifeq +199 -> 9921
    //   9725: aload_1
    //   9726: astore 5
    //   9728: aload_0
    //   9729: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   9732: invokestatic 1529	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   9735: aload 15
    //   9737: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9740: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   9743: ineg
    //   9744: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   9747: invokevirtual 1573	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   9750: astore_3
    //   9751: aload_1
    //   9752: astore 5
    //   9754: aload_2
    //   9755: new 1564	org/telegram/tgnet/TLRPC$TL_inputPeerChannel
    //   9758: dup
    //   9759: invokespecial 2221	org/telegram/tgnet/TLRPC$TL_inputPeerChannel:<init>	()V
    //   9762: putfield 2224	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:from_peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   9765: aload_1
    //   9766: astore 5
    //   9768: aload_2
    //   9769: getfield 2224	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:from_peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   9772: aload 15
    //   9774: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9777: getfield 1696	org/telegram/tgnet/TLRPC$Message:ttl	I
    //   9780: ineg
    //   9781: putfield 1569	org/telegram/tgnet/TLRPC$InputPeer:channel_id	I
    //   9784: aload_3
    //   9785: ifnull +17 -> 9802
    //   9788: aload_1
    //   9789: astore 5
    //   9791: aload_2
    //   9792: getfield 2224	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:from_peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   9795: aload_3
    //   9796: getfield 2225	org/telegram/tgnet/TLRPC$Chat:access_hash	J
    //   9799: putfield 1869	org/telegram/tgnet/TLRPC$InputPeer:access_hash	J
    //   9802: aload_1
    //   9803: astore 5
    //   9805: aload 15
    //   9807: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9810: getfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   9813: instanceof 1938
    //   9816: ifeq +44 -> 9860
    //   9819: aload_1
    //   9820: astore 5
    //   9822: aload_2
    //   9823: aload_0
    //   9824: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   9827: invokestatic 1942	org/telegram/messenger/MessagesController:getNotificationsSettings	(I)Landroid/content/SharedPreferences;
    //   9830: new 507	java/lang/StringBuilder
    //   9833: dup
    //   9834: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   9837: ldc_w 1944
    //   9840: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   9843: lload 9
    //   9845: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   9848: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   9851: iconst_0
    //   9852: invokeinterface 1948 3 0
    //   9857: putfield 2226	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:silent	Z
    //   9860: aload_1
    //   9861: astore 5
    //   9863: aload_2
    //   9864: getfield 2227	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:random_id	Ljava/util/ArrayList;
    //   9867: aload 8
    //   9869: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   9872: invokestatic 1882	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   9875: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   9878: pop
    //   9879: aload_1
    //   9880: astore 5
    //   9882: aload 15
    //   9884: invokevirtual 766	org/telegram/messenger/MessageObject:getId	()I
    //   9887: iflt +51 -> 9938
    //   9890: aload_1
    //   9891: astore 5
    //   9893: aload_2
    //   9894: getfield 2229	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:id	Ljava/util/ArrayList;
    //   9897: aload 15
    //   9899: invokevirtual 766	org/telegram/messenger/MessageObject:getId	()I
    //   9902: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   9905: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   9908: pop
    //   9909: aload_1
    //   9910: astore 5
    //   9912: aload_0
    //   9913: aload_2
    //   9914: aload 13
    //   9916: aconst_null
    //   9917: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   9920: return
    //   9921: aload_1
    //   9922: astore 5
    //   9924: aload_2
    //   9925: new 2231	org/telegram/tgnet/TLRPC$TL_inputPeerEmpty
    //   9928: dup
    //   9929: invokespecial 2232	org/telegram/tgnet/TLRPC$TL_inputPeerEmpty:<init>	()V
    //   9932: putfield 2224	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:from_peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   9935: goto -133 -> 9802
    //   9938: aload_1
    //   9939: astore 5
    //   9941: aload 15
    //   9943: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9946: getfield 2235	org/telegram/tgnet/TLRPC$Message:fwd_msg_id	I
    //   9949: ifeq +28 -> 9977
    //   9952: aload_1
    //   9953: astore 5
    //   9955: aload_2
    //   9956: getfield 2229	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:id	Ljava/util/ArrayList;
    //   9959: aload 15
    //   9961: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9964: getfield 2235	org/telegram/tgnet/TLRPC$Message:fwd_msg_id	I
    //   9967: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   9970: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   9973: pop
    //   9974: goto -65 -> 9909
    //   9977: aload_1
    //   9978: astore 5
    //   9980: aload 15
    //   9982: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9985: getfield 2239	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   9988: ifnull -79 -> 9909
    //   9991: aload_1
    //   9992: astore 5
    //   9994: aload_2
    //   9995: getfield 2229	org/telegram/tgnet/TLRPC$TL_messages_forwardMessages:id	Ljava/util/ArrayList;
    //   9998: aload 15
    //   10000: getfield 873	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   10003: getfield 2239	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   10006: getfield 2244	org/telegram/tgnet/TLRPC$MessageFwdHeader:channel_post	I
    //   10009: invokestatic 1537	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   10012: invokevirtual 1219	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   10015: pop
    //   10016: goto -107 -> 9909
    //   10019: iload 21
    //   10021: bipush 9
    //   10023: if_icmpne +208 -> 10231
    //   10026: aload_1
    //   10027: astore 5
    //   10029: new 2246	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult
    //   10032: dup
    //   10033: invokespecial 2247	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:<init>	()V
    //   10036: astore_2
    //   10037: aload_1
    //   10038: astore 5
    //   10040: aload_2
    //   10041: aload 38
    //   10043: putfield 2248	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   10046: aload_1
    //   10047: astore 5
    //   10049: aload_2
    //   10050: aload 8
    //   10052: getfield 1584	org/telegram/tgnet/TLRPC$Message:random_id	J
    //   10055: putfield 2249	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:random_id	J
    //   10058: aload_1
    //   10059: astore 5
    //   10061: aload 8
    //   10063: getfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   10066: ifeq +28 -> 10094
    //   10069: aload_1
    //   10070: astore 5
    //   10072: aload_2
    //   10073: aload_2
    //   10074: getfield 2250	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:flags	I
    //   10077: iconst_1
    //   10078: ior
    //   10079: putfield 2250	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:flags	I
    //   10082: aload_1
    //   10083: astore 5
    //   10085: aload_2
    //   10086: aload 8
    //   10088: getfield 1617	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   10091: putfield 2251	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:reply_to_msg_id	I
    //   10094: aload_1
    //   10095: astore 5
    //   10097: aload 8
    //   10099: getfield 1807	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   10102: instanceof 1938
    //   10105: ifeq +44 -> 10149
    //   10108: aload_1
    //   10109: astore 5
    //   10111: aload_2
    //   10112: aload_0
    //   10113: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   10116: invokestatic 1942	org/telegram/messenger/MessagesController:getNotificationsSettings	(I)Landroid/content/SharedPreferences;
    //   10119: new 507	java/lang/StringBuilder
    //   10122: dup
    //   10123: invokespecial 508	java/lang/StringBuilder:<init>	()V
    //   10126: ldc_w 1944
    //   10129: invokevirtual 514	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   10132: lload 9
    //   10134: invokevirtual 899	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   10137: invokevirtual 517	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   10140: iconst_0
    //   10141: invokeinterface 1948 3 0
    //   10146: putfield 2252	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:silent	Z
    //   10149: aload_1
    //   10150: astore 5
    //   10152: aload_2
    //   10153: aload 41
    //   10155: ldc_w 1628
    //   10158: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   10161: checkcast 489	java/lang/String
    //   10164: invokestatic 1836	org/telegram/messenger/Utilities:parseLong	(Ljava/lang/String;)Ljava/lang/Long;
    //   10167: invokevirtual 1839	java/lang/Long:longValue	()J
    //   10170: putfield 2254	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:query_id	J
    //   10173: aload_1
    //   10174: astore 5
    //   10176: aload_2
    //   10177: aload 41
    //   10179: ldc_w 2255
    //   10182: invokevirtual 1502	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   10185: checkcast 489	java/lang/String
    //   10188: putfield 2257	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:id	Ljava/lang/String;
    //   10191: aload 15
    //   10193: ifnonnull +27 -> 10220
    //   10196: aload_1
    //   10197: astore 5
    //   10199: aload_2
    //   10200: iconst_1
    //   10201: putfield 2258	org/telegram/tgnet/TLRPC$TL_messages_sendInlineBotResult:clear_draft	Z
    //   10204: aload_1
    //   10205: astore 5
    //   10207: aload_0
    //   10208: getfield 253	org/telegram/messenger/SendMessagesHelper:currentAccount	I
    //   10211: invokestatic 1761	org/telegram/messenger/DataQuery:getInstance	(I)Lorg/telegram/messenger/DataQuery;
    //   10214: lload 9
    //   10216: iconst_0
    //   10217: invokevirtual 1963	org/telegram/messenger/DataQuery:cleanDraft	(JZ)V
    //   10220: aload_1
    //   10221: astore 5
    //   10223: aload_0
    //   10224: aload_2
    //   10225: aload 13
    //   10227: aconst_null
    //   10228: invokespecial 334	org/telegram/messenger/SendMessagesHelper:performSendMessageRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/messenger/MessageObject;Ljava/lang/String;)V
    //   10231: return
    //   10232: astore_1
    //   10233: aconst_null
    //   10234: astore_2
    //   10235: aload 31
    //   10237: astore 8
    //   10239: goto -9445 -> 794
    //   10242: astore_1
    //   10243: aconst_null
    //   10244: astore_2
    //   10245: aload 31
    //   10247: astore 8
    //   10249: goto -9455 -> 794
    //   10252: aload_1
    //   10253: astore_2
    //   10254: goto -1764 -> 8490
    //   10257: aload_1
    //   10258: astore_2
    //   10259: goto -2310 -> 7949
    //   10262: goto -4939 -> 5323
    //   10265: goto -4334 -> 5931
    //   10268: goto -4631 -> 5637
    //   10271: goto -5727 -> 4544
    //   10274: aconst_null
    //   10275: astore_1
    //   10276: goto -5811 -> 4465
    //   10279: aload 31
    //   10281: astore 8
    //   10283: aload 13
    //   10285: astore 33
    //   10287: aload 18
    //   10289: astore 32
    //   10291: goto -8716 -> 1575
    //   10294: iconst_0
    //   10295: istore 20
    //   10297: aload_1
    //   10298: astore 32
    //   10300: aload_3
    //   10301: astore 33
    //   10303: aload 4
    //   10305: astore 34
    //   10307: aload 6
    //   10309: astore 35
    //   10311: goto -9410 -> 901
    //   10314: aconst_null
    //   10315: astore 31
    //   10317: goto -8802 -> 1515
    //   10320: iconst_0
    //   10321: istore 20
    //   10323: goto -8765 -> 1558
    //   10326: iconst_1
    //   10327: istore 20
    //   10329: aload 13
    //   10331: astore 33
    //   10333: aload 18
    //   10335: astore 32
    //   10337: goto -8762 -> 1575
    //   10340: iconst_2
    //   10341: istore 20
    //   10343: goto -8302 -> 2041
    //   10346: bipush 6
    //   10348: istore 20
    //   10350: aload 13
    //   10352: astore 33
    //   10354: aload 18
    //   10356: astore 32
    //   10358: goto -8783 -> 1575
    //   10361: iconst_3
    //   10362: istore 21
    //   10364: goto -7753 -> 2611
    //   10367: bipush 7
    //   10369: istore 21
    //   10371: goto -7760 -> 2611
    //   10374: iload 23
    //   10376: iconst_1
    //   10377: iadd
    //   10378: istore 23
    //   10380: goto -7621 -> 2759
    //   10383: astore_1
    //   10384: aconst_null
    //   10385: astore_2
    //   10386: goto -9592 -> 794
    //   10389: iload 21
    //   10391: ifeq -6584 -> 3807
    //   10394: iload 21
    //   10396: bipush 9
    //   10398: if_icmpne +49 -> 10447
    //   10401: aload 43
    //   10403: ifnull +44 -> 10447
    //   10406: aload 39
    //   10408: ifnull +39 -> 10447
    //   10411: goto -6604 -> 3807
    //   10414: iload 20
    //   10416: iconst_1
    //   10417: iadd
    //   10418: istore 20
    //   10420: goto -6332 -> 4088
    //   10423: iconst_0
    //   10424: istore 23
    //   10426: iconst_0
    //   10427: istore 20
    //   10429: goto -6124 -> 4305
    //   10432: return
    //   10433: iconst_0
    //   10434: istore 30
    //   10436: goto -5795 -> 4641
    //   10439: return
    //   10440: astore_1
    //   10441: aload 13
    //   10443: astore_2
    //   10444: goto -9650 -> 794
    //   10447: iload 21
    //   10449: iconst_1
    //   10450: if_icmplt +9 -> 10459
    //   10453: iload 21
    //   10455: iconst_3
    //   10456: if_icmple -5276 -> 5180
    //   10459: iload 21
    //   10461: iconst_5
    //   10462: if_icmplt +10 -> 10472
    //   10465: iload 21
    //   10467: bipush 8
    //   10469: if_icmple -5289 -> 5180
    //   10472: iload 21
    //   10474: bipush 9
    //   10476: if_icmpne -806 -> 9670
    //   10479: aload 39
    //   10481: ifnull -811 -> 9670
    //   10484: goto -5304 -> 5180
    //   10487: iload 21
    //   10489: iconst_2
    //   10490: if_icmpeq -5058 -> 5432
    //   10493: iload 21
    //   10495: bipush 9
    //   10497: if_icmpne -4738 -> 5759
    //   10500: aload 4
    //   10502: ifnull -4743 -> 5759
    //   10505: goto -5073 -> 5432
    //   10508: astore_1
    //   10509: aload 13
    //   10511: astore_2
    //   10512: goto -9718 -> 794
    //   10515: iload 21
    //   10517: bipush 7
    //   10519: if_icmpeq -4452 -> 6067
    //   10522: iload 21
    //   10524: bipush 9
    //   10526: if_icmpne -4165 -> 6361
    //   10529: goto -4462 -> 6067
    //   10532: aload_3
    //   10533: astore_2
    //   10534: goto -3933 -> 6601
    //   10537: iload 21
    //   10539: iconst_2
    //   10540: if_icmpeq -2834 -> 7706
    //   10543: iload 21
    //   10545: bipush 9
    //   10547: if_icmpne -2399 -> 8148
    //   10550: aload 4
    //   10552: ifnull -2404 -> 8148
    //   10555: goto -2849 -> 7706
    //   10558: goto -3005 -> 7553
    //   10561: iload 19
    //   10563: iconst_1
    //   10564: iadd
    //   10565: istore 19
    //   10567: goto -2262 -> 8305
    //   10570: iload 21
    //   10572: bipush 7
    //   10574: if_icmpeq -1756 -> 8818
    //   10577: iload 21
    //   10579: bipush 9
    //   10581: if_icmpne -1145 -> 9436
    //   10584: aload 40
    //   10586: ifnull -1150 -> 9436
    //   10589: goto -1771 -> 8818
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	10592	0	this	SendMessagesHelper
    //   0	10592	1	paramString1	String
    //   0	10592	2	paramString2	String
    //   0	10592	3	paramMessageMedia	TLRPC.MessageMedia
    //   0	10592	4	paramTL_photo	TLRPC.TL_photo
    //   0	10592	5	paramVideoEditedInfo	VideoEditedInfo
    //   0	10592	6	paramUser	TLRPC.User
    //   0	10592	7	paramTL_document	TLRPC.TL_document
    //   0	10592	8	paramTL_game	TLRPC.TL_game
    //   0	10592	9	paramLong	long
    //   0	10592	11	paramString3	String
    //   0	10592	12	paramMessageObject1	MessageObject
    //   0	10592	13	paramWebPage	TLRPC.WebPage
    //   0	10592	14	paramBoolean	boolean
    //   0	10592	15	paramMessageObject2	MessageObject
    //   0	10592	16	paramArrayList	ArrayList<TLRPC.MessageEntity>
    //   0	10592	17	paramReplyMarkup	TLRPC.ReplyMarkup
    //   0	10592	18	paramHashMap	HashMap<String, String>
    //   0	10592	19	paramInt	int
    //   70	10358	20	i	int
    //   293	10289	21	j	int
    //   86	4301	22	k	int
    //   2757	7668	23	m	int
    //   83	3328	24	n	int
    //   75	3845	25	i1	int
    //   3556	6110	26	l1	long
    //   3563	4936	28	l2	long
    //   2203	8232	30	bool	boolean
    //   65	10251	31	localObject1	Object
    //   89	10268	32	localObject2	Object
    //   247	10106	33	localObject3	Object
    //   917	9389	34	localObject4	Object
    //   921	9389	35	localObject5	Object
    //   30	9574	36	str1	String
    //   9	9498	37	str2	String
    //   108	9934	38	localInputPeer	TLRPC.InputPeer
    //   137	10343	39	localObject6	Object
    //   305	10280	40	localTL_document	TLRPC.TL_document
    //   297	9881	41	localObject7	Object
    //   308	7210	42	localObject8	Object
    //   311	10091	43	localObject9	Object
    //   301	4777	44	localObject10	Object
    //   111	4283	45	localObject11	Object
    // Exception table:
    //   from	to	target	type
    //   274	281	786	java/lang/Exception
    //   284	292	786	java/lang/Exception
    //   320	330	786	java/lang/Exception
    //   333	342	786	java/lang/Exception
    //   350	361	786	java/lang/Exception
    //   369	385	786	java/lang/Exception
    //   388	396	786	java/lang/Exception
    //   399	407	786	java/lang/Exception
    //   410	424	786	java/lang/Exception
    //   427	434	786	java/lang/Exception
    //   442	450	786	java/lang/Exception
    //   453	468	786	java/lang/Exception
    //   471	479	786	java/lang/Exception
    //   487	493	786	java/lang/Exception
    //   496	510	786	java/lang/Exception
    //   513	532	786	java/lang/Exception
    //   539	546	786	java/lang/Exception
    //   549	563	786	java/lang/Exception
    //   566	572	786	java/lang/Exception
    //   575	589	786	java/lang/Exception
    //   592	599	786	java/lang/Exception
    //   612	625	786	java/lang/Exception
    //   628	641	786	java/lang/Exception
    //   644	657	786	java/lang/Exception
    //   660	670	786	java/lang/Exception
    //   683	696	786	java/lang/Exception
    //   699	706	786	java/lang/Exception
    //   720	727	786	java/lang/Exception
    //   730	742	786	java/lang/Exception
    //   745	773	786	java/lang/Exception
    //   776	785	786	java/lang/Exception
    //   873	881	786	java/lang/Exception
    //   884	898	786	java/lang/Exception
    //   981	992	786	java/lang/Exception
    //   1034	1040	786	java/lang/Exception
    //   1046	1055	786	java/lang/Exception
    //   1058	1065	786	java/lang/Exception
    //   1085	1094	786	java/lang/Exception
    //   1097	1110	786	java/lang/Exception
    //   1129	1138	786	java/lang/Exception
    //   1141	1150	786	java/lang/Exception
    //   1161	1174	786	java/lang/Exception
    //   1194	1204	786	java/lang/Exception
    //   1207	1216	786	java/lang/Exception
    //   1288	1298	786	java/lang/Exception
    //   1301	1311	786	java/lang/Exception
    //   1314	1324	786	java/lang/Exception
    //   1327	1337	786	java/lang/Exception
    //   1340	1353	786	java/lang/Exception
    //   1391	1400	786	java/lang/Exception
    //   1403	1416	786	java/lang/Exception
    //   1446	1455	786	java/lang/Exception
    //   1471	1479	786	java/lang/Exception
    //   1482	1490	786	java/lang/Exception
    //   1493	1502	786	java/lang/Exception
    //   1505	1515	786	java/lang/Exception
    //   1523	1535	786	java/lang/Exception
    //   1543	1554	786	java/lang/Exception
    //   1561	1567	786	java/lang/Exception
    //   1583	1591	786	java/lang/Exception
    //   1594	1601	786	java/lang/Exception
    //   1604	1618	786	java/lang/Exception
    //   1626	1633	786	java/lang/Exception
    //   1636	1644	786	java/lang/Exception
    //   1647	1655	786	java/lang/Exception
    //   1658	1670	786	java/lang/Exception
    //   1673	1680	786	java/lang/Exception
    //   1683	1690	786	java/lang/Exception
    //   1693	1699	786	java/lang/Exception
    //   1712	1723	786	java/lang/Exception
    //   1726	1737	786	java/lang/Exception
    //   1762	1771	786	java/lang/Exception
    //   1777	1789	786	java/lang/Exception
    //   1792	1802	786	java/lang/Exception
    //   1814	1823	786	java/lang/Exception
    //   1826	1832	786	java/lang/Exception
    //   1840	1851	786	java/lang/Exception
    //   1866	1875	786	java/lang/Exception
    //   1888	1897	786	java/lang/Exception
    //   1900	1912	786	java/lang/Exception
    //   1915	1922	786	java/lang/Exception
    //   1925	1937	786	java/lang/Exception
    //   1945	1952	786	java/lang/Exception
    //   1960	1970	786	java/lang/Exception
    //   1973	1980	786	java/lang/Exception
    //   1983	1990	786	java/lang/Exception
    //   1993	2005	786	java/lang/Exception
    //   2008	2018	786	java/lang/Exception
    //   2026	2037	786	java/lang/Exception
    //   2049	2057	786	java/lang/Exception
    //   2060	2071	786	java/lang/Exception
    //   2074	2081	786	java/lang/Exception
    //   2092	2101	786	java/lang/Exception
    //   2107	2143	786	java/lang/Exception
    //   2159	2168	786	java/lang/Exception
    //   2239	2248	786	java/lang/Exception
    //   2251	2263	786	java/lang/Exception
    //   2266	2279	786	java/lang/Exception
    //   2282	2295	786	java/lang/Exception
    //   2298	2311	786	java/lang/Exception
    //   2314	2327	786	java/lang/Exception
    //   2330	2341	786	java/lang/Exception
    //   2344	2355	786	java/lang/Exception
    //   2358	2366	786	java/lang/Exception
    //   2369	2380	786	java/lang/Exception
    //   2383	2394	786	java/lang/Exception
    //   2397	2405	786	java/lang/Exception
    //   2413	2424	786	java/lang/Exception
    //   2439	2448	786	java/lang/Exception
    //   2473	2482	786	java/lang/Exception
    //   2485	2497	786	java/lang/Exception
    //   2500	2507	786	java/lang/Exception
    //   2510	2522	786	java/lang/Exception
    //   2530	2540	786	java/lang/Exception
    //   2543	2550	786	java/lang/Exception
    //   2553	2560	786	java/lang/Exception
    //   2563	2575	786	java/lang/Exception
    //   2578	2588	786	java/lang/Exception
    //   2596	2607	786	java/lang/Exception
    //   2623	2630	786	java/lang/Exception
    //   2642	2651	786	java/lang/Exception
    //   2654	2665	786	java/lang/Exception
    //   2673	2681	786	java/lang/Exception
    //   2684	2692	786	java/lang/Exception
    //   2695	2708	786	java/lang/Exception
    //   2748	2756	786	java/lang/Exception
    //   2778	2791	786	java/lang/Exception
    //   2794	2809	786	java/lang/Exception
    //   2812	2820	786	java/lang/Exception
    //   2823	2834	786	java/lang/Exception
    //   2837	2846	786	java/lang/Exception
    //   2849	2860	786	java/lang/Exception
    //   2863	2873	786	java/lang/Exception
    //   2876	2884	786	java/lang/Exception
    //   2887	2898	786	java/lang/Exception
    //   2901	2911	786	java/lang/Exception
    //   2914	2922	786	java/lang/Exception
    //   2925	2937	786	java/lang/Exception
    //   2940	2950	786	java/lang/Exception
    //   2969	2978	786	java/lang/Exception
    //   2984	2992	786	java/lang/Exception
    //   2995	3003	786	java/lang/Exception
    //   3014	3022	786	java/lang/Exception
    //   3032	3039	786	java/lang/Exception
    //   3045	3065	786	java/lang/Exception
    //   3071	3083	786	java/lang/Exception
    //   3105	3117	786	java/lang/Exception
    //   3139	3147	786	java/lang/Exception
    //   3150	3158	786	java/lang/Exception
    //   3164	3179	786	java/lang/Exception
    //   3182	3196	786	java/lang/Exception
    //   3202	3224	786	java/lang/Exception
    //   3230	3236	786	java/lang/Exception
    //   3239	3246	786	java/lang/Exception
    //   3249	3264	786	java/lang/Exception
    //   3270	3276	786	java/lang/Exception
    //   3282	3295	786	java/lang/Exception
    //   3301	3309	786	java/lang/Exception
    //   3416	3424	786	java/lang/Exception
    //   3427	3435	786	java/lang/Exception
    //   3438	3444	786	java/lang/Exception
    //   3447	3453	786	java/lang/Exception
    //   3456	3472	786	java/lang/Exception
    //   3884	3901	786	java/lang/Exception
    //   3912	3928	786	java/lang/Exception
    //   3935	3944	786	java/lang/Exception
    //   3951	3958	786	java/lang/Exception
    //   3961	3967	786	java/lang/Exception
    //   3976	3988	786	java/lang/Exception
    //   3991	4009	786	java/lang/Exception
    //   4012	4025	786	java/lang/Exception
    //   4033	4040	786	java/lang/Exception
    //   4046	4054	786	java/lang/Exception
    //   4060	4071	786	java/lang/Exception
    //   4074	4082	786	java/lang/Exception
    //   4095	4114	786	java/lang/Exception
    //   4117	4137	786	java/lang/Exception
    //   4140	4147	786	java/lang/Exception
    //   4150	4156	786	java/lang/Exception
    //   4159	4176	786	java/lang/Exception
    //   4185	4198	786	java/lang/Exception
    //   4204	4214	786	java/lang/Exception
    //   4217	4225	786	java/lang/Exception
    //   4228	4236	786	java/lang/Exception
    //   4239	4252	786	java/lang/Exception
    //   4255	4261	786	java/lang/Exception
    //   4264	4274	786	java/lang/Exception
    //   4280	4288	786	java/lang/Exception
    //   4294	4302	786	java/lang/Exception
    //   4312	4331	786	java/lang/Exception
    //   4334	4354	786	java/lang/Exception
    //   4357	4364	786	java/lang/Exception
    //   4367	4373	786	java/lang/Exception
    //   4376	4393	786	java/lang/Exception
    //   1216	1268	10232	java/lang/Exception
    //   2168	2190	10242	java/lang/Exception
    //   2195	2205	10242	java/lang/Exception
    //   3309	3323	10383	java/lang/Exception
    //   3323	3372	10383	java/lang/Exception
    //   3376	3382	10383	java/lang/Exception
    //   3385	3407	10383	java/lang/Exception
    //   3721	3727	10440	java/lang/Exception
    //   3735	3804	10440	java/lang/Exception
    //   3819	3827	10440	java/lang/Exception
    //   3830	3839	10440	java/lang/Exception
    //   3845	3854	10440	java/lang/Exception
    //   3857	3872	10440	java/lang/Exception
    //   4472	4483	10440	java/lang/Exception
    //   4564	4570	10440	java/lang/Exception
    //   4573	4578	10440	java/lang/Exception
    //   4581	4592	10440	java/lang/Exception
    //   4595	4601	10440	java/lang/Exception
    //   4604	4612	10440	java/lang/Exception
    //   4616	4624	10440	java/lang/Exception
    //   4627	4633	10440	java/lang/Exception
    //   4644	4650	10440	java/lang/Exception
    //   4653	4664	10440	java/lang/Exception
    //   4667	4705	10440	java/lang/Exception
    //   4708	4714	10440	java/lang/Exception
    //   4717	4726	10440	java/lang/Exception
    //   4729	4737	10440	java/lang/Exception
    //   4740	4750	10440	java/lang/Exception
    //   4753	4762	10440	java/lang/Exception
    //   4770	4775	10440	java/lang/Exception
    //   4783	4791	10440	java/lang/Exception
    //   4794	4800	10440	java/lang/Exception
    //   4803	4814	10440	java/lang/Exception
    //   4817	4825	10440	java/lang/Exception
    //   4833	4846	10440	java/lang/Exception
    //   4852	4865	10440	java/lang/Exception
    //   4868	4876	10440	java/lang/Exception
    //   4879	4888	10440	java/lang/Exception
    //   4896	4904	10440	java/lang/Exception
    //   4907	4913	10440	java/lang/Exception
    //   4916	4928	10440	java/lang/Exception
    //   4931	4941	10440	java/lang/Exception
    //   4944	4953	10440	java/lang/Exception
    //   4956	4967	10440	java/lang/Exception
    //   4975	4986	10440	java/lang/Exception
    //   4989	5004	10440	java/lang/Exception
    //   5007	5019	10440	java/lang/Exception
    //   5022	5031	10440	java/lang/Exception
    //   5034	5040	10440	java/lang/Exception
    //   5048	5056	10440	java/lang/Exception
    //   5059	5070	10440	java/lang/Exception
    //   5073	5085	10440	java/lang/Exception
    //   5088	5100	10440	java/lang/Exception
    //   5103	5125	10440	java/lang/Exception
    //   5133	5146	10440	java/lang/Exception
    //   5152	5160	10440	java/lang/Exception
    //   5166	5177	10440	java/lang/Exception
    //   5196	5204	10440	java/lang/Exception
    //   5207	5215	10440	java/lang/Exception
    //   5218	5227	10440	java/lang/Exception
    //   5230	5239	10440	java/lang/Exception
    //   5242	5251	10440	java/lang/Exception
    //   5254	5263	10440	java/lang/Exception
    //   5266	5273	10440	java/lang/Exception
    //   5276	5287	10440	java/lang/Exception
    //   5290	5305	10440	java/lang/Exception
    //   5308	5323	10440	java/lang/Exception
    //   5384	5392	10440	java/lang/Exception
    //   5395	5403	10440	java/lang/Exception
    //   5406	5415	10440	java/lang/Exception
    //   5421	5429	10440	java/lang/Exception
    //   5435	5445	10440	java/lang/Exception
    //   5448	5456	10440	java/lang/Exception
    //   5464	5470	10440	java/lang/Exception
    //   5473	5480	10440	java/lang/Exception
    //   5483	5493	10440	java/lang/Exception
    //   5501	5514	10440	java/lang/Exception
    //   5522	5536	10440	java/lang/Exception
    //   5539	5547	10440	java/lang/Exception
    //   5560	5580	10440	java/lang/Exception
    //   5592	5602	10440	java/lang/Exception
    //   5609	5620	10440	java/lang/Exception
    //   5704	5712	10440	java/lang/Exception
    //   5715	5726	10440	java/lang/Exception
    //   5729	5741	10440	java/lang/Exception
    //   5744	5756	10440	java/lang/Exception
    //   5768	5778	10440	java/lang/Exception
    //   5781	5789	10440	java/lang/Exception
    //   5792	5801	10440	java/lang/Exception
    //   5804	5813	10440	java/lang/Exception
    //   5816	5824	10440	java/lang/Exception
    //   5832	5840	10440	java/lang/Exception
    //   5843	5851	10440	java/lang/Exception
    //   5854	5859	10440	java/lang/Exception
    //   5867	5873	10440	java/lang/Exception
    //   5876	5883	10440	java/lang/Exception
    //   5886	5896	10440	java/lang/Exception
    //   5903	5914	10440	java/lang/Exception
    //   5955	5963	10440	java/lang/Exception
    //   5966	5977	10440	java/lang/Exception
    //   5980	5992	10440	java/lang/Exception
    //   5995	6007	10440	java/lang/Exception
    //   6020	6028	10440	java/lang/Exception
    //   6031	6040	10440	java/lang/Exception
    //   6043	6052	10440	java/lang/Exception
    //   6055	6064	10440	java/lang/Exception
    //   6070	6080	10440	java/lang/Exception
    //   6093	6101	10440	java/lang/Exception
    //   6104	6115	10440	java/lang/Exception
    //   6123	6131	10440	java/lang/Exception
    //   6134	6153	10440	java/lang/Exception
    //   6156	6163	10440	java/lang/Exception
    //   6166	6177	10440	java/lang/Exception
    //   6180	6188	10440	java/lang/Exception
    //   6212	6220	10440	java/lang/Exception
    //   6228	6234	10440	java/lang/Exception
    //   6237	6244	10440	java/lang/Exception
    //   6247	6257	10440	java/lang/Exception
    //   6260	6271	10440	java/lang/Exception
    //   6306	6314	10440	java/lang/Exception
    //   6317	6328	10440	java/lang/Exception
    //   6331	6343	10440	java/lang/Exception
    //   6346	6358	10440	java/lang/Exception
    //   6371	6381	10440	java/lang/Exception
    //   6384	6392	10440	java/lang/Exception
    //   6395	6404	10440	java/lang/Exception
    //   6407	6416	10440	java/lang/Exception
    //   6424	6430	10440	java/lang/Exception
    //   6433	6440	10440	java/lang/Exception
    //   6443	6453	10440	java/lang/Exception
    //   6456	6467	10440	java/lang/Exception
    //   6484	6492	10440	java/lang/Exception
    //   6495	6506	10440	java/lang/Exception
    //   6509	6521	10440	java/lang/Exception
    //   6524	6536	10440	java/lang/Exception
    //   7162	7175	10440	java/lang/Exception
    //   7178	7186	10440	java/lang/Exception
    //   7198	7204	10440	java/lang/Exception
    //   7207	7219	10440	java/lang/Exception
    //   7224	7233	10440	java/lang/Exception
    //   7241	7249	10440	java/lang/Exception
    //   7252	7258	10440	java/lang/Exception
    //   7261	7273	10440	java/lang/Exception
    //   7276	7286	10440	java/lang/Exception
    //   7289	7298	10440	java/lang/Exception
    //   7301	7312	10440	java/lang/Exception
    //   7315	7327	10440	java/lang/Exception
    //   7335	7346	10440	java/lang/Exception
    //   7349	7364	10440	java/lang/Exception
    //   7367	7379	10440	java/lang/Exception
    //   7382	7391	10440	java/lang/Exception
    //   7394	7401	10440	java/lang/Exception
    //   7410	7418	10440	java/lang/Exception
    //   7421	7432	10440	java/lang/Exception
    //   7435	7447	10440	java/lang/Exception
    //   7450	7462	10440	java/lang/Exception
    //   7465	7477	10440	java/lang/Exception
    //   7480	7492	10440	java/lang/Exception
    //   7495	7510	10440	java/lang/Exception
    //   7513	7528	10440	java/lang/Exception
    //   7531	7553	10440	java/lang/Exception
    //   7678	7686	10440	java/lang/Exception
    //   7692	7703	10440	java/lang/Exception
    //   7709	7723	10440	java/lang/Exception
    //   7726	7748	10440	java/lang/Exception
    //   7751	7756	10440	java/lang/Exception
    //   7759	7770	10440	java/lang/Exception
    //   7773	7782	10440	java/lang/Exception
    //   7785	7793	10440	java/lang/Exception
    //   7796	7811	10440	java/lang/Exception
    //   7814	7826	10440	java/lang/Exception
    //   7829	7841	10440	java/lang/Exception
    //   7844	7855	10440	java/lang/Exception
    //   7858	7869	10440	java/lang/Exception
    //   7872	7883	10440	java/lang/Exception
    //   7886	7896	10440	java/lang/Exception
    //   7910	7921	10440	java/lang/Exception
    //   7996	8009	10440	java/lang/Exception
    //   8046	8055	10440	java/lang/Exception
    //   8058	8070	10440	java/lang/Exception
    //   8073	8085	10440	java/lang/Exception
    //   8088	8102	10440	java/lang/Exception
    //   8105	8119	10440	java/lang/Exception
    //   8122	8145	10440	java/lang/Exception
    //   8157	8165	10440	java/lang/Exception
    //   8168	8176	10440	java/lang/Exception
    //   8179	8187	10440	java/lang/Exception
    //   8190	8201	10440	java/lang/Exception
    //   8204	8216	10440	java/lang/Exception
    //   8219	8227	10440	java/lang/Exception
    //   8230	8241	10440	java/lang/Exception
    //   8244	8262	10440	java/lang/Exception
    //   8265	8274	10440	java/lang/Exception
    //   8277	8287	10440	java/lang/Exception
    //   8290	8302	10440	java/lang/Exception
    //   8308	8321	10440	java/lang/Exception
    //   8324	8338	10440	java/lang/Exception
    //   8341	8348	10440	java/lang/Exception
    //   8351	8362	10440	java/lang/Exception
    //   8365	8376	10440	java/lang/Exception
    //   8379	8390	10440	java/lang/Exception
    //   8393	8408	10440	java/lang/Exception
    //   8411	8426	10440	java/lang/Exception
    //   8429	8437	10440	java/lang/Exception
    //   8451	8462	10440	java/lang/Exception
    //   8518	8531	10440	java/lang/Exception
    //   8537	8548	10440	java/lang/Exception
    //   8551	8559	10440	java/lang/Exception
    //   8562	8573	10440	java/lang/Exception
    //   8576	8594	10440	java/lang/Exception
    //   8600	8613	10440	java/lang/Exception
    //   8619	8627	10440	java/lang/Exception
    //   8630	8639	10440	java/lang/Exception
    //   8642	8651	10440	java/lang/Exception
    //   8654	8666	10440	java/lang/Exception
    //   8669	8681	10440	java/lang/Exception
    //   8684	8706	10440	java/lang/Exception
    //   8719	8730	10440	java/lang/Exception
    //   8733	8745	10440	java/lang/Exception
    //   8748	8760	10440	java/lang/Exception
    //   8763	8775	10440	java/lang/Exception
    //   8778	8790	10440	java/lang/Exception
    //   8793	8815	10440	java/lang/Exception
    //   8821	8829	10440	java/lang/Exception
    //   8832	8843	10440	java/lang/Exception
    //   8846	8858	10440	java/lang/Exception
    //   8861	8873	10440	java/lang/Exception
    //   8876	8888	10440	java/lang/Exception
    //   8891	8903	10440	java/lang/Exception
    //   8906	8918	10440	java/lang/Exception
    //   8921	8933	10440	java/lang/Exception
    //   8936	8948	10440	java/lang/Exception
    //   8951	8959	10440	java/lang/Exception
    //   8962	8979	10440	java/lang/Exception
    //   8982	8998	10440	java/lang/Exception
    //   9001	9023	10440	java/lang/Exception
    //   9029	9044	10440	java/lang/Exception
    //   9050	9058	10440	java/lang/Exception
    //   9061	9072	10440	java/lang/Exception
    //   9075	9087	10440	java/lang/Exception
    //   9090	9099	10440	java/lang/Exception
    //   9102	9110	10440	java/lang/Exception
    //   9113	9124	10440	java/lang/Exception
    //   9127	9145	10440	java/lang/Exception
    //   9148	9163	10440	java/lang/Exception
    //   9166	9181	10440	java/lang/Exception
    //   9184	9196	10440	java/lang/Exception
    //   9199	9211	10440	java/lang/Exception
    //   9214	9222	10440	java/lang/Exception
    //   9225	9236	10440	java/lang/Exception
    //   9305	9318	10440	java/lang/Exception
    //   9321	9329	10440	java/lang/Exception
    //   9332	9340	10440	java/lang/Exception
    //   9346	9354	10440	java/lang/Exception
    //   9357	9366	10440	java/lang/Exception
    //   9369	9378	10440	java/lang/Exception
    //   9381	9393	10440	java/lang/Exception
    //   9396	9408	10440	java/lang/Exception
    //   9411	9433	10440	java/lang/Exception
    //   9446	9457	10440	java/lang/Exception
    //   9679	9687	10440	java/lang/Exception
    //   9690	9696	10440	java/lang/Exception
    //   9699	9711	10440	java/lang/Exception
    //   9714	9725	10440	java/lang/Exception
    //   9728	9751	10440	java/lang/Exception
    //   9754	9765	10440	java/lang/Exception
    //   9768	9784	10440	java/lang/Exception
    //   9791	9802	10440	java/lang/Exception
    //   9805	9819	10440	java/lang/Exception
    //   9822	9860	10440	java/lang/Exception
    //   9863	9879	10440	java/lang/Exception
    //   9882	9890	10440	java/lang/Exception
    //   9893	9909	10440	java/lang/Exception
    //   9912	9920	10440	java/lang/Exception
    //   9924	9935	10440	java/lang/Exception
    //   9941	9952	10440	java/lang/Exception
    //   9955	9974	10440	java/lang/Exception
    //   9980	9991	10440	java/lang/Exception
    //   9994	10016	10440	java/lang/Exception
    //   10029	10037	10440	java/lang/Exception
    //   10040	10046	10440	java/lang/Exception
    //   10049	10058	10440	java/lang/Exception
    //   10061	10069	10440	java/lang/Exception
    //   10072	10082	10440	java/lang/Exception
    //   10085	10094	10440	java/lang/Exception
    //   10097	10108	10440	java/lang/Exception
    //   10111	10149	10440	java/lang/Exception
    //   10152	10173	10440	java/lang/Exception
    //   10176	10191	10440	java/lang/Exception
    //   10199	10204	10440	java/lang/Exception
    //   10207	10220	10440	java/lang/Exception
    //   10223	10231	10440	java/lang/Exception
    //   3472	3496	10508	java/lang/Exception
    //   3501	3510	10508	java/lang/Exception
    //   3510	3527	10508	java/lang/Exception
    //   3531	3539	10508	java/lang/Exception
    //   3548	3555	10508	java/lang/Exception
    //   3570	3582	10508	java/lang/Exception
    //   3586	3616	10508	java/lang/Exception
    //   3616	3627	10508	java/lang/Exception
    //   3641	3716	10508	java/lang/Exception
    //   4418	4452	10508	java/lang/Exception
    //   4456	4465	10508	java/lang/Exception
    //   4483	4544	10508	java/lang/Exception
    //   4549	4558	10508	java/lang/Exception
    //   5327	5345	10508	java/lang/Exception
    //   5348	5372	10508	java/lang/Exception
    //   5620	5637	10508	java/lang/Exception
    //   5642	5667	10508	java/lang/Exception
    //   5670	5698	10508	java/lang/Exception
    //   5914	5931	10508	java/lang/Exception
    //   5931	5949	10508	java/lang/Exception
    //   6188	6206	10508	java/lang/Exception
    //   6271	6300	10508	java/lang/Exception
    //   6467	6478	10508	java/lang/Exception
    //   6539	6566	10508	java/lang/Exception
    //   6570	6576	10508	java/lang/Exception
    //   6586	6599	10508	java/lang/Exception
    //   6608	6613	10508	java/lang/Exception
    //   6621	6636	10508	java/lang/Exception
    //   6636	6698	10508	java/lang/Exception
    //   6703	6730	10508	java/lang/Exception
    //   6730	6740	10508	java/lang/Exception
    //   6745	6808	10508	java/lang/Exception
    //   6808	6835	10508	java/lang/Exception
    //   6835	6840	10508	java/lang/Exception
    //   6843	6906	10508	java/lang/Exception
    //   6906	6933	10508	java/lang/Exception
    //   6933	6953	10508	java/lang/Exception
    //   6958	6983	10508	java/lang/Exception
    //   6987	6992	10508	java/lang/Exception
    //   7001	7009	10508	java/lang/Exception
    //   7016	7031	10508	java/lang/Exception
    //   7032	7042	10508	java/lang/Exception
    //   7049	7064	10508	java/lang/Exception
    //   7065	7073	10508	java/lang/Exception
    //   7081	7089	10508	java/lang/Exception
    //   7097	7107	10508	java/lang/Exception
    //   7111	7116	10508	java/lang/Exception
    //   7117	7126	10508	java/lang/Exception
    //   7134	7149	10508	java/lang/Exception
    //   7150	7158	10508	java/lang/Exception
    //   7560	7575	10508	java/lang/Exception
    //   7575	7627	10508	java/lang/Exception
    //   7636	7656	10508	java/lang/Exception
    //   7661	7674	10508	java/lang/Exception
    //   7921	7949	10508	java/lang/Exception
    //   7949	7974	10508	java/lang/Exception
    //   7983	7988	10508	java/lang/Exception
    //   8012	8040	10508	java/lang/Exception
    //   8462	8490	10508	java/lang/Exception
    //   8490	8496	10508	java/lang/Exception
    //   8505	8510	10508	java/lang/Exception
    //   9236	9264	10508	java/lang/Exception
    //   9269	9294	10508	java/lang/Exception
    //   9294	9299	10508	java/lang/Exception
    //   9457	9578	10508	java/lang/Exception
    //   9578	9613	10508	java/lang/Exception
    //   9616	9645	10508	java/lang/Exception
    //   9648	9661	10508	java/lang/Exception
  }
  
  private void sendReadyToSendGroup(DelayedMessage paramDelayedMessage, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramDelayedMessage.messageObjects.isEmpty()) {
      paramDelayedMessage.markAsError();
    }
    label226:
    do
    {
      do
      {
        return;
        localObject = "group_" + paramDelayedMessage.groupId;
        if (paramDelayedMessage.finalGroupMessage == ((MessageObject)paramDelayedMessage.messageObjects.get(paramDelayedMessage.messageObjects.size() - 1)).getId()) {
          break;
        }
      } while (!paramBoolean1);
      putToDelayedMessages((String)localObject, paramDelayedMessage);
      return;
      if (paramBoolean1)
      {
        this.delayedMessages.remove(localObject);
        MessagesStorage.getInstance(this.currentAccount).putMessages(paramDelayedMessage.messages, false, true, false, 0);
        MessagesController.getInstance(this.currentAccount).updateInterfaceWithMessages(paramDelayedMessage.peer, paramDelayedMessage.messageObjects);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
      }
      if (!(paramDelayedMessage.sendRequest instanceof TLRPC.TL_messages_sendMultiMedia)) {
        break;
      }
      localObject = (TLRPC.TL_messages_sendMultiMedia)paramDelayedMessage.sendRequest;
      i = 0;
      for (;;)
      {
        if (i >= ((TLRPC.TL_messages_sendMultiMedia)localObject).multi_media.size()) {
          break label226;
        }
        TLRPC.InputMedia localInputMedia = ((TLRPC.TL_inputSingleMedia)((TLRPC.TL_messages_sendMultiMedia)localObject).multi_media.get(i)).media;
        if (((localInputMedia instanceof TLRPC.TL_inputMediaUploadedPhoto)) || ((localInputMedia instanceof TLRPC.TL_inputMediaUploadedDocument))) {
          break;
        }
        i += 1;
      }
      if (!paramBoolean2) {
        break label340;
      }
      localObject = findMaxDelayedMessageForMessageId(paramDelayedMessage.finalGroupMessage, paramDelayedMessage.peer);
      if (localObject == null) {
        break label340;
      }
      ((DelayedMessage)localObject).addDelayedRequest(paramDelayedMessage.sendRequest, paramDelayedMessage.messageObjects, paramDelayedMessage.originalPaths);
    } while (paramDelayedMessage.requests == null);
    ((DelayedMessage)localObject).requests.addAll(paramDelayedMessage.requests);
    return;
    Object localObject = (TLRPC.TL_messages_sendEncryptedMultiMedia)paramDelayedMessage.sendEncryptedRequest;
    int i = 0;
    for (;;)
    {
      if (i >= ((TLRPC.TL_messages_sendEncryptedMultiMedia)localObject).files.size()) {
        break label340;
      }
      if (((TLRPC.InputEncryptedFile)((TLRPC.TL_messages_sendEncryptedMultiMedia)localObject).files.get(i) instanceof TLRPC.TL_inputEncryptedFile)) {
        break;
      }
      i += 1;
    }
    label340:
    if ((paramDelayedMessage.sendRequest instanceof TLRPC.TL_messages_sendMultiMedia)) {
      performSendMessageRequestMulti((TLRPC.TL_messages_sendMultiMedia)paramDelayedMessage.sendRequest, paramDelayedMessage.messageObjects, paramDelayedMessage.originalPaths);
    }
    for (;;)
    {
      paramDelayedMessage.sendDelayedRequests();
      return;
      SecretChatHelper.getInstance(this.currentAccount).performSendEncryptedRequest((TLRPC.TL_messages_sendEncryptedMultiMedia)paramDelayedMessage.sendEncryptedRequest, paramDelayedMessage);
    }
  }
  
  private void updateMediaPaths(MessageObject paramMessageObject, TLRPC.Message paramMessage, String paramString, boolean paramBoolean)
  {
    TLRPC.Message localMessage = paramMessageObject.messageOwner;
    if (paramMessage == null) {}
    label212:
    label283:
    label286:
    label356:
    label1026:
    label1305:
    label1395:
    label1626:
    label1632:
    label1724:
    do
    {
      do
      {
        Object localObject2;
        do
        {
          return;
          int i;
          Object localObject1;
          String str;
          Object localObject3;
          if (((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) && (paramMessage.media.photo != null) && ((localMessage.media instanceof TLRPC.TL_messageMediaPhoto)) && (localMessage.media.photo != null))
          {
            if (paramMessage.media.ttl_seconds == 0) {
              MessagesStorage.getInstance(this.currentAccount).putSentFile(paramString, paramMessage.media.photo, 0);
            }
            if ((localMessage.media.photo.sizes.size() == 1) && ((((TLRPC.PhotoSize)localMessage.media.photo.sizes.get(0)).location instanceof TLRPC.TL_fileLocationUnavailable)))
            {
              localMessage.media.photo.sizes = paramMessage.media.photo.sizes;
              paramMessage.message = localMessage.message;
              paramMessage.attachPath = localMessage.attachPath;
              localMessage.media.photo.id = paramMessage.media.photo.id;
              localMessage.media.photo.access_hash = paramMessage.media.photo.access_hash;
              return;
            }
            i = 0;
            if (i < paramMessage.media.photo.sizes.size())
            {
              paramString = (TLRPC.PhotoSize)paramMessage.media.photo.sizes.get(i);
              if ((paramString != null) && (paramString.location != null) && (!(paramString instanceof TLRPC.TL_photoSizeEmpty)) && (paramString.type != null)) {
                break label283;
              }
            }
            do
            {
              i += 1;
              break label212;
              break;
              int j = 0;
              if (j < localMessage.media.photo.sizes.size())
              {
                localObject1 = (TLRPC.PhotoSize)localMessage.media.photo.sizes.get(j);
                if ((localObject1 != null) && (((TLRPC.PhotoSize)localObject1).location != null) && (((TLRPC.PhotoSize)localObject1).type != null)) {
                  break label356;
                }
              }
              while (((((TLRPC.PhotoSize)localObject1).location.volume_id != -2147483648L) || (!paramString.type.equals(((TLRPC.PhotoSize)localObject1).type))) && ((paramString.w != ((TLRPC.PhotoSize)localObject1).w) || (paramString.h != ((TLRPC.PhotoSize)localObject1).h)))
              {
                j += 1;
                break label286;
                break;
              }
              localObject2 = ((TLRPC.PhotoSize)localObject1).location.volume_id + "_" + ((TLRPC.PhotoSize)localObject1).location.local_id;
              str = paramString.location.volume_id + "_" + paramString.location.local_id;
            } while (((String)localObject2).equals(str));
            localObject3 = new File(FileLoader.getDirectory(4), (String)localObject2 + ".jpg");
            if ((paramMessage.media.ttl_seconds == 0) && ((paramMessage.media.photo.sizes.size() == 1) || (paramString.w > 90) || (paramString.h > 90))) {}
            for (paramMessageObject = FileLoader.getPathToAttach(paramString);; paramMessageObject = new File(FileLoader.getDirectory(4), str + ".jpg"))
            {
              ((File)localObject3).renameTo(paramMessageObject);
              ImageLoader.getInstance().replaceImageInCache((String)localObject2, str, paramString.location, paramBoolean);
              ((TLRPC.PhotoSize)localObject1).location = paramString.location;
              ((TLRPC.PhotoSize)localObject1).size = paramString.size;
              break;
            }
          }
          if ((!(paramMessage.media instanceof TLRPC.TL_messageMediaDocument)) || (paramMessage.media.document == null) || (!(localMessage.media instanceof TLRPC.TL_messageMediaDocument)) || (localMessage.media.document == null)) {
            break label1724;
          }
          if (MessageObject.isVideoMessage(paramMessage))
          {
            if (paramMessage.media.ttl_seconds == 0) {
              MessagesStorage.getInstance(this.currentAccount).putSentFile(paramString, paramMessage.media.document, 2);
            }
            paramMessage.attachPath = localMessage.attachPath;
            localObject1 = localMessage.media.document.thumb;
            localObject2 = paramMessage.media.document.thumb;
            if ((localObject1 == null) || (((TLRPC.PhotoSize)localObject1).location == null) || (((TLRPC.PhotoSize)localObject1).location.volume_id != -2147483648L) || (localObject2 == null) || (((TLRPC.PhotoSize)localObject2).location == null) || ((localObject2 instanceof TLRPC.TL_photoSizeEmpty)) || ((localObject1 instanceof TLRPC.TL_photoSizeEmpty))) {
              break label1305;
            }
            str = ((TLRPC.PhotoSize)localObject1).location.volume_id + "_" + ((TLRPC.PhotoSize)localObject1).location.local_id;
            localObject3 = ((TLRPC.PhotoSize)localObject2).location.volume_id + "_" + ((TLRPC.PhotoSize)localObject2).location.local_id;
            if (!str.equals(localObject3))
            {
              new File(FileLoader.getDirectory(4), str + ".jpg").renameTo(new File(FileLoader.getDirectory(4), (String)localObject3 + ".jpg"));
              ImageLoader.getInstance().replaceImageInCache(str, (String)localObject3, ((TLRPC.PhotoSize)localObject2).location, paramBoolean);
              ((TLRPC.PhotoSize)localObject1).location = ((TLRPC.PhotoSize)localObject2).location;
              ((TLRPC.PhotoSize)localObject1).size = ((TLRPC.PhotoSize)localObject2).size;
            }
            localMessage.media.document.dc_id = paramMessage.media.document.dc_id;
            localMessage.media.document.id = paramMessage.media.document.id;
            localMessage.media.document.access_hash = paramMessage.media.document.access_hash;
            localObject2 = null;
            i = 0;
          }
          for (;;)
          {
            localObject1 = localObject2;
            if (i < localMessage.media.document.attributes.size())
            {
              localObject1 = (TLRPC.DocumentAttribute)localMessage.media.document.attributes.get(i);
              if ((localObject1 instanceof TLRPC.TL_documentAttributeAudio)) {
                localObject1 = ((TLRPC.DocumentAttribute)localObject1).waveform;
              }
            }
            else
            {
              localMessage.media.document.attributes = paramMessage.media.document.attributes;
              if (localObject1 == null) {
                break label1395;
              }
              i = 0;
              while (i < localMessage.media.document.attributes.size())
              {
                localObject2 = (TLRPC.DocumentAttribute)localMessage.media.document.attributes.get(i);
                if ((localObject2 instanceof TLRPC.TL_documentAttributeAudio))
                {
                  ((TLRPC.DocumentAttribute)localObject2).waveform = ((byte[])localObject1);
                  ((TLRPC.DocumentAttribute)localObject2).flags |= 0x4;
                }
                i += 1;
              }
              if ((MessageObject.isVoiceMessage(paramMessage)) || (MessageObject.isRoundVideoMessage(paramMessage)) || (paramMessage.media.ttl_seconds != 0)) {
                break;
              }
              MessagesStorage.getInstance(this.currentAccount).putSentFile(paramString, paramMessage.media.document, 1);
              break;
              if ((localObject1 != null) && (MessageObject.isStickerMessage(paramMessage)) && (((TLRPC.PhotoSize)localObject1).location != null))
              {
                ((TLRPC.PhotoSize)localObject2).location = ((TLRPC.PhotoSize)localObject1).location;
                break label1026;
              }
              if (((localObject1 == null) || (!(((TLRPC.PhotoSize)localObject1).location instanceof TLRPC.TL_fileLocationUnavailable))) && (!(localObject1 instanceof TLRPC.TL_photoSizeEmpty))) {
                break label1026;
              }
              localMessage.media.document.thumb = paramMessage.media.document.thumb;
              break label1026;
            }
            i += 1;
          }
          localMessage.media.document.size = paramMessage.media.document.size;
          localMessage.media.document.mime_type = paramMessage.media.document.mime_type;
          if (((paramMessage.flags & 0x4) == 0) && (MessageObject.isOut(paramMessage)))
          {
            if (MessageObject.isNewGifDocument(paramMessage.media.document)) {
              DataQuery.getInstance(this.currentAccount).addRecentGif(paramMessage.media.document, paramMessage.date);
            }
          }
          else
          {
            if ((localMessage.attachPath == null) || (!localMessage.attachPath.startsWith(FileLoader.getDirectory(4).getAbsolutePath()))) {
              break;
            }
            localObject1 = new File(localMessage.attachPath);
            localObject2 = paramMessage.media.document;
            if (paramMessage.media.ttl_seconds == 0) {
              break label1626;
            }
          }
          for (paramBoolean = true;; paramBoolean = false)
          {
            localObject2 = FileLoader.getPathToAttach((TLObject)localObject2, paramBoolean);
            if (((File)localObject1).renameTo((File)localObject2)) {
              break label1632;
            }
            paramMessage.attachPath = localMessage.attachPath;
            paramMessage.message = localMessage.message;
            return;
            if (!MessageObject.isStickerDocument(paramMessage.media.document)) {
              break;
            }
            DataQuery.getInstance(this.currentAccount).addRecentSticker(0, paramMessage.media.document, paramMessage.date, false);
            break;
          }
          if (MessageObject.isVideoMessage(paramMessage))
          {
            paramMessageObject.attachPathExists = true;
            return;
          }
          paramMessageObject.mediaExists = paramMessageObject.attachPathExists;
          paramMessageObject.attachPathExists = false;
          localMessage.attachPath = "";
        } while ((paramString == null) || (!paramString.startsWith("http")));
        MessagesStorage.getInstance(this.currentAccount).addRecentLocalFile(paramString, ((File)localObject2).toString(), localMessage.media.document);
        return;
        paramMessage.attachPath = localMessage.attachPath;
        paramMessage.message = localMessage.message;
        return;
        if (((paramMessage.media instanceof TLRPC.TL_messageMediaContact)) && ((localMessage.media instanceof TLRPC.TL_messageMediaContact)))
        {
          localMessage.media = paramMessage.media;
          return;
        }
        if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage))
        {
          localMessage.media = paramMessage.media;
          return;
        }
      } while (!(paramMessage.media instanceof TLRPC.TL_messageMediaGame));
      localMessage.media = paramMessage.media;
    } while ((!(localMessage.media instanceof TLRPC.TL_messageMediaGame)) || (TextUtils.isEmpty(paramMessage.message)));
    localMessage.entities = paramMessage.entities;
    localMessage.message = paramMessage.message;
  }
  
  private void uploadMultiMedia(final DelayedMessage paramDelayedMessage, final TLRPC.InputMedia paramInputMedia, TLRPC.InputEncryptedFile paramInputEncryptedFile, String paramString)
  {
    if (paramInputMedia != null)
    {
      paramInputEncryptedFile = (TLRPC.TL_messages_sendMultiMedia)paramDelayedMessage.sendRequest;
      i = 0;
      if (i < paramInputEncryptedFile.multi_media.size())
      {
        if (((TLRPC.TL_inputSingleMedia)paramInputEncryptedFile.multi_media.get(i)).media == paramInputMedia)
        {
          putToSendingMessages((TLRPC.Message)paramDelayedMessage.messages.get(i));
          NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.FileUploadProgressChanged, new Object[] { paramString, Float.valueOf(1.0F), Boolean.valueOf(false) });
        }
      }
      else
      {
        paramInputEncryptedFile = new TLRPC.TL_messages_uploadMedia();
        paramInputEncryptedFile.media = paramInputMedia;
        paramInputEncryptedFile.peer = ((TLRPC.TL_messages_sendMultiMedia)paramDelayedMessage.sendRequest).peer;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramInputEncryptedFile, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                TLRPC.TL_messages_sendMultiMedia localTL_messages_sendMultiMedia = null;
                Object localObject = localTL_messages_sendMultiMedia;
                TLRPC.MessageMedia localMessageMedia;
                int i;
                if (paramAnonymousTLObject != null)
                {
                  localMessageMedia = (TLRPC.MessageMedia)paramAnonymousTLObject;
                  if (((SendMessagesHelper.10.this.val$inputMedia instanceof TLRPC.TL_inputMediaUploadedPhoto)) && ((localMessageMedia instanceof TLRPC.TL_messageMediaPhoto)))
                  {
                    localObject = new TLRPC.TL_inputMediaPhoto();
                    ((TLRPC.TL_inputMediaPhoto)localObject).id = new TLRPC.TL_inputPhoto();
                    ((TLRPC.TL_inputMediaPhoto)localObject).id.id = localMessageMedia.photo.id;
                    ((TLRPC.TL_inputMediaPhoto)localObject).id.access_hash = localMessageMedia.photo.access_hash;
                  }
                }
                else
                {
                  if (localObject == null) {
                    break label301;
                  }
                  if (SendMessagesHelper.10.this.val$inputMedia.ttl_seconds != 0)
                  {
                    ((TLRPC.InputMedia)localObject).ttl_seconds = SendMessagesHelper.10.this.val$inputMedia.ttl_seconds;
                    ((TLRPC.InputMedia)localObject).flags |= 0x1;
                  }
                  localTL_messages_sendMultiMedia = (TLRPC.TL_messages_sendMultiMedia)SendMessagesHelper.10.this.val$message.sendRequest;
                  i = 0;
                }
                for (;;)
                {
                  if (i < localTL_messages_sendMultiMedia.multi_media.size())
                  {
                    if (((TLRPC.TL_inputSingleMedia)localTL_messages_sendMultiMedia.multi_media.get(i)).media == SendMessagesHelper.10.this.val$inputMedia) {
                      ((TLRPC.TL_inputSingleMedia)localTL_messages_sendMultiMedia.multi_media.get(i)).media = ((TLRPC.InputMedia)localObject);
                    }
                  }
                  else
                  {
                    SendMessagesHelper.this.sendReadyToSendGroup(SendMessagesHelper.10.this.val$message, false, true);
                    return;
                    localObject = localTL_messages_sendMultiMedia;
                    if (!(SendMessagesHelper.10.this.val$inputMedia instanceof TLRPC.TL_inputMediaUploadedDocument)) {
                      break;
                    }
                    localObject = localTL_messages_sendMultiMedia;
                    if (!(localMessageMedia instanceof TLRPC.TL_messageMediaDocument)) {
                      break;
                    }
                    localObject = new TLRPC.TL_inputMediaDocument();
                    ((TLRPC.TL_inputMediaDocument)localObject).id = new TLRPC.TL_inputDocument();
                    ((TLRPC.TL_inputMediaDocument)localObject).id.id = localMessageMedia.document.id;
                    ((TLRPC.TL_inputMediaDocument)localObject).id.access_hash = localMessageMedia.document.access_hash;
                    break;
                  }
                  i += 1;
                }
                label301:
                SendMessagesHelper.10.this.val$message.markAsError();
              }
            });
          }
        });
      }
    }
    while (paramInputEncryptedFile == null) {
      for (;;)
      {
        return;
        i += 1;
      }
    }
    paramInputMedia = (TLRPC.TL_messages_sendEncryptedMultiMedia)paramDelayedMessage.sendEncryptedRequest;
    int i = 0;
    for (;;)
    {
      if (i < paramInputMedia.files.size())
      {
        if (paramInputMedia.files.get(i) == paramInputEncryptedFile)
        {
          putToSendingMessages((TLRPC.Message)paramDelayedMessage.messages.get(i));
          NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.FileUploadProgressChanged, new Object[] { paramString, Float.valueOf(1.0F), Boolean.valueOf(false) });
        }
      }
      else
      {
        sendReadyToSendGroup(paramDelayedMessage, false, true);
        return;
      }
      i += 1;
    }
  }
  
  public void cancelSendingMessage(MessageObject paramMessageObject)
  {
    ArrayList localArrayList = new ArrayList();
    boolean bool = false;
    Iterator localIterator = this.delayedMessages.entrySet().iterator();
    label99:
    label261:
    label294:
    label490:
    while (localIterator.hasNext())
    {
      localObject1 = (Map.Entry)localIterator.next();
      Object localObject2 = (ArrayList)((Map.Entry)localObject1).getValue();
      i = 0;
      for (;;)
      {
        if (i >= ((ArrayList)localObject2).size()) {
          break label490;
        }
        DelayedMessage localDelayedMessage = (DelayedMessage)((ArrayList)localObject2).get(i);
        if (localDelayedMessage.type == 4)
        {
          int k = -1;
          localObject1 = null;
          i = 0;
          int j = k;
          if (i < localDelayedMessage.messageObjects.size())
          {
            localObject1 = (MessageObject)localDelayedMessage.messageObjects.get(i);
            if (((MessageObject)localObject1).getId() == paramMessageObject.getId()) {
              j = i;
            }
          }
          else
          {
            if (j < 0) {
              break;
            }
            localDelayedMessage.messageObjects.remove(j);
            localDelayedMessage.messages.remove(j);
            localDelayedMessage.originalPaths.remove(j);
            if (localDelayedMessage.sendRequest == null) {
              break label261;
            }
            ((TLRPC.TL_messages_sendMultiMedia)localDelayedMessage.sendRequest).multi_media.remove(j);
          }
          for (;;)
          {
            MediaController.getInstance().cancelVideoConvert(paramMessageObject);
            localObject1 = (String)localDelayedMessage.extraHashMap.get(localObject1);
            if (localObject1 != null) {
              localArrayList.add(localObject1);
            }
            if (!localDelayedMessage.messageObjects.isEmpty()) {
              break label294;
            }
            localDelayedMessage.sendDelayedRequests();
            break;
            i += 1;
            break label99;
            localObject2 = (TLRPC.TL_messages_sendEncryptedMultiMedia)localDelayedMessage.sendEncryptedRequest;
            ((TLRPC.TL_messages_sendEncryptedMultiMedia)localObject2).messages.remove(j);
            ((TLRPC.TL_messages_sendEncryptedMultiMedia)localObject2).files.remove(j);
          }
          if (localDelayedMessage.finalGroupMessage == paramMessageObject.getId())
          {
            localObject1 = (MessageObject)localDelayedMessage.messageObjects.get(localDelayedMessage.messageObjects.size() - 1);
            localDelayedMessage.finalGroupMessage = ((MessageObject)localObject1).getId();
            ((MessageObject)localObject1).messageOwner.params.put("final", "1");
            localObject2 = new TLRPC.TL_messages_messages();
            ((TLRPC.TL_messages_messages)localObject2).messages.add(((MessageObject)localObject1).messageOwner);
            MessagesStorage.getInstance(this.currentAccount).putMessages((TLRPC.messages_Messages)localObject2, localDelayedMessage.peer, -2, 0, false);
          }
          sendReadyToSendGroup(localDelayedMessage, false, true);
          break;
        }
        if (localDelayedMessage.obj.getId() == paramMessageObject.getId())
        {
          ((ArrayList)localObject2).remove(i);
          localDelayedMessage.sendDelayedRequests();
          MediaController.getInstance().cancelVideoConvert(localDelayedMessage.obj);
          if (((ArrayList)localObject2).size() != 0) {
            break;
          }
          localArrayList.add(((Map.Entry)localObject1).getKey());
          if (localDelayedMessage.sendEncryptedRequest == null) {
            break;
          }
          bool = true;
          break;
        }
        i += 1;
      }
    }
    int i = 0;
    if (i < localArrayList.size())
    {
      localObject1 = (String)localArrayList.get(i);
      if (((String)localObject1).startsWith("http")) {
        ImageLoader.getInstance().cancelLoadHttpFile((String)localObject1);
      }
      for (;;)
      {
        stopVideoService((String)localObject1);
        this.delayedMessages.remove(localObject1);
        i += 1;
        break;
        FileLoader.getInstance(this.currentAccount).cancelUploadFile((String)localObject1, bool);
      }
    }
    Object localObject1 = new ArrayList();
    ((ArrayList)localObject1).add(Integer.valueOf(paramMessageObject.getId()));
    MessagesController.getInstance(this.currentAccount).deleteMessages((ArrayList)localObject1, null, null, paramMessageObject.messageOwner.to_id.channel_id, false);
  }
  
  public void checkUnsentMessages()
  {
    MessagesStorage.getInstance(this.currentAccount).getUnsentMessages(1000);
  }
  
  public void cleanup()
  {
    this.delayedMessages.clear();
    this.unsentMessages.clear();
    this.sendingMessages.clear();
    this.waitingForLocation.clear();
    this.waitingForCallback.clear();
    this.currentChatInfo = null;
    this.locationProvider.stop();
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, final Object... paramVarArgs)
  {
    Object localObject2;
    final Object localObject3;
    final Object localObject4;
    final Object localObject1;
    label99:
    label146:
    label220:
    label742:
    label940:
    long l1;
    if (paramInt1 == NotificationCenter.FileDidUpload)
    {
      localObject2 = (String)paramVarArgs[0];
      localObject3 = (TLRPC.InputFile)paramVarArgs[1];
      localObject4 = (TLRPC.InputEncryptedFile)paramVarArgs[2];
      ArrayList localArrayList = (ArrayList)this.delayedMessages.get(localObject2);
      if (localArrayList != null)
      {
        paramInt1 = 0;
        if (paramInt1 < localArrayList.size())
        {
          DelayedMessage localDelayedMessage = (DelayedMessage)localArrayList.get(paramInt1);
          localObject1 = null;
          if ((localDelayedMessage.sendRequest instanceof TLRPC.TL_messages_sendMedia))
          {
            localObject1 = ((TLRPC.TL_messages_sendMedia)localDelayedMessage.sendRequest).media;
            if ((localObject3 == null) || (localObject1 == null)) {
              break label742;
            }
            if (localDelayedMessage.type != 0) {
              break label220;
            }
            ((TLRPC.InputMedia)localObject1).file = ((TLRPC.InputFile)localObject3);
            performSendMessageRequest(localDelayedMessage.sendRequest, localDelayedMessage.obj, localDelayedMessage.originalPath, localDelayedMessage, true);
            localArrayList.remove(paramInt1);
            paramInt2 = paramInt1 - 1;
          }
          Object localObject5;
          do
          {
            do
            {
              paramInt1 = paramInt2 + 1;
              break;
              if ((localDelayedMessage.sendRequest instanceof TLRPC.TL_messages_sendBroadcast))
              {
                localObject1 = ((TLRPC.TL_messages_sendBroadcast)localDelayedMessage.sendRequest).media;
                break label99;
              }
              if (!(localDelayedMessage.sendRequest instanceof TLRPC.TL_messages_sendMultiMedia)) {
                break label99;
              }
              localObject1 = (TLRPC.InputMedia)localDelayedMessage.extraHashMap.get(localObject2);
              break label99;
              if (localDelayedMessage.type == 1)
              {
                if (((TLRPC.InputMedia)localObject1).file == null)
                {
                  ((TLRPC.InputMedia)localObject1).file = ((TLRPC.InputFile)localObject3);
                  if ((((TLRPC.InputMedia)localObject1).thumb == null) && (localDelayedMessage.location != null))
                  {
                    performSendDelayedMessage(localDelayedMessage);
                    break label146;
                  }
                  performSendMessageRequest(localDelayedMessage.sendRequest, localDelayedMessage.obj, localDelayedMessage.originalPath);
                  break label146;
                }
                ((TLRPC.InputMedia)localObject1).thumb = ((TLRPC.InputFile)localObject3);
                ((TLRPC.InputMedia)localObject1).flags |= 0x4;
                performSendMessageRequest(localDelayedMessage.sendRequest, localDelayedMessage.obj, localDelayedMessage.originalPath);
                break label146;
              }
              if (localDelayedMessage.type == 2)
              {
                if (((TLRPC.InputMedia)localObject1).file == null)
                {
                  ((TLRPC.InputMedia)localObject1).file = ((TLRPC.InputFile)localObject3);
                  if ((((TLRPC.InputMedia)localObject1).thumb == null) && (localDelayedMessage.location != null))
                  {
                    performSendDelayedMessage(localDelayedMessage);
                    break label146;
                  }
                  performSendMessageRequest(localDelayedMessage.sendRequest, localDelayedMessage.obj, localDelayedMessage.originalPath);
                  break label146;
                }
                ((TLRPC.InputMedia)localObject1).thumb = ((TLRPC.InputFile)localObject3);
                ((TLRPC.InputMedia)localObject1).flags |= 0x4;
                performSendMessageRequest(localDelayedMessage.sendRequest, localDelayedMessage.obj, localDelayedMessage.originalPath);
                break label146;
              }
              if (localDelayedMessage.type == 3)
              {
                ((TLRPC.InputMedia)localObject1).file = ((TLRPC.InputFile)localObject3);
                performSendMessageRequest(localDelayedMessage.sendRequest, localDelayedMessage.obj, localDelayedMessage.originalPath);
                break label146;
              }
              if (localDelayedMessage.type != 4) {
                break label146;
              }
              if ((localObject1 instanceof TLRPC.TL_inputMediaUploadedDocument))
              {
                if (((TLRPC.InputMedia)localObject1).file == null)
                {
                  ((TLRPC.InputMedia)localObject1).file = ((TLRPC.InputFile)localObject3);
                  localObject5 = (MessageObject)localDelayedMessage.extraHashMap.get((String)localObject2 + "_i");
                  paramInt2 = localDelayedMessage.messageObjects.indexOf(localObject5);
                  localDelayedMessage.location = ((TLRPC.FileLocation)localDelayedMessage.extraHashMap.get((String)localObject2 + "_t"));
                  stopVideoService(((MessageObject)localDelayedMessage.messageObjects.get(paramInt2)).messageOwner.attachPath);
                  if ((((TLRPC.InputMedia)localObject1).thumb == null) && (localDelayedMessage.location != null))
                  {
                    performSendDelayedMessage(localDelayedMessage, paramInt2);
                    break label146;
                  }
                  uploadMultiMedia(localDelayedMessage, (TLRPC.InputMedia)localObject1, null, (String)localObject2);
                  break label146;
                }
                ((TLRPC.InputMedia)localObject1).thumb = ((TLRPC.InputFile)localObject3);
                ((TLRPC.InputMedia)localObject1).flags |= 0x4;
                uploadMultiMedia(localDelayedMessage, (TLRPC.InputMedia)localObject1, null, (String)localDelayedMessage.extraHashMap.get((String)localObject2 + "_o"));
                break label146;
              }
              ((TLRPC.InputMedia)localObject1).file = ((TLRPC.InputFile)localObject3);
              uploadMultiMedia(localDelayedMessage, (TLRPC.InputMedia)localObject1, null, (String)localObject2);
              break label146;
              paramInt2 = paramInt1;
            } while (localObject4 == null);
            paramInt2 = paramInt1;
          } while (localDelayedMessage.sendEncryptedRequest == null);
          localObject1 = null;
          if (localDelayedMessage.type == 4)
          {
            localObject5 = (TLRPC.TL_messages_sendEncryptedMultiMedia)localDelayedMessage.sendEncryptedRequest;
            TLRPC.InputEncryptedFile localInputEncryptedFile = (TLRPC.InputEncryptedFile)localDelayedMessage.extraHashMap.get(localObject2);
            paramInt2 = ((TLRPC.TL_messages_sendEncryptedMultiMedia)localObject5).files.indexOf(localInputEncryptedFile);
            if (paramInt2 >= 0)
            {
              ((TLRPC.TL_messages_sendEncryptedMultiMedia)localObject5).files.set(paramInt2, localObject4);
              if (localInputEncryptedFile.id == 1L)
              {
                localObject1 = (MessageObject)localDelayedMessage.extraHashMap.get((String)localObject2 + "_i");
                localDelayedMessage.location = ((TLRPC.FileLocation)localDelayedMessage.extraHashMap.get((String)localObject2 + "_t"));
                stopVideoService(((MessageObject)localDelayedMessage.messageObjects.get(paramInt2)).messageOwner.attachPath);
              }
              localObject1 = (TLRPC.TL_decryptedMessage)((TLRPC.TL_messages_sendEncryptedMultiMedia)localObject5).messages.get(paramInt2);
            }
            if (localObject1 != null)
            {
              if (((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaVideo)) || ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaPhoto)) || ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaDocument)))
              {
                l1 = ((Long)paramVarArgs[5]).longValue();
                ((TLRPC.TL_decryptedMessage)localObject1).media.size = ((int)l1);
              }
              ((TLRPC.TL_decryptedMessage)localObject1).media.key = ((byte[])paramVarArgs[3]);
              ((TLRPC.TL_decryptedMessage)localObject1).media.iv = ((byte[])paramVarArgs[4]);
              if (localDelayedMessage.type != 4) {
                break label1081;
              }
              uploadMultiMedia(localDelayedMessage, null, (TLRPC.InputEncryptedFile)localObject4, (String)localObject2);
            }
          }
          for (;;)
          {
            localArrayList.remove(paramInt1);
            paramInt2 = paramInt1 - 1;
            break;
            localObject1 = (TLRPC.TL_decryptedMessage)localDelayedMessage.sendEncryptedRequest;
            break label940;
            label1081:
            SecretChatHelper.getInstance(this.currentAccount).performSendEncryptedRequest((TLRPC.DecryptedMessage)localObject1, localDelayedMessage.obj.messageOwner, localDelayedMessage.encryptedChat, (TLRPC.InputEncryptedFile)localObject4, localDelayedMessage.originalPath, localDelayedMessage.obj);
          }
        }
        if (localArrayList.isEmpty()) {
          this.delayedMessages.remove(localObject2);
        }
      }
    }
    label1446:
    label1477:
    label1549:
    label1599:
    label1748:
    label1755:
    label1943:
    label1996:
    label2128:
    label2277:
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
                  boolean bool;
                  long l2;
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
                                if (paramInt1 != NotificationCenter.FileDidFailUpload) {
                                  break;
                                }
                                localObject1 = (String)paramVarArgs[0];
                                bool = ((Boolean)paramVarArgs[1]).booleanValue();
                                paramVarArgs = (ArrayList)this.delayedMessages.get(localObject1);
                              } while (paramVarArgs == null);
                              for (paramInt1 = 0; paramInt1 < paramVarArgs.size(); paramInt1 = paramInt2 + 1)
                              {
                                localObject2 = (DelayedMessage)paramVarArgs.get(paramInt1);
                                if ((!bool) || (((DelayedMessage)localObject2).sendEncryptedRequest == null))
                                {
                                  paramInt2 = paramInt1;
                                  if (!bool)
                                  {
                                    paramInt2 = paramInt1;
                                    if (((DelayedMessage)localObject2).sendRequest == null) {}
                                  }
                                }
                                else
                                {
                                  ((DelayedMessage)localObject2).markAsError();
                                  paramVarArgs.remove(paramInt1);
                                  paramInt2 = paramInt1 - 1;
                                }
                              }
                            } while (!paramVarArgs.isEmpty());
                            this.delayedMessages.remove(localObject1);
                            return;
                            if (paramInt1 != NotificationCenter.FilePreparingStarted) {
                              break;
                            }
                            localObject1 = (MessageObject)paramVarArgs[0];
                          } while (((MessageObject)localObject1).getId() == 0);
                          paramVarArgs = (String)paramVarArgs[1];
                          paramVarArgs = (ArrayList)this.delayedMessages.get(((MessageObject)localObject1).messageOwner.attachPath);
                        } while (paramVarArgs == null);
                        paramInt1 = 0;
                        for (;;)
                        {
                          if (paramInt1 < paramVarArgs.size())
                          {
                            localObject2 = (DelayedMessage)paramVarArgs.get(paramInt1);
                            if (((DelayedMessage)localObject2).type != 4) {
                              break label1446;
                            }
                            paramInt2 = ((DelayedMessage)localObject2).messageObjects.indexOf(localObject1);
                            ((DelayedMessage)localObject2).location = ((TLRPC.FileLocation)((DelayedMessage)localObject2).extraHashMap.get(((MessageObject)localObject1).messageOwner.attachPath + "_t"));
                            performSendDelayedMessage((DelayedMessage)localObject2, paramInt2);
                            paramVarArgs.remove(paramInt1);
                          }
                          while (paramVarArgs.isEmpty())
                          {
                            this.delayedMessages.remove(((MessageObject)localObject1).messageOwner.attachPath);
                            return;
                            if (((DelayedMessage)localObject2).obj != localObject1) {
                              break label1477;
                            }
                            ((DelayedMessage)localObject2).videoEditedInfo = null;
                            performSendDelayedMessage((DelayedMessage)localObject2);
                            paramVarArgs.remove(paramInt1);
                          }
                          break;
                          paramInt1 += 1;
                        }
                        if (paramInt1 != NotificationCenter.FileNewChunkAvailable) {
                          break;
                        }
                        localObject1 = (MessageObject)paramVarArgs[0];
                      } while (((MessageObject)localObject1).getId() == 0);
                      localObject2 = (String)paramVarArgs[1];
                      l1 = ((Long)paramVarArgs[2]).longValue();
                      l2 = ((Long)paramVarArgs[3]).longValue();
                      if ((int)((MessageObject)localObject1).getDialogId() != 0) {
                        break;
                      }
                      bool = true;
                      FileLoader.getInstance(this.currentAccount).checkUploadNewDataAvailable((String)localObject2, bool, l1, l2);
                    } while (l2 == 0L);
                    paramVarArgs = (ArrayList)this.delayedMessages.get(((MessageObject)localObject1).messageOwner.attachPath);
                  } while (paramVarArgs == null);
                  paramInt1 = 0;
                  if (paramInt1 < paramVarArgs.size())
                  {
                    localObject2 = (DelayedMessage)paramVarArgs.get(paramInt1);
                    if (((DelayedMessage)localObject2).type != 4) {
                      break label1755;
                    }
                    paramInt2 = 0;
                    if (paramInt2 < ((DelayedMessage)localObject2).messageObjects.size())
                    {
                      localObject3 = (MessageObject)((DelayedMessage)localObject2).messageObjects.get(paramInt2);
                      if (localObject3 != localObject1) {
                        break label1748;
                      }
                      ((MessageObject)localObject3).videoEditedInfo = null;
                      ((MessageObject)localObject3).messageOwner.params.remove("ve");
                      ((MessageObject)localObject3).messageOwner.media.document.size = ((int)l2);
                      localObject2 = new ArrayList();
                      ((ArrayList)localObject2).add(((MessageObject)localObject3).messageOwner);
                      MessagesStorage.getInstance(this.currentAccount).putMessages((ArrayList)localObject2, false, true, false, 0);
                    }
                  }
                  while (((DelayedMessage)localObject2).obj != localObject1) {
                    for (;;)
                    {
                      paramInt1 += 1;
                      break label1599;
                      break;
                      bool = false;
                      break label1549;
                      paramInt2 += 1;
                    }
                  }
                  ((DelayedMessage)localObject2).obj.videoEditedInfo = null;
                  ((DelayedMessage)localObject2).obj.messageOwner.params.remove("ve");
                  ((DelayedMessage)localObject2).obj.messageOwner.media.document.size = ((int)l2);
                  paramVarArgs = new ArrayList();
                  paramVarArgs.add(((DelayedMessage)localObject2).obj.messageOwner);
                  MessagesStorage.getInstance(this.currentAccount).putMessages(paramVarArgs, false, true, false, 0);
                  return;
                  if (paramInt1 != NotificationCenter.FilePreparingFailed) {
                    break;
                  }
                  localObject1 = (MessageObject)paramVarArgs[0];
                } while (((MessageObject)localObject1).getId() == 0);
                paramVarArgs = (String)paramVarArgs[1];
                stopVideoService(((MessageObject)localObject1).messageOwner.attachPath);
                localObject2 = (ArrayList)this.delayedMessages.get(paramVarArgs);
              } while (localObject2 == null);
              paramInt1 = 0;
              if (paramInt1 < ((ArrayList)localObject2).size())
              {
                localObject3 = (DelayedMessage)((ArrayList)localObject2).get(paramInt1);
                int i;
                if (((DelayedMessage)localObject3).type == 4)
                {
                  i = 0;
                  paramInt2 = paramInt1;
                  if (i < ((DelayedMessage)localObject3).messages.size())
                  {
                    if (((DelayedMessage)localObject3).messageObjects.get(i) != localObject1) {
                      break label1996;
                    }
                    ((DelayedMessage)localObject3).markAsError();
                    ((ArrayList)localObject2).remove(paramInt1);
                    paramInt2 = paramInt1 - 1;
                  }
                }
                for (;;)
                {
                  paramInt1 = paramInt2 + 1;
                  break;
                  i += 1;
                  break label1943;
                  paramInt2 = paramInt1;
                  if (((DelayedMessage)localObject3).obj == localObject1)
                  {
                    ((DelayedMessage)localObject3).markAsError();
                    ((ArrayList)localObject2).remove(paramInt1);
                    paramInt2 = paramInt1 - 1;
                  }
                }
              }
            } while (!((ArrayList)localObject2).isEmpty());
            this.delayedMessages.remove(paramVarArgs);
            return;
            if (paramInt1 != NotificationCenter.httpFileDidLoaded) {
              break;
            }
            localObject1 = (String)paramVarArgs[0];
            localObject2 = (ArrayList)this.delayedMessages.get(localObject1);
          } while (localObject2 == null);
          paramInt2 = 0;
          if (paramInt2 < ((ArrayList)localObject2).size())
          {
            localObject3 = (DelayedMessage)((ArrayList)localObject2).get(paramInt2);
            paramInt1 = -1;
            if (((DelayedMessage)localObject3).type == 0)
            {
              paramInt1 = 0;
              paramVarArgs = ((DelayedMessage)localObject3).obj;
              if (paramInt1 != 0) {
                break label2277;
              }
              localObject4 = Utilities.MD5((String)localObject1) + "." + ImageLoader.getHttpUrlExtension((String)localObject1, "file");
              localObject4 = new File(FileLoader.getDirectory(4), (String)localObject4);
              Utilities.globalQueue.postRunnable(new Runnable()
              {
                public void run()
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      if (this.val$photo != null)
                      {
                        SendMessagesHelper.3.this.val$messageObject.messageOwner.media.photo = this.val$photo;
                        SendMessagesHelper.3.this.val$messageObject.messageOwner.attachPath = SendMessagesHelper.3.this.val$cacheFile.toString();
                        ArrayList localArrayList = new ArrayList();
                        localArrayList.add(SendMessagesHelper.3.this.val$messageObject.messageOwner);
                        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(localArrayList, false, true, false, 0);
                        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.updateMessageMedia, new Object[] { SendMessagesHelper.3.this.val$messageObject.messageOwner });
                        SendMessagesHelper.3.this.val$message.location = ((TLRPC.PhotoSize)this.val$photo.sizes.get(this.val$photo.sizes.size() - 1)).location;
                        SendMessagesHelper.3.this.val$message.httpLocation = null;
                        if (SendMessagesHelper.3.this.val$message.type == 4)
                        {
                          SendMessagesHelper.this.performSendDelayedMessage(SendMessagesHelper.3.this.val$message, SendMessagesHelper.3.this.val$message.messageObjects.indexOf(SendMessagesHelper.3.this.val$messageObject));
                          return;
                        }
                        SendMessagesHelper.this.performSendDelayedMessage(SendMessagesHelper.3.this.val$message);
                        return;
                      }
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.e("can't load image " + SendMessagesHelper.3.this.val$path + " to file " + SendMessagesHelper.3.this.val$cacheFile.toString());
                      }
                      SendMessagesHelper.3.this.val$message.markAsError();
                    }
                  });
                }
              });
            }
            for (;;)
            {
              paramInt2 += 1;
              break;
              if (((DelayedMessage)localObject3).type == 2)
              {
                paramInt1 = 1;
                paramVarArgs = ((DelayedMessage)localObject3).obj;
                break label2128;
              }
              if (((DelayedMessage)localObject3).type == 4)
              {
                paramVarArgs = (MessageObject)((DelayedMessage)localObject3).extraHashMap.get(localObject1);
                if (paramVarArgs.getDocument() != null)
                {
                  paramInt1 = 1;
                  break label2128;
                }
                paramInt1 = 0;
                break label2128;
              }
              paramVarArgs = null;
              break label2128;
              if (paramInt1 == 1)
              {
                localObject4 = Utilities.MD5((String)localObject1) + ".gif";
                localObject4 = new File(FileLoader.getDirectory(4), (String)localObject4);
                Utilities.globalQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    boolean bool = true;
                    localDocument = localObject3.obj.getDocument();
                    if ((localDocument.thumb.location instanceof TLRPC.TL_fileLocationUnavailable)) {}
                    for (;;)
                    {
                      try
                      {
                        Bitmap localBitmap = ImageLoader.loadBitmap(localObject4.getAbsolutePath(), null, 90.0F, 90.0F, true);
                        if (localBitmap != null)
                        {
                          if (localObject3.sendEncryptedRequest == null) {
                            continue;
                          }
                          localDocument.thumb = ImageLoader.scaleAndSaveImage(localBitmap, 90.0F, 90.0F, 55, bool);
                          localBitmap.recycle();
                        }
                      }
                      catch (Exception localException)
                      {
                        localDocument.thumb = null;
                        FileLog.e(localException);
                        continue;
                      }
                      if (localDocument.thumb == null)
                      {
                        localDocument.thumb = new TLRPC.TL_photoSizeEmpty();
                        localDocument.thumb.type = "s";
                      }
                      AndroidUtilities.runOnUIThread(new Runnable()
                      {
                        public void run()
                        {
                          SendMessagesHelper.4.this.val$message.httpLocation = null;
                          SendMessagesHelper.4.this.val$message.obj.messageOwner.attachPath = SendMessagesHelper.4.this.val$cacheFile.toString();
                          SendMessagesHelper.4.this.val$message.location = localDocument.thumb.location;
                          ArrayList localArrayList = new ArrayList();
                          localArrayList.add(SendMessagesHelper.4.this.val$messageObject.messageOwner);
                          MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(localArrayList, false, true, false, 0);
                          SendMessagesHelper.this.performSendDelayedMessage(SendMessagesHelper.4.this.val$message);
                          NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.updateMessageMedia, new Object[] { SendMessagesHelper.4.this.val$message.obj.messageOwner });
                        }
                      });
                      return;
                      bool = false;
                    }
                  }
                });
              }
            }
          }
          this.delayedMessages.remove(localObject1);
          return;
          if (paramInt1 != NotificationCenter.FileDidLoaded) {
            break;
          }
          paramVarArgs = (String)paramVarArgs[0];
          localObject1 = (ArrayList)this.delayedMessages.get(paramVarArgs);
        } while (localObject1 == null);
        paramInt1 = 0;
        while (paramInt1 < ((ArrayList)localObject1).size())
        {
          performSendDelayedMessage((DelayedMessage)((ArrayList)localObject1).get(paramInt1));
          paramInt1 += 1;
        }
        this.delayedMessages.remove(paramVarArgs);
        return;
      } while ((paramInt1 != NotificationCenter.httpFileDidFailedLoad) && (paramInt1 != NotificationCenter.FileDidFailedLoad));
      paramVarArgs = (String)paramVarArgs[0];
      localObject1 = (ArrayList)this.delayedMessages.get(paramVarArgs);
    } while (localObject1 == null);
    paramInt1 = 0;
    while (paramInt1 < ((ArrayList)localObject1).size())
    {
      ((DelayedMessage)((ArrayList)localObject1).get(paramInt1)).markAsError();
      paramInt1 += 1;
    }
    this.delayedMessages.remove(paramVarArgs);
  }
  
  public int editMessage(MessageObject paramMessageObject, String paramString, boolean paramBoolean, final BaseFragment paramBaseFragment, ArrayList<TLRPC.MessageEntity> paramArrayList, final Runnable paramRunnable)
  {
    boolean bool = false;
    if ((paramBaseFragment == null) || (paramBaseFragment.getParentActivity() == null) || (paramRunnable == null)) {
      return 0;
    }
    final TLRPC.TL_messages_editMessage localTL_messages_editMessage = new TLRPC.TL_messages_editMessage();
    localTL_messages_editMessage.peer = MessagesController.getInstance(this.currentAccount).getInputPeer((int)paramMessageObject.getDialogId());
    localTL_messages_editMessage.message = paramString;
    localTL_messages_editMessage.flags |= 0x800;
    localTL_messages_editMessage.id = paramMessageObject.getId();
    if (!paramBoolean) {
      bool = true;
    }
    localTL_messages_editMessage.no_webpage = bool;
    if (paramArrayList != null)
    {
      localTL_messages_editMessage.entities = paramArrayList;
      localTL_messages_editMessage.flags |= 0x8;
    }
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_editMessage, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            SendMessagesHelper.6.this.val$callback.run();
          }
        });
        if (paramAnonymousTL_error == null)
        {
          MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            AlertsCreator.processError(SendMessagesHelper.this.currentAccount, paramAnonymousTL_error, SendMessagesHelper.6.this.val$fragment, SendMessagesHelper.6.this.val$req, new Object[0]);
          }
        });
      }
    });
  }
  
  public TLRPC.TL_photo generatePhotoSizes(String paramString, Uri paramUri)
  {
    Bitmap localBitmap2 = ImageLoader.loadBitmap(paramString, paramUri, AndroidUtilities.getPhotoSize(), AndroidUtilities.getPhotoSize(), true);
    Bitmap localBitmap1 = localBitmap2;
    if (localBitmap2 == null)
    {
      localBitmap1 = localBitmap2;
      if (AndroidUtilities.getPhotoSize() != 800) {
        localBitmap1 = ImageLoader.loadBitmap(paramString, paramUri, 800.0F, 800.0F, true);
      }
    }
    paramString = new ArrayList();
    paramUri = ImageLoader.scaleAndSaveImage(localBitmap1, 90.0F, 90.0F, 55, true);
    if (paramUri != null) {
      paramString.add(paramUri);
    }
    paramUri = ImageLoader.scaleAndSaveImage(localBitmap1, AndroidUtilities.getPhotoSize(), AndroidUtilities.getPhotoSize(), 80, false, 101, 101);
    if (paramUri != null) {
      paramString.add(paramUri);
    }
    if (localBitmap1 != null) {
      localBitmap1.recycle();
    }
    if (paramString.isEmpty()) {
      return null;
    }
    UserConfig.getInstance(this.currentAccount).saveConfig(false);
    paramUri = new TLRPC.TL_photo();
    paramUri.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    paramUri.sizes = paramString;
    return paramUri;
  }
  
  protected ArrayList<DelayedMessage> getDelayedMessages(String paramString)
  {
    return (ArrayList)this.delayedMessages.get(paramString);
  }
  
  protected long getNextRandomId()
  {
    for (long l = 0L; l == 0L; l = Utilities.random.nextLong()) {}
    return l;
  }
  
  public boolean isSendingCallback(MessageObject paramMessageObject, TLRPC.KeyboardButton paramKeyboardButton)
  {
    if ((paramMessageObject == null) || (paramKeyboardButton == null)) {
      return false;
    }
    int i;
    if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonGame)) {
      i = 1;
    }
    for (;;)
    {
      paramMessageObject = paramMessageObject.getDialogId() + "_" + paramMessageObject.getId() + "_" + Utilities.bytesToHex(paramKeyboardButton.data) + "_" + i;
      return this.waitingForCallback.containsKey(paramMessageObject);
      if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonBuy)) {
        i = 2;
      } else {
        i = 0;
      }
    }
  }
  
  public boolean isSendingCurrentLocation(MessageObject paramMessageObject, TLRPC.KeyboardButton paramKeyboardButton)
  {
    if ((paramMessageObject == null) || (paramKeyboardButton == null)) {
      return false;
    }
    StringBuilder localStringBuilder = new StringBuilder().append(paramMessageObject.getDialogId()).append("_").append(paramMessageObject.getId()).append("_").append(Utilities.bytesToHex(paramKeyboardButton.data)).append("_");
    if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonGame)) {}
    for (paramMessageObject = "1";; paramMessageObject = "0")
    {
      paramMessageObject = paramMessageObject;
      return this.waitingForLocation.containsKey(paramMessageObject);
    }
  }
  
  public boolean isSendingMessage(int paramInt)
  {
    return this.sendingMessages.indexOfKey(paramInt) >= 0;
  }
  
  public void processForwardFromMyName(MessageObject paramMessageObject, long paramLong)
  {
    if (paramMessageObject == null) {}
    do
    {
      do
      {
        return;
        if ((paramMessageObject.messageOwner.media == null) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaEmpty)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice))) {
          break;
        }
        if ((paramMessageObject.messageOwner.media.photo instanceof TLRPC.TL_photo))
        {
          sendMessage((TLRPC.TL_photo)paramMessageObject.messageOwner.media.photo, null, paramLong, paramMessageObject.replyMessageObject, paramMessageObject.messageOwner.message, paramMessageObject.messageOwner.entities, null, null, paramMessageObject.messageOwner.media.ttl_seconds);
          return;
        }
        if ((paramMessageObject.messageOwner.media.document instanceof TLRPC.TL_document))
        {
          sendMessage((TLRPC.TL_document)paramMessageObject.messageOwner.media.document, null, paramMessageObject.messageOwner.attachPath, paramLong, paramMessageObject.replyMessageObject, paramMessageObject.messageOwner.message, paramMessageObject.messageOwner.entities, null, null, paramMessageObject.messageOwner.media.ttl_seconds);
          return;
        }
        if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVenue)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo)))
        {
          sendMessage(paramMessageObject.messageOwner.media, paramLong, paramMessageObject.replyMessageObject, null, null);
          return;
        }
        if (paramMessageObject.messageOwner.media.phone_number != null)
        {
          localObject1 = new TLRPC.TL_userContact_old2();
          ((TLRPC.User)localObject1).phone = paramMessageObject.messageOwner.media.phone_number;
          ((TLRPC.User)localObject1).first_name = paramMessageObject.messageOwner.media.first_name;
          ((TLRPC.User)localObject1).last_name = paramMessageObject.messageOwner.media.last_name;
          ((TLRPC.User)localObject1).id = paramMessageObject.messageOwner.media.user_id;
          sendMessage((TLRPC.User)localObject1, paramLong, paramMessageObject.replyMessageObject, null, null);
          return;
        }
      } while ((int)paramLong == 0);
      localObject1 = new ArrayList();
      ((ArrayList)localObject1).add(paramMessageObject);
      sendMessage((ArrayList)localObject1, paramLong);
      return;
      if (paramMessageObject.messageOwner.message != null)
      {
        localObject1 = null;
        if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) {
          localObject1 = paramMessageObject.messageOwner.media.webpage;
        }
        if ((paramMessageObject.messageOwner.entities != null) && (!paramMessageObject.messageOwner.entities.isEmpty()))
        {
          ArrayList localArrayList = new ArrayList();
          int i = 0;
          for (;;)
          {
            localObject2 = localArrayList;
            if (i >= paramMessageObject.messageOwner.entities.size()) {
              break;
            }
            localObject2 = (TLRPC.MessageEntity)paramMessageObject.messageOwner.entities.get(i);
            if (((localObject2 instanceof TLRPC.TL_messageEntityBold)) || ((localObject2 instanceof TLRPC.TL_messageEntityItalic)) || ((localObject2 instanceof TLRPC.TL_messageEntityPre)) || ((localObject2 instanceof TLRPC.TL_messageEntityCode)) || ((localObject2 instanceof TLRPC.TL_messageEntityTextUrl))) {
              localArrayList.add(localObject2);
            }
            i += 1;
          }
        }
        Object localObject2 = null;
        sendMessage(paramMessageObject.messageOwner.message, paramLong, paramMessageObject.replyMessageObject, (TLRPC.WebPage)localObject1, true, (ArrayList)localObject2, null, null);
        return;
      }
    } while ((int)paramLong == 0);
    Object localObject1 = new ArrayList();
    ((ArrayList)localObject1).add(paramMessageObject);
    sendMessage((ArrayList)localObject1, paramLong);
  }
  
  protected void processSentMessage(int paramInt)
  {
    int i = this.unsentMessages.size();
    this.unsentMessages.remove(paramInt);
    if ((i != 0) && (this.unsentMessages.size() == 0)) {
      checkUnsentMessages();
    }
  }
  
  protected void processUnsentMessages(final ArrayList<TLRPC.Message> paramArrayList, final ArrayList<TLRPC.User> paramArrayList1, final ArrayList<TLRPC.Chat> paramArrayList2, final ArrayList<TLRPC.EncryptedChat> paramArrayList3)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putUsers(paramArrayList1, true);
        MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putChats(paramArrayList2, true);
        MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putEncryptedChats(paramArrayList3, true);
        int i = 0;
        while (i < paramArrayList.size())
        {
          Object localObject = (TLRPC.Message)paramArrayList.get(i);
          localObject = new MessageObject(SendMessagesHelper.this.currentAccount, (TLRPC.Message)localObject, false);
          SendMessagesHelper.this.retrySendMessage((MessageObject)localObject, true);
          i += 1;
        }
      }
    });
  }
  
  protected void putToSendingMessages(TLRPC.Message paramMessage)
  {
    this.sendingMessages.put(paramMessage.id, paramMessage);
  }
  
  protected void removeFromSendingMessages(int paramInt)
  {
    this.sendingMessages.remove(paramInt);
  }
  
  public boolean retrySendMessage(MessageObject paramMessageObject, boolean paramBoolean)
  {
    if (paramMessageObject.getId() >= 0) {
      return false;
    }
    if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageEncryptedAction))
    {
      int i = (int)(paramMessageObject.getDialogId() >> 32);
      TLRPC.EncryptedChat localEncryptedChat = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(i));
      if (localEncryptedChat == null)
      {
        MessagesStorage.getInstance(this.currentAccount).markMessageAsSendError(paramMessageObject.messageOwner);
        paramMessageObject.messageOwner.send_state = 2;
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.messageSendError, new Object[] { Integer.valueOf(paramMessageObject.getId()) });
        processSentMessage(paramMessageObject.getId());
        return false;
      }
      if (paramMessageObject.messageOwner.random_id == 0L) {
        paramMessageObject.messageOwner.random_id = getNextRandomId();
      }
      if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL)) {
        SecretChatHelper.getInstance(this.currentAccount).sendTTLMessage(localEncryptedChat, paramMessageObject.messageOwner);
      }
      for (;;)
      {
        return true;
        if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionDeleteMessages)) {
          SecretChatHelper.getInstance(this.currentAccount).sendMessagesDeleteMessage(localEncryptedChat, null, paramMessageObject.messageOwner);
        } else if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionFlushHistory)) {
          SecretChatHelper.getInstance(this.currentAccount).sendClearHistoryMessage(localEncryptedChat, paramMessageObject.messageOwner);
        } else if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionNotifyLayer)) {
          SecretChatHelper.getInstance(this.currentAccount).sendNotifyLayerMessage(localEncryptedChat, paramMessageObject.messageOwner);
        } else if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionReadMessages)) {
          SecretChatHelper.getInstance(this.currentAccount).sendMessagesReadMessage(localEncryptedChat, null, paramMessageObject.messageOwner);
        } else if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages)) {
          SecretChatHelper.getInstance(this.currentAccount).sendScreenshotMessage(localEncryptedChat, null, paramMessageObject.messageOwner);
        } else if ((!(paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionTyping)) && (!(paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionResend))) {
          if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionCommitKey)) {
            SecretChatHelper.getInstance(this.currentAccount).sendCommitKeyMessage(localEncryptedChat, paramMessageObject.messageOwner);
          } else if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionAbortKey)) {
            SecretChatHelper.getInstance(this.currentAccount).sendAbortKeyMessage(localEncryptedChat, paramMessageObject.messageOwner, 0L);
          } else if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionRequestKey)) {
            SecretChatHelper.getInstance(this.currentAccount).sendRequestKeyMessage(localEncryptedChat, paramMessageObject.messageOwner);
          } else if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionAcceptKey)) {
            SecretChatHelper.getInstance(this.currentAccount).sendAcceptKeyMessage(localEncryptedChat, paramMessageObject.messageOwner);
          } else if ((paramMessageObject.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionNoop)) {
            SecretChatHelper.getInstance(this.currentAccount).sendNoopMessage(localEncryptedChat, paramMessageObject.messageOwner);
          }
        }
      }
    }
    if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionScreenshotTaken)) {
      sendScreenshotMessage(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf((int)paramMessageObject.getDialogId())), paramMessageObject.messageOwner.reply_to_msg_id, paramMessageObject.messageOwner);
    }
    if (paramBoolean) {
      this.unsentMessages.put(paramMessageObject.getId(), paramMessageObject);
    }
    sendMessage(paramMessageObject);
    return true;
  }
  
  public void sendCallback(final boolean paramBoolean, final MessageObject paramMessageObject, final TLRPC.KeyboardButton paramKeyboardButton, final ChatActivity paramChatActivity)
  {
    if ((paramMessageObject == null) || (paramKeyboardButton == null) || (paramChatActivity == null)) {
      return;
    }
    int i;
    if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonGame))
    {
      paramBoolean = false;
      i = 1;
    }
    for (;;)
    {
      localObject = paramMessageObject.getDialogId() + "_" + paramMessageObject.getId() + "_" + Utilities.bytesToHex(paramKeyboardButton.data) + "_" + i;
      this.waitingForCallback.put(localObject, Boolean.valueOf(true));
      paramChatActivity = new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              SendMessagesHelper.this.waitingForCallback.remove(SendMessagesHelper.8.this.val$key);
              if ((SendMessagesHelper.8.this.val$cacheFinal) && (paramAnonymousTLObject == null)) {
                SendMessagesHelper.this.sendCallback(false, SendMessagesHelper.8.this.val$messageObject, SendMessagesHelper.8.this.val$button, SendMessagesHelper.8.this.val$parentFragment);
              }
              label70:
              Object localObject3;
              Object localObject2;
              label520:
              do
              {
                do
                {
                  do
                  {
                    break label70;
                    do
                    {
                      return;
                    } while (paramAnonymousTLObject == null);
                    if (!(SendMessagesHelper.8.this.val$button instanceof TLRPC.TL_keyboardButtonBuy)) {
                      break;
                    }
                    if ((paramAnonymousTLObject instanceof TLRPC.TL_payments_paymentForm))
                    {
                      localObject1 = (TLRPC.TL_payments_paymentForm)paramAnonymousTLObject;
                      MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putUsers(((TLRPC.TL_payments_paymentForm)localObject1).users, false);
                      SendMessagesHelper.8.this.val$parentFragment.presentFragment(new PaymentFormActivity((TLRPC.TL_payments_paymentForm)localObject1, SendMessagesHelper.8.this.val$messageObject));
                      return;
                    }
                  } while (!(paramAnonymousTLObject instanceof TLRPC.TL_payments_paymentReceipt));
                  SendMessagesHelper.8.this.val$parentFragment.presentFragment(new PaymentFormActivity(SendMessagesHelper.8.this.val$messageObject, (TLRPC.TL_payments_paymentReceipt)paramAnonymousTLObject));
                  return;
                  localObject3 = (TLRPC.TL_messages_botCallbackAnswer)paramAnonymousTLObject;
                  if ((!SendMessagesHelper.8.this.val$cacheFinal) && (((TLRPC.TL_messages_botCallbackAnswer)localObject3).cache_time != 0)) {
                    MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).saveBotCache(SendMessagesHelper.8.this.val$key, (TLObject)localObject3);
                  }
                  if (((TLRPC.TL_messages_botCallbackAnswer)localObject3).message == null) {
                    break label520;
                  }
                  if (!((TLRPC.TL_messages_botCallbackAnswer)localObject3).alert) {
                    break;
                  }
                } while (SendMessagesHelper.8.this.val$parentFragment.getParentActivity() == null);
                localObject1 = new AlertDialog.Builder(SendMessagesHelper.8.this.val$parentFragment.getParentActivity());
                ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
                ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
                ((AlertDialog.Builder)localObject1).setMessage(((TLRPC.TL_messages_botCallbackAnswer)localObject3).message);
                SendMessagesHelper.8.this.val$parentFragment.showDialog(((AlertDialog.Builder)localObject1).create());
                return;
                i = SendMessagesHelper.8.this.val$messageObject.messageOwner.from_id;
                if (SendMessagesHelper.8.this.val$messageObject.messageOwner.via_bot_id != 0) {
                  i = SendMessagesHelper.8.this.val$messageObject.messageOwner.via_bot_id;
                }
                localObject1 = null;
                if (i > 0)
                {
                  localObject2 = MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getUser(Integer.valueOf(i));
                  if (localObject2 != null) {
                    localObject1 = ContactsController.formatName(((TLRPC.User)localObject2).first_name, ((TLRPC.User)localObject2).last_name);
                  }
                }
                for (;;)
                {
                  localObject2 = localObject1;
                  if (localObject1 == null) {
                    localObject2 = "bot";
                  }
                  SendMessagesHelper.8.this.val$parentFragment.showAlert((String)localObject2, ((TLRPC.TL_messages_botCallbackAnswer)localObject3).message);
                  return;
                  localObject2 = MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getChat(Integer.valueOf(-i));
                  if (localObject2 != null) {
                    localObject1 = ((TLRPC.Chat)localObject2).title;
                  }
                }
              } while ((((TLRPC.TL_messages_botCallbackAnswer)localObject3).url == null) || (SendMessagesHelper.8.this.val$parentFragment.getParentActivity() == null));
              int i = SendMessagesHelper.8.this.val$messageObject.messageOwner.from_id;
              if (SendMessagesHelper.8.this.val$messageObject.messageOwner.via_bot_id != 0) {
                i = SendMessagesHelper.8.this.val$messageObject.messageOwner.via_bot_id;
              }
              Object localObject1 = MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getUser(Integer.valueOf(i));
              int j;
              label622:
              label672:
              MessageObject localMessageObject;
              if ((localObject1 != null) && (((TLRPC.User)localObject1).verified))
              {
                j = 1;
                if (!(SendMessagesHelper.8.this.val$button instanceof TLRPC.TL_keyboardButtonGame)) {
                  break label779;
                }
                if (!(SendMessagesHelper.8.this.val$messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) {
                  break label768;
                }
                localObject1 = SendMessagesHelper.8.this.val$messageObject.messageOwner.media.game;
                if (localObject1 == null) {
                  break label772;
                }
                localObject2 = SendMessagesHelper.8.this.val$parentFragment;
                localMessageObject = SendMessagesHelper.8.this.val$messageObject;
                localObject3 = ((TLRPC.TL_messages_botCallbackAnswer)localObject3).url;
                if ((j != 0) || (!MessagesController.getNotificationsSettings(SendMessagesHelper.this.currentAccount).getBoolean("askgame_" + i, true))) {
                  break label774;
                }
              }
              label768:
              label772:
              label774:
              for (boolean bool = true;; bool = false)
              {
                ((ChatActivity)localObject2).showOpenGameAlert((TLRPC.TL_game)localObject1, localMessageObject, (String)localObject3, bool, i);
                return;
                j = 0;
                break label622;
                localObject1 = null;
                break label672;
                break;
              }
              label779:
              SendMessagesHelper.8.this.val$parentFragment.showOpenUrlAlert(((TLRPC.TL_messages_botCallbackAnswer)localObject3).url, false);
            }
          });
        }
      };
      if (!paramBoolean) {
        break;
      }
      MessagesStorage.getInstance(this.currentAccount).getBotCache((String)localObject, paramChatActivity);
      return;
      if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonBuy)) {
        i = 2;
      } else {
        i = 0;
      }
    }
    if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonBuy))
    {
      if ((paramMessageObject.messageOwner.media.flags & 0x4) == 0)
      {
        paramKeyboardButton = new TLRPC.TL_payments_getPaymentForm();
        paramKeyboardButton.msg_id = paramMessageObject.getId();
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramKeyboardButton, paramChatActivity, 2);
        return;
      }
      paramKeyboardButton = new TLRPC.TL_payments_getPaymentReceipt();
      paramKeyboardButton.msg_id = paramMessageObject.messageOwner.media.receipt_msg_id;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramKeyboardButton, paramChatActivity, 2);
      return;
    }
    final Object localObject = new TLRPC.TL_messages_getBotCallbackAnswer();
    ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).peer = MessagesController.getInstance(this.currentAccount).getInputPeer((int)paramMessageObject.getDialogId());
    ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).msg_id = paramMessageObject.getId();
    ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).game = (paramKeyboardButton instanceof TLRPC.TL_keyboardButtonGame);
    if (paramKeyboardButton.data != null)
    {
      ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).flags |= 0x1;
      ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).data = paramKeyboardButton.data;
    }
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, paramChatActivity, 2);
  }
  
  public void sendCurrentLocation(MessageObject paramMessageObject, TLRPC.KeyboardButton paramKeyboardButton)
  {
    if ((paramMessageObject == null) || (paramKeyboardButton == null)) {
      return;
    }
    StringBuilder localStringBuilder = new StringBuilder().append(paramMessageObject.getDialogId()).append("_").append(paramMessageObject.getId()).append("_").append(Utilities.bytesToHex(paramKeyboardButton.data)).append("_");
    if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonGame)) {}
    for (paramKeyboardButton = "1";; paramKeyboardButton = "0")
    {
      paramKeyboardButton = paramKeyboardButton;
      this.waitingForLocation.put(paramKeyboardButton, paramMessageObject);
      this.locationProvider.start();
      return;
    }
  }
  
  public void sendGame(TLRPC.InputPeer paramInputPeer, TLRPC.TL_inputMediaGame paramTL_inputMediaGame, final long paramLong1, long paramLong2)
  {
    if ((paramInputPeer == null) || (paramTL_inputMediaGame == null)) {
      return;
    }
    TLRPC.TL_messages_sendMedia localTL_messages_sendMedia = new TLRPC.TL_messages_sendMedia();
    localTL_messages_sendMedia.peer = paramInputPeer;
    if ((localTL_messages_sendMedia.peer instanceof TLRPC.TL_inputPeerChannel)) {
      localTL_messages_sendMedia.silent = MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("silent_" + paramInputPeer.channel_id, false);
    }
    long l;
    Object localObject;
    if (paramLong1 != 0L)
    {
      l = paramLong1;
      localTL_messages_sendMedia.random_id = l;
      localTL_messages_sendMedia.message = "";
      localTL_messages_sendMedia.media = paramTL_inputMediaGame;
      if (paramLong2 != 0L) {
        break label221;
      }
      localObject = null;
    }
    for (;;)
    {
      try
      {
        localNativeByteBuffer = new NativeByteBuffer(paramInputPeer.getObjectSize() + paramTL_inputMediaGame.getObjectSize() + 4 + 8);
      }
      catch (Exception paramTL_inputMediaGame)
      {
        paramInputPeer = (TLRPC.InputPeer)localObject;
      }
      try
      {
        localNativeByteBuffer.writeInt32(3);
        localNativeByteBuffer.writeInt64(paramLong1);
        paramInputPeer.serializeToStream(localNativeByteBuffer);
        paramTL_inputMediaGame.serializeToStream(localNativeByteBuffer);
        paramInputPeer = localNativeByteBuffer;
        paramLong1 = MessagesStorage.getInstance(this.currentAccount).createPendingTask(paramInputPeer);
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_sendMedia, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTL_error == null) {
              MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
            }
            if (paramLong1 != 0L) {
              MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).removePendingTask(paramLong1);
            }
          }
        });
        return;
      }
      catch (Exception paramTL_inputMediaGame)
      {
        for (;;)
        {
          paramInputPeer = localNativeByteBuffer;
        }
      }
      l = getNextRandomId();
      break;
      FileLog.e(paramTL_inputMediaGame);
      continue;
      label221:
      paramLong1 = paramLong2;
    }
  }
  
  public int sendMessage(ArrayList<MessageObject> paramArrayList, final long paramLong)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      k = 0;
    }
    int i3;
    int i1;
    int j;
    final TLRPC.Peer localPeer;
    boolean bool1;
    int i4;
    int i2;
    int m;
    int n;
    Object localObject1;
    final boolean bool2;
    label206:
    label236:
    LongSparseArray localLongSparseArray;
    final Object localObject2;
    final Object localObject5;
    Object localObject4;
    final Object localObject3;
    TLRPC.InputPeer localInputPeer;
    label327:
    label338:
    do
    {
      return k;
      i3 = (int)paramLong;
      i1 = 0;
      j = 0;
      if (i3 == 0) {
        break label2677;
      }
      localPeer = MessagesController.getInstance(this.currentAccount).getPeer((int)paramLong);
      bool1 = false;
      i4 = 0;
      j = 1;
      k = 1;
      i2 = 1;
      if (i3 > 0)
      {
        m = k;
        i = i2;
        n = j;
        if (MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i3)) == null) {
          return 0;
        }
      }
      else
      {
        localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i3));
        m = k;
        i = i2;
        n = j;
        if (ChatObject.isChannel((TLRPC.Chat)localObject1))
        {
          bool2 = ((TLRPC.Chat)localObject1).megagroup;
          bool3 = ((TLRPC.Chat)localObject1).signatures;
          m = k;
          i = i2;
          n = j;
          bool1 = bool2;
          i4 = bool3;
          if (((TLRPC.Chat)localObject1).banned_rights != null)
          {
            if (((TLRPC.Chat)localObject1).banned_rights.send_stickers) {
              break;
            }
            j = 1;
            if (((TLRPC.Chat)localObject1).banned_rights.send_media) {
              break label465;
            }
            k = 1;
            if (((TLRPC.Chat)localObject1).banned_rights.embed_links) {
              break label471;
            }
            i = 1;
            i4 = bool3;
            bool1 = bool2;
            n = j;
            m = k;
          }
        }
      }
      localLongSparseArray = new LongSparseArray();
      localObject2 = new ArrayList();
      localObject5 = new ArrayList();
      localObject1 = new ArrayList();
      localObject4 = new ArrayList();
      localObject3 = new LongSparseArray();
      localInputPeer = MessagesController.getInstance(this.currentAccount).getInputPeer(i3);
      i3 = UserConfig.getInstance(this.currentAccount).getClientUserId();
      if (paramLong != i3) {
        break label493;
      }
      bool2 = true;
      k = 0;
      j = i1;
      i1 = k;
      k = j;
    } while (i1 >= paramArrayList.size());
    MessageObject localMessageObject = (MessageObject)paramArrayList.get(i1);
    final Object localObject6 = localObject5;
    Object localObject7 = localObject4;
    Object localObject8 = localObject3;
    Object localObject9 = localObject2;
    Object localObject10 = localObject1;
    int k = j;
    if (localMessageObject.getId() > 0)
    {
      if (!localMessageObject.needDrawBluredPreview()) {
        break label499;
      }
      k = j;
      localObject10 = localObject1;
      localObject9 = localObject2;
      localObject8 = localObject3;
      localObject7 = localObject4;
      localObject6 = localObject5;
    }
    label465:
    label471:
    label493:
    label499:
    label671:
    label719:
    label859:
    label955:
    long l;
    label1331:
    label1344:
    label1451:
    do
    {
      do
      {
        for (;;)
        {
          i1 += 1;
          localObject5 = localObject6;
          localObject4 = localObject7;
          localObject3 = localObject8;
          localObject2 = localObject9;
          localObject1 = localObject10;
          j = k;
          break label338;
          j = 0;
          break;
          k = 0;
          break label206;
          i = 0;
          m = k;
          n = j;
          bool1 = bool2;
          i4 = bool3;
          break label236;
          bool2 = false;
          break label327;
          if ((n == 0) && ((localMessageObject.isSticker()) || (localMessageObject.isGif()) || (localMessageObject.isGame())))
          {
            localObject6 = localObject5;
            localObject7 = localObject4;
            localObject8 = localObject3;
            localObject9 = localObject2;
            localObject10 = localObject1;
            k = j;
            if (j == 0)
            {
              k = 1;
              localObject6 = localObject5;
              localObject7 = localObject4;
              localObject8 = localObject3;
              localObject9 = localObject2;
              localObject10 = localObject1;
            }
          }
          else
          {
            if ((m != 0) || ((!(localMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) && (!(localMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument)))) {
              break label671;
            }
            localObject6 = localObject5;
            localObject7 = localObject4;
            localObject8 = localObject3;
            localObject9 = localObject2;
            localObject10 = localObject1;
            k = j;
            if (j == 0)
            {
              k = 2;
              localObject6 = localObject5;
              localObject7 = localObject4;
              localObject8 = localObject3;
              localObject9 = localObject2;
              localObject10 = localObject1;
            }
          }
        }
        i2 = 0;
        localObject8 = new TLRPC.TL_message();
        if ((localMessageObject.getDialogId() != i3) || (localMessageObject.messageOwner.from_id != UserConfig.getInstance(this.currentAccount).getClientUserId())) {
          break label2164;
        }
        k = 1;
        if (!localMessageObject.isForwarded()) {
          break label2170;
        }
        ((TLRPC.Message)localObject8).fwd_from = new TLRPC.TL_messageFwdHeader();
        ((TLRPC.Message)localObject8).fwd_from.flags = localMessageObject.messageOwner.fwd_from.flags;
        ((TLRPC.Message)localObject8).fwd_from.from_id = localMessageObject.messageOwner.fwd_from.from_id;
        ((TLRPC.Message)localObject8).fwd_from.date = localMessageObject.messageOwner.fwd_from.date;
        ((TLRPC.Message)localObject8).fwd_from.channel_id = localMessageObject.messageOwner.fwd_from.channel_id;
        ((TLRPC.Message)localObject8).fwd_from.channel_post = localMessageObject.messageOwner.fwd_from.channel_post;
        ((TLRPC.Message)localObject8).fwd_from.post_author = localMessageObject.messageOwner.fwd_from.post_author;
        ((TLRPC.Message)localObject8).flags = 4;
        if ((paramLong == i3) && (((TLRPC.Message)localObject8).fwd_from != null))
        {
          localObject6 = ((TLRPC.Message)localObject8).fwd_from;
          ((TLRPC.MessageFwdHeader)localObject6).flags |= 0x10;
          ((TLRPC.Message)localObject8).fwd_from.saved_from_msg_id = localMessageObject.getId();
          ((TLRPC.Message)localObject8).fwd_from.saved_from_peer = localMessageObject.messageOwner.to_id;
        }
        if ((i != 0) || (!(localMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage))) {
          break label2531;
        }
        ((TLRPC.Message)localObject8).media = new TLRPC.TL_messageMediaEmpty();
        if (((TLRPC.Message)localObject8).media != null) {
          ((TLRPC.Message)localObject8).flags |= 0x200;
        }
        if (bool1) {
          ((TLRPC.Message)localObject8).flags |= 0x80000000;
        }
        if (localMessageObject.messageOwner.via_bot_id != 0)
        {
          ((TLRPC.Message)localObject8).via_bot_id = localMessageObject.messageOwner.via_bot_id;
          ((TLRPC.Message)localObject8).flags |= 0x800;
        }
        ((TLRPC.Message)localObject8).message = localMessageObject.messageOwner.message;
        ((TLRPC.Message)localObject8).fwd_msg_id = localMessageObject.getId();
        ((TLRPC.Message)localObject8).attachPath = localMessageObject.messageOwner.attachPath;
        ((TLRPC.Message)localObject8).entities = localMessageObject.messageOwner.entities;
        if (!((TLRPC.Message)localObject8).entities.isEmpty()) {
          ((TLRPC.Message)localObject8).flags |= 0x80;
        }
        if (((TLRPC.Message)localObject8).attachPath == null) {
          ((TLRPC.Message)localObject8).attachPath = "";
        }
        k = UserConfig.getInstance(this.currentAccount).getNewMessageId();
        ((TLRPC.Message)localObject8).id = k;
        ((TLRPC.Message)localObject8).local_id = k;
        ((TLRPC.Message)localObject8).out = true;
        l = localMessageObject.messageOwner.grouped_id;
        if (l != 0L)
        {
          localObject7 = (Long)localLongSparseArray.get(localMessageObject.messageOwner.grouped_id);
          localObject6 = localObject7;
          if (localObject7 == null)
          {
            localObject6 = Long.valueOf(Utilities.random.nextLong());
            localLongSparseArray.put(localMessageObject.messageOwner.grouped_id, localObject6);
          }
          ((TLRPC.Message)localObject8).grouped_id = ((Long)localObject6).longValue();
          ((TLRPC.Message)localObject8).flags |= 0x20000;
        }
        k = i2;
        if (i1 != paramArrayList.size() - 1)
        {
          k = i2;
          if (((MessageObject)paramArrayList.get(i1 + 1)).messageOwner.grouped_id != localMessageObject.messageOwner.grouped_id) {
            k = 1;
          }
        }
        if ((localPeer.channel_id == 0) || (bool1)) {
          break label2558;
        }
        if (i4 == 0) {
          break label2547;
        }
        i2 = UserConfig.getInstance(this.currentAccount).getClientUserId();
        ((TLRPC.Message)localObject8).from_id = i2;
        ((TLRPC.Message)localObject8).post = true;
        if (((TLRPC.Message)localObject8).random_id == 0L) {
          ((TLRPC.Message)localObject8).random_id = getNextRandomId();
        }
        ((ArrayList)localObject1).add(Long.valueOf(((TLRPC.Message)localObject8).random_id));
        ((LongSparseArray)localObject3).put(((TLRPC.Message)localObject8).random_id, localObject8);
        ((ArrayList)localObject4).add(Integer.valueOf(((TLRPC.Message)localObject8).fwd_msg_id));
        ((TLRPC.Message)localObject8).date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
        if (!(localInputPeer instanceof TLRPC.TL_inputPeerChannel)) {
          break label2599;
        }
        if (bool1) {
          break label2590;
        }
        ((TLRPC.Message)localObject8).views = 1;
        ((TLRPC.Message)localObject8).flags |= 0x400;
        ((TLRPC.Message)localObject8).dialog_id = paramLong;
        ((TLRPC.Message)localObject8).to_id = localPeer;
        if ((MessageObject.isVoiceMessage((TLRPC.Message)localObject8)) || (MessageObject.isRoundVideoMessage((TLRPC.Message)localObject8))) {
          ((TLRPC.Message)localObject8).media_unread = true;
        }
        if ((localMessageObject.messageOwner.to_id instanceof TLRPC.TL_peerChannel)) {
          ((TLRPC.Message)localObject8).ttl = (-localMessageObject.messageOwner.to_id.channel_id);
        }
        localObject6 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject8, true);
        ((MessageObject)localObject6).messageOwner.send_state = 1;
        ((ArrayList)localObject2).add(localObject6);
        ((ArrayList)localObject5).add(localObject8);
        putToSendingMessages((TLRPC.Message)localObject8);
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("forward message user_id = " + localInputPeer.user_id + " chat_id = " + localInputPeer.chat_id + " channel_id = " + localInputPeer.channel_id + " access_hash = " + localInputPeer.access_hash);
        }
        if (((k != 0) && (((ArrayList)localObject5).size() > 0)) || (((ArrayList)localObject5).size() == 100) || (i1 == paramArrayList.size() - 1)) {
          break label1755;
        }
        localObject6 = localObject5;
        localObject7 = localObject4;
        localObject8 = localObject3;
        localObject9 = localObject2;
        localObject10 = localObject1;
        k = j;
      } while (i1 == paramArrayList.size() - 1);
      localObject6 = localObject5;
      localObject7 = localObject4;
      localObject8 = localObject3;
      localObject9 = localObject2;
      localObject10 = localObject1;
      k = j;
    } while (((MessageObject)paramArrayList.get(i1 + 1)).getDialogId() == localMessageObject.getDialogId());
    label1755:
    MessagesStorage.getInstance(this.currentAccount).putMessages(new ArrayList((Collection)localObject5), false, true, false, 0);
    MessagesController.getInstance(this.currentAccount).updateInterfaceWithMessages(paramLong, (ArrayList)localObject2);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
    UserConfig.getInstance(this.currentAccount).saveConfig(false);
    localObject6 = new TLRPC.TL_messages_forwardMessages();
    ((TLRPC.TL_messages_forwardMessages)localObject6).to_peer = localInputPeer;
    if (l != 0L)
    {
      bool3 = true;
      label1845:
      ((TLRPC.TL_messages_forwardMessages)localObject6).grouped = bool3;
      if ((((TLRPC.TL_messages_forwardMessages)localObject6).to_peer instanceof TLRPC.TL_inputPeerChannel)) {
        ((TLRPC.TL_messages_forwardMessages)localObject6).silent = MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("silent_" + paramLong, false);
      }
      if (!(localMessageObject.messageOwner.to_id instanceof TLRPC.TL_peerChannel)) {
        break label2656;
      }
      localObject7 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(localMessageObject.messageOwner.to_id.channel_id));
      ((TLRPC.TL_messages_forwardMessages)localObject6).from_peer = new TLRPC.TL_inputPeerChannel();
      ((TLRPC.TL_messages_forwardMessages)localObject6).from_peer.channel_id = localMessageObject.messageOwner.to_id.channel_id;
      if (localObject7 != null) {
        ((TLRPC.TL_messages_forwardMessages)localObject6).from_peer.access_hash = ((TLRPC.Chat)localObject7).access_hash;
      }
      label1990:
      ((TLRPC.TL_messages_forwardMessages)localObject6).random_id = ((ArrayList)localObject1);
      ((TLRPC.TL_messages_forwardMessages)localObject6).id = ((ArrayList)localObject4);
      if ((paramArrayList.size() != 1) || (!((MessageObject)paramArrayList.get(0)).messageOwner.with_my_score)) {
        break label2671;
      }
    }
    label2164:
    label2170:
    label2262:
    label2429:
    label2531:
    label2547:
    label2558:
    label2590:
    label2599:
    label2656:
    label2671:
    for (boolean bool3 = true;; bool3 = false)
    {
      ((TLRPC.TL_messages_forwardMessages)localObject6).with_my_score = bool3;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject6, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          int i;
          if (paramAnonymousTL_error == null)
          {
            SparseLongArray localSparseLongArray = new SparseLongArray();
            TLRPC.Updates localUpdates = (TLRPC.Updates)paramAnonymousTLObject;
            for (i = 0; i < localUpdates.updates.size(); i = j + 1)
            {
              paramAnonymousTLObject = (TLRPC.Update)localUpdates.updates.get(i);
              j = i;
              if ((paramAnonymousTLObject instanceof TLRPC.TL_updateMessageID))
              {
                paramAnonymousTLObject = (TLRPC.TL_updateMessageID)paramAnonymousTLObject;
                localSparseLongArray.put(paramAnonymousTLObject.id, paramAnonymousTLObject.random_id);
                localUpdates.updates.remove(i);
                j = i - 1;
              }
            }
            paramAnonymousTL_error = (Integer)MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.get(Long.valueOf(paramLong));
            paramAnonymousTLObject = paramAnonymousTL_error;
            if (paramAnonymousTL_error == null)
            {
              paramAnonymousTLObject = Integer.valueOf(MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getDialogReadMax(true, paramLong));
              MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.put(Long.valueOf(paramLong), paramAnonymousTLObject);
            }
            int j = 0;
            i = 0;
            if (i < localUpdates.updates.size())
            {
              paramAnonymousTL_error = (TLRPC.Update)localUpdates.updates.get(i);
              final int k;
              int m;
              final Object localObject;
              label286:
              boolean bool;
              if (!(paramAnonymousTL_error instanceof TLRPC.TL_updateNewMessage))
              {
                k = i;
                m = j;
                if (!(paramAnonymousTL_error instanceof TLRPC.TL_updateNewChannelMessage)) {}
              }
              else
              {
                localUpdates.updates.remove(i);
                i -= 1;
                if (!(paramAnonymousTL_error instanceof TLRPC.TL_updateNewMessage)) {
                  break label395;
                }
                localObject = (TLRPC.TL_updateNewMessage)paramAnonymousTL_error;
                paramAnonymousTL_error = ((TLRPC.TL_updateNewMessage)localObject).message;
                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewDifferenceParams(-1, ((TLRPC.TL_updateNewMessage)localObject).pts, -1, ((TLRPC.TL_updateNewMessage)localObject).pts_count);
                ImageLoader.saveMessageThumbs(paramAnonymousTL_error);
                if (paramAnonymousTLObject.intValue() >= paramAnonymousTL_error.id) {
                  break label464;
                }
                bool = true;
                label304:
                paramAnonymousTL_error.unread = bool;
                if (localObject3)
                {
                  paramAnonymousTL_error.out = true;
                  paramAnonymousTL_error.unread = false;
                  paramAnonymousTL_error.media_unread = false;
                }
                long l = localSparseLongArray.get(paramAnonymousTL_error.id);
                k = i;
                m = j;
                if (l != 0L)
                {
                  localObject = (TLRPC.Message)localObject5.get(l);
                  if (localObject != null) {
                    break label470;
                  }
                  m = j;
                  k = i;
                }
              }
              for (;;)
              {
                i = k + 1;
                j = m;
                break;
                label395:
                paramAnonymousTL_error = (TLRPC.TL_updateNewChannelMessage)paramAnonymousTL_error;
                localObject = paramAnonymousTL_error.message;
                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewChannelDifferenceParams(paramAnonymousTL_error.pts, paramAnonymousTL_error.pts_count, ((TLRPC.Message)localObject).to_id.channel_id);
                paramAnonymousTL_error = (TLRPC.TL_error)localObject;
                if (!bool2) {
                  break label286;
                }
                ((TLRPC.Message)localObject).flags |= 0x80000000;
                paramAnonymousTL_error = (TLRPC.TL_error)localObject;
                break label286;
                label464:
                bool = false;
                break label304;
                label470:
                int n = localObject2.indexOf(localObject);
                k = i;
                m = j;
                if (n != -1)
                {
                  MessageObject localMessageObject = (MessageObject)localPeer.get(n);
                  localObject2.remove(n);
                  localPeer.remove(n);
                  k = ((TLRPC.Message)localObject).id;
                  final ArrayList localArrayList = new ArrayList();
                  localArrayList.add(paramAnonymousTL_error);
                  ((TLRPC.Message)localObject).id = paramAnonymousTL_error.id;
                  m = j + 1;
                  SendMessagesHelper.this.updateMediaPaths(localMessageObject, paramAnonymousTL_error, null, true);
                  MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                  {
                    public void run()
                    {
                      MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).updateMessageStateAndId(localObject.random_id, Integer.valueOf(k), localObject.id, 0, false, SendMessagesHelper.5.this.val$to_id.channel_id);
                      MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(localArrayList, true, false, false, 0);
                      AndroidUtilities.runOnUIThread(new Runnable()
                      {
                        public void run()
                        {
                          SendMessagesHelper.5.1.this.val$newMsgObj.send_state = 0;
                          DataQuery.getInstance(SendMessagesHelper.this.currentAccount).increasePeerRaiting(SendMessagesHelper.5.this.val$peer);
                          NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, new Object[] { Integer.valueOf(SendMessagesHelper.5.1.this.val$oldId), Integer.valueOf(SendMessagesHelper.5.1.this.val$message.id), SendMessagesHelper.5.1.this.val$message, Long.valueOf(SendMessagesHelper.5.this.val$peer) });
                          SendMessagesHelper.this.processSentMessage(SendMessagesHelper.5.1.this.val$oldId);
                          SendMessagesHelper.this.removeFromSendingMessages(SendMessagesHelper.5.1.this.val$oldId);
                        }
                      });
                    }
                  });
                  k = i;
                }
              }
            }
            if (!localUpdates.updates.isEmpty()) {
              MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates(localUpdates, false);
            }
            StatsController.getInstance(SendMessagesHelper.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 1, j);
          }
          for (;;)
          {
            i = 0;
            while (i < localObject2.size())
            {
              paramAnonymousTLObject = (TLRPC.Message)localObject2.get(i);
              MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(paramAnonymousTLObject);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  paramAnonymousTLObject.send_state = 2;
                  NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, new Object[] { Integer.valueOf(paramAnonymousTLObject.id) });
                  SendMessagesHelper.this.processSentMessage(paramAnonymousTLObject.id);
                  SendMessagesHelper.this.removeFromSendingMessages(paramAnonymousTLObject.id);
                }
              });
              i += 1;
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                AlertsCreator.processError(SendMessagesHelper.this.currentAccount, paramAnonymousTL_error, null, SendMessagesHelper.5.this.val$req, new Object[0]);
              }
            });
          }
        }
      }, 68);
      localObject6 = localObject5;
      localObject7 = localObject4;
      localObject8 = localObject3;
      localObject9 = localObject2;
      localObject10 = localObject1;
      k = j;
      if (i1 == paramArrayList.size() - 1) {
        break;
      }
      localObject9 = new ArrayList();
      localObject6 = new ArrayList();
      localObject10 = new ArrayList();
      localObject7 = new ArrayList();
      localObject8 = new LongSparseArray();
      k = j;
      break;
      k = 0;
      break label719;
      if (k != 0) {
        break label859;
      }
      ((TLRPC.Message)localObject8).fwd_from = new TLRPC.TL_messageFwdHeader();
      ((TLRPC.Message)localObject8).fwd_from.channel_post = localMessageObject.getId();
      localObject6 = ((TLRPC.Message)localObject8).fwd_from;
      ((TLRPC.MessageFwdHeader)localObject6).flags |= 0x4;
      if (localMessageObject.isFromUser())
      {
        ((TLRPC.Message)localObject8).fwd_from.from_id = localMessageObject.messageOwner.from_id;
        localObject6 = ((TLRPC.Message)localObject8).fwd_from;
        ((TLRPC.MessageFwdHeader)localObject6).flags |= 0x1;
        if (localMessageObject.messageOwner.post_author == null) {
          break label2429;
        }
        ((TLRPC.Message)localObject8).fwd_from.post_author = localMessageObject.messageOwner.post_author;
        localObject6 = ((TLRPC.Message)localObject8).fwd_from;
        ((TLRPC.MessageFwdHeader)localObject6).flags |= 0x8;
      }
      for (;;)
      {
        ((TLRPC.Message)localObject8).date = localMessageObject.messageOwner.date;
        ((TLRPC.Message)localObject8).flags = 4;
        break;
        ((TLRPC.Message)localObject8).fwd_from.channel_id = localMessageObject.messageOwner.to_id.channel_id;
        localObject6 = ((TLRPC.Message)localObject8).fwd_from;
        ((TLRPC.MessageFwdHeader)localObject6).flags |= 0x2;
        if ((!localMessageObject.messageOwner.post) || (localMessageObject.messageOwner.from_id <= 0)) {
          break label2262;
        }
        ((TLRPC.Message)localObject8).fwd_from.from_id = localMessageObject.messageOwner.from_id;
        localObject6 = ((TLRPC.Message)localObject8).fwd_from;
        ((TLRPC.MessageFwdHeader)localObject6).flags |= 0x1;
        break label2262;
        if ((!localMessageObject.isOutOwner()) && (localMessageObject.messageOwner.from_id > 0) && (localMessageObject.messageOwner.post))
        {
          localObject6 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(localMessageObject.messageOwner.from_id));
          if (localObject6 != null)
          {
            ((TLRPC.Message)localObject8).fwd_from.post_author = ContactsController.formatName(((TLRPC.User)localObject6).first_name, ((TLRPC.User)localObject6).last_name);
            localObject6 = ((TLRPC.Message)localObject8).fwd_from;
            ((TLRPC.MessageFwdHeader)localObject6).flags |= 0x8;
          }
        }
      }
      ((TLRPC.Message)localObject8).media = localMessageObject.messageOwner.media;
      break label955;
      i2 = -localPeer.channel_id;
      break label1331;
      ((TLRPC.Message)localObject8).from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
      ((TLRPC.Message)localObject8).flags |= 0x100;
      break label1344;
      ((TLRPC.Message)localObject8).unread = true;
      break label1451;
      if ((localMessageObject.messageOwner.flags & 0x400) != 0)
      {
        ((TLRPC.Message)localObject8).views = localMessageObject.messageOwner.views;
        ((TLRPC.Message)localObject8).flags |= 0x400;
      }
      ((TLRPC.Message)localObject8).unread = true;
      break label1451;
      bool3 = false;
      break label1845;
      ((TLRPC.TL_messages_forwardMessages)localObject6).from_peer = new TLRPC.TL_inputPeerEmpty();
      break label1990;
    }
    label2677:
    int i = 0;
    for (;;)
    {
      k = j;
      if (i >= paramArrayList.size()) {
        break;
      }
      processForwardFromMyName((MessageObject)paramArrayList.get(i), paramLong);
      i += 1;
    }
  }
  
  public void sendMessage(String paramString, long paramLong, MessageObject paramMessageObject, TLRPC.WebPage paramWebPage, boolean paramBoolean, ArrayList<TLRPC.MessageEntity> paramArrayList, TLRPC.ReplyMarkup paramReplyMarkup, HashMap<String, String> paramHashMap)
  {
    sendMessage(paramString, null, null, null, null, null, null, null, paramLong, null, paramMessageObject, paramWebPage, paramBoolean, null, paramArrayList, paramReplyMarkup, paramHashMap, 0);
  }
  
  public void sendMessage(MessageObject paramMessageObject)
  {
    sendMessage(null, null, null, null, null, null, null, null, paramMessageObject.getDialogId(), paramMessageObject.messageOwner.attachPath, null, null, true, paramMessageObject, null, paramMessageObject.messageOwner.reply_markup, paramMessageObject.messageOwner.params, 0);
  }
  
  public void sendMessage(TLRPC.MessageMedia paramMessageMedia, long paramLong, MessageObject paramMessageObject, TLRPC.ReplyMarkup paramReplyMarkup, HashMap<String, String> paramHashMap)
  {
    sendMessage(null, null, paramMessageMedia, null, null, null, null, null, paramLong, null, paramMessageObject, null, true, null, null, paramReplyMarkup, paramHashMap, 0);
  }
  
  public void sendMessage(TLRPC.TL_document paramTL_document, VideoEditedInfo paramVideoEditedInfo, String paramString1, long paramLong, MessageObject paramMessageObject, String paramString2, ArrayList<TLRPC.MessageEntity> paramArrayList, TLRPC.ReplyMarkup paramReplyMarkup, HashMap<String, String> paramHashMap, int paramInt)
  {
    sendMessage(null, paramString2, null, null, paramVideoEditedInfo, null, paramTL_document, null, paramLong, paramString1, paramMessageObject, null, true, null, paramArrayList, paramReplyMarkup, paramHashMap, paramInt);
  }
  
  public void sendMessage(TLRPC.TL_game paramTL_game, long paramLong, TLRPC.ReplyMarkup paramReplyMarkup, HashMap<String, String> paramHashMap)
  {
    sendMessage(null, null, null, null, null, null, null, paramTL_game, paramLong, null, null, null, true, null, null, paramReplyMarkup, paramHashMap, 0);
  }
  
  public void sendMessage(TLRPC.TL_photo paramTL_photo, String paramString1, long paramLong, MessageObject paramMessageObject, String paramString2, ArrayList<TLRPC.MessageEntity> paramArrayList, TLRPC.ReplyMarkup paramReplyMarkup, HashMap<String, String> paramHashMap, int paramInt)
  {
    sendMessage(null, paramString2, null, paramTL_photo, null, null, null, null, paramLong, paramString1, paramMessageObject, null, true, null, paramArrayList, paramReplyMarkup, paramHashMap, paramInt);
  }
  
  public void sendMessage(TLRPC.User paramUser, long paramLong, MessageObject paramMessageObject, TLRPC.ReplyMarkup paramReplyMarkup, HashMap<String, String> paramHashMap)
  {
    sendMessage(null, null, null, null, null, paramUser, null, null, paramLong, null, paramMessageObject, null, true, null, null, paramReplyMarkup, paramHashMap, 0);
  }
  
  public void sendNotificationCallback(final long paramLong, int paramInt, final byte[] paramArrayOfByte)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        int i = (int)paramLong;
        final String str = paramLong + "_" + paramArrayOfByte + "_" + Utilities.bytesToHex(this.val$data) + "_" + 0;
        SendMessagesHelper.this.waitingForCallback.put(str, Boolean.valueOf(true));
        Object localObject;
        if (i > 0) {
          if (MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getUser(Integer.valueOf(i)) == null)
          {
            localObject = MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getUserSync(i);
            if (localObject != null) {
              MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putUser((TLRPC.User)localObject, true);
            }
          }
        }
        for (;;)
        {
          localObject = new TLRPC.TL_messages_getBotCallbackAnswer();
          ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).peer = MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getInputPeer(i);
          ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).msg_id = paramArrayOfByte;
          ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).game = false;
          if (this.val$data != null)
          {
            ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).flags |= 0x1;
            ((TLRPC.TL_messages_getBotCallbackAnswer)localObject).data = this.val$data;
          }
          ConnectionsManager.getInstance(SendMessagesHelper.this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
          {
            public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  SendMessagesHelper.this.waitingForCallback.remove(SendMessagesHelper.7.1.this.val$key);
                }
              });
            }
          }, 2);
          MessagesController.getInstance(SendMessagesHelper.this.currentAccount).markDialogAsRead(paramLong, paramArrayOfByte, paramArrayOfByte, 0, false, 0, true);
          return;
          if (MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getChat(Integer.valueOf(-i)) == null)
          {
            localObject = MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getChatSync(-i);
            if (localObject != null) {
              MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putChat((TLRPC.Chat)localObject, true);
            }
          }
        }
      }
    });
  }
  
  public void sendScreenshotMessage(TLRPC.User paramUser, int paramInt, TLRPC.Message paramMessage)
  {
    if ((paramUser == null) || (paramInt == 0) || (paramUser.id == UserConfig.getInstance(this.currentAccount).getClientUserId())) {
      return;
    }
    TLRPC.TL_messages_sendScreenshotNotification localTL_messages_sendScreenshotNotification = new TLRPC.TL_messages_sendScreenshotNotification();
    localTL_messages_sendScreenshotNotification.peer = new TLRPC.TL_inputPeerUser();
    localTL_messages_sendScreenshotNotification.peer.access_hash = paramUser.access_hash;
    localTL_messages_sendScreenshotNotification.peer.user_id = paramUser.id;
    if (paramMessage != null)
    {
      paramUser = paramMessage;
      localTL_messages_sendScreenshotNotification.reply_to_msg_id = paramInt;
      localTL_messages_sendScreenshotNotification.random_id = paramMessage.random_id;
    }
    for (;;)
    {
      localTL_messages_sendScreenshotNotification.random_id = paramUser.random_id;
      paramMessage = new MessageObject(this.currentAccount, paramUser, false);
      paramMessage.messageOwner.send_state = 1;
      ArrayList localArrayList = new ArrayList();
      localArrayList.add(paramMessage);
      MessagesController.getInstance(this.currentAccount).updateInterfaceWithMessages(paramUser.dialog_id, localArrayList);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
      localArrayList = new ArrayList();
      localArrayList.add(paramUser);
      MessagesStorage.getInstance(this.currentAccount).putMessages(localArrayList, false, true, false, 0);
      performSendMessageRequest(localTL_messages_sendScreenshotNotification, paramMessage, null);
      return;
      paramMessage = new TLRPC.TL_messageService();
      paramMessage.random_id = getNextRandomId();
      paramMessage.dialog_id = paramUser.id;
      paramMessage.unread = true;
      paramMessage.out = true;
      int i = UserConfig.getInstance(this.currentAccount).getNewMessageId();
      paramMessage.id = i;
      paramMessage.local_id = i;
      paramMessage.from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
      paramMessage.flags |= 0x100;
      paramMessage.flags |= 0x8;
      paramMessage.reply_to_msg_id = paramInt;
      paramMessage.to_id = new TLRPC.TL_peerUser();
      paramMessage.to_id.user_id = paramUser.id;
      paramMessage.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
      paramMessage.action = new TLRPC.TL_messageActionScreenshotTaken();
      UserConfig.getInstance(this.currentAccount).saveConfig(false);
      paramUser = paramMessage;
    }
  }
  
  public void sendSticker(TLRPC.Document paramDocument, long paramLong, MessageObject paramMessageObject)
  {
    if (paramDocument == null) {}
    for (;;)
    {
      return;
      Object localObject = paramDocument;
      int i;
      File localFile;
      if ((int)paramLong == 0)
      {
        i = (int)(paramLong >> 32);
        if (MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(i)) == null) {
          continue;
        }
        localObject = new TLRPC.TL_document();
        ((TLRPC.TL_document)localObject).id = paramDocument.id;
        ((TLRPC.TL_document)localObject).access_hash = paramDocument.access_hash;
        ((TLRPC.TL_document)localObject).date = paramDocument.date;
        ((TLRPC.TL_document)localObject).mime_type = paramDocument.mime_type;
        ((TLRPC.TL_document)localObject).size = paramDocument.size;
        ((TLRPC.TL_document)localObject).dc_id = paramDocument.dc_id;
        ((TLRPC.TL_document)localObject).attributes = new ArrayList(paramDocument.attributes);
        if (((TLRPC.TL_document)localObject).mime_type == null) {
          ((TLRPC.TL_document)localObject).mime_type = "";
        }
        if ((paramDocument.thumb instanceof TLRPC.TL_photoSize))
        {
          localFile = FileLoader.getPathToAttach(paramDocument.thumb, true);
          if (!localFile.exists()) {}
        }
      }
      try
      {
        i = (int)localFile.length();
        byte[] arrayOfByte = new byte[(int)localFile.length()];
        new RandomAccessFile(localFile, "r").readFully(arrayOfByte);
        ((TLRPC.TL_document)localObject).thumb = new TLRPC.TL_photoCachedSize();
        ((TLRPC.TL_document)localObject).thumb.location = paramDocument.thumb.location;
        ((TLRPC.TL_document)localObject).thumb.size = paramDocument.thumb.size;
        ((TLRPC.TL_document)localObject).thumb.w = paramDocument.thumb.w;
        ((TLRPC.TL_document)localObject).thumb.h = paramDocument.thumb.h;
        ((TLRPC.TL_document)localObject).thumb.type = paramDocument.thumb.type;
        ((TLRPC.TL_document)localObject).thumb.bytes = arrayOfByte;
        if (((TLRPC.TL_document)localObject).thumb == null)
        {
          ((TLRPC.TL_document)localObject).thumb = new TLRPC.TL_photoSizeEmpty();
          ((TLRPC.TL_document)localObject).thumb.type = "s";
        }
        if (!(localObject instanceof TLRPC.TL_document)) {
          continue;
        }
        sendMessage((TLRPC.TL_document)localObject, null, null, paramLong, paramMessageObject, null, null, null, null, 0);
        return;
      }
      catch (Exception paramDocument)
      {
        for (;;)
        {
          FileLog.e(paramDocument);
        }
      }
    }
  }
  
  public void setCurrentChatInfo(TLRPC.ChatFull paramChatFull)
  {
    this.currentChatInfo = paramChatFull;
  }
  
  protected void stopVideoService(final String paramString)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.stopEncodingService, new Object[] { SendMessagesHelper.11.this.val$path, Integer.valueOf(SendMessagesHelper.this.currentAccount) });
          }
        });
      }
    });
  }
  
  protected class DelayedMessage
  {
    public TLRPC.EncryptedChat encryptedChat;
    public HashMap<Object, Object> extraHashMap;
    public int finalGroupMessage;
    public long groupId;
    public String httpLocation;
    public TLRPC.FileLocation location;
    public ArrayList<MessageObject> messageObjects;
    public ArrayList<TLRPC.Message> messages;
    public MessageObject obj;
    public String originalPath;
    public ArrayList<String> originalPaths;
    public long peer;
    ArrayList<SendMessagesHelper.DelayedMessageSendAfterRequest> requests;
    public TLObject sendEncryptedRequest;
    public TLObject sendRequest;
    public int type;
    public boolean upload;
    public VideoEditedInfo videoEditedInfo;
    
    public DelayedMessage(long paramLong)
    {
      this.peer = paramLong;
    }
    
    public void addDelayedRequest(TLObject paramTLObject, ArrayList<MessageObject> paramArrayList, ArrayList<String> paramArrayList1)
    {
      SendMessagesHelper.DelayedMessageSendAfterRequest localDelayedMessageSendAfterRequest = new SendMessagesHelper.DelayedMessageSendAfterRequest(SendMessagesHelper.this);
      localDelayedMessageSendAfterRequest.request = paramTLObject;
      localDelayedMessageSendAfterRequest.msgObjs = paramArrayList;
      localDelayedMessageSendAfterRequest.originalPaths = paramArrayList1;
      if (this.requests == null) {
        this.requests = new ArrayList();
      }
      this.requests.add(localDelayedMessageSendAfterRequest);
    }
    
    public void addDelayedRequest(TLObject paramTLObject, MessageObject paramMessageObject, String paramString)
    {
      SendMessagesHelper.DelayedMessageSendAfterRequest localDelayedMessageSendAfterRequest = new SendMessagesHelper.DelayedMessageSendAfterRequest(SendMessagesHelper.this);
      localDelayedMessageSendAfterRequest.request = paramTLObject;
      localDelayedMessageSendAfterRequest.msgObj = paramMessageObject;
      localDelayedMessageSendAfterRequest.originalPath = paramString;
      if (this.requests == null) {
        this.requests = new ArrayList();
      }
      this.requests.add(localDelayedMessageSendAfterRequest);
    }
    
    public void markAsError()
    {
      if (this.type == 4)
      {
        int i = 0;
        while (i < this.messageObjects.size())
        {
          MessageObject localMessageObject = (MessageObject)this.messageObjects.get(i);
          MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(localMessageObject.messageOwner);
          localMessageObject.messageOwner.send_state = 2;
          NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, new Object[] { Integer.valueOf(localMessageObject.getId()) });
          SendMessagesHelper.this.processSentMessage(localMessageObject.getId());
          i += 1;
        }
        SendMessagesHelper.this.delayedMessages.remove("group_" + this.groupId);
      }
      for (;;)
      {
        sendDelayedRequests();
        return;
        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(this.obj.messageOwner);
        this.obj.messageOwner.send_state = 2;
        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, new Object[] { Integer.valueOf(this.obj.getId()) });
        SendMessagesHelper.this.processSentMessage(this.obj.getId());
      }
    }
    
    public void sendDelayedRequests()
    {
      if ((this.requests == null) || ((this.type != 4) && (this.type != 0))) {
        return;
      }
      int j = this.requests.size();
      int i = 0;
      if (i < j)
      {
        SendMessagesHelper.DelayedMessageSendAfterRequest localDelayedMessageSendAfterRequest = (SendMessagesHelper.DelayedMessageSendAfterRequest)this.requests.get(i);
        if ((localDelayedMessageSendAfterRequest.request instanceof TLRPC.TL_messages_sendEncryptedMultiMedia)) {
          SecretChatHelper.getInstance(SendMessagesHelper.this.currentAccount).performSendEncryptedRequest((TLRPC.TL_messages_sendEncryptedMultiMedia)localDelayedMessageSendAfterRequest.request, this);
        }
        for (;;)
        {
          i += 1;
          break;
          if ((localDelayedMessageSendAfterRequest.request instanceof TLRPC.TL_messages_sendMultiMedia)) {
            SendMessagesHelper.this.performSendMessageRequestMulti((TLRPC.TL_messages_sendMultiMedia)localDelayedMessageSendAfterRequest.request, localDelayedMessageSendAfterRequest.msgObjs, localDelayedMessageSendAfterRequest.originalPaths);
          } else {
            SendMessagesHelper.this.performSendMessageRequest(localDelayedMessageSendAfterRequest.request, localDelayedMessageSendAfterRequest.msgObj, localDelayedMessageSendAfterRequest.originalPath);
          }
        }
      }
      this.requests = null;
    }
  }
  
  protected class DelayedMessageSendAfterRequest
  {
    public MessageObject msgObj;
    public ArrayList<MessageObject> msgObjs;
    public String originalPath;
    public ArrayList<String> originalPaths;
    public TLObject request;
    
    protected DelayedMessageSendAfterRequest() {}
  }
  
  public static class LocationProvider
  {
    private LocationProviderDelegate delegate;
    private GpsLocationListener gpsLocationListener = new GpsLocationListener(null);
    private Location lastKnownLocation;
    private LocationManager locationManager;
    private Runnable locationQueryCancelRunnable;
    private GpsLocationListener networkLocationListener = new GpsLocationListener(null);
    
    public LocationProvider() {}
    
    public LocationProvider(LocationProviderDelegate paramLocationProviderDelegate)
    {
      this.delegate = paramLocationProviderDelegate;
    }
    
    private void cleanup()
    {
      this.locationManager.removeUpdates(this.gpsLocationListener);
      this.locationManager.removeUpdates(this.networkLocationListener);
      this.lastKnownLocation = null;
      this.locationQueryCancelRunnable = null;
    }
    
    public void setDelegate(LocationProviderDelegate paramLocationProviderDelegate)
    {
      this.delegate = paramLocationProviderDelegate;
    }
    
    public void start()
    {
      if (this.locationManager == null) {
        this.locationManager = ((LocationManager)ApplicationLoader.applicationContext.getSystemService("location"));
      }
      try
      {
        this.locationManager.requestLocationUpdates("gps", 1L, 0.0F, this.gpsLocationListener);
      }
      catch (Exception localException2)
      {
        try
        {
          this.locationManager.requestLocationUpdates("network", 1L, 0.0F, this.networkLocationListener);
        }
        catch (Exception localException2)
        {
          try
          {
            for (;;)
            {
              this.lastKnownLocation = this.locationManager.getLastKnownLocation("gps");
              if (this.lastKnownLocation == null) {
                this.lastKnownLocation = this.locationManager.getLastKnownLocation("network");
              }
              if (this.locationQueryCancelRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(this.locationQueryCancelRunnable);
              }
              this.locationQueryCancelRunnable = new Runnable()
              {
                public void run()
                {
                  if (SendMessagesHelper.LocationProvider.this.locationQueryCancelRunnable != this) {
                    return;
                  }
                  if (SendMessagesHelper.LocationProvider.this.delegate != null)
                  {
                    if (SendMessagesHelper.LocationProvider.this.lastKnownLocation == null) {
                      break label59;
                    }
                    SendMessagesHelper.LocationProvider.this.delegate.onLocationAcquired(SendMessagesHelper.LocationProvider.this.lastKnownLocation);
                  }
                  for (;;)
                  {
                    SendMessagesHelper.LocationProvider.this.cleanup();
                    return;
                    label59:
                    SendMessagesHelper.LocationProvider.this.delegate.onUnableLocationAcquire();
                  }
                }
              };
              AndroidUtilities.runOnUIThread(this.locationQueryCancelRunnable, 5000L);
              return;
              localException1 = localException1;
              FileLog.e(localException1);
              continue;
              localException2 = localException2;
              FileLog.e(localException2);
            }
          }
          catch (Exception localException3)
          {
            for (;;)
            {
              FileLog.e(localException3);
            }
          }
        }
      }
    }
    
    public void stop()
    {
      if (this.locationManager == null) {
        return;
      }
      if (this.locationQueryCancelRunnable != null) {
        AndroidUtilities.cancelRunOnUIThread(this.locationQueryCancelRunnable);
      }
      cleanup();
    }
    
    private class GpsLocationListener
      implements LocationListener
    {
      private GpsLocationListener() {}
      
      public void onLocationChanged(Location paramLocation)
      {
        if ((paramLocation == null) || (SendMessagesHelper.LocationProvider.this.locationQueryCancelRunnable == null)) {}
        do
        {
          return;
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("found location " + paramLocation);
          }
          SendMessagesHelper.LocationProvider.access$502(SendMessagesHelper.LocationProvider.this, paramLocation);
        } while (paramLocation.getAccuracy() >= 100.0F);
        if (SendMessagesHelper.LocationProvider.this.delegate != null) {
          SendMessagesHelper.LocationProvider.this.delegate.onLocationAcquired(paramLocation);
        }
        if (SendMessagesHelper.LocationProvider.this.locationQueryCancelRunnable != null) {
          AndroidUtilities.cancelRunOnUIThread(SendMessagesHelper.LocationProvider.this.locationQueryCancelRunnable);
        }
        SendMessagesHelper.LocationProvider.this.cleanup();
      }
      
      public void onProviderDisabled(String paramString) {}
      
      public void onProviderEnabled(String paramString) {}
      
      public void onStatusChanged(String paramString, int paramInt, Bundle paramBundle) {}
    }
    
    public static abstract interface LocationProviderDelegate
    {
      public abstract void onLocationAcquired(Location paramLocation);
      
      public abstract void onUnableLocationAcquire();
    }
  }
  
  private static class MediaSendPrepareWorker
  {
    public volatile TLRPC.TL_photo photo;
    public CountDownLatch sync;
  }
  
  public static class SendingMediaInfo
  {
    public String caption;
    public ArrayList<TLRPC.MessageEntity> entities;
    public boolean isVideo;
    public ArrayList<TLRPC.InputDocument> masks;
    public String path;
    public MediaController.SearchImage searchImage;
    public int ttl;
    public Uri uri;
    public VideoEditedInfo videoEditedInfo;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/SendMessagesHelper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */