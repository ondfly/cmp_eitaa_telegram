package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video.Media;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.messenger.exoplayer2.ui.AspectRatioFrameLayout;
import org.telegram.messenger.video.MP4Builder;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.InputDocument;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAnimated;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_messages_messages;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.PhotoFilterView.CurvesToolValue;
import org.telegram.ui.Components.PipRoundVideoView;
import org.telegram.ui.Components.VideoPlayer;
import org.telegram.ui.Components.VideoPlayer.VideoPlayerDelegate;
import org.telegram.ui.PhotoViewer;

public class MediaController
  implements SensorEventListener, AudioManager.OnAudioFocusChangeListener, NotificationCenter.NotificationCenterDelegate
{
  private static final int AUDIO_FOCUSED = 2;
  private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
  private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
  private static volatile MediaController Instance;
  public static final String MIME_TYPE = "video/avc";
  private static final int PROCESSOR_TYPE_INTEL = 2;
  private static final int PROCESSOR_TYPE_MTK = 3;
  private static final int PROCESSOR_TYPE_OTHER = 0;
  private static final int PROCESSOR_TYPE_QCOM = 1;
  private static final int PROCESSOR_TYPE_SEC = 4;
  private static final int PROCESSOR_TYPE_TI = 5;
  private static final float VOLUME_DUCK = 0.2F;
  private static final float VOLUME_NORMAL = 1.0F;
  public static AlbumEntry allMediaAlbumEntry;
  public static AlbumEntry allPhotosAlbumEntry;
  private static Runnable broadcastPhotosRunnable;
  private static final String[] projectionPhotos = { "_id", "bucket_id", "bucket_display_name", "_data", "datetaken", "orientation" };
  private static final String[] projectionVideo = { "_id", "bucket_id", "bucket_display_name", "_data", "datetaken", "duration" };
  public static int[] readArgs = new int[3];
  private static Runnable refreshGalleryRunnable;
  private Sensor accelerometerSensor;
  private boolean accelerometerVertical;
  private boolean allowStartRecord;
  private int audioFocus = 0;
  private AudioInfo audioInfo;
  private VideoPlayer audioPlayer = null;
  private AudioRecord audioRecorder;
  private AudioTrack audioTrackPlayer = null;
  private Activity baseActivity;
  private int buffersWrited;
  private boolean callInProgress;
  private boolean cancelCurrentVideoConversion = false;
  private int countLess;
  private AspectRatioFrameLayout currentAspectRatioFrameLayout;
  private float currentAspectRatioFrameLayoutRatio;
  private boolean currentAspectRatioFrameLayoutReady;
  private int currentAspectRatioFrameLayoutRotation;
  private int currentPlaylistNum;
  private TextureView currentTextureView;
  private FrameLayout currentTextureViewContainer;
  private long currentTotalPcmDuration;
  private boolean decodingFinished = false;
  private boolean downloadingCurrentMessage;
  private ExternalObserver externalObserver;
  private View feedbackView;
  private ByteBuffer fileBuffer;
  private DispatchQueue fileDecodingQueue;
  private DispatchQueue fileEncodingQueue;
  private BaseFragment flagSecureFragment;
  private boolean forceLoopCurrentPlaylist;
  private ArrayList<AudioBuffer> freePlayerBuffers = new ArrayList();
  private HashMap<String, MessageObject> generatingWaveform = new HashMap();
  private float[] gravity = new float[3];
  private float[] gravityFast = new float[3];
  private Sensor gravitySensor;
  private int hasAudioFocus;
  private int ignoreFirstProgress = 0;
  private boolean ignoreOnPause;
  private boolean ignoreProximity;
  private boolean inputFieldHasText;
  private InternalObserver internalObserver;
  private boolean isDrawingWasReady;
  private boolean isPaused = false;
  private int lastChatAccount;
  private long lastChatEnterTime;
  private long lastChatLeaveTime;
  private ArrayList<Long> lastChatVisibleMessages;
  private long lastMediaCheckTime;
  private int lastMessageId;
  private long lastPlayPcm;
  private long lastProgress = 0L;
  private float lastProximityValue = -100.0F;
  private TLRPC.EncryptedChat lastSecretChat;
  private long lastTimestamp = 0L;
  private TLRPC.User lastUser;
  private float[] linearAcceleration = new float[3];
  private Sensor linearSensor;
  private String[] mediaProjections = null;
  private PipRoundVideoView pipRoundVideoView;
  private int pipSwitchingState;
  private boolean playMusicAgain;
  private int playerBufferSize = 3840;
  private final Object playerObjectSync = new Object();
  private DispatchQueue playerQueue;
  private final Object playerSync = new Object();
  private MessageObject playingMessageObject;
  private ArrayList<MessageObject> playlist = new ArrayList();
  private float previousAccValue;
  private Timer progressTimer = null;
  private final Object progressTimerSync = new Object();
  private boolean proximityHasDifferentValues;
  private Sensor proximitySensor;
  private boolean proximityTouched;
  private PowerManager.WakeLock proximityWakeLock;
  private ChatActivity raiseChat;
  private boolean raiseToEarRecord;
  private int raisedToBack;
  private int raisedToTop;
  private int raisedToTopSign;
  private int recordBufferSize = 1280;
  private ArrayList<ByteBuffer> recordBuffers = new ArrayList();
  private long recordDialogId;
  private DispatchQueue recordQueue = new DispatchQueue("recordQueue");
  private MessageObject recordReplyingMessageObject;
  private Runnable recordRunnable = new Runnable()
  {
    public void run()
    {
      final ByteBuffer localByteBuffer;
      int n;
      double d2;
      final double d1;
      if (MediaController.this.audioRecorder != null) {
        if (!MediaController.this.recordBuffers.isEmpty())
        {
          localByteBuffer = (ByteBuffer)MediaController.this.recordBuffers.get(0);
          MediaController.this.recordBuffers.remove(0);
          localByteBuffer.rewind();
          n = MediaController.this.audioRecorder.read(localByteBuffer, localByteBuffer.capacity());
          if (n <= 0) {
            break label508;
          }
          localByteBuffer.limit(n);
          d2 = 0.0D;
          d1 = d2;
        }
      }
      for (;;)
      {
        int k;
        int m;
        float f2;
        double d3;
        try
        {
          long l = MediaController.this.samplesCount + n / 2;
          d1 = d2;
          k = (int)(MediaController.this.samplesCount / l * MediaController.this.recordSamples.length);
          d1 = d2;
          m = MediaController.this.recordSamples.length;
          if (k != 0)
          {
            d1 = d2;
            f2 = MediaController.this.recordSamples.length / k;
            f1 = 0.0F;
            j = 0;
            if (j < k)
            {
              d1 = d2;
              MediaController.this.recordSamples[j] = MediaController.this.recordSamples[((int)f1)];
              f1 += f2;
              j += 1;
              continue;
              localByteBuffer = ByteBuffer.allocateDirect(MediaController.this.recordBufferSize);
              localByteBuffer.order(ByteOrder.nativeOrder());
              break;
            }
          }
          j = k;
          f1 = 0.0F;
          float f3 = n / 2.0F / (m - k);
          k = 0;
          d1 = d2;
          if (k < n / 2)
          {
            d1 = d2;
            int i = localByteBuffer.getShort();
            d3 = d2;
            if (i > 2500) {
              d3 = d2 + i * i;
            }
            m = j;
            f2 = f1;
            if (k != (int)f1) {
              break label536;
            }
            d1 = d3;
            m = j;
            f2 = f1;
            if (j >= MediaController.this.recordSamples.length) {
              break label536;
            }
            d1 = d3;
            MediaController.this.recordSamples[j] = i;
            f2 = f1 + f3;
            m = j + 1;
            break label536;
          }
          d1 = d2;
          MediaController.access$302(MediaController.this, l);
          d1 = d2;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          continue;
          final boolean bool = false;
          continue;
        }
        localByteBuffer.position(0);
        d1 = Math.sqrt(d1 / n / 2.0D);
        if (n != localByteBuffer.capacity())
        {
          bool = true;
          if (n != 0) {
            MediaController.this.fileEncodingQueue.postRunnable(new Runnable()
            {
              public void run()
              {
                if (localByteBuffer.hasRemaining())
                {
                  int i = -1;
                  if (localByteBuffer.remaining() > MediaController.this.fileBuffer.remaining())
                  {
                    i = localByteBuffer.limit();
                    localByteBuffer.limit(MediaController.this.fileBuffer.remaining() + localByteBuffer.position());
                  }
                  MediaController.this.fileBuffer.put(localByteBuffer);
                  MediaController localMediaController;
                  ByteBuffer localByteBuffer;
                  if ((MediaController.this.fileBuffer.position() == MediaController.this.fileBuffer.limit()) || (bool))
                  {
                    localMediaController = MediaController.this;
                    localByteBuffer = MediaController.this.fileBuffer;
                    if (bool) {
                      break label249;
                    }
                  }
                  label249:
                  for (int j = MediaController.this.fileBuffer.limit();; j = localByteBuffer.position())
                  {
                    if (localMediaController.writeFrame(localByteBuffer, j) != 0)
                    {
                      MediaController.this.fileBuffer.rewind();
                      MediaController.access$702(MediaController.this, MediaController.this.recordTimeCount + MediaController.this.fileBuffer.limit() / 2 / 16);
                    }
                    if (i == -1) {
                      break;
                    }
                    localByteBuffer.limit(i);
                    break;
                  }
                }
                MediaController.this.recordQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    MediaController.this.recordBuffers.add(MediaController.1.1.this.val$finalBuffer);
                  }
                });
              }
            });
          }
          MediaController.this.recordQueue.postRunnable(MediaController.this.recordRunnable);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationCenter.getInstance(MediaController.this.recordingCurrentAccount).postNotificationName(NotificationCenter.recordProgressChanged, new Object[] { Long.valueOf(System.currentTimeMillis() - MediaController.this.recordStartTime), Double.valueOf(d1) });
            }
          });
          return;
        }
        label508:
        MediaController.this.recordBuffers.add(localByteBuffer);
        MediaController.this.stopRecordingInternal(MediaController.this.sendAfterDone);
        return;
        label536:
        k += 1;
        int j = m;
        float f1 = f2;
        d2 = d3;
      }
    }
  };
  private short[] recordSamples = new short['Ѐ'];
  private Runnable recordStartRunnable;
  private long recordStartTime;
  private long recordTimeCount;
  private TLRPC.TL_document recordingAudio;
  private File recordingAudioFile;
  private int recordingCurrentAccount;
  private boolean resumeAudioOnFocusGain;
  private long samplesCount;
  private float seekToProgressPending;
  private int sendAfterDone;
  private SensorManager sensorManager;
  private boolean sensorsStarted;
  private ArrayList<MessageObject> shuffledPlaylist = new ArrayList();
  private SmsObserver smsObserver;
  private int startObserverToken;
  private StopMediaObserverRunnable stopMediaObserverRunnable;
  private final Object sync = new Object();
  private long timeSinceRaise;
  private boolean useFrontSpeaker;
  private ArrayList<AudioBuffer> usedPlayerBuffers = new ArrayList();
  private boolean videoConvertFirstWrite = true;
  private ArrayList<MessageObject> videoConvertQueue = new ArrayList();
  private final Object videoConvertSync = new Object();
  private VideoPlayer videoPlayer;
  private final Object videoQueueSync = new Object();
  private ArrayList<MessageObject> voiceMessagesPlaylist;
  private SparseArray<MessageObject> voiceMessagesPlaylistMap;
  private boolean voiceMessagesPlaylistUnread;
  
  public MediaController()
  {
    this.recordQueue.setPriority(10);
    this.fileEncodingQueue = new DispatchQueue("fileEncodingQueue");
    this.fileEncodingQueue.setPriority(10);
    this.playerQueue = new DispatchQueue("playerQueue");
    this.fileDecodingQueue = new DispatchQueue("fileDecodingQueue");
    this.recordQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i;
        for (;;)
        {
          try
          {
            MediaController.access$202(MediaController.this, AudioRecord.getMinBufferSize(16000, 16, 2));
            if (MediaController.this.recordBufferSize <= 0) {
              MediaController.access$202(MediaController.this, 1280);
            }
            MediaController.access$2202(MediaController.this, AudioTrack.getMinBufferSize(48000, 4, 2));
            if (MediaController.this.playerBufferSize > 0) {
              break;
            }
            MediaController.access$2202(MediaController.this, 3840);
          }
          catch (Exception localException)
          {
            ByteBuffer localByteBuffer;
            FileLog.e(localException);
          }
          if (i >= 5) {
            break label168;
          }
          localByteBuffer = ByteBuffer.allocateDirect(4096);
          localByteBuffer.order(ByteOrder.nativeOrder());
          MediaController.this.recordBuffers.add(localByteBuffer);
          i += 1;
        }
        for (;;)
        {
          if (i < 3)
          {
            MediaController.this.freePlayerBuffers.add(new MediaController.AudioBuffer(MediaController.this, MediaController.this.playerBufferSize));
            i += 1;
          }
          else
          {
            return;
            i = 0;
            break;
            label168:
            i = 0;
          }
        }
      }
    });
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MediaController.access$2402(MediaController.this, (SensorManager)ApplicationLoader.applicationContext.getSystemService("sensor"));
          MediaController.access$2502(MediaController.this, MediaController.this.sensorManager.getDefaultSensor(10));
          MediaController.access$2602(MediaController.this, MediaController.this.sensorManager.getDefaultSensor(9));
          if ((MediaController.this.linearSensor == null) || (MediaController.this.gravitySensor == null))
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("gravity or linear sensor not found");
            }
            MediaController.access$2702(MediaController.this, MediaController.this.sensorManager.getDefaultSensor(1));
            MediaController.access$2502(MediaController.this, null);
            MediaController.access$2602(MediaController.this, null);
          }
          MediaController.access$2802(MediaController.this, MediaController.this.sensorManager.getDefaultSensor(8));
          Object localObject = (PowerManager)ApplicationLoader.applicationContext.getSystemService("power");
          MediaController.access$2902(MediaController.this, ((PowerManager)localObject).newWakeLock(32, "proximity"));
          TelephonyManager localTelephonyManager;
          return;
        }
        catch (Exception localException1)
        {
          for (;;)
          {
            try
            {
              localObject = new PhoneStateListener()
              {
                public void onCallStateChanged(final int paramAnonymous2Int, String paramAnonymous2String)
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      if (paramAnonymous2Int == 1) {
                        if ((!MediaController.this.isPlayingMessage(MediaController.this.playingMessageObject)) || (MediaController.this.isMessagePaused())) {}
                      }
                      do
                      {
                        MediaController.this.pauseMessage(MediaController.this.playingMessageObject);
                        for (;;)
                        {
                          localEmbedBottomSheet = EmbedBottomSheet.getInstance();
                          if (localEmbedBottomSheet != null) {
                            localEmbedBottomSheet.pause();
                          }
                          MediaController.access$3302(MediaController.this, true);
                          return;
                          if ((MediaController.this.recordStartRunnable != null) || (MediaController.this.recordingAudio != null)) {
                            MediaController.this.stopRecording(2);
                          }
                        }
                        if (paramAnonymous2Int == 0)
                        {
                          MediaController.access$3302(MediaController.this, false);
                          return;
                        }
                      } while (paramAnonymous2Int != 2);
                      EmbedBottomSheet localEmbedBottomSheet = EmbedBottomSheet.getInstance();
                      if (localEmbedBottomSheet != null) {
                        localEmbedBottomSheet.pause();
                      }
                      MediaController.access$3302(MediaController.this, true);
                    }
                  });
                }
              };
              localTelephonyManager = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService("phone");
              if (localTelephonyManager != null) {
                localTelephonyManager.listen((PhoneStateListener)localObject, 32);
              }
              return;
            }
            catch (Exception localException2)
            {
              FileLog.e(localException2);
            }
            localException1 = localException1;
            FileLog.e(localException1);
          }
        }
      }
    });
    this.fileBuffer = ByteBuffer.allocateDirect(1920);
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        int i = 0;
        while (i < 3)
        {
          NotificationCenter.getInstance(i).addObserver(MediaController.this, NotificationCenter.FileDidLoaded);
          NotificationCenter.getInstance(i).addObserver(MediaController.this, NotificationCenter.httpFileDidLoaded);
          NotificationCenter.getInstance(i).addObserver(MediaController.this, NotificationCenter.didReceivedNewMessages);
          NotificationCenter.getInstance(i).addObserver(MediaController.this, NotificationCenter.messagesDeleted);
          NotificationCenter.getInstance(i).addObserver(MediaController.this, NotificationCenter.removeAllMessagesFromDialog);
          NotificationCenter.getInstance(i).addObserver(MediaController.this, NotificationCenter.musicDidLoaded);
          NotificationCenter.getGlobalInstance().addObserver(MediaController.this, NotificationCenter.playerDidStartPlaying);
          i += 1;
        }
      }
    });
    this.mediaProjections = new String[] { "_data", "_display_name", "bucket_display_name", "datetaken", "title", "width", "height" };
    ContentResolver localContentResolver = ApplicationLoader.applicationContext.getContentResolver();
    try
    {
      localContentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, new GalleryObserverExternal());
    }
    catch (Exception localException3)
    {
      try
      {
        localContentResolver.registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, new GalleryObserverInternal());
      }
      catch (Exception localException3)
      {
        try
        {
          for (;;)
          {
            localContentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, new GalleryObserverExternal());
            try
            {
              localContentResolver.registerContentObserver(MediaStore.Video.Media.INTERNAL_CONTENT_URI, true, new GalleryObserverInternal());
              return;
            }
            catch (Exception localException1)
            {
              FileLog.e(localException1);
            }
            localException2 = localException2;
            FileLog.e(localException2);
            continue;
            localException3 = localException3;
            FileLog.e(localException3);
          }
        }
        catch (Exception localException4)
        {
          for (;;)
          {
            FileLog.e(localException4);
          }
        }
      }
    }
  }
  
  private static void broadcastNewPhotos(int paramInt1, final ArrayList<AlbumEntry> paramArrayList1, final ArrayList<AlbumEntry> paramArrayList2, final Integer paramInteger, final AlbumEntry paramAlbumEntry1, final AlbumEntry paramAlbumEntry2, int paramInt2)
  {
    if (broadcastPhotosRunnable != null) {
      AndroidUtilities.cancelRunOnUIThread(broadcastPhotosRunnable);
    }
    paramArrayList1 = new Runnable()
    {
      public void run()
      {
        if (PhotoViewer.getInstance().isVisible()) {
          MediaController.broadcastNewPhotos(this.val$guid, paramArrayList1, paramArrayList2, paramInteger, paramAlbumEntry1, paramAlbumEntry2, 1000);
        }
        for (;;)
        {
          return;
          MediaController.access$8002(null);
          MediaController.allPhotosAlbumEntry = paramAlbumEntry2;
          MediaController.allMediaAlbumEntry = paramAlbumEntry1;
          int i = 0;
          while (i < 3)
          {
            NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.albumsDidLoaded, new Object[] { Integer.valueOf(this.val$guid), paramArrayList1, paramArrayList2, paramInteger });
            i += 1;
          }
        }
      }
    };
    broadcastPhotosRunnable = paramArrayList1;
    AndroidUtilities.runOnUIThread(paramArrayList1, paramInt2);
  }
  
  private void buildShuffledPlayList()
  {
    if (this.playlist.isEmpty()) {}
    for (;;)
    {
      return;
      ArrayList localArrayList = new ArrayList(this.playlist);
      this.shuffledPlaylist.clear();
      MessageObject localMessageObject = (MessageObject)this.playlist.get(this.currentPlaylistNum);
      localArrayList.remove(this.currentPlaylistNum);
      this.shuffledPlaylist.add(localMessageObject);
      int j = localArrayList.size();
      int i = 0;
      while (i < j)
      {
        int k = Utilities.random.nextInt(localArrayList.size());
        this.shuffledPlaylist.add(localArrayList.get(k));
        localArrayList.remove(k);
        i += 1;
      }
    }
  }
  
  private void checkAudioFocus(MessageObject paramMessageObject)
  {
    if ((paramMessageObject.isVoice()) || (paramMessageObject.isRoundVideo())) {
      if (this.useFrontSpeaker) {
        i = 3;
      }
    }
    for (;;)
    {
      if (this.hasAudioFocus != i)
      {
        this.hasAudioFocus = i;
        if (i != 3) {
          break;
        }
        i = NotificationsController.audioManager.requestAudioFocus(this, 0, 1);
        if (i == 1) {
          this.audioFocus = 2;
        }
      }
      return;
      i = 2;
      continue;
      i = 1;
    }
    paramMessageObject = NotificationsController.audioManager;
    if (i == 2) {}
    for (int i = 3;; i = 1)
    {
      i = paramMessageObject.requestAudioFocus(this, 3, i);
      break;
    }
  }
  
  private void checkConversionCanceled()
  {
    synchronized (this.videoConvertSync)
    {
      boolean bool = this.cancelCurrentVideoConversion;
      if (bool) {
        throw new RuntimeException("canceled conversion");
      }
    }
  }
  
  private void checkDecoderQueue()
  {
    this.fileDecodingQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (MediaController.this.decodingFinished) {
          MediaController.this.checkPlayerQueue();
        }
        for (;;)
        {
          return;
          int i = 0;
          for (;;)
          {
            MediaController.AudioBuffer localAudioBuffer = null;
            synchronized (MediaController.this.playerSync)
            {
              if (!MediaController.this.freePlayerBuffers.isEmpty())
              {
                localAudioBuffer = (MediaController.AudioBuffer)MediaController.this.freePlayerBuffers.get(0);
                MediaController.this.freePlayerBuffers.remove(0);
              }
              if (!MediaController.this.usedPlayerBuffers.isEmpty()) {
                i = 1;
              }
              if (localAudioBuffer == null) {
                break label249;
              }
              MediaController.this.readOpusFile(localAudioBuffer.buffer, MediaController.this.playerBufferSize, MediaController.readArgs);
              localAudioBuffer.size = MediaController.readArgs[0];
              localAudioBuffer.pcmOffset = MediaController.readArgs[1];
              localAudioBuffer.finished = MediaController.readArgs[2];
              if (localAudioBuffer.finished == 1) {
                MediaController.access$4802(MediaController.this, true);
              }
              if (localAudioBuffer.size == 0) {
                break;
              }
              localAudioBuffer.buffer.rewind();
              localAudioBuffer.buffer.get(localAudioBuffer.bufferBytes);
            }
            synchronized (MediaController.this.playerSync)
            {
              MediaController.this.usedPlayerBuffers.add(localAudioBuffer);
              i = 1;
              continue;
              localObject1 = finally;
              throw ((Throwable)localObject1);
            }
          }
          synchronized (MediaController.this.playerSync)
          {
            MediaController.this.freePlayerBuffers.add(localObject2);
            label249:
            if (i == 0) {
              continue;
            }
            MediaController.this.checkPlayerQueue();
            return;
          }
        }
      }
    });
  }
  
  public static void checkGallery()
  {
    if ((Build.VERSION.SDK_INT < 24) || (allPhotosAlbumEntry == null)) {
      return;
    }
    int i = allPhotosAlbumEntry.photos.size();
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      @SuppressLint({"NewApi"})
      public void run()
      {
        k = 0;
        i = 0;
        Object localObject6 = null;
        Object localObject7 = null;
        Object localObject5 = null;
        int j = i;
        localObject3 = localObject6;
        localObject1 = localObject7;
        try
        {
          if (ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0)
          {
            localObject3 = localObject6;
            localObject1 = localObject7;
            localObject6 = MediaStore.Images.Media.query(ApplicationLoader.applicationContext.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { "COUNT(_id)" }, null, null, null);
            j = i;
            localObject5 = localObject6;
            if (localObject6 != null)
            {
              j = i;
              localObject5 = localObject6;
              localObject3 = localObject6;
              localObject1 = localObject6;
              if (((Cursor)localObject6).moveToNext())
              {
                localObject3 = localObject6;
                localObject1 = localObject6;
                i = ((Cursor)localObject6).getInt(0);
                j = 0 + i;
                localObject5 = localObject6;
              }
            }
          }
          i = j;
          localObject1 = localObject5;
          if (localObject5 != null)
          {
            ((Cursor)localObject5).close();
            localObject1 = localObject5;
            i = j;
          }
        }
        catch (Throwable localThrowable3)
        {
          for (;;)
          {
            localObject1 = localObject3;
            FileLog.e(localThrowable3);
            i = k;
            localObject1 = localObject3;
            if (localObject3 != null)
            {
              ((Cursor)localObject3).close();
              i = k;
              localObject1 = localObject3;
            }
          }
        }
        finally
        {
          if (localObject1 == null) {
            break label369;
          }
          ((Cursor)localObject1).close();
        }
        j = i;
        localObject6 = localObject1;
        localObject5 = localObject1;
        localObject3 = localObject1;
        try
        {
          if (ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0)
          {
            localObject5 = localObject1;
            localObject3 = localObject1;
            localObject1 = MediaStore.Images.Media.query(ApplicationLoader.applicationContext.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[] { "COUNT(_id)" }, null, null, null);
            j = i;
            localObject6 = localObject1;
            if (localObject1 != null)
            {
              j = i;
              localObject6 = localObject1;
              localObject5 = localObject1;
              localObject3 = localObject1;
              if (((Cursor)localObject1).moveToNext())
              {
                localObject5 = localObject1;
                localObject3 = localObject1;
                j = ((Cursor)localObject1).getInt(0);
                j = i + j;
                localObject6 = localObject1;
              }
            }
          }
          k = j;
          if (localObject6 != null)
          {
            ((Cursor)localObject6).close();
            k = j;
          }
        }
        catch (Throwable localThrowable1)
        {
          for (;;)
          {
            localThrowable2 = localThrowable3;
            FileLog.e(localThrowable1);
            k = i;
            if (localThrowable3 != null)
            {
              localThrowable3.close();
              k = i;
            }
          }
        }
        finally
        {
          Throwable localThrowable2;
          if (localThrowable2 == null) {
            break label416;
          }
          localThrowable2.close();
        }
        if (this.val$prevSize != k)
        {
          if (MediaController.refreshGalleryRunnable != null)
          {
            AndroidUtilities.cancelRunOnUIThread(MediaController.refreshGalleryRunnable);
            MediaController.access$1702(null);
          }
          MediaController.loadGalleryPhotosAlbums(0);
        }
      }
    }, 2000L);
  }
  
  private void checkIsNextMusicFileDownloaded(int paramInt)
  {
    if ((DownloadController.getInstance(paramInt).getCurrentDownloadMask() & 0x10) == 0) {}
    label26:
    label66:
    label147:
    label210:
    label212:
    label236:
    label249:
    label250:
    for (;;)
    {
      return;
      Object localObject1;
      int j;
      int i;
      MessageObject localMessageObject;
      Object localObject2;
      if (SharedConfig.shuffleMusic)
      {
        localObject1 = this.shuffledPlaylist;
        if ((localObject1 == null) || (((ArrayList)localObject1).size() < 2)) {
          break label210;
        }
        if (!SharedConfig.playOrderReversed) {
          break label212;
        }
        j = this.currentPlaylistNum + 1;
        i = j;
        if (j >= ((ArrayList)localObject1).size()) {
          i = 0;
        }
        localMessageObject = (MessageObject)((ArrayList)localObject1).get(i);
        if (!DownloadController.getInstance(paramInt).canDownloadMedia(localMessageObject)) {
          continue;
        }
        localObject1 = null;
        if (!TextUtils.isEmpty(localMessageObject.messageOwner.attachPath))
        {
          localObject2 = new File(localMessageObject.messageOwner.attachPath);
          localObject1 = localObject2;
          if (!((File)localObject2).exists()) {
            localObject1 = null;
          }
        }
        if (localObject1 == null) {
          break label236;
        }
        localObject2 = localObject1;
        if ((localObject2 == null) || (!((File)localObject2).exists())) {
          break label249;
        }
      }
      for (;;)
      {
        if ((localObject2 == null) || (localObject2 == localObject1) || (((File)localObject2).exists()) || (!localMessageObject.isMusic())) {
          break label250;
        }
        FileLoader.getInstance(paramInt).loadFile(localMessageObject.getDocument(), false, 0);
        return;
        localObject1 = this.playlist;
        break label26;
        break;
        j = this.currentPlaylistNum - 1;
        i = j;
        if (j >= 0) {
          break label66;
        }
        i = ((ArrayList)localObject1).size() - 1;
        break label66;
        localObject2 = FileLoader.getPathToMessage(localMessageObject.messageOwner);
        break label147;
      }
    }
  }
  
  private void checkIsNextVoiceFileDownloaded(int paramInt)
  {
    if ((this.voiceMessagesPlaylist == null) || (this.voiceMessagesPlaylist.size() < 2)) {}
    label150:
    label151:
    for (;;)
    {
      return;
      MessageObject localMessageObject = (MessageObject)this.voiceMessagesPlaylist.get(1);
      Object localObject2 = null;
      Object localObject1 = localObject2;
      if (localMessageObject.messageOwner.attachPath != null)
      {
        localObject1 = localObject2;
        if (localMessageObject.messageOwner.attachPath.length() > 0)
        {
          localObject2 = new File(localMessageObject.messageOwner.attachPath);
          localObject1 = localObject2;
          if (!((File)localObject2).exists()) {
            localObject1 = null;
          }
        }
      }
      if (localObject1 != null)
      {
        localObject2 = localObject1;
        if ((localObject2 == null) || (!((File)localObject2).exists())) {
          break label150;
        }
      }
      for (;;)
      {
        if ((localObject2 == null) || (localObject2 == localObject1) || (((File)localObject2).exists())) {
          break label151;
        }
        FileLoader.getInstance(paramInt).loadFile(localMessageObject.getDocument(), false, 0);
        return;
        localObject2 = FileLoader.getPathToMessage(localMessageObject.messageOwner);
        break;
      }
    }
  }
  
  private void checkPlayerQueue()
  {
    this.playerQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        synchronized (MediaController.this.playerObjectSync)
        {
          if ((MediaController.this.audioTrackPlayer == null) || (MediaController.this.audioTrackPlayer.getPlayState() != 3)) {
            return;
          }
          ??? = null;
        }
        int i;
        synchronized (MediaController.this.playerSync)
        {
          if (!MediaController.this.usedPlayerBuffers.isEmpty())
          {
            ??? = (MediaController.AudioBuffer)MediaController.this.usedPlayerBuffers.get(0);
            MediaController.this.usedPlayerBuffers.remove(0);
          }
          if (??? != null) {
            i = 0;
          }
          try
          {
            int j = MediaController.this.audioTrackPlayer.write(((MediaController.AudioBuffer)???).bufferBytes, 0, ((MediaController.AudioBuffer)???).size);
            i = j;
          }
          catch (Exception localException)
          {
            for (;;)
            {
              final long l;
              FileLog.e(localException);
              continue;
              i = -1;
            }
          }
          MediaController.access$5408(MediaController.this);
          if (i > 0)
          {
            l = ((MediaController.AudioBuffer)???).pcmOffset;
            if (((MediaController.AudioBuffer)???).finished == 1) {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MediaController.access$4102(MediaController.this, l);
                  if (this.val$marker != -1)
                  {
                    if (MediaController.this.audioTrackPlayer != null) {
                      MediaController.this.audioTrackPlayer.setNotificationMarkerPosition(1);
                    }
                    if (this.val$finalBuffersWrited == 1) {
                      MediaController.this.cleanupPlayer(true, true, true);
                    }
                  }
                }
              });
            }
          }
          else
          {
            if (((MediaController.AudioBuffer)???).finished != 1) {
              MediaController.this.checkPlayerQueue();
            }
            if ((??? == null) || ((??? != null) && (((MediaController.AudioBuffer)???).finished != 1))) {
              MediaController.this.checkDecoderQueue();
            }
            if (??? == null) {
              return;
            }
            synchronized (MediaController.this.playerSync)
            {
              MediaController.this.freePlayerBuffers.add(???);
              return;
            }
            localObject5 = finally;
            throw ((Throwable)localObject5);
          }
        }
      }
    });
  }
  
  private void checkScreenshots(ArrayList<Long> paramArrayList)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty()) || (this.lastChatEnterTime == 0L) || ((this.lastUser == null) && (!(this.lastSecretChat instanceof TLRPC.TL_encryptedChat)))) {}
    int j;
    do
    {
      return;
      j = 0;
      int i = 0;
      if (i < paramArrayList.size())
      {
        Long localLong = (Long)paramArrayList.get(i);
        int k;
        if ((this.lastMediaCheckTime != 0L) && (localLong.longValue() <= this.lastMediaCheckTime)) {
          k = j;
        }
        for (;;)
        {
          i += 1;
          j = k;
          break;
          k = j;
          if (localLong.longValue() >= this.lastChatEnterTime) {
            if (this.lastChatLeaveTime != 0L)
            {
              k = j;
              if (localLong.longValue() > this.lastChatLeaveTime + 2000L) {}
            }
            else
            {
              this.lastMediaCheckTime = Math.max(this.lastMediaCheckTime, localLong.longValue());
              k = 1;
            }
          }
        }
      }
    } while (j == 0);
    if (this.lastSecretChat != null)
    {
      SecretChatHelper.getInstance(this.lastChatAccount).sendScreenshotMessage(this.lastSecretChat, this.lastChatVisibleMessages, null);
      return;
    }
    SendMessagesHelper.getInstance(this.lastChatAccount).sendScreenshotMessage(this.lastUser, this.lastMessageId, null);
  }
  
  private native void closeOpusFile();
  
  /* Error */
  private boolean convertVideo(MessageObject paramMessageObject)
  {
    // Byte code:
    //   0: aload_1
    //   1: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   4: getfield 1085	org/telegram/messenger/VideoEditedInfo:originalPath	Ljava/lang/String;
    //   7: astore 44
    //   9: aload_1
    //   10: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   13: getfield 1088	org/telegram/messenger/VideoEditedInfo:startTime	J
    //   16: lstore 28
    //   18: aload_1
    //   19: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   22: getfield 1091	org/telegram/messenger/VideoEditedInfo:endTime	J
    //   25: lstore 30
    //   27: aload_1
    //   28: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   31: getfield 1094	org/telegram/messenger/VideoEditedInfo:resultWidth	I
    //   34: istore 7
    //   36: aload_1
    //   37: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   40: getfield 1097	org/telegram/messenger/VideoEditedInfo:resultHeight	I
    //   43: istore 6
    //   45: aload_1
    //   46: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   49: getfield 1100	org/telegram/messenger/VideoEditedInfo:rotationValue	I
    //   52: istore 8
    //   54: aload_1
    //   55: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   58: getfield 1103	org/telegram/messenger/VideoEditedInfo:originalWidth	I
    //   61: istore 11
    //   63: aload_1
    //   64: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   67: getfield 1106	org/telegram/messenger/VideoEditedInfo:originalHeight	I
    //   70: istore 12
    //   72: aload_1
    //   73: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   76: getfield 1109	org/telegram/messenger/VideoEditedInfo:framerate	I
    //   79: istore 9
    //   81: aload_1
    //   82: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   85: getfield 1112	org/telegram/messenger/VideoEditedInfo:bitrate	I
    //   88: istore 14
    //   90: iconst_0
    //   91: istore 10
    //   93: aload_1
    //   94: invokevirtual 1115	org/telegram/messenger/MessageObject:getDialogId	()J
    //   97: l2i
    //   98: ifne +177 -> 275
    //   101: iconst_1
    //   102: istore 34
    //   104: new 998	java/io/File
    //   107: dup
    //   108: aload_1
    //   109: getfield 986	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   112: getfield 991	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   115: invokespecial 999	java/io/File:<init>	(Ljava/lang/String;)V
    //   118: astore 55
    //   120: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   123: bipush 18
    //   125: if_icmpge +156 -> 281
    //   128: iload 6
    //   130: iload 7
    //   132: if_icmple +149 -> 281
    //   135: iload 7
    //   137: iload 11
    //   139: if_icmpeq +142 -> 281
    //   142: iload 6
    //   144: iload 12
    //   146: if_icmpeq +135 -> 281
    //   149: iload 7
    //   151: istore 4
    //   153: iload 6
    //   155: istore 5
    //   157: bipush 90
    //   159: istore_2
    //   160: sipush 270
    //   163: istore_3
    //   164: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   167: ldc_w 1117
    //   170: iconst_0
    //   171: invokevirtual 1121	android/content/Context:getSharedPreferences	(Ljava/lang/String;I)Landroid/content/SharedPreferences;
    //   174: astore 56
    //   176: new 998	java/io/File
    //   179: dup
    //   180: aload 44
    //   182: invokespecial 999	java/io/File:<init>	(Ljava/lang/String;)V
    //   185: astore 37
    //   187: aload_1
    //   188: invokevirtual 1124	org/telegram/messenger/MessageObject:getId	()I
    //   191: ifeq +199 -> 390
    //   194: aload 56
    //   196: ldc_w 1126
    //   199: iconst_1
    //   200: invokeinterface 1132 3 0
    //   205: istore 35
    //   207: aload 56
    //   209: invokeinterface 1136 1 0
    //   214: ldc_w 1126
    //   217: iconst_0
    //   218: invokeinterface 1142 3 0
    //   223: invokeinterface 1145 1 0
    //   228: pop
    //   229: aload 37
    //   231: invokevirtual 1148	java/io/File:canRead	()Z
    //   234: ifeq +8 -> 242
    //   237: iload 35
    //   239: ifne +151 -> 390
    //   242: aload_0
    //   243: aload_1
    //   244: aload 55
    //   246: iconst_1
    //   247: iconst_1
    //   248: invokespecial 1152	org/telegram/messenger/MediaController:didWriteData	(Lorg/telegram/messenger/MessageObject;Ljava/io/File;ZZ)V
    //   251: aload 56
    //   253: invokeinterface 1136 1 0
    //   258: ldc_w 1126
    //   261: iconst_1
    //   262: invokeinterface 1142 3 0
    //   267: invokeinterface 1145 1 0
    //   272: pop
    //   273: iconst_0
    //   274: ireturn
    //   275: iconst_0
    //   276: istore 34
    //   278: goto -174 -> 104
    //   281: iload 6
    //   283: istore 4
    //   285: iload 7
    //   287: istore 5
    //   289: iload 10
    //   291: istore_3
    //   292: iload 8
    //   294: istore_2
    //   295: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   298: bipush 20
    //   300: if_icmple -136 -> 164
    //   303: iload 8
    //   305: bipush 90
    //   307: if_icmpne +20 -> 327
    //   310: iload 7
    //   312: istore 4
    //   314: iload 6
    //   316: istore 5
    //   318: iconst_0
    //   319: istore_2
    //   320: sipush 270
    //   323: istore_3
    //   324: goto -160 -> 164
    //   327: iload 8
    //   329: sipush 180
    //   332: if_icmpne +20 -> 352
    //   335: sipush 180
    //   338: istore_3
    //   339: iconst_0
    //   340: istore_2
    //   341: iload 6
    //   343: istore 4
    //   345: iload 7
    //   347: istore 5
    //   349: goto -185 -> 164
    //   352: iload 6
    //   354: istore 4
    //   356: iload 7
    //   358: istore 5
    //   360: iload 10
    //   362: istore_3
    //   363: iload 8
    //   365: istore_2
    //   366: iload 8
    //   368: sipush 270
    //   371: if_icmpne -207 -> 164
    //   374: iload 7
    //   376: istore 4
    //   378: iload 6
    //   380: istore 5
    //   382: iconst_0
    //   383: istore_2
    //   384: bipush 90
    //   386: istore_3
    //   387: goto -223 -> 164
    //   390: aload_0
    //   391: iconst_1
    //   392: putfield 404	org/telegram/messenger/MediaController:videoConvertFirstWrite	Z
    //   395: iconst_0
    //   396: istore 35
    //   398: iconst_0
    //   399: istore 36
    //   401: invokestatic 1157	java/lang/System:currentTimeMillis	()J
    //   404: lstore 32
    //   406: iload 5
    //   408: ifeq +5684 -> 6092
    //   411: iload 4
    //   413: ifeq +5679 -> 6092
    //   416: aconst_null
    //   417: astore 43
    //   419: aconst_null
    //   420: astore 37
    //   422: aconst_null
    //   423: astore 41
    //   425: aconst_null
    //   426: astore 42
    //   428: aload 41
    //   430: astore 39
    //   432: aload 37
    //   434: astore 38
    //   436: aload 43
    //   438: astore 40
    //   440: new 1159	android/media/MediaCodec$BufferInfo
    //   443: dup
    //   444: invokespecial 1160	android/media/MediaCodec$BufferInfo:<init>	()V
    //   447: astore 57
    //   449: aload 41
    //   451: astore 39
    //   453: aload 37
    //   455: astore 38
    //   457: aload 43
    //   459: astore 40
    //   461: new 1162	org/telegram/messenger/video/Mp4Movie
    //   464: dup
    //   465: invokespecial 1163	org/telegram/messenger/video/Mp4Movie:<init>	()V
    //   468: astore 45
    //   470: aload 41
    //   472: astore 39
    //   474: aload 37
    //   476: astore 38
    //   478: aload 43
    //   480: astore 40
    //   482: aload 45
    //   484: aload 55
    //   486: invokevirtual 1167	org/telegram/messenger/video/Mp4Movie:setCacheFile	(Ljava/io/File;)V
    //   489: aload 41
    //   491: astore 39
    //   493: aload 37
    //   495: astore 38
    //   497: aload 43
    //   499: astore 40
    //   501: aload 45
    //   503: iload_2
    //   504: invokevirtual 1170	org/telegram/messenger/video/Mp4Movie:setRotation	(I)V
    //   507: aload 41
    //   509: astore 39
    //   511: aload 37
    //   513: astore 38
    //   515: aload 43
    //   517: astore 40
    //   519: aload 45
    //   521: iload 5
    //   523: iload 4
    //   525: invokevirtual 1174	org/telegram/messenger/video/Mp4Movie:setSize	(II)V
    //   528: aload 41
    //   530: astore 39
    //   532: aload 37
    //   534: astore 38
    //   536: aload 43
    //   538: astore 40
    //   540: new 1176	org/telegram/messenger/video/MP4Builder
    //   543: dup
    //   544: invokespecial 1177	org/telegram/messenger/video/MP4Builder:<init>	()V
    //   547: aload 45
    //   549: iload 34
    //   551: invokevirtual 1181	org/telegram/messenger/video/MP4Builder:createMovie	(Lorg/telegram/messenger/video/Mp4Movie;Z)Lorg/telegram/messenger/video/MP4Builder;
    //   554: astore 37
    //   556: aload 41
    //   558: astore 39
    //   560: aload 37
    //   562: astore 38
    //   564: aload 37
    //   566: astore 40
    //   568: new 1183	android/media/MediaExtractor
    //   571: dup
    //   572: invokespecial 1184	android/media/MediaExtractor:<init>	()V
    //   575: astore 46
    //   577: aload 46
    //   579: aload 44
    //   581: invokevirtual 1187	android/media/MediaExtractor:setDataSource	(Ljava/lang/String;)V
    //   584: aload_0
    //   585: invokespecial 1189	org/telegram/messenger/MediaController:checkConversionCanceled	()V
    //   588: iload 5
    //   590: iload 11
    //   592: if_icmpne +24 -> 616
    //   595: iload 4
    //   597: iload 12
    //   599: if_icmpne +17 -> 616
    //   602: iload_3
    //   603: ifne +13 -> 616
    //   606: aload_1
    //   607: getfield 1080	org/telegram/messenger/MessageObject:videoEditedInfo	Lorg/telegram/messenger/VideoEditedInfo;
    //   610: getfield 1192	org/telegram/messenger/VideoEditedInfo:roundVideo	Z
    //   613: ifeq +5316 -> 5929
    //   616: aload_0
    //   617: aload 46
    //   619: iconst_0
    //   620: invokespecial 1196	org/telegram/messenger/MediaController:findTrack	(Landroid/media/MediaExtractor;Z)I
    //   623: istore 22
    //   625: iload 14
    //   627: iconst_m1
    //   628: if_icmpeq +370 -> 998
    //   631: aload_0
    //   632: aload 46
    //   634: iconst_1
    //   635: invokespecial 1196	org/telegram/messenger/MediaController:findTrack	(Landroid/media/MediaExtractor;Z)I
    //   638: istore 10
    //   640: iload 35
    //   642: istore 34
    //   644: iload 22
    //   646: iflt +264 -> 910
    //   649: aconst_null
    //   650: astore 48
    //   652: aconst_null
    //   653: astore 49
    //   655: aconst_null
    //   656: astore 41
    //   658: aconst_null
    //   659: astore 38
    //   661: aconst_null
    //   662: astore 50
    //   664: aconst_null
    //   665: astore 39
    //   667: aconst_null
    //   668: astore 45
    //   670: ldc2_w 1197
    //   673: lstore 26
    //   675: iconst_0
    //   676: istore 16
    //   678: iconst_0
    //   679: istore 15
    //   681: iconst_0
    //   682: istore 18
    //   684: iconst_0
    //   685: istore 12
    //   687: iconst_0
    //   688: istore 8
    //   690: bipush -5
    //   692: istore 17
    //   694: bipush -5
    //   696: istore 13
    //   698: iconst_0
    //   699: istore_2
    //   700: iconst_0
    //   701: istore 6
    //   703: aload 49
    //   705: astore 40
    //   707: aload 41
    //   709: astore 42
    //   711: aload 50
    //   713: astore 43
    //   715: aload 45
    //   717: astore 44
    //   719: getstatic 1203	android/os/Build:MANUFACTURER	Ljava/lang/String;
    //   722: invokevirtual 1207	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   725: astore 47
    //   727: aload 49
    //   729: astore 40
    //   731: aload 41
    //   733: astore 42
    //   735: aload 50
    //   737: astore 43
    //   739: aload 45
    //   741: astore 44
    //   743: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   746: bipush 18
    //   748: if_icmpge +5497 -> 6245
    //   751: aload 49
    //   753: astore 40
    //   755: aload 41
    //   757: astore 42
    //   759: aload 50
    //   761: astore 43
    //   763: aload 45
    //   765: astore 44
    //   767: ldc -88
    //   769: invokestatic 1211	org/telegram/messenger/MediaController:selectCodec	(Ljava/lang/String;)Landroid/media/MediaCodecInfo;
    //   772: astore 51
    //   774: aload 49
    //   776: astore 40
    //   778: aload 41
    //   780: astore 42
    //   782: aload 50
    //   784: astore 43
    //   786: aload 45
    //   788: astore 44
    //   790: aload 51
    //   792: ldc -88
    //   794: invokestatic 1215	org/telegram/messenger/MediaController:selectColorFormat	(Landroid/media/MediaCodecInfo;Ljava/lang/String;)I
    //   797: istore 7
    //   799: iload 7
    //   801: ifne +203 -> 1004
    //   804: aload 49
    //   806: astore 40
    //   808: aload 41
    //   810: astore 42
    //   812: aload 50
    //   814: astore 43
    //   816: aload 45
    //   818: astore 44
    //   820: new 940	java/lang/RuntimeException
    //   823: dup
    //   824: ldc_w 1217
    //   827: invokespecial 943	java/lang/RuntimeException:<init>	(Ljava/lang/String;)V
    //   830: athrow
    //   831: astore 38
    //   833: aload 42
    //   835: astore 41
    //   837: aload 38
    //   839: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   842: iconst_1
    //   843: istore 34
    //   845: aload 41
    //   847: astore 42
    //   849: aload 46
    //   851: iload 22
    //   853: invokevirtual 1220	android/media/MediaExtractor:unselectTrack	(I)V
    //   856: aload 44
    //   858: ifnull +8 -> 866
    //   861: aload 44
    //   863: invokevirtual 1225	org/telegram/messenger/video/OutputSurface:release	()V
    //   866: aload 43
    //   868: ifnull +8 -> 876
    //   871: aload 43
    //   873: invokevirtual 1228	org/telegram/messenger/video/InputSurface:release	()V
    //   876: aload 40
    //   878: ifnull +13 -> 891
    //   881: aload 40
    //   883: invokevirtual 1233	android/media/MediaCodec:stop	()V
    //   886: aload 40
    //   888: invokevirtual 1234	android/media/MediaCodec:release	()V
    //   891: aload 42
    //   893: ifnull +13 -> 906
    //   896: aload 42
    //   898: invokevirtual 1233	android/media/MediaCodec:stop	()V
    //   901: aload 42
    //   903: invokevirtual 1234	android/media/MediaCodec:release	()V
    //   906: aload_0
    //   907: invokespecial 1189	org/telegram/messenger/MediaController:checkConversionCanceled	()V
    //   910: aload 46
    //   912: ifnull +8 -> 920
    //   915: aload 46
    //   917: invokevirtual 1235	android/media/MediaExtractor:release	()V
    //   920: aload 37
    //   922: ifnull +8 -> 930
    //   925: aload 37
    //   927: invokevirtual 1238	org/telegram/messenger/video/MP4Builder:finishMovie	()V
    //   930: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   933: ifeq +5230 -> 6163
    //   936: new 1245	java/lang/StringBuilder
    //   939: dup
    //   940: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   943: ldc_w 1248
    //   946: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   949: invokestatic 1157	java/lang/System:currentTimeMillis	()J
    //   952: lload 32
    //   954: lsub
    //   955: invokevirtual 1255	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   958: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   961: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   964: aload 56
    //   966: invokeinterface 1136 1 0
    //   971: ldc_w 1126
    //   974: iconst_1
    //   975: invokeinterface 1142 3 0
    //   980: invokeinterface 1145 1 0
    //   985: pop
    //   986: aload_0
    //   987: aload_1
    //   988: aload 55
    //   990: iconst_1
    //   991: iload 34
    //   993: invokespecial 1152	org/telegram/messenger/MediaController:didWriteData	(Lorg/telegram/messenger/MessageObject;Ljava/io/File;ZZ)V
    //   996: iconst_1
    //   997: ireturn
    //   998: iconst_m1
    //   999: istore 10
    //   1001: goto -361 -> 640
    //   1004: aload 49
    //   1006: astore 40
    //   1008: aload 41
    //   1010: astore 42
    //   1012: aload 50
    //   1014: astore 43
    //   1016: aload 45
    //   1018: astore 44
    //   1020: aload 51
    //   1022: invokevirtual 1266	android/media/MediaCodecInfo:getName	()Ljava/lang/String;
    //   1025: astore 52
    //   1027: aload 49
    //   1029: astore 40
    //   1031: aload 41
    //   1033: astore 42
    //   1035: aload 50
    //   1037: astore 43
    //   1039: aload 45
    //   1041: astore 44
    //   1043: aload 52
    //   1045: ldc_w 1268
    //   1048: invokevirtual 1271	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   1051: ifeq +1669 -> 2720
    //   1054: iconst_1
    //   1055: istore 11
    //   1057: aload 49
    //   1059: astore 40
    //   1061: aload 41
    //   1063: astore 42
    //   1065: aload 50
    //   1067: astore 43
    //   1069: aload 45
    //   1071: astore 44
    //   1073: iload 11
    //   1075: istore_2
    //   1076: iload 8
    //   1078: istore 6
    //   1080: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   1083: bipush 16
    //   1085: if_icmpne +67 -> 1152
    //   1088: aload 49
    //   1090: astore 40
    //   1092: aload 41
    //   1094: astore 42
    //   1096: aload 50
    //   1098: astore 43
    //   1100: aload 45
    //   1102: astore 44
    //   1104: aload 47
    //   1106: ldc_w 1273
    //   1109: invokevirtual 1276	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1112: ifne +5054 -> 6166
    //   1115: aload 49
    //   1117: astore 40
    //   1119: aload 41
    //   1121: astore 42
    //   1123: aload 50
    //   1125: astore 43
    //   1127: aload 45
    //   1129: astore 44
    //   1131: iload 11
    //   1133: istore_2
    //   1134: iload 8
    //   1136: istore 6
    //   1138: aload 47
    //   1140: ldc_w 1278
    //   1143: invokevirtual 1276	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1146: ifeq +6 -> 1152
    //   1149: goto +5017 -> 6166
    //   1152: aload 49
    //   1154: astore 40
    //   1156: aload 41
    //   1158: astore 42
    //   1160: aload 50
    //   1162: astore 43
    //   1164: aload 45
    //   1166: astore 44
    //   1168: iload 7
    //   1170: istore 11
    //   1172: iload_2
    //   1173: istore 8
    //   1175: iload 6
    //   1177: istore 12
    //   1179: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   1182: ifeq +80 -> 1262
    //   1185: aload 49
    //   1187: astore 40
    //   1189: aload 41
    //   1191: astore 42
    //   1193: aload 50
    //   1195: astore 43
    //   1197: aload 45
    //   1199: astore 44
    //   1201: new 1245	java/lang/StringBuilder
    //   1204: dup
    //   1205: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   1208: ldc_w 1280
    //   1211: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1214: aload 51
    //   1216: invokevirtual 1266	android/media/MediaCodecInfo:getName	()Ljava/lang/String;
    //   1219: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1222: ldc_w 1282
    //   1225: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1228: aload 47
    //   1230: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1233: ldc_w 1284
    //   1236: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1239: getstatic 1287	android/os/Build:MODEL	Ljava/lang/String;
    //   1242: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1245: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1248: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   1251: iload 6
    //   1253: istore 12
    //   1255: iload_2
    //   1256: istore 8
    //   1258: iload 7
    //   1260: istore 11
    //   1262: aload 49
    //   1264: astore 40
    //   1266: aload 41
    //   1268: astore 42
    //   1270: aload 50
    //   1272: astore 43
    //   1274: aload 45
    //   1276: astore 44
    //   1278: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   1281: ifeq +43 -> 1324
    //   1284: aload 49
    //   1286: astore 40
    //   1288: aload 41
    //   1290: astore 42
    //   1292: aload 50
    //   1294: astore 43
    //   1296: aload 45
    //   1298: astore 44
    //   1300: new 1245	java/lang/StringBuilder
    //   1303: dup
    //   1304: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   1307: ldc_w 1289
    //   1310: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1313: iload 11
    //   1315: invokevirtual 1292	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   1318: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1321: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   1324: iconst_0
    //   1325: istore 19
    //   1327: aload 49
    //   1329: astore 40
    //   1331: aload 41
    //   1333: astore 42
    //   1335: aload 50
    //   1337: astore 43
    //   1339: aload 45
    //   1341: astore 44
    //   1343: iload 5
    //   1345: iload 4
    //   1347: imul
    //   1348: iconst_3
    //   1349: imul
    //   1350: iconst_2
    //   1351: idiv
    //   1352: istore_2
    //   1353: iload 8
    //   1355: ifne +1515 -> 2870
    //   1358: iload_2
    //   1359: istore 6
    //   1361: iload 19
    //   1363: istore 7
    //   1365: iload 4
    //   1367: bipush 16
    //   1369: irem
    //   1370: ifeq +48 -> 1418
    //   1373: iload 5
    //   1375: iload 4
    //   1377: bipush 16
    //   1379: iload 4
    //   1381: bipush 16
    //   1383: irem
    //   1384: isub
    //   1385: iadd
    //   1386: iload 4
    //   1388: isub
    //   1389: imul
    //   1390: istore 7
    //   1392: aload 49
    //   1394: astore 40
    //   1396: aload 41
    //   1398: astore 42
    //   1400: aload 50
    //   1402: astore 43
    //   1404: aload 45
    //   1406: astore 44
    //   1408: iload_2
    //   1409: iload 7
    //   1411: iconst_5
    //   1412: imul
    //   1413: iconst_4
    //   1414: idiv
    //   1415: iadd
    //   1416: istore 6
    //   1418: aload 49
    //   1420: astore 40
    //   1422: aload 41
    //   1424: astore 42
    //   1426: aload 50
    //   1428: astore 43
    //   1430: aload 45
    //   1432: astore 44
    //   1434: aload 46
    //   1436: iload 22
    //   1438: invokevirtual 1295	android/media/MediaExtractor:selectTrack	(I)V
    //   1441: aload 49
    //   1443: astore 40
    //   1445: aload 41
    //   1447: astore 42
    //   1449: aload 50
    //   1451: astore 43
    //   1453: aload 45
    //   1455: astore 44
    //   1457: aload 46
    //   1459: iload 22
    //   1461: invokevirtual 1299	android/media/MediaExtractor:getTrackFormat	(I)Landroid/media/MediaFormat;
    //   1464: astore 51
    //   1466: aconst_null
    //   1467: astore 47
    //   1469: iload 10
    //   1471: iflt +106 -> 1577
    //   1474: aload 49
    //   1476: astore 40
    //   1478: aload 41
    //   1480: astore 42
    //   1482: aload 50
    //   1484: astore 43
    //   1486: aload 45
    //   1488: astore 44
    //   1490: aload 46
    //   1492: iload 10
    //   1494: invokevirtual 1295	android/media/MediaExtractor:selectTrack	(I)V
    //   1497: aload 49
    //   1499: astore 40
    //   1501: aload 41
    //   1503: astore 42
    //   1505: aload 50
    //   1507: astore 43
    //   1509: aload 45
    //   1511: astore 44
    //   1513: aload 46
    //   1515: iload 10
    //   1517: invokevirtual 1299	android/media/MediaExtractor:getTrackFormat	(I)Landroid/media/MediaFormat;
    //   1520: astore 52
    //   1522: aload 49
    //   1524: astore 40
    //   1526: aload 41
    //   1528: astore 42
    //   1530: aload 50
    //   1532: astore 43
    //   1534: aload 45
    //   1536: astore 44
    //   1538: aload 52
    //   1540: ldc_w 1301
    //   1543: invokevirtual 1306	android/media/MediaFormat:getInteger	(Ljava/lang/String;)I
    //   1546: invokestatic 492	java/nio/ByteBuffer:allocateDirect	(I)Ljava/nio/ByteBuffer;
    //   1549: astore 47
    //   1551: aload 49
    //   1553: astore 40
    //   1555: aload 41
    //   1557: astore 42
    //   1559: aload 50
    //   1561: astore 43
    //   1563: aload 45
    //   1565: astore 44
    //   1567: aload 37
    //   1569: aload 52
    //   1571: iconst_1
    //   1572: invokevirtual 1310	org/telegram/messenger/video/MP4Builder:addTrack	(Landroid/media/MediaFormat;Z)I
    //   1575: istore 13
    //   1577: lload 28
    //   1579: lconst_0
    //   1580: lcmp
    //   1581: ifle +1470 -> 3051
    //   1584: aload 49
    //   1586: astore 40
    //   1588: aload 41
    //   1590: astore 42
    //   1592: aload 50
    //   1594: astore 43
    //   1596: aload 45
    //   1598: astore 44
    //   1600: aload 46
    //   1602: lload 28
    //   1604: iconst_0
    //   1605: invokevirtual 1314	android/media/MediaExtractor:seekTo	(JI)V
    //   1608: aload 49
    //   1610: astore 40
    //   1612: aload 41
    //   1614: astore 42
    //   1616: aload 50
    //   1618: astore 43
    //   1620: aload 45
    //   1622: astore 44
    //   1624: ldc -88
    //   1626: iload 5
    //   1628: iload 4
    //   1630: invokestatic 1318	android/media/MediaFormat:createVideoFormat	(Ljava/lang/String;II)Landroid/media/MediaFormat;
    //   1633: astore 52
    //   1635: aload 49
    //   1637: astore 40
    //   1639: aload 41
    //   1641: astore 42
    //   1643: aload 50
    //   1645: astore 43
    //   1647: aload 45
    //   1649: astore 44
    //   1651: aload 52
    //   1653: ldc_w 1320
    //   1656: iload 11
    //   1658: invokevirtual 1324	android/media/MediaFormat:setInteger	(Ljava/lang/String;I)V
    //   1661: iload 14
    //   1663: ifle +1475 -> 3138
    //   1666: iload 14
    //   1668: istore_2
    //   1669: aload 49
    //   1671: astore 40
    //   1673: aload 41
    //   1675: astore 42
    //   1677: aload 50
    //   1679: astore 43
    //   1681: aload 45
    //   1683: astore 44
    //   1685: aload 52
    //   1687: ldc_w 1325
    //   1690: iload_2
    //   1691: invokevirtual 1324	android/media/MediaFormat:setInteger	(Ljava/lang/String;I)V
    //   1694: iload 9
    //   1696: ifeq +1449 -> 3145
    //   1699: iload 9
    //   1701: istore_2
    //   1702: aload 49
    //   1704: astore 40
    //   1706: aload 41
    //   1708: astore 42
    //   1710: aload 50
    //   1712: astore 43
    //   1714: aload 45
    //   1716: astore 44
    //   1718: aload 52
    //   1720: ldc_w 1327
    //   1723: iload_2
    //   1724: invokevirtual 1324	android/media/MediaFormat:setInteger	(Ljava/lang/String;I)V
    //   1727: aload 49
    //   1729: astore 40
    //   1731: aload 41
    //   1733: astore 42
    //   1735: aload 50
    //   1737: astore 43
    //   1739: aload 45
    //   1741: astore 44
    //   1743: aload 52
    //   1745: ldc_w 1329
    //   1748: bipush 10
    //   1750: invokevirtual 1324	android/media/MediaFormat:setInteger	(Ljava/lang/String;I)V
    //   1753: aload 49
    //   1755: astore 40
    //   1757: aload 41
    //   1759: astore 42
    //   1761: aload 50
    //   1763: astore 43
    //   1765: aload 45
    //   1767: astore 44
    //   1769: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   1772: bipush 18
    //   1774: if_icmpge +58 -> 1832
    //   1777: aload 49
    //   1779: astore 40
    //   1781: aload 41
    //   1783: astore 42
    //   1785: aload 50
    //   1787: astore 43
    //   1789: aload 45
    //   1791: astore 44
    //   1793: aload 52
    //   1795: ldc_w 1331
    //   1798: iload 5
    //   1800: bipush 32
    //   1802: iadd
    //   1803: invokevirtual 1324	android/media/MediaFormat:setInteger	(Ljava/lang/String;I)V
    //   1806: aload 49
    //   1808: astore 40
    //   1810: aload 41
    //   1812: astore 42
    //   1814: aload 50
    //   1816: astore 43
    //   1818: aload 45
    //   1820: astore 44
    //   1822: aload 52
    //   1824: ldc_w 1333
    //   1827: iload 4
    //   1829: invokevirtual 1324	android/media/MediaFormat:setInteger	(Ljava/lang/String;I)V
    //   1832: aload 49
    //   1834: astore 40
    //   1836: aload 41
    //   1838: astore 42
    //   1840: aload 50
    //   1842: astore 43
    //   1844: aload 45
    //   1846: astore 44
    //   1848: ldc -88
    //   1850: invokestatic 1337	android/media/MediaCodec:createEncoderByType	(Ljava/lang/String;)Landroid/media/MediaCodec;
    //   1853: astore 41
    //   1855: aload 49
    //   1857: astore 40
    //   1859: aload 41
    //   1861: astore 42
    //   1863: aload 50
    //   1865: astore 43
    //   1867: aload 45
    //   1869: astore 44
    //   1871: aload 41
    //   1873: aload 52
    //   1875: aconst_null
    //   1876: aconst_null
    //   1877: iconst_1
    //   1878: invokevirtual 1341	android/media/MediaCodec:configure	(Landroid/media/MediaFormat;Landroid/view/Surface;Landroid/media/MediaCrypto;I)V
    //   1881: aload 49
    //   1883: astore 40
    //   1885: aload 41
    //   1887: astore 42
    //   1889: aload 50
    //   1891: astore 43
    //   1893: aload 45
    //   1895: astore 44
    //   1897: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   1900: bipush 18
    //   1902: if_icmplt +38 -> 1940
    //   1905: aload 49
    //   1907: astore 40
    //   1909: aload 41
    //   1911: astore 42
    //   1913: aload 50
    //   1915: astore 43
    //   1917: aload 45
    //   1919: astore 44
    //   1921: new 1227	org/telegram/messenger/video/InputSurface
    //   1924: dup
    //   1925: aload 41
    //   1927: invokevirtual 1345	android/media/MediaCodec:createInputSurface	()Landroid/view/Surface;
    //   1930: invokespecial 1348	org/telegram/messenger/video/InputSurface:<init>	(Landroid/view/Surface;)V
    //   1933: astore 38
    //   1935: aload 38
    //   1937: invokevirtual 1351	org/telegram/messenger/video/InputSurface:makeCurrent	()V
    //   1940: aload 49
    //   1942: astore 40
    //   1944: aload 41
    //   1946: astore 42
    //   1948: aload 38
    //   1950: astore 43
    //   1952: aload 45
    //   1954: astore 44
    //   1956: aload 41
    //   1958: invokevirtual 1354	android/media/MediaCodec:start	()V
    //   1961: aload 49
    //   1963: astore 40
    //   1965: aload 41
    //   1967: astore 42
    //   1969: aload 38
    //   1971: astore 43
    //   1973: aload 45
    //   1975: astore 44
    //   1977: aload 51
    //   1979: ldc_w 1356
    //   1982: invokevirtual 1360	android/media/MediaFormat:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1985: invokestatic 1363	android/media/MediaCodec:createDecoderByType	(Ljava/lang/String;)Landroid/media/MediaCodec;
    //   1988: astore 48
    //   1990: aload 48
    //   1992: astore 40
    //   1994: aload 41
    //   1996: astore 42
    //   1998: aload 38
    //   2000: astore 43
    //   2002: aload 45
    //   2004: astore 44
    //   2006: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   2009: bipush 18
    //   2011: if_icmplt +1140 -> 3151
    //   2014: aload 48
    //   2016: astore 40
    //   2018: aload 41
    //   2020: astore 42
    //   2022: aload 38
    //   2024: astore 43
    //   2026: aload 45
    //   2028: astore 44
    //   2030: new 1222	org/telegram/messenger/video/OutputSurface
    //   2033: dup
    //   2034: invokespecial 1364	org/telegram/messenger/video/OutputSurface:<init>	()V
    //   2037: astore 39
    //   2039: aload 48
    //   2041: astore 40
    //   2043: aload 41
    //   2045: astore 42
    //   2047: aload 38
    //   2049: astore 43
    //   2051: aload 39
    //   2053: astore 44
    //   2055: aload 48
    //   2057: aload 51
    //   2059: aload 39
    //   2061: invokevirtual 1367	org/telegram/messenger/video/OutputSurface:getSurface	()Landroid/view/Surface;
    //   2064: aconst_null
    //   2065: iconst_0
    //   2066: invokevirtual 1341	android/media/MediaCodec:configure	(Landroid/media/MediaFormat;Landroid/view/Surface;Landroid/media/MediaCrypto;I)V
    //   2069: aload 48
    //   2071: astore 40
    //   2073: aload 41
    //   2075: astore 42
    //   2077: aload 38
    //   2079: astore 43
    //   2081: aload 39
    //   2083: astore 44
    //   2085: aload 48
    //   2087: invokevirtual 1354	android/media/MediaCodec:start	()V
    //   2090: aconst_null
    //   2091: astore 49
    //   2093: aconst_null
    //   2094: astore 45
    //   2096: aconst_null
    //   2097: astore 51
    //   2099: aload 48
    //   2101: astore 40
    //   2103: aload 41
    //   2105: astore 42
    //   2107: aload 38
    //   2109: astore 43
    //   2111: aload 39
    //   2113: astore 44
    //   2115: aload 51
    //   2117: astore 50
    //   2119: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   2122: bipush 21
    //   2124: if_icmpge +116 -> 2240
    //   2127: aload 48
    //   2129: astore 40
    //   2131: aload 41
    //   2133: astore 42
    //   2135: aload 38
    //   2137: astore 43
    //   2139: aload 39
    //   2141: astore 44
    //   2143: aload 48
    //   2145: invokevirtual 1371	android/media/MediaCodec:getInputBuffers	()[Ljava/nio/ByteBuffer;
    //   2148: astore 52
    //   2150: aload 48
    //   2152: astore 40
    //   2154: aload 41
    //   2156: astore 42
    //   2158: aload 38
    //   2160: astore 43
    //   2162: aload 39
    //   2164: astore 44
    //   2166: aload 41
    //   2168: invokevirtual 1374	android/media/MediaCodec:getOutputBuffers	()[Ljava/nio/ByteBuffer;
    //   2171: astore 53
    //   2173: aload 48
    //   2175: astore 40
    //   2177: aload 41
    //   2179: astore 42
    //   2181: aload 38
    //   2183: astore 43
    //   2185: aload 39
    //   2187: astore 44
    //   2189: aload 52
    //   2191: astore 49
    //   2193: aload 51
    //   2195: astore 50
    //   2197: aload 53
    //   2199: astore 45
    //   2201: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   2204: bipush 18
    //   2206: if_icmpge +34 -> 2240
    //   2209: aload 48
    //   2211: astore 40
    //   2213: aload 41
    //   2215: astore 42
    //   2217: aload 38
    //   2219: astore 43
    //   2221: aload 39
    //   2223: astore 44
    //   2225: aload 41
    //   2227: invokevirtual 1371	android/media/MediaCodec:getInputBuffers	()[Ljava/nio/ByteBuffer;
    //   2230: astore 50
    //   2232: aload 53
    //   2234: astore 45
    //   2236: aload 52
    //   2238: astore 49
    //   2240: aload 48
    //   2242: astore 40
    //   2244: aload 41
    //   2246: astore 42
    //   2248: aload 38
    //   2250: astore 43
    //   2252: aload 39
    //   2254: astore 44
    //   2256: aload_0
    //   2257: invokespecial 1189	org/telegram/messenger/MediaController:checkConversionCanceled	()V
    //   2260: iload 17
    //   2262: istore 14
    //   2264: iload 15
    //   2266: istore_2
    //   2267: aload 45
    //   2269: astore 51
    //   2271: aload 48
    //   2273: astore 40
    //   2275: aload 41
    //   2277: astore 42
    //   2279: iload 36
    //   2281: istore 34
    //   2283: aload 38
    //   2285: astore 43
    //   2287: aload 39
    //   2289: astore 44
    //   2291: iload 16
    //   2293: ifne -1444 -> 849
    //   2296: aload 48
    //   2298: astore 40
    //   2300: aload 41
    //   2302: astore 42
    //   2304: aload 38
    //   2306: astore 43
    //   2308: aload 39
    //   2310: astore 44
    //   2312: aload_0
    //   2313: invokespecial 1189	org/telegram/messenger/MediaController:checkConversionCanceled	()V
    //   2316: iload_2
    //   2317: istore_3
    //   2318: iload_2
    //   2319: ifne +3856 -> 6175
    //   2322: iconst_0
    //   2323: istore_3
    //   2324: aload 48
    //   2326: astore 40
    //   2328: aload 41
    //   2330: astore 42
    //   2332: aload 38
    //   2334: astore 43
    //   2336: aload 39
    //   2338: astore 44
    //   2340: aload 46
    //   2342: invokevirtual 1377	android/media/MediaExtractor:getSampleTrackIndex	()I
    //   2345: istore 15
    //   2347: iload 15
    //   2349: iload 22
    //   2351: if_icmpne +924 -> 3275
    //   2354: aload 48
    //   2356: astore 40
    //   2358: aload 41
    //   2360: astore 42
    //   2362: aload 38
    //   2364: astore 43
    //   2366: aload 39
    //   2368: astore 44
    //   2370: aload 48
    //   2372: ldc2_w 1378
    //   2375: invokevirtual 1383	android/media/MediaCodec:dequeueInputBuffer	(J)I
    //   2378: istore 15
    //   2380: iload_3
    //   2381: istore 9
    //   2383: iload_2
    //   2384: istore 8
    //   2386: iload 15
    //   2388: iflt +98 -> 2486
    //   2391: aload 48
    //   2393: astore 40
    //   2395: aload 41
    //   2397: astore 42
    //   2399: aload 38
    //   2401: astore 43
    //   2403: aload 39
    //   2405: astore 44
    //   2407: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   2410: bipush 21
    //   2412: if_icmpge +772 -> 3184
    //   2415: aload 49
    //   2417: iload 15
    //   2419: aaload
    //   2420: astore 45
    //   2422: aload 48
    //   2424: astore 40
    //   2426: aload 41
    //   2428: astore 42
    //   2430: aload 38
    //   2432: astore 43
    //   2434: aload 39
    //   2436: astore 44
    //   2438: aload 46
    //   2440: aload 45
    //   2442: iconst_0
    //   2443: invokevirtual 1386	android/media/MediaExtractor:readSampleData	(Ljava/nio/ByteBuffer;I)I
    //   2446: istore 8
    //   2448: iload 8
    //   2450: ifge +762 -> 3212
    //   2453: aload 48
    //   2455: astore 40
    //   2457: aload 41
    //   2459: astore 42
    //   2461: aload 38
    //   2463: astore 43
    //   2465: aload 39
    //   2467: astore 44
    //   2469: aload 48
    //   2471: iload 15
    //   2473: iconst_0
    //   2474: iconst_0
    //   2475: lconst_0
    //   2476: iconst_4
    //   2477: invokevirtual 1390	android/media/MediaCodec:queueInputBuffer	(IIIJI)V
    //   2480: iconst_1
    //   2481: istore 8
    //   2483: iload_3
    //   2484: istore 9
    //   2486: iload 8
    //   2488: istore_3
    //   2489: iload 9
    //   2491: ifeq +3684 -> 6175
    //   2494: aload 48
    //   2496: astore 40
    //   2498: aload 41
    //   2500: astore 42
    //   2502: aload 38
    //   2504: astore 43
    //   2506: aload 39
    //   2508: astore 44
    //   2510: aload 48
    //   2512: ldc2_w 1378
    //   2515: invokevirtual 1383	android/media/MediaCodec:dequeueInputBuffer	(J)I
    //   2518: istore_2
    //   2519: iload 8
    //   2521: istore_3
    //   2522: iload_2
    //   2523: iflt +3652 -> 6175
    //   2526: aload 48
    //   2528: astore 40
    //   2530: aload 41
    //   2532: astore 42
    //   2534: aload 38
    //   2536: astore 43
    //   2538: aload 39
    //   2540: astore 44
    //   2542: aload 48
    //   2544: iload_2
    //   2545: iconst_0
    //   2546: iconst_0
    //   2547: lconst_0
    //   2548: iconst_4
    //   2549: invokevirtual 1390	android/media/MediaCodec:queueInputBuffer	(IIIJI)V
    //   2552: iconst_1
    //   2553: istore_3
    //   2554: goto +3621 -> 6175
    //   2557: aload 48
    //   2559: astore 40
    //   2561: aload 41
    //   2563: astore 42
    //   2565: aload 38
    //   2567: astore 43
    //   2569: aload 39
    //   2571: astore 44
    //   2573: aload_0
    //   2574: invokespecial 1189	org/telegram/messenger/MediaController:checkConversionCanceled	()V
    //   2577: aload 48
    //   2579: astore 40
    //   2581: aload 41
    //   2583: astore 42
    //   2585: aload 38
    //   2587: astore 43
    //   2589: aload 39
    //   2591: astore 44
    //   2593: aload 41
    //   2595: aload 57
    //   2597: ldc2_w 1378
    //   2600: invokevirtual 1394	android/media/MediaCodec:dequeueOutputBuffer	(Landroid/media/MediaCodec$BufferInfo;J)I
    //   2603: istore 18
    //   2605: iload 18
    //   2607: iconst_m1
    //   2608: if_icmpne +1071 -> 3679
    //   2611: iconst_0
    //   2612: istore 14
    //   2614: iload 9
    //   2616: istore_2
    //   2617: iload 19
    //   2619: istore 16
    //   2621: aload 45
    //   2623: astore 51
    //   2625: iload 14
    //   2627: istore 17
    //   2629: aload 51
    //   2631: astore 45
    //   2633: iload 16
    //   2635: istore 19
    //   2637: iload_2
    //   2638: istore 9
    //   2640: iload 18
    //   2642: iconst_m1
    //   2643: if_icmpne +3567 -> 6210
    //   2646: iload 14
    //   2648: istore 17
    //   2650: aload 51
    //   2652: astore 45
    //   2654: iload 16
    //   2656: istore 19
    //   2658: iload_2
    //   2659: istore 9
    //   2661: iload 15
    //   2663: ifne +3547 -> 6210
    //   2666: aload 48
    //   2668: astore 40
    //   2670: aload 41
    //   2672: astore 42
    //   2674: aload 38
    //   2676: astore 43
    //   2678: aload 39
    //   2680: astore 44
    //   2682: aload 48
    //   2684: aload 57
    //   2686: ldc2_w 1378
    //   2689: invokevirtual 1394	android/media/MediaCodec:dequeueOutputBuffer	(Landroid/media/MediaCodec$BufferInfo;J)I
    //   2692: istore 21
    //   2694: iload 21
    //   2696: iconst_m1
    //   2697: if_icmpne +1958 -> 4655
    //   2700: iconst_0
    //   2701: istore_3
    //   2702: iload 14
    //   2704: istore 17
    //   2706: aload 51
    //   2708: astore 45
    //   2710: iload 16
    //   2712: istore 19
    //   2714: iload_2
    //   2715: istore 9
    //   2717: goto +3493 -> 6210
    //   2720: aload 49
    //   2722: astore 40
    //   2724: aload 41
    //   2726: astore 42
    //   2728: aload 50
    //   2730: astore 43
    //   2732: aload 45
    //   2734: astore 44
    //   2736: aload 52
    //   2738: ldc_w 1396
    //   2741: invokevirtual 1271	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   2744: ifeq +12 -> 2756
    //   2747: iconst_2
    //   2748: istore_2
    //   2749: iload 8
    //   2751: istore 6
    //   2753: goto -1601 -> 1152
    //   2756: aload 49
    //   2758: astore 40
    //   2760: aload 41
    //   2762: astore 42
    //   2764: aload 50
    //   2766: astore 43
    //   2768: aload 45
    //   2770: astore 44
    //   2772: aload 52
    //   2774: ldc_w 1398
    //   2777: invokevirtual 1276	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   2780: ifeq +12 -> 2792
    //   2783: iconst_3
    //   2784: istore_2
    //   2785: iload 8
    //   2787: istore 6
    //   2789: goto -1637 -> 1152
    //   2792: aload 49
    //   2794: astore 40
    //   2796: aload 41
    //   2798: astore 42
    //   2800: aload 50
    //   2802: astore 43
    //   2804: aload 45
    //   2806: astore 44
    //   2808: aload 52
    //   2810: ldc_w 1400
    //   2813: invokevirtual 1276	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   2816: ifeq +11 -> 2827
    //   2819: iconst_4
    //   2820: istore_2
    //   2821: iconst_1
    //   2822: istore 6
    //   2824: goto -1672 -> 1152
    //   2827: aload 49
    //   2829: astore 40
    //   2831: aload 41
    //   2833: astore 42
    //   2835: aload 50
    //   2837: astore 43
    //   2839: aload 45
    //   2841: astore 44
    //   2843: iload 6
    //   2845: istore_2
    //   2846: iload 8
    //   2848: istore 6
    //   2850: aload 52
    //   2852: ldc_w 1402
    //   2855: invokevirtual 1276	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   2858: ifeq -1706 -> 1152
    //   2861: iconst_5
    //   2862: istore_2
    //   2863: iload 8
    //   2865: istore 6
    //   2867: goto -1715 -> 1152
    //   2870: iload 8
    //   2872: iconst_1
    //   2873: if_icmpne +70 -> 2943
    //   2876: aload 49
    //   2878: astore 40
    //   2880: aload 41
    //   2882: astore 42
    //   2884: aload 50
    //   2886: astore 43
    //   2888: aload 45
    //   2890: astore 44
    //   2892: iload_2
    //   2893: istore 6
    //   2895: iload 19
    //   2897: istore 7
    //   2899: aload 47
    //   2901: invokevirtual 1207	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   2904: ldc_w 1273
    //   2907: invokevirtual 1276	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   2910: ifne -1492 -> 1418
    //   2913: iload 5
    //   2915: iload 4
    //   2917: imul
    //   2918: sipush 2047
    //   2921: iadd
    //   2922: sipush 63488
    //   2925: iand
    //   2926: iload 5
    //   2928: iload 4
    //   2930: imul
    //   2931: isub
    //   2932: istore 7
    //   2934: iload_2
    //   2935: iload 7
    //   2937: iadd
    //   2938: istore 6
    //   2940: goto -1522 -> 1418
    //   2943: iload_2
    //   2944: istore 6
    //   2946: iload 19
    //   2948: istore 7
    //   2950: iload 8
    //   2952: iconst_5
    //   2953: if_icmpeq -1535 -> 1418
    //   2956: iload_2
    //   2957: istore 6
    //   2959: iload 19
    //   2961: istore 7
    //   2963: iload 8
    //   2965: iconst_3
    //   2966: if_icmpne -1548 -> 1418
    //   2969: aload 49
    //   2971: astore 40
    //   2973: aload 41
    //   2975: astore 42
    //   2977: aload 50
    //   2979: astore 43
    //   2981: aload 45
    //   2983: astore 44
    //   2985: iload_2
    //   2986: istore 6
    //   2988: iload 19
    //   2990: istore 7
    //   2992: aload 47
    //   2994: ldc_w 1404
    //   2997: invokevirtual 1276	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   3000: ifeq -1582 -> 1418
    //   3003: iload 5
    //   3005: iload 4
    //   3007: bipush 16
    //   3009: iload 4
    //   3011: bipush 16
    //   3013: irem
    //   3014: isub
    //   3015: iadd
    //   3016: iload 4
    //   3018: isub
    //   3019: imul
    //   3020: istore 7
    //   3022: aload 49
    //   3024: astore 40
    //   3026: aload 41
    //   3028: astore 42
    //   3030: aload 50
    //   3032: astore 43
    //   3034: aload 45
    //   3036: astore 44
    //   3038: iload_2
    //   3039: iload 7
    //   3041: iconst_5
    //   3042: imul
    //   3043: iconst_4
    //   3044: idiv
    //   3045: iadd
    //   3046: istore 6
    //   3048: goto -1630 -> 1418
    //   3051: aload 49
    //   3053: astore 40
    //   3055: aload 41
    //   3057: astore 42
    //   3059: aload 50
    //   3061: astore 43
    //   3063: aload 45
    //   3065: astore 44
    //   3067: aload 46
    //   3069: lconst_0
    //   3070: iconst_0
    //   3071: invokevirtual 1314	android/media/MediaExtractor:seekTo	(JI)V
    //   3074: goto -1466 -> 1608
    //   3077: astore_1
    //   3078: aload 46
    //   3080: astore 39
    //   3082: aload 39
    //   3084: ifnull +8 -> 3092
    //   3087: aload 39
    //   3089: invokevirtual 1235	android/media/MediaExtractor:release	()V
    //   3092: aload 37
    //   3094: ifnull +8 -> 3102
    //   3097: aload 37
    //   3099: invokevirtual 1238	org/telegram/messenger/video/MP4Builder:finishMovie	()V
    //   3102: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   3105: ifeq +31 -> 3136
    //   3108: new 1245	java/lang/StringBuilder
    //   3111: dup
    //   3112: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   3115: ldc_w 1248
    //   3118: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   3121: invokestatic 1157	java/lang/System:currentTimeMillis	()J
    //   3124: lload 32
    //   3126: lsub
    //   3127: invokevirtual 1255	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   3130: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   3133: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   3136: aload_1
    //   3137: athrow
    //   3138: ldc_w 1405
    //   3141: istore_2
    //   3142: goto -1473 -> 1669
    //   3145: bipush 25
    //   3147: istore_2
    //   3148: goto -1446 -> 1702
    //   3151: aload 48
    //   3153: astore 40
    //   3155: aload 41
    //   3157: astore 42
    //   3159: aload 38
    //   3161: astore 43
    //   3163: aload 45
    //   3165: astore 44
    //   3167: new 1222	org/telegram/messenger/video/OutputSurface
    //   3170: dup
    //   3171: iload 5
    //   3173: iload 4
    //   3175: iload_3
    //   3176: invokespecial 1408	org/telegram/messenger/video/OutputSurface:<init>	(III)V
    //   3179: astore 39
    //   3181: goto -1142 -> 2039
    //   3184: aload 48
    //   3186: astore 40
    //   3188: aload 41
    //   3190: astore 42
    //   3192: aload 38
    //   3194: astore 43
    //   3196: aload 39
    //   3198: astore 44
    //   3200: aload 48
    //   3202: iload 15
    //   3204: invokevirtual 1411	android/media/MediaCodec:getInputBuffer	(I)Ljava/nio/ByteBuffer;
    //   3207: astore 45
    //   3209: goto -787 -> 2422
    //   3212: aload 48
    //   3214: astore 40
    //   3216: aload 41
    //   3218: astore 42
    //   3220: aload 38
    //   3222: astore 43
    //   3224: aload 39
    //   3226: astore 44
    //   3228: aload 48
    //   3230: iload 15
    //   3232: iconst_0
    //   3233: iload 8
    //   3235: aload 46
    //   3237: invokevirtual 1414	android/media/MediaExtractor:getSampleTime	()J
    //   3240: iconst_0
    //   3241: invokevirtual 1390	android/media/MediaCodec:queueInputBuffer	(IIIJI)V
    //   3244: aload 48
    //   3246: astore 40
    //   3248: aload 41
    //   3250: astore 42
    //   3252: aload 38
    //   3254: astore 43
    //   3256: aload 39
    //   3258: astore 44
    //   3260: aload 46
    //   3262: invokevirtual 1417	android/media/MediaExtractor:advance	()Z
    //   3265: pop
    //   3266: iload_3
    //   3267: istore 9
    //   3269: iload_2
    //   3270: istore 8
    //   3272: goto -786 -> 2486
    //   3275: iload 10
    //   3277: iconst_m1
    //   3278: if_icmpeq +2978 -> 6256
    //   3281: iload 15
    //   3283: iload 10
    //   3285: if_icmpne +2971 -> 6256
    //   3288: aload 48
    //   3290: astore 40
    //   3292: aload 41
    //   3294: astore 42
    //   3296: aload 38
    //   3298: astore 43
    //   3300: aload 39
    //   3302: astore 44
    //   3304: aload 57
    //   3306: aload 46
    //   3308: aload 47
    //   3310: iconst_0
    //   3311: invokevirtual 1386	android/media/MediaExtractor:readSampleData	(Ljava/nio/ByteBuffer;I)I
    //   3314: putfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   3317: aload 48
    //   3319: astore 40
    //   3321: aload 41
    //   3323: astore 42
    //   3325: aload 38
    //   3327: astore 43
    //   3329: aload 39
    //   3331: astore 44
    //   3333: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   3336: bipush 21
    //   3338: if_icmpge +53 -> 3391
    //   3341: aload 48
    //   3343: astore 40
    //   3345: aload 41
    //   3347: astore 42
    //   3349: aload 38
    //   3351: astore 43
    //   3353: aload 39
    //   3355: astore 44
    //   3357: aload 47
    //   3359: iconst_0
    //   3360: invokevirtual 1423	java/nio/ByteBuffer:position	(I)Ljava/nio/Buffer;
    //   3363: pop
    //   3364: aload 48
    //   3366: astore 40
    //   3368: aload 41
    //   3370: astore 42
    //   3372: aload 38
    //   3374: astore 43
    //   3376: aload 39
    //   3378: astore 44
    //   3380: aload 47
    //   3382: aload 57
    //   3384: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   3387: invokevirtual 1426	java/nio/ByteBuffer:limit	(I)Ljava/nio/Buffer;
    //   3390: pop
    //   3391: aload 48
    //   3393: astore 40
    //   3395: aload 41
    //   3397: astore 42
    //   3399: aload 38
    //   3401: astore 43
    //   3403: aload 39
    //   3405: astore 44
    //   3407: aload 57
    //   3409: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   3412: iflt +240 -> 3652
    //   3415: aload 48
    //   3417: astore 40
    //   3419: aload 41
    //   3421: astore 42
    //   3423: aload 38
    //   3425: astore 43
    //   3427: aload 39
    //   3429: astore 44
    //   3431: aload 57
    //   3433: aload 46
    //   3435: invokevirtual 1414	android/media/MediaExtractor:getSampleTime	()J
    //   3438: putfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   3441: aload 48
    //   3443: astore 40
    //   3445: aload 41
    //   3447: astore 42
    //   3449: aload 38
    //   3451: astore 43
    //   3453: aload 39
    //   3455: astore 44
    //   3457: aload 46
    //   3459: invokevirtual 1417	android/media/MediaExtractor:advance	()Z
    //   3462: pop
    //   3463: aload 48
    //   3465: astore 40
    //   3467: aload 41
    //   3469: astore 42
    //   3471: aload 38
    //   3473: astore 43
    //   3475: aload 39
    //   3477: astore 44
    //   3479: iload_3
    //   3480: istore 9
    //   3482: iload_2
    //   3483: istore 8
    //   3485: aload 57
    //   3487: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   3490: ifle -1004 -> 2486
    //   3493: lload 30
    //   3495: lconst_0
    //   3496: lcmp
    //   3497: iflt +36 -> 3533
    //   3500: aload 48
    //   3502: astore 40
    //   3504: aload 41
    //   3506: astore 42
    //   3508: aload 38
    //   3510: astore 43
    //   3512: aload 39
    //   3514: astore 44
    //   3516: iload_3
    //   3517: istore 9
    //   3519: iload_2
    //   3520: istore 8
    //   3522: aload 57
    //   3524: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   3527: lload 30
    //   3529: lcmp
    //   3530: ifge -1044 -> 2486
    //   3533: aload 48
    //   3535: astore 40
    //   3537: aload 41
    //   3539: astore 42
    //   3541: aload 38
    //   3543: astore 43
    //   3545: aload 39
    //   3547: astore 44
    //   3549: aload 57
    //   3551: iconst_0
    //   3552: putfield 1432	android/media/MediaCodec$BufferInfo:offset	I
    //   3555: aload 48
    //   3557: astore 40
    //   3559: aload 41
    //   3561: astore 42
    //   3563: aload 38
    //   3565: astore 43
    //   3567: aload 39
    //   3569: astore 44
    //   3571: aload 57
    //   3573: aload 46
    //   3575: invokevirtual 1435	android/media/MediaExtractor:getSampleFlags	()I
    //   3578: putfield 1438	android/media/MediaCodec$BufferInfo:flags	I
    //   3581: aload 48
    //   3583: astore 40
    //   3585: aload 41
    //   3587: astore 42
    //   3589: aload 38
    //   3591: astore 43
    //   3593: aload 39
    //   3595: astore 44
    //   3597: iload_3
    //   3598: istore 9
    //   3600: iload_2
    //   3601: istore 8
    //   3603: aload 37
    //   3605: iload 13
    //   3607: aload 47
    //   3609: aload 57
    //   3611: iconst_0
    //   3612: invokevirtual 1442	org/telegram/messenger/video/MP4Builder:writeSampleData	(ILjava/nio/ByteBuffer;Landroid/media/MediaCodec$BufferInfo;Z)Z
    //   3615: ifeq -1129 -> 2486
    //   3618: aload 48
    //   3620: astore 40
    //   3622: aload 41
    //   3624: astore 42
    //   3626: aload 38
    //   3628: astore 43
    //   3630: aload 39
    //   3632: astore 44
    //   3634: aload_0
    //   3635: aload_1
    //   3636: aload 55
    //   3638: iconst_0
    //   3639: iconst_0
    //   3640: invokespecial 1152	org/telegram/messenger/MediaController:didWriteData	(Lorg/telegram/messenger/MessageObject;Ljava/io/File;ZZ)V
    //   3643: iload_3
    //   3644: istore 9
    //   3646: iload_2
    //   3647: istore 8
    //   3649: goto -1163 -> 2486
    //   3652: aload 48
    //   3654: astore 40
    //   3656: aload 41
    //   3658: astore 42
    //   3660: aload 38
    //   3662: astore 43
    //   3664: aload 39
    //   3666: astore 44
    //   3668: aload 57
    //   3670: iconst_0
    //   3671: putfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   3674: iconst_1
    //   3675: istore_2
    //   3676: goto -213 -> 3463
    //   3679: iload 18
    //   3681: bipush -3
    //   3683: if_icmpne +79 -> 3762
    //   3686: aload 48
    //   3688: astore 40
    //   3690: aload 41
    //   3692: astore 42
    //   3694: aload 38
    //   3696: astore 43
    //   3698: aload 39
    //   3700: astore 44
    //   3702: iload 17
    //   3704: istore 14
    //   3706: aload 45
    //   3708: astore 51
    //   3710: iload 19
    //   3712: istore 16
    //   3714: iload 9
    //   3716: istore_2
    //   3717: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   3720: bipush 21
    //   3722: if_icmpge -1097 -> 2625
    //   3725: aload 48
    //   3727: astore 40
    //   3729: aload 41
    //   3731: astore 42
    //   3733: aload 38
    //   3735: astore 43
    //   3737: aload 39
    //   3739: astore 44
    //   3741: aload 41
    //   3743: invokevirtual 1374	android/media/MediaCodec:getOutputBuffers	()[Ljava/nio/ByteBuffer;
    //   3746: astore 51
    //   3748: iload 17
    //   3750: istore 14
    //   3752: iload 19
    //   3754: istore 16
    //   3756: iload 9
    //   3758: istore_2
    //   3759: goto -1134 -> 2625
    //   3762: iload 18
    //   3764: bipush -2
    //   3766: if_icmpne +88 -> 3854
    //   3769: aload 48
    //   3771: astore 40
    //   3773: aload 41
    //   3775: astore 42
    //   3777: aload 38
    //   3779: astore 43
    //   3781: aload 39
    //   3783: astore 44
    //   3785: aload 41
    //   3787: invokevirtual 1446	android/media/MediaCodec:getOutputFormat	()Landroid/media/MediaFormat;
    //   3790: astore 52
    //   3792: iload 17
    //   3794: istore 14
    //   3796: aload 45
    //   3798: astore 51
    //   3800: iload 19
    //   3802: istore 16
    //   3804: iload 9
    //   3806: istore_2
    //   3807: iload 9
    //   3809: bipush -5
    //   3811: if_icmpne -1186 -> 2625
    //   3814: aload 48
    //   3816: astore 40
    //   3818: aload 41
    //   3820: astore 42
    //   3822: aload 38
    //   3824: astore 43
    //   3826: aload 39
    //   3828: astore 44
    //   3830: aload 37
    //   3832: aload 52
    //   3834: iconst_0
    //   3835: invokevirtual 1310	org/telegram/messenger/video/MP4Builder:addTrack	(Landroid/media/MediaFormat;Z)I
    //   3838: istore_2
    //   3839: iload 17
    //   3841: istore 14
    //   3843: aload 45
    //   3845: astore 51
    //   3847: iload 19
    //   3849: istore 16
    //   3851: goto -1226 -> 2625
    //   3854: iload 18
    //   3856: ifge +48 -> 3904
    //   3859: aload 48
    //   3861: astore 40
    //   3863: aload 41
    //   3865: astore 42
    //   3867: aload 38
    //   3869: astore 43
    //   3871: aload 39
    //   3873: astore 44
    //   3875: new 940	java/lang/RuntimeException
    //   3878: dup
    //   3879: new 1245	java/lang/StringBuilder
    //   3882: dup
    //   3883: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   3886: ldc_w 1448
    //   3889: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   3892: iload 18
    //   3894: invokevirtual 1292	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   3897: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   3900: invokespecial 943	java/lang/RuntimeException:<init>	(Ljava/lang/String;)V
    //   3903: athrow
    //   3904: aload 48
    //   3906: astore 40
    //   3908: aload 41
    //   3910: astore 42
    //   3912: aload 38
    //   3914: astore 43
    //   3916: aload 39
    //   3918: astore 44
    //   3920: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   3923: bipush 21
    //   3925: if_icmpge +66 -> 3991
    //   3928: aload 45
    //   3930: iload 18
    //   3932: aaload
    //   3933: astore 51
    //   3935: aload 51
    //   3937: ifnonnull +82 -> 4019
    //   3940: aload 48
    //   3942: astore 40
    //   3944: aload 41
    //   3946: astore 42
    //   3948: aload 38
    //   3950: astore 43
    //   3952: aload 39
    //   3954: astore 44
    //   3956: new 940	java/lang/RuntimeException
    //   3959: dup
    //   3960: new 1245	java/lang/StringBuilder
    //   3963: dup
    //   3964: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   3967: ldc_w 1450
    //   3970: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   3973: iload 18
    //   3975: invokevirtual 1292	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   3978: ldc_w 1452
    //   3981: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   3984: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   3987: invokespecial 943	java/lang/RuntimeException:<init>	(Ljava/lang/String;)V
    //   3990: athrow
    //   3991: aload 48
    //   3993: astore 40
    //   3995: aload 41
    //   3997: astore 42
    //   3999: aload 38
    //   4001: astore 43
    //   4003: aload 39
    //   4005: astore 44
    //   4007: aload 41
    //   4009: iload 18
    //   4011: invokevirtual 1455	android/media/MediaCodec:getOutputBuffer	(I)Ljava/nio/ByteBuffer;
    //   4014: astore 51
    //   4016: goto -81 -> 3935
    //   4019: aload 48
    //   4021: astore 40
    //   4023: aload 41
    //   4025: astore 42
    //   4027: aload 38
    //   4029: astore 43
    //   4031: aload 39
    //   4033: astore 44
    //   4035: iload 9
    //   4037: istore_2
    //   4038: aload 57
    //   4040: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   4043: iconst_1
    //   4044: if_icmple +91 -> 4135
    //   4047: aload 48
    //   4049: astore 40
    //   4051: aload 41
    //   4053: astore 42
    //   4055: aload 38
    //   4057: astore 43
    //   4059: aload 39
    //   4061: astore 44
    //   4063: aload 57
    //   4065: getfield 1438	android/media/MediaCodec$BufferInfo:flags	I
    //   4068: iconst_2
    //   4069: iand
    //   4070: ifne +133 -> 4203
    //   4073: aload 48
    //   4075: astore 40
    //   4077: aload 41
    //   4079: astore 42
    //   4081: aload 38
    //   4083: astore 43
    //   4085: aload 39
    //   4087: astore 44
    //   4089: iload 9
    //   4091: istore_2
    //   4092: aload 37
    //   4094: iload 9
    //   4096: aload 51
    //   4098: aload 57
    //   4100: iconst_1
    //   4101: invokevirtual 1442	org/telegram/messenger/video/MP4Builder:writeSampleData	(ILjava/nio/ByteBuffer;Landroid/media/MediaCodec$BufferInfo;Z)Z
    //   4104: ifeq +31 -> 4135
    //   4107: aload 48
    //   4109: astore 40
    //   4111: aload 41
    //   4113: astore 42
    //   4115: aload 38
    //   4117: astore 43
    //   4119: aload 39
    //   4121: astore 44
    //   4123: aload_0
    //   4124: aload_1
    //   4125: aload 55
    //   4127: iconst_0
    //   4128: iconst_0
    //   4129: invokespecial 1152	org/telegram/messenger/MediaController:didWriteData	(Lorg/telegram/messenger/MessageObject;Ljava/io/File;ZZ)V
    //   4132: iload 9
    //   4134: istore_2
    //   4135: aload 48
    //   4137: astore 40
    //   4139: aload 41
    //   4141: astore 42
    //   4143: aload 38
    //   4145: astore 43
    //   4147: aload 39
    //   4149: astore 44
    //   4151: aload 57
    //   4153: getfield 1438	android/media/MediaCodec$BufferInfo:flags	I
    //   4156: iconst_4
    //   4157: iand
    //   4158: ifeq +2131 -> 6289
    //   4161: iconst_1
    //   4162: istore 9
    //   4164: aload 48
    //   4166: astore 40
    //   4168: aload 41
    //   4170: astore 42
    //   4172: aload 38
    //   4174: astore 43
    //   4176: aload 39
    //   4178: astore 44
    //   4180: aload 41
    //   4182: iload 18
    //   4184: iconst_0
    //   4185: invokevirtual 1459	android/media/MediaCodec:releaseOutputBuffer	(IZ)V
    //   4188: iload 17
    //   4190: istore 14
    //   4192: aload 45
    //   4194: astore 51
    //   4196: iload 9
    //   4198: istore 16
    //   4200: goto -1575 -> 2625
    //   4203: iload 9
    //   4205: istore_2
    //   4206: iload 9
    //   4208: bipush -5
    //   4210: if_icmpne -75 -> 4135
    //   4213: aload 48
    //   4215: astore 40
    //   4217: aload 41
    //   4219: astore 42
    //   4221: aload 38
    //   4223: astore 43
    //   4225: aload 39
    //   4227: astore 44
    //   4229: aload 57
    //   4231: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   4234: newarray <illegal type>
    //   4236: astore 58
    //   4238: aload 48
    //   4240: astore 40
    //   4242: aload 41
    //   4244: astore 42
    //   4246: aload 38
    //   4248: astore 43
    //   4250: aload 39
    //   4252: astore 44
    //   4254: aload 51
    //   4256: aload 57
    //   4258: getfield 1432	android/media/MediaCodec$BufferInfo:offset	I
    //   4261: aload 57
    //   4263: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   4266: iadd
    //   4267: invokevirtual 1426	java/nio/ByteBuffer:limit	(I)Ljava/nio/Buffer;
    //   4270: pop
    //   4271: aload 48
    //   4273: astore 40
    //   4275: aload 41
    //   4277: astore 42
    //   4279: aload 38
    //   4281: astore 43
    //   4283: aload 39
    //   4285: astore 44
    //   4287: aload 51
    //   4289: aload 57
    //   4291: getfield 1432	android/media/MediaCodec$BufferInfo:offset	I
    //   4294: invokevirtual 1423	java/nio/ByteBuffer:position	(I)Ljava/nio/Buffer;
    //   4297: pop
    //   4298: aload 48
    //   4300: astore 40
    //   4302: aload 41
    //   4304: astore 42
    //   4306: aload 38
    //   4308: astore 43
    //   4310: aload 39
    //   4312: astore 44
    //   4314: aload 51
    //   4316: aload 58
    //   4318: invokevirtual 1462	java/nio/ByteBuffer:get	([B)Ljava/nio/ByteBuffer;
    //   4321: pop
    //   4322: aconst_null
    //   4323: astore 53
    //   4325: aconst_null
    //   4326: astore 54
    //   4328: aload 48
    //   4330: astore 40
    //   4332: aload 41
    //   4334: astore 42
    //   4336: aload 38
    //   4338: astore 43
    //   4340: aload 39
    //   4342: astore 44
    //   4344: aload 57
    //   4346: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   4349: iconst_1
    //   4350: isub
    //   4351: istore_2
    //   4352: aload 54
    //   4354: astore 52
    //   4356: aload 53
    //   4358: astore 51
    //   4360: iload_2
    //   4361: iflt +177 -> 4538
    //   4364: aload 54
    //   4366: astore 52
    //   4368: aload 53
    //   4370: astore 51
    //   4372: iload_2
    //   4373: iconst_3
    //   4374: if_icmple +164 -> 4538
    //   4377: aload 58
    //   4379: iload_2
    //   4380: baload
    //   4381: iconst_1
    //   4382: if_icmpne +1900 -> 6282
    //   4385: aload 58
    //   4387: iload_2
    //   4388: iconst_1
    //   4389: isub
    //   4390: baload
    //   4391: ifne +1891 -> 6282
    //   4394: aload 58
    //   4396: iload_2
    //   4397: iconst_2
    //   4398: isub
    //   4399: baload
    //   4400: ifne +1882 -> 6282
    //   4403: aload 58
    //   4405: iload_2
    //   4406: iconst_3
    //   4407: isub
    //   4408: baload
    //   4409: ifne +1873 -> 6282
    //   4412: aload 48
    //   4414: astore 40
    //   4416: aload 41
    //   4418: astore 42
    //   4420: aload 38
    //   4422: astore 43
    //   4424: aload 39
    //   4426: astore 44
    //   4428: iload_2
    //   4429: iconst_3
    //   4430: isub
    //   4431: invokestatic 1465	java/nio/ByteBuffer:allocate	(I)Ljava/nio/ByteBuffer;
    //   4434: astore 51
    //   4436: aload 48
    //   4438: astore 40
    //   4440: aload 41
    //   4442: astore 42
    //   4444: aload 38
    //   4446: astore 43
    //   4448: aload 39
    //   4450: astore 44
    //   4452: aload 57
    //   4454: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   4457: iload_2
    //   4458: iconst_3
    //   4459: isub
    //   4460: isub
    //   4461: invokestatic 1465	java/nio/ByteBuffer:allocate	(I)Ljava/nio/ByteBuffer;
    //   4464: astore 52
    //   4466: aload 48
    //   4468: astore 40
    //   4470: aload 41
    //   4472: astore 42
    //   4474: aload 38
    //   4476: astore 43
    //   4478: aload 39
    //   4480: astore 44
    //   4482: aload 51
    //   4484: aload 58
    //   4486: iconst_0
    //   4487: iload_2
    //   4488: iconst_3
    //   4489: isub
    //   4490: invokevirtual 1469	java/nio/ByteBuffer:put	([BII)Ljava/nio/ByteBuffer;
    //   4493: iconst_0
    //   4494: invokevirtual 1423	java/nio/ByteBuffer:position	(I)Ljava/nio/Buffer;
    //   4497: pop
    //   4498: aload 48
    //   4500: astore 40
    //   4502: aload 41
    //   4504: astore 42
    //   4506: aload 38
    //   4508: astore 43
    //   4510: aload 39
    //   4512: astore 44
    //   4514: aload 52
    //   4516: aload 58
    //   4518: iload_2
    //   4519: iconst_3
    //   4520: isub
    //   4521: aload 57
    //   4523: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   4526: iload_2
    //   4527: iconst_3
    //   4528: isub
    //   4529: isub
    //   4530: invokevirtual 1469	java/nio/ByteBuffer:put	([BII)Ljava/nio/ByteBuffer;
    //   4533: iconst_0
    //   4534: invokevirtual 1423	java/nio/ByteBuffer:position	(I)Ljava/nio/Buffer;
    //   4537: pop
    //   4538: aload 48
    //   4540: astore 40
    //   4542: aload 41
    //   4544: astore 42
    //   4546: aload 38
    //   4548: astore 43
    //   4550: aload 39
    //   4552: astore 44
    //   4554: ldc -88
    //   4556: iload 5
    //   4558: iload 4
    //   4560: invokestatic 1318	android/media/MediaFormat:createVideoFormat	(Ljava/lang/String;II)Landroid/media/MediaFormat;
    //   4563: astore 53
    //   4565: aload 51
    //   4567: ifnull +60 -> 4627
    //   4570: aload 52
    //   4572: ifnull +55 -> 4627
    //   4575: aload 48
    //   4577: astore 40
    //   4579: aload 41
    //   4581: astore 42
    //   4583: aload 38
    //   4585: astore 43
    //   4587: aload 39
    //   4589: astore 44
    //   4591: aload 53
    //   4593: ldc_w 1471
    //   4596: aload 51
    //   4598: invokevirtual 1475	android/media/MediaFormat:setByteBuffer	(Ljava/lang/String;Ljava/nio/ByteBuffer;)V
    //   4601: aload 48
    //   4603: astore 40
    //   4605: aload 41
    //   4607: astore 42
    //   4609: aload 38
    //   4611: astore 43
    //   4613: aload 39
    //   4615: astore 44
    //   4617: aload 53
    //   4619: ldc_w 1477
    //   4622: aload 52
    //   4624: invokevirtual 1475	android/media/MediaFormat:setByteBuffer	(Ljava/lang/String;Ljava/nio/ByteBuffer;)V
    //   4627: aload 48
    //   4629: astore 40
    //   4631: aload 41
    //   4633: astore 42
    //   4635: aload 38
    //   4637: astore 43
    //   4639: aload 39
    //   4641: astore 44
    //   4643: aload 37
    //   4645: aload 53
    //   4647: iconst_0
    //   4648: invokevirtual 1310	org/telegram/messenger/video/MP4Builder:addTrack	(Landroid/media/MediaFormat;Z)I
    //   4651: istore_2
    //   4652: goto -517 -> 4135
    //   4655: iload 14
    //   4657: istore 17
    //   4659: aload 51
    //   4661: astore 45
    //   4663: iload 16
    //   4665: istore 19
    //   4667: iload_2
    //   4668: istore 9
    //   4670: iload 21
    //   4672: bipush -3
    //   4674: if_icmpeq +1536 -> 6210
    //   4677: iload 21
    //   4679: bipush -2
    //   4681: if_icmpne +121 -> 4802
    //   4684: aload 48
    //   4686: astore 40
    //   4688: aload 41
    //   4690: astore 42
    //   4692: aload 38
    //   4694: astore 43
    //   4696: aload 39
    //   4698: astore 44
    //   4700: aload 48
    //   4702: invokevirtual 1446	android/media/MediaCodec:getOutputFormat	()Landroid/media/MediaFormat;
    //   4705: astore 52
    //   4707: aload 48
    //   4709: astore 40
    //   4711: aload 41
    //   4713: astore 42
    //   4715: aload 38
    //   4717: astore 43
    //   4719: aload 39
    //   4721: astore 44
    //   4723: iload 14
    //   4725: istore 17
    //   4727: aload 51
    //   4729: astore 45
    //   4731: iload 16
    //   4733: istore 19
    //   4735: iload_2
    //   4736: istore 9
    //   4738: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   4741: ifeq +1469 -> 6210
    //   4744: aload 48
    //   4746: astore 40
    //   4748: aload 41
    //   4750: astore 42
    //   4752: aload 38
    //   4754: astore 43
    //   4756: aload 39
    //   4758: astore 44
    //   4760: new 1245	java/lang/StringBuilder
    //   4763: dup
    //   4764: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   4767: ldc_w 1479
    //   4770: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   4773: aload 52
    //   4775: invokevirtual 1482	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   4778: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   4781: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   4784: iload 14
    //   4786: istore 17
    //   4788: aload 51
    //   4790: astore 45
    //   4792: iload 16
    //   4794: istore 19
    //   4796: iload_2
    //   4797: istore 9
    //   4799: goto +1411 -> 6210
    //   4802: iload 21
    //   4804: ifge +48 -> 4852
    //   4807: aload 48
    //   4809: astore 40
    //   4811: aload 41
    //   4813: astore 42
    //   4815: aload 38
    //   4817: astore 43
    //   4819: aload 39
    //   4821: astore 44
    //   4823: new 940	java/lang/RuntimeException
    //   4826: dup
    //   4827: new 1245	java/lang/StringBuilder
    //   4830: dup
    //   4831: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   4834: ldc_w 1484
    //   4837: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   4840: iload 21
    //   4842: invokevirtual 1292	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   4845: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   4848: invokespecial 943	java/lang/RuntimeException:<init>	(Ljava/lang/String;)V
    //   4851: athrow
    //   4852: aload 48
    //   4854: astore 40
    //   4856: aload 41
    //   4858: astore 42
    //   4860: aload 38
    //   4862: astore 43
    //   4864: aload 39
    //   4866: astore 44
    //   4868: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   4871: bipush 18
    //   4873: if_icmplt +598 -> 5471
    //   4876: aload 48
    //   4878: astore 40
    //   4880: aload 41
    //   4882: astore 42
    //   4884: aload 38
    //   4886: astore 43
    //   4888: aload 39
    //   4890: astore 44
    //   4892: aload 57
    //   4894: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   4897: ifeq +1398 -> 6295
    //   4900: iconst_1
    //   4901: istore 34
    //   4903: iload 15
    //   4905: istore 20
    //   4907: iload 34
    //   4909: istore 35
    //   4911: iload 8
    //   4913: istore 18
    //   4915: lload 30
    //   4917: lconst_0
    //   4918: lcmp
    //   4919: ifle +79 -> 4998
    //   4922: aload 48
    //   4924: astore 40
    //   4926: aload 41
    //   4928: astore 42
    //   4930: aload 38
    //   4932: astore 43
    //   4934: aload 39
    //   4936: astore 44
    //   4938: iload 15
    //   4940: istore 20
    //   4942: iload 34
    //   4944: istore 35
    //   4946: iload 8
    //   4948: istore 18
    //   4950: aload 57
    //   4952: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   4955: lload 30
    //   4957: lcmp
    //   4958: iflt +40 -> 4998
    //   4961: iconst_1
    //   4962: istore 18
    //   4964: iconst_1
    //   4965: istore 20
    //   4967: iconst_0
    //   4968: istore 35
    //   4970: aload 48
    //   4972: astore 40
    //   4974: aload 41
    //   4976: astore 42
    //   4978: aload 38
    //   4980: astore 43
    //   4982: aload 39
    //   4984: astore 44
    //   4986: aload 57
    //   4988: aload 57
    //   4990: getfield 1438	android/media/MediaCodec$BufferInfo:flags	I
    //   4993: iconst_4
    //   4994: ior
    //   4995: putfield 1438	android/media/MediaCodec$BufferInfo:flags	I
    //   4998: iload 35
    //   5000: istore 34
    //   5002: lload 24
    //   5004: lstore 26
    //   5006: lload 28
    //   5008: lconst_0
    //   5009: lcmp
    //   5010: ifle +142 -> 5152
    //   5013: iload 35
    //   5015: istore 34
    //   5017: lload 24
    //   5019: lstore 26
    //   5021: lload 24
    //   5023: ldc2_w 1197
    //   5026: lcmp
    //   5027: ifne +125 -> 5152
    //   5030: aload 48
    //   5032: astore 40
    //   5034: aload 41
    //   5036: astore 42
    //   5038: aload 38
    //   5040: astore 43
    //   5042: aload 39
    //   5044: astore 44
    //   5046: aload 57
    //   5048: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   5051: lload 28
    //   5053: lcmp
    //   5054: ifge +470 -> 5524
    //   5057: iconst_0
    //   5058: istore 35
    //   5060: aload 48
    //   5062: astore 40
    //   5064: aload 41
    //   5066: astore 42
    //   5068: aload 38
    //   5070: astore 43
    //   5072: aload 39
    //   5074: astore 44
    //   5076: iload 35
    //   5078: istore 34
    //   5080: lload 24
    //   5082: lstore 26
    //   5084: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   5087: ifeq +65 -> 5152
    //   5090: aload 48
    //   5092: astore 40
    //   5094: aload 41
    //   5096: astore 42
    //   5098: aload 38
    //   5100: astore 43
    //   5102: aload 39
    //   5104: astore 44
    //   5106: new 1245	java/lang/StringBuilder
    //   5109: dup
    //   5110: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   5113: ldc_w 1486
    //   5116: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   5119: lload 28
    //   5121: invokevirtual 1255	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   5124: ldc_w 1488
    //   5127: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   5130: aload 57
    //   5132: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   5135: invokevirtual 1255	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   5138: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   5141: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   5144: lload 24
    //   5146: lstore 26
    //   5148: iload 35
    //   5150: istore 34
    //   5152: aload 48
    //   5154: astore 40
    //   5156: aload 41
    //   5158: astore 42
    //   5160: aload 38
    //   5162: astore 43
    //   5164: aload 39
    //   5166: astore 44
    //   5168: aload 48
    //   5170: iload 21
    //   5172: iload 34
    //   5174: invokevirtual 1459	android/media/MediaCodec:releaseOutputBuffer	(IZ)V
    //   5177: iload 34
    //   5179: ifeq +114 -> 5293
    //   5182: iconst_0
    //   5183: istore 8
    //   5185: aload 39
    //   5187: invokevirtual 1491	org/telegram/messenger/video/OutputSurface:awaitNewImage	()V
    //   5190: iload 8
    //   5192: ifne +101 -> 5293
    //   5195: aload 48
    //   5197: astore 40
    //   5199: aload 41
    //   5201: astore 42
    //   5203: aload 38
    //   5205: astore 43
    //   5207: aload 39
    //   5209: astore 44
    //   5211: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   5214: bipush 18
    //   5216: if_icmplt +367 -> 5583
    //   5219: aload 48
    //   5221: astore 40
    //   5223: aload 41
    //   5225: astore 42
    //   5227: aload 38
    //   5229: astore 43
    //   5231: aload 39
    //   5233: astore 44
    //   5235: aload 39
    //   5237: iconst_0
    //   5238: invokevirtual 1494	org/telegram/messenger/video/OutputSurface:drawImage	(Z)V
    //   5241: aload 48
    //   5243: astore 40
    //   5245: aload 41
    //   5247: astore 42
    //   5249: aload 38
    //   5251: astore 43
    //   5253: aload 39
    //   5255: astore 44
    //   5257: aload 38
    //   5259: aload 57
    //   5261: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   5264: ldc2_w 1495
    //   5267: lmul
    //   5268: invokevirtual 1500	org/telegram/messenger/video/InputSurface:setPresentationTime	(J)V
    //   5271: aload 48
    //   5273: astore 40
    //   5275: aload 41
    //   5277: astore 42
    //   5279: aload 38
    //   5281: astore 43
    //   5283: aload 39
    //   5285: astore 44
    //   5287: aload 38
    //   5289: invokevirtual 1503	org/telegram/messenger/video/InputSurface:swapBuffers	()Z
    //   5292: pop
    //   5293: aload 48
    //   5295: astore 40
    //   5297: aload 41
    //   5299: astore 42
    //   5301: aload 38
    //   5303: astore 43
    //   5305: aload 39
    //   5307: astore 44
    //   5309: iload 20
    //   5311: istore 15
    //   5313: iload 14
    //   5315: istore 17
    //   5317: aload 51
    //   5319: astore 45
    //   5321: iload 18
    //   5323: istore 8
    //   5325: iload 16
    //   5327: istore 19
    //   5329: lload 26
    //   5331: lstore 24
    //   5333: iload_2
    //   5334: istore 9
    //   5336: aload 57
    //   5338: getfield 1438	android/media/MediaCodec$BufferInfo:flags	I
    //   5341: iconst_4
    //   5342: iand
    //   5343: ifeq +867 -> 6210
    //   5346: iconst_0
    //   5347: istore 21
    //   5349: aload 48
    //   5351: astore 40
    //   5353: aload 41
    //   5355: astore 42
    //   5357: aload 38
    //   5359: astore 43
    //   5361: aload 39
    //   5363: astore 44
    //   5365: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   5368: ifeq +25 -> 5393
    //   5371: aload 48
    //   5373: astore 40
    //   5375: aload 41
    //   5377: astore 42
    //   5379: aload 38
    //   5381: astore 43
    //   5383: aload 39
    //   5385: astore 44
    //   5387: ldc_w 1505
    //   5390: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   5393: aload 48
    //   5395: astore 40
    //   5397: aload 41
    //   5399: astore 42
    //   5401: aload 38
    //   5403: astore 43
    //   5405: aload 39
    //   5407: astore 44
    //   5409: getstatic 950	android/os/Build$VERSION:SDK_INT	I
    //   5412: bipush 18
    //   5414: if_icmplt +390 -> 5804
    //   5417: aload 48
    //   5419: astore 40
    //   5421: aload 41
    //   5423: astore 42
    //   5425: aload 38
    //   5427: astore 43
    //   5429: aload 39
    //   5431: astore 44
    //   5433: aload 41
    //   5435: invokevirtual 1508	android/media/MediaCodec:signalEndOfInputStream	()V
    //   5438: iload 20
    //   5440: istore 15
    //   5442: iload 21
    //   5444: istore_3
    //   5445: iload 14
    //   5447: istore 17
    //   5449: aload 51
    //   5451: astore 45
    //   5453: iload 18
    //   5455: istore 8
    //   5457: iload 16
    //   5459: istore 19
    //   5461: lload 26
    //   5463: lstore 24
    //   5465: iload_2
    //   5466: istore 9
    //   5468: goto +742 -> 6210
    //   5471: aload 48
    //   5473: astore 40
    //   5475: aload 41
    //   5477: astore 42
    //   5479: aload 38
    //   5481: astore 43
    //   5483: aload 39
    //   5485: astore 44
    //   5487: aload 57
    //   5489: getfield 1419	android/media/MediaCodec$BufferInfo:size	I
    //   5492: ifne +809 -> 6301
    //   5495: aload 48
    //   5497: astore 40
    //   5499: aload 41
    //   5501: astore 42
    //   5503: aload 38
    //   5505: astore 43
    //   5507: aload 39
    //   5509: astore 44
    //   5511: aload 57
    //   5513: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   5516: lconst_0
    //   5517: lcmp
    //   5518: ifeq +789 -> 6307
    //   5521: goto +780 -> 6301
    //   5524: aload 48
    //   5526: astore 40
    //   5528: aload 41
    //   5530: astore 42
    //   5532: aload 38
    //   5534: astore 43
    //   5536: aload 39
    //   5538: astore 44
    //   5540: aload 57
    //   5542: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   5545: lstore 26
    //   5547: iload 35
    //   5549: istore 34
    //   5551: goto -399 -> 5152
    //   5554: astore 45
    //   5556: iconst_1
    //   5557: istore 8
    //   5559: aload 48
    //   5561: astore 40
    //   5563: aload 41
    //   5565: astore 42
    //   5567: aload 38
    //   5569: astore 43
    //   5571: aload 39
    //   5573: astore 44
    //   5575: aload 45
    //   5577: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   5580: goto -390 -> 5190
    //   5583: aload 48
    //   5585: astore 40
    //   5587: aload 41
    //   5589: astore 42
    //   5591: aload 38
    //   5593: astore 43
    //   5595: aload 39
    //   5597: astore 44
    //   5599: aload 41
    //   5601: ldc2_w 1378
    //   5604: invokevirtual 1383	android/media/MediaCodec:dequeueInputBuffer	(J)I
    //   5607: istore 8
    //   5609: iload 8
    //   5611: iflt +146 -> 5757
    //   5614: aload 48
    //   5616: astore 40
    //   5618: aload 41
    //   5620: astore 42
    //   5622: aload 38
    //   5624: astore 43
    //   5626: aload 39
    //   5628: astore 44
    //   5630: aload 39
    //   5632: iconst_1
    //   5633: invokevirtual 1494	org/telegram/messenger/video/OutputSurface:drawImage	(Z)V
    //   5636: aload 48
    //   5638: astore 40
    //   5640: aload 41
    //   5642: astore 42
    //   5644: aload 38
    //   5646: astore 43
    //   5648: aload 39
    //   5650: astore 44
    //   5652: aload 39
    //   5654: invokevirtual 1512	org/telegram/messenger/video/OutputSurface:getFrame	()Ljava/nio/ByteBuffer;
    //   5657: astore 45
    //   5659: aload 50
    //   5661: iload 8
    //   5663: aaload
    //   5664: astore 52
    //   5666: aload 48
    //   5668: astore 40
    //   5670: aload 41
    //   5672: astore 42
    //   5674: aload 38
    //   5676: astore 43
    //   5678: aload 39
    //   5680: astore 44
    //   5682: aload 52
    //   5684: invokevirtual 1515	java/nio/ByteBuffer:clear	()Ljava/nio/Buffer;
    //   5687: pop
    //   5688: aload 48
    //   5690: astore 40
    //   5692: aload 41
    //   5694: astore 42
    //   5696: aload 38
    //   5698: astore 43
    //   5700: aload 39
    //   5702: astore 44
    //   5704: aload 45
    //   5706: aload 52
    //   5708: iload 11
    //   5710: iload 5
    //   5712: iload 4
    //   5714: iload 7
    //   5716: iload 12
    //   5718: invokestatic 1519	org/telegram/messenger/Utilities:convertVideoFrame	(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;IIIII)I
    //   5721: pop
    //   5722: aload 48
    //   5724: astore 40
    //   5726: aload 41
    //   5728: astore 42
    //   5730: aload 38
    //   5732: astore 43
    //   5734: aload 39
    //   5736: astore 44
    //   5738: aload 41
    //   5740: iload 8
    //   5742: iconst_0
    //   5743: iload 6
    //   5745: aload 57
    //   5747: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   5750: iconst_0
    //   5751: invokevirtual 1390	android/media/MediaCodec:queueInputBuffer	(IIIJI)V
    //   5754: goto -461 -> 5293
    //   5757: aload 48
    //   5759: astore 40
    //   5761: aload 41
    //   5763: astore 42
    //   5765: aload 38
    //   5767: astore 43
    //   5769: aload 39
    //   5771: astore 44
    //   5773: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   5776: ifeq -483 -> 5293
    //   5779: aload 48
    //   5781: astore 40
    //   5783: aload 41
    //   5785: astore 42
    //   5787: aload 38
    //   5789: astore 43
    //   5791: aload 39
    //   5793: astore 44
    //   5795: ldc_w 1521
    //   5798: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   5801: goto -508 -> 5293
    //   5804: aload 48
    //   5806: astore 40
    //   5808: aload 41
    //   5810: astore 42
    //   5812: aload 38
    //   5814: astore 43
    //   5816: aload 39
    //   5818: astore 44
    //   5820: aload 41
    //   5822: ldc2_w 1378
    //   5825: invokevirtual 1383	android/media/MediaCodec:dequeueInputBuffer	(J)I
    //   5828: istore 23
    //   5830: iload 20
    //   5832: istore 15
    //   5834: iload 21
    //   5836: istore_3
    //   5837: iload 14
    //   5839: istore 17
    //   5841: aload 51
    //   5843: astore 45
    //   5845: iload 18
    //   5847: istore 8
    //   5849: iload 16
    //   5851: istore 19
    //   5853: lload 26
    //   5855: lstore 24
    //   5857: iload_2
    //   5858: istore 9
    //   5860: iload 23
    //   5862: iflt +348 -> 6210
    //   5865: aload 48
    //   5867: astore 40
    //   5869: aload 41
    //   5871: astore 42
    //   5873: aload 38
    //   5875: astore 43
    //   5877: aload 39
    //   5879: astore 44
    //   5881: aload 41
    //   5883: iload 23
    //   5885: iconst_0
    //   5886: iconst_1
    //   5887: aload 57
    //   5889: getfield 1429	android/media/MediaCodec$BufferInfo:presentationTimeUs	J
    //   5892: iconst_4
    //   5893: invokevirtual 1390	android/media/MediaCodec:queueInputBuffer	(IIIJI)V
    //   5896: iload 20
    //   5898: istore 15
    //   5900: iload 21
    //   5902: istore_3
    //   5903: iload 14
    //   5905: istore 17
    //   5907: aload 51
    //   5909: astore 45
    //   5911: iload 18
    //   5913: istore 8
    //   5915: iload 16
    //   5917: istore 19
    //   5919: lload 26
    //   5921: lstore 24
    //   5923: iload_2
    //   5924: istore 9
    //   5926: goto +284 -> 6210
    //   5929: iload 14
    //   5931: iconst_m1
    //   5932: if_icmpeq +124 -> 6056
    //   5935: iconst_1
    //   5936: istore 34
    //   5938: aload_0
    //   5939: aload_1
    //   5940: aload 46
    //   5942: aload 37
    //   5944: aload 57
    //   5946: lload 28
    //   5948: lload 30
    //   5950: aload 55
    //   5952: iload 34
    //   5954: invokespecial 1525	org/telegram/messenger/MediaController:readAndWriteTracks	(Lorg/telegram/messenger/MessageObject;Landroid/media/MediaExtractor;Lorg/telegram/messenger/video/MP4Builder;Landroid/media/MediaCodec$BufferInfo;JJLjava/io/File;Z)J
    //   5957: pop2
    //   5958: iload 35
    //   5960: istore 34
    //   5962: goto -5052 -> 910
    //   5965: astore 41
    //   5967: aload 37
    //   5969: astore 40
    //   5971: aload 46
    //   5973: astore 37
    //   5975: iconst_1
    //   5976: istore 35
    //   5978: aload 37
    //   5980: astore 39
    //   5982: aload 40
    //   5984: astore 38
    //   5986: aload 41
    //   5988: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   5991: aload 37
    //   5993: ifnull +8 -> 6001
    //   5996: aload 37
    //   5998: invokevirtual 1235	android/media/MediaExtractor:release	()V
    //   6001: aload 40
    //   6003: ifnull +8 -> 6011
    //   6006: aload 40
    //   6008: invokevirtual 1238	org/telegram/messenger/video/MP4Builder:finishMovie	()V
    //   6011: iload 35
    //   6013: istore 34
    //   6015: getstatic 1243	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   6018: ifeq -5054 -> 964
    //   6021: new 1245	java/lang/StringBuilder
    //   6024: dup
    //   6025: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   6028: ldc_w 1248
    //   6031: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   6034: invokestatic 1157	java/lang/System:currentTimeMillis	()J
    //   6037: lload 32
    //   6039: lsub
    //   6040: invokevirtual 1255	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   6043: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   6046: invokestatic 1261	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   6049: iload 35
    //   6051: istore 34
    //   6053: goto -5089 -> 964
    //   6056: iconst_0
    //   6057: istore 34
    //   6059: goto -121 -> 5938
    //   6062: astore 37
    //   6064: aload 37
    //   6066: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   6069: goto -5139 -> 930
    //   6072: astore 37
    //   6074: aload 37
    //   6076: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   6079: goto -68 -> 6011
    //   6082: astore 37
    //   6084: aload 37
    //   6086: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   6089: goto -2987 -> 3102
    //   6092: aload 56
    //   6094: invokeinterface 1136 1 0
    //   6099: ldc_w 1126
    //   6102: iconst_1
    //   6103: invokeinterface 1142 3 0
    //   6108: invokeinterface 1145 1 0
    //   6113: pop
    //   6114: aload_0
    //   6115: aload_1
    //   6116: aload 55
    //   6118: iconst_1
    //   6119: iconst_1
    //   6120: invokespecial 1152	org/telegram/messenger/MediaController:didWriteData	(Lorg/telegram/messenger/MessageObject;Ljava/io/File;ZZ)V
    //   6123: iconst_0
    //   6124: ireturn
    //   6125: astore_1
    //   6126: aload 38
    //   6128: astore 37
    //   6130: goto -3048 -> 3082
    //   6133: astore 41
    //   6135: aload 42
    //   6137: astore 37
    //   6139: goto -164 -> 5975
    //   6142: astore 42
    //   6144: aload 38
    //   6146: astore 43
    //   6148: aload 48
    //   6150: astore 40
    //   6152: aload 42
    //   6154: astore 38
    //   6156: aload 39
    //   6158: astore 44
    //   6160: goto -5323 -> 837
    //   6163: goto -5199 -> 964
    //   6166: iconst_1
    //   6167: istore 6
    //   6169: iload 11
    //   6171: istore_2
    //   6172: goto -5020 -> 1152
    //   6175: iload 18
    //   6177: ifne +100 -> 6277
    //   6180: iconst_1
    //   6181: istore_2
    //   6182: iconst_1
    //   6183: istore 17
    //   6185: iload 14
    //   6187: istore 9
    //   6189: lload 26
    //   6191: lstore 24
    //   6193: iload 16
    //   6195: istore 19
    //   6197: iload_3
    //   6198: istore 8
    //   6200: aload 51
    //   6202: astore 45
    //   6204: iload_2
    //   6205: istore_3
    //   6206: iload 18
    //   6208: istore 15
    //   6210: iload_3
    //   6211: ifne -3654 -> 2557
    //   6214: iload 15
    //   6216: istore 18
    //   6218: aload 45
    //   6220: astore 51
    //   6222: iload 8
    //   6224: istore_2
    //   6225: iload 19
    //   6227: istore 16
    //   6229: lload 24
    //   6231: lstore 26
    //   6233: iload 9
    //   6235: istore 14
    //   6237: iload 17
    //   6239: ifeq -3968 -> 2271
    //   6242: goto -3685 -> 2557
    //   6245: ldc_w 1526
    //   6248: istore 11
    //   6250: iload_2
    //   6251: istore 8
    //   6253: goto -4991 -> 1262
    //   6256: iload_3
    //   6257: istore 9
    //   6259: iload_2
    //   6260: istore 8
    //   6262: iload 15
    //   6264: iconst_m1
    //   6265: if_icmpne -3779 -> 2486
    //   6268: iconst_1
    //   6269: istore 9
    //   6271: iload_2
    //   6272: istore 8
    //   6274: goto -3788 -> 2486
    //   6277: iconst_0
    //   6278: istore_2
    //   6279: goto -97 -> 6182
    //   6282: iload_2
    //   6283: iconst_1
    //   6284: isub
    //   6285: istore_2
    //   6286: goto -1934 -> 4352
    //   6289: iconst_0
    //   6290: istore 9
    //   6292: goto -2128 -> 4164
    //   6295: iconst_0
    //   6296: istore 34
    //   6298: goto -1395 -> 4903
    //   6301: iconst_1
    //   6302: istore 34
    //   6304: goto -1401 -> 4903
    //   6307: iconst_0
    //   6308: istore 34
    //   6310: goto -6 -> 6304
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	6313	0	this	MediaController
    //   0	6313	1	paramMessageObject	MessageObject
    //   159	6127	2	i	int
    //   163	6094	3	j	int
    //   151	5562	4	k	int
    //   155	5556	5	m	int
    //   43	6125	6	n	int
    //   34	5681	7	i1	int
    //   52	6221	8	i2	int
    //   79	6212	9	i3	int
    //   91	3195	10	i4	int
    //   61	6188	11	i5	int
    //   70	5647	12	i6	int
    //   696	2910	13	i7	int
    //   88	6148	14	i8	int
    //   679	5587	15	i9	int
    //   676	5552	16	i10	int
    //   692	5546	17	i11	int
    //   682	5535	18	i12	int
    //   1325	4901	19	i13	int
    //   4905	992	20	i14	int
    //   2692	3209	21	i15	int
    //   623	1729	22	i16	int
    //   5828	56	23	i17	int
    //   5002	143	24	localObject1	Object
    //   5331	899	24	l1	long
    //   673	5559	26	l2	long
    //   16	5931	28	l3	long
    //   25	5924	30	l4	long
    //   404	5634	32	l5	long
    //   102	6207	34	bool1	boolean
    //   205	5845	35	bool2	boolean
    //   399	1881	36	bool3	boolean
    //   185	5812	37	localObject2	Object
    //   6062	3	37	localException1	Exception
    //   6072	3	37	localException2	Exception
    //   6082	3	37	localException3	Exception
    //   6128	10	37	localObject3	Object
    //   434	226	38	localObject4	Object
    //   831	7	38	localException4	Exception
    //   1933	4222	38	localObject5	Object
    //   430	5727	39	localObject6	Object
    //   438	5713	40	localObject7	Object
    //   423	5459	41	localObject8	Object
    //   5965	22	41	localException5	Exception
    //   6133	1	41	localException6	Exception
    //   426	5710	42	localObject9	Object
    //   6142	11	42	localException7	Exception
    //   417	5730	43	localObject10	Object
    //   7	6152	44	localObject11	Object
    //   468	4984	45	localObject12	Object
    //   5554	22	45	localException8	Exception
    //   5657	562	45	localObject13	Object
    //   575	5397	46	localMediaExtractor	MediaExtractor
    //   725	2883	47	localObject14	Object
    //   650	5499	48	localMediaCodec	android.media.MediaCodec
    //   653	2399	49	localObject15	Object
    //   662	4998	50	localObject16	Object
    //   772	5449	51	localObject17	Object
    //   1025	4682	52	localObject18	Object
    //   2171	2475	53	localObject19	Object
    //   4326	39	54	localObject20	Object
    //   118	5999	55	localFile	File
    //   174	5919	56	localSharedPreferences	android.content.SharedPreferences
    //   447	5498	57	localBufferInfo	MediaCodec.BufferInfo
    //   4236	281	58	arrayOfByte	byte[]
    // Exception table:
    //   from	to	target	type
    //   719	727	831	java/lang/Exception
    //   743	751	831	java/lang/Exception
    //   767	774	831	java/lang/Exception
    //   790	799	831	java/lang/Exception
    //   820	831	831	java/lang/Exception
    //   1020	1027	831	java/lang/Exception
    //   1043	1054	831	java/lang/Exception
    //   1080	1088	831	java/lang/Exception
    //   1104	1115	831	java/lang/Exception
    //   1138	1149	831	java/lang/Exception
    //   1179	1185	831	java/lang/Exception
    //   1201	1251	831	java/lang/Exception
    //   1278	1284	831	java/lang/Exception
    //   1300	1324	831	java/lang/Exception
    //   1343	1353	831	java/lang/Exception
    //   1408	1418	831	java/lang/Exception
    //   1434	1441	831	java/lang/Exception
    //   1457	1466	831	java/lang/Exception
    //   1490	1497	831	java/lang/Exception
    //   1513	1522	831	java/lang/Exception
    //   1538	1551	831	java/lang/Exception
    //   1567	1577	831	java/lang/Exception
    //   1600	1608	831	java/lang/Exception
    //   1624	1635	831	java/lang/Exception
    //   1651	1661	831	java/lang/Exception
    //   1685	1694	831	java/lang/Exception
    //   1718	1727	831	java/lang/Exception
    //   1743	1753	831	java/lang/Exception
    //   1769	1777	831	java/lang/Exception
    //   1793	1806	831	java/lang/Exception
    //   1822	1832	831	java/lang/Exception
    //   1848	1855	831	java/lang/Exception
    //   1871	1881	831	java/lang/Exception
    //   1897	1905	831	java/lang/Exception
    //   1921	1935	831	java/lang/Exception
    //   1956	1961	831	java/lang/Exception
    //   1977	1990	831	java/lang/Exception
    //   2006	2014	831	java/lang/Exception
    //   2030	2039	831	java/lang/Exception
    //   2055	2069	831	java/lang/Exception
    //   2085	2090	831	java/lang/Exception
    //   2119	2127	831	java/lang/Exception
    //   2143	2150	831	java/lang/Exception
    //   2166	2173	831	java/lang/Exception
    //   2201	2209	831	java/lang/Exception
    //   2225	2232	831	java/lang/Exception
    //   2256	2260	831	java/lang/Exception
    //   2312	2316	831	java/lang/Exception
    //   2340	2347	831	java/lang/Exception
    //   2370	2380	831	java/lang/Exception
    //   2407	2415	831	java/lang/Exception
    //   2438	2448	831	java/lang/Exception
    //   2469	2480	831	java/lang/Exception
    //   2510	2519	831	java/lang/Exception
    //   2542	2552	831	java/lang/Exception
    //   2573	2577	831	java/lang/Exception
    //   2593	2605	831	java/lang/Exception
    //   2682	2694	831	java/lang/Exception
    //   2736	2747	831	java/lang/Exception
    //   2772	2783	831	java/lang/Exception
    //   2808	2819	831	java/lang/Exception
    //   2850	2861	831	java/lang/Exception
    //   2899	2913	831	java/lang/Exception
    //   2992	3003	831	java/lang/Exception
    //   3038	3048	831	java/lang/Exception
    //   3067	3074	831	java/lang/Exception
    //   3167	3181	831	java/lang/Exception
    //   3200	3209	831	java/lang/Exception
    //   3228	3244	831	java/lang/Exception
    //   3260	3266	831	java/lang/Exception
    //   3304	3317	831	java/lang/Exception
    //   3333	3341	831	java/lang/Exception
    //   3357	3364	831	java/lang/Exception
    //   3380	3391	831	java/lang/Exception
    //   3407	3415	831	java/lang/Exception
    //   3431	3441	831	java/lang/Exception
    //   3457	3463	831	java/lang/Exception
    //   3485	3493	831	java/lang/Exception
    //   3522	3533	831	java/lang/Exception
    //   3549	3555	831	java/lang/Exception
    //   3571	3581	831	java/lang/Exception
    //   3603	3618	831	java/lang/Exception
    //   3634	3643	831	java/lang/Exception
    //   3668	3674	831	java/lang/Exception
    //   3717	3725	831	java/lang/Exception
    //   3741	3748	831	java/lang/Exception
    //   3785	3792	831	java/lang/Exception
    //   3830	3839	831	java/lang/Exception
    //   3875	3904	831	java/lang/Exception
    //   3920	3928	831	java/lang/Exception
    //   3956	3991	831	java/lang/Exception
    //   4007	4016	831	java/lang/Exception
    //   4038	4047	831	java/lang/Exception
    //   4063	4073	831	java/lang/Exception
    //   4092	4107	831	java/lang/Exception
    //   4123	4132	831	java/lang/Exception
    //   4151	4161	831	java/lang/Exception
    //   4180	4188	831	java/lang/Exception
    //   4229	4238	831	java/lang/Exception
    //   4254	4271	831	java/lang/Exception
    //   4287	4298	831	java/lang/Exception
    //   4314	4322	831	java/lang/Exception
    //   4344	4352	831	java/lang/Exception
    //   4428	4436	831	java/lang/Exception
    //   4452	4466	831	java/lang/Exception
    //   4482	4498	831	java/lang/Exception
    //   4514	4538	831	java/lang/Exception
    //   4554	4565	831	java/lang/Exception
    //   4591	4601	831	java/lang/Exception
    //   4617	4627	831	java/lang/Exception
    //   4643	4652	831	java/lang/Exception
    //   4700	4707	831	java/lang/Exception
    //   4738	4744	831	java/lang/Exception
    //   4760	4784	831	java/lang/Exception
    //   4823	4852	831	java/lang/Exception
    //   4868	4876	831	java/lang/Exception
    //   4892	4900	831	java/lang/Exception
    //   4950	4961	831	java/lang/Exception
    //   4986	4998	831	java/lang/Exception
    //   5046	5057	831	java/lang/Exception
    //   5084	5090	831	java/lang/Exception
    //   5106	5144	831	java/lang/Exception
    //   5168	5177	831	java/lang/Exception
    //   5211	5219	831	java/lang/Exception
    //   5235	5241	831	java/lang/Exception
    //   5257	5271	831	java/lang/Exception
    //   5287	5293	831	java/lang/Exception
    //   5336	5346	831	java/lang/Exception
    //   5365	5371	831	java/lang/Exception
    //   5387	5393	831	java/lang/Exception
    //   5409	5417	831	java/lang/Exception
    //   5433	5438	831	java/lang/Exception
    //   5487	5495	831	java/lang/Exception
    //   5511	5521	831	java/lang/Exception
    //   5540	5547	831	java/lang/Exception
    //   5575	5580	831	java/lang/Exception
    //   5599	5609	831	java/lang/Exception
    //   5630	5636	831	java/lang/Exception
    //   5652	5659	831	java/lang/Exception
    //   5682	5688	831	java/lang/Exception
    //   5704	5722	831	java/lang/Exception
    //   5738	5754	831	java/lang/Exception
    //   5773	5779	831	java/lang/Exception
    //   5795	5801	831	java/lang/Exception
    //   5820	5830	831	java/lang/Exception
    //   5881	5896	831	java/lang/Exception
    //   577	588	3077	finally
    //   606	616	3077	finally
    //   616	625	3077	finally
    //   631	640	3077	finally
    //   719	727	3077	finally
    //   743	751	3077	finally
    //   767	774	3077	finally
    //   790	799	3077	finally
    //   820	831	3077	finally
    //   837	842	3077	finally
    //   849	856	3077	finally
    //   861	866	3077	finally
    //   871	876	3077	finally
    //   881	891	3077	finally
    //   896	906	3077	finally
    //   906	910	3077	finally
    //   1020	1027	3077	finally
    //   1043	1054	3077	finally
    //   1080	1088	3077	finally
    //   1104	1115	3077	finally
    //   1138	1149	3077	finally
    //   1179	1185	3077	finally
    //   1201	1251	3077	finally
    //   1278	1284	3077	finally
    //   1300	1324	3077	finally
    //   1343	1353	3077	finally
    //   1408	1418	3077	finally
    //   1434	1441	3077	finally
    //   1457	1466	3077	finally
    //   1490	1497	3077	finally
    //   1513	1522	3077	finally
    //   1538	1551	3077	finally
    //   1567	1577	3077	finally
    //   1600	1608	3077	finally
    //   1624	1635	3077	finally
    //   1651	1661	3077	finally
    //   1685	1694	3077	finally
    //   1718	1727	3077	finally
    //   1743	1753	3077	finally
    //   1769	1777	3077	finally
    //   1793	1806	3077	finally
    //   1822	1832	3077	finally
    //   1848	1855	3077	finally
    //   1871	1881	3077	finally
    //   1897	1905	3077	finally
    //   1921	1935	3077	finally
    //   1935	1940	3077	finally
    //   1956	1961	3077	finally
    //   1977	1990	3077	finally
    //   2006	2014	3077	finally
    //   2030	2039	3077	finally
    //   2055	2069	3077	finally
    //   2085	2090	3077	finally
    //   2119	2127	3077	finally
    //   2143	2150	3077	finally
    //   2166	2173	3077	finally
    //   2201	2209	3077	finally
    //   2225	2232	3077	finally
    //   2256	2260	3077	finally
    //   2312	2316	3077	finally
    //   2340	2347	3077	finally
    //   2370	2380	3077	finally
    //   2407	2415	3077	finally
    //   2438	2448	3077	finally
    //   2469	2480	3077	finally
    //   2510	2519	3077	finally
    //   2542	2552	3077	finally
    //   2573	2577	3077	finally
    //   2593	2605	3077	finally
    //   2682	2694	3077	finally
    //   2736	2747	3077	finally
    //   2772	2783	3077	finally
    //   2808	2819	3077	finally
    //   2850	2861	3077	finally
    //   2899	2913	3077	finally
    //   2992	3003	3077	finally
    //   3038	3048	3077	finally
    //   3067	3074	3077	finally
    //   3167	3181	3077	finally
    //   3200	3209	3077	finally
    //   3228	3244	3077	finally
    //   3260	3266	3077	finally
    //   3304	3317	3077	finally
    //   3333	3341	3077	finally
    //   3357	3364	3077	finally
    //   3380	3391	3077	finally
    //   3407	3415	3077	finally
    //   3431	3441	3077	finally
    //   3457	3463	3077	finally
    //   3485	3493	3077	finally
    //   3522	3533	3077	finally
    //   3549	3555	3077	finally
    //   3571	3581	3077	finally
    //   3603	3618	3077	finally
    //   3634	3643	3077	finally
    //   3668	3674	3077	finally
    //   3717	3725	3077	finally
    //   3741	3748	3077	finally
    //   3785	3792	3077	finally
    //   3830	3839	3077	finally
    //   3875	3904	3077	finally
    //   3920	3928	3077	finally
    //   3956	3991	3077	finally
    //   4007	4016	3077	finally
    //   4038	4047	3077	finally
    //   4063	4073	3077	finally
    //   4092	4107	3077	finally
    //   4123	4132	3077	finally
    //   4151	4161	3077	finally
    //   4180	4188	3077	finally
    //   4229	4238	3077	finally
    //   4254	4271	3077	finally
    //   4287	4298	3077	finally
    //   4314	4322	3077	finally
    //   4344	4352	3077	finally
    //   4428	4436	3077	finally
    //   4452	4466	3077	finally
    //   4482	4498	3077	finally
    //   4514	4538	3077	finally
    //   4554	4565	3077	finally
    //   4591	4601	3077	finally
    //   4617	4627	3077	finally
    //   4643	4652	3077	finally
    //   4700	4707	3077	finally
    //   4738	4744	3077	finally
    //   4760	4784	3077	finally
    //   4823	4852	3077	finally
    //   4868	4876	3077	finally
    //   4892	4900	3077	finally
    //   4950	4961	3077	finally
    //   4986	4998	3077	finally
    //   5046	5057	3077	finally
    //   5084	5090	3077	finally
    //   5106	5144	3077	finally
    //   5168	5177	3077	finally
    //   5185	5190	3077	finally
    //   5211	5219	3077	finally
    //   5235	5241	3077	finally
    //   5257	5271	3077	finally
    //   5287	5293	3077	finally
    //   5336	5346	3077	finally
    //   5365	5371	3077	finally
    //   5387	5393	3077	finally
    //   5409	5417	3077	finally
    //   5433	5438	3077	finally
    //   5487	5495	3077	finally
    //   5511	5521	3077	finally
    //   5540	5547	3077	finally
    //   5575	5580	3077	finally
    //   5599	5609	3077	finally
    //   5630	5636	3077	finally
    //   5652	5659	3077	finally
    //   5682	5688	3077	finally
    //   5704	5722	3077	finally
    //   5738	5754	3077	finally
    //   5773	5779	3077	finally
    //   5795	5801	3077	finally
    //   5820	5830	3077	finally
    //   5881	5896	3077	finally
    //   5938	5958	3077	finally
    //   5185	5190	5554	java/lang/Exception
    //   577	588	5965	java/lang/Exception
    //   606	616	5965	java/lang/Exception
    //   616	625	5965	java/lang/Exception
    //   631	640	5965	java/lang/Exception
    //   837	842	5965	java/lang/Exception
    //   849	856	5965	java/lang/Exception
    //   861	866	5965	java/lang/Exception
    //   871	876	5965	java/lang/Exception
    //   881	891	5965	java/lang/Exception
    //   896	906	5965	java/lang/Exception
    //   906	910	5965	java/lang/Exception
    //   5938	5958	5965	java/lang/Exception
    //   925	930	6062	java/lang/Exception
    //   6006	6011	6072	java/lang/Exception
    //   3097	3102	6082	java/lang/Exception
    //   440	449	6125	finally
    //   461	470	6125	finally
    //   482	489	6125	finally
    //   501	507	6125	finally
    //   519	528	6125	finally
    //   540	556	6125	finally
    //   568	577	6125	finally
    //   5986	5991	6125	finally
    //   440	449	6133	java/lang/Exception
    //   461	470	6133	java/lang/Exception
    //   482	489	6133	java/lang/Exception
    //   501	507	6133	java/lang/Exception
    //   519	528	6133	java/lang/Exception
    //   540	556	6133	java/lang/Exception
    //   568	577	6133	java/lang/Exception
    //   1935	1940	6142	java/lang/Exception
  }
  
  /* Error */
  public static String copyFileToCache(Uri paramUri, String paramString)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 11
    //   3: aconst_null
    //   4: astore 10
    //   6: aconst_null
    //   7: astore 9
    //   9: aconst_null
    //   10: astore 8
    //   12: aload 10
    //   14: astore 4
    //   16: aload 9
    //   18: astore 5
    //   20: aload 11
    //   22: astore 6
    //   24: aload_0
    //   25: invokestatic 1532	org/telegram/messenger/MediaController:getFileName	(Landroid/net/Uri;)Ljava/lang/String;
    //   28: invokestatic 1535	org/telegram/messenger/FileLoader:fixFileName	(Ljava/lang/String;)Ljava/lang/String;
    //   31: astore 12
    //   33: aload 12
    //   35: astore 7
    //   37: aload 12
    //   39: ifnonnull +72 -> 111
    //   42: aload 10
    //   44: astore 4
    //   46: aload 9
    //   48: astore 5
    //   50: aload 11
    //   52: astore 6
    //   54: invokestatic 1538	org/telegram/messenger/SharedConfig:getLastLocalId	()I
    //   57: istore_2
    //   58: aload 10
    //   60: astore 4
    //   62: aload 9
    //   64: astore 5
    //   66: aload 11
    //   68: astore 6
    //   70: invokestatic 1541	org/telegram/messenger/SharedConfig:saveConfig	()V
    //   73: aload 10
    //   75: astore 4
    //   77: aload 9
    //   79: astore 5
    //   81: aload 11
    //   83: astore 6
    //   85: getstatic 1547	java/util/Locale:US	Ljava/util/Locale;
    //   88: ldc_w 1549
    //   91: iconst_2
    //   92: anewarray 4	java/lang/Object
    //   95: dup
    //   96: iconst_0
    //   97: iload_2
    //   98: invokestatic 1555	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   101: aastore
    //   102: dup
    //   103: iconst_1
    //   104: aload_1
    //   105: aastore
    //   106: invokestatic 1559	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   109: astore 7
    //   111: aload 10
    //   113: astore 4
    //   115: aload 9
    //   117: astore 5
    //   119: aload 11
    //   121: astore 6
    //   123: new 998	java/io/File
    //   126: dup
    //   127: iconst_4
    //   128: invokestatic 1563	org/telegram/messenger/FileLoader:getDirectory	(I)Ljava/io/File;
    //   131: ldc_w 1565
    //   134: invokespecial 1568	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   137: astore_1
    //   138: aload 10
    //   140: astore 4
    //   142: aload 9
    //   144: astore 5
    //   146: aload 11
    //   148: astore 6
    //   150: aload_1
    //   151: invokevirtual 1571	java/io/File:mkdirs	()Z
    //   154: pop
    //   155: aload 10
    //   157: astore 4
    //   159: aload 9
    //   161: astore 5
    //   163: aload 11
    //   165: astore 6
    //   167: new 998	java/io/File
    //   170: dup
    //   171: aload_1
    //   172: aload 7
    //   174: invokespecial 1568	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   177: astore 7
    //   179: aload 10
    //   181: astore 4
    //   183: aload 9
    //   185: astore 5
    //   187: aload 11
    //   189: astore 6
    //   191: aload 7
    //   193: invokestatic 1577	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   196: invokestatic 1581	org/telegram/messenger/AndroidUtilities:isInternalUri	(Landroid/net/Uri;)Z
    //   199: istore_3
    //   200: iload_3
    //   201: ifeq +44 -> 245
    //   204: iconst_0
    //   205: ifeq +11 -> 216
    //   208: new 1583	java/lang/NullPointerException
    //   211: dup
    //   212: invokespecial 1584	java/lang/NullPointerException:<init>	()V
    //   215: athrow
    //   216: iconst_0
    //   217: ifeq +11 -> 228
    //   220: new 1583	java/lang/NullPointerException
    //   223: dup
    //   224: invokespecial 1584	java/lang/NullPointerException:<init>	()V
    //   227: athrow
    //   228: aconst_null
    //   229: areturn
    //   230: astore_0
    //   231: aload_0
    //   232: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   235: goto -19 -> 216
    //   238: astore_0
    //   239: aload_0
    //   240: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   243: aconst_null
    //   244: areturn
    //   245: aload 10
    //   247: astore 4
    //   249: aload 9
    //   251: astore 5
    //   253: aload 11
    //   255: astore 6
    //   257: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   260: invokevirtual 520	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
    //   263: aload_0
    //   264: invokevirtual 1588	android/content/ContentResolver:openInputStream	(Landroid/net/Uri;)Ljava/io/InputStream;
    //   267: astore_0
    //   268: aload_0
    //   269: astore 4
    //   271: aload 9
    //   273: astore 5
    //   275: aload_0
    //   276: astore 6
    //   278: new 1590	java/io/FileOutputStream
    //   281: dup
    //   282: aload 7
    //   284: invokespecial 1592	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   287: astore_1
    //   288: sipush 20480
    //   291: newarray <illegal type>
    //   293: astore 4
    //   295: aload_0
    //   296: aload 4
    //   298: invokevirtual 1598	java/io/InputStream:read	([B)I
    //   301: istore_2
    //   302: iload_2
    //   303: iconst_m1
    //   304: if_icmpeq +45 -> 349
    //   307: aload_1
    //   308: aload 4
    //   310: iconst_0
    //   311: iload_2
    //   312: invokevirtual 1602	java/io/FileOutputStream:write	([BII)V
    //   315: goto -20 -> 295
    //   318: astore 7
    //   320: aload_0
    //   321: astore 4
    //   323: aload_1
    //   324: astore 5
    //   326: aload 7
    //   328: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   331: aload_0
    //   332: ifnull +7 -> 339
    //   335: aload_0
    //   336: invokevirtual 1605	java/io/InputStream:close	()V
    //   339: aload_1
    //   340: ifnull +7 -> 347
    //   343: aload_1
    //   344: invokevirtual 1606	java/io/FileOutputStream:close	()V
    //   347: aconst_null
    //   348: areturn
    //   349: aload 7
    //   351: invokevirtual 1609	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   354: astore 4
    //   356: aload_0
    //   357: ifnull +7 -> 364
    //   360: aload_0
    //   361: invokevirtual 1605	java/io/InputStream:close	()V
    //   364: aload_1
    //   365: ifnull +7 -> 372
    //   368: aload_1
    //   369: invokevirtual 1606	java/io/FileOutputStream:close	()V
    //   372: aload 4
    //   374: areturn
    //   375: astore_0
    //   376: aload_0
    //   377: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   380: goto -16 -> 364
    //   383: astore_0
    //   384: aload_0
    //   385: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   388: goto -16 -> 372
    //   391: astore_0
    //   392: aload_0
    //   393: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   396: goto -57 -> 339
    //   399: astore_0
    //   400: aload_0
    //   401: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   404: goto -57 -> 347
    //   407: astore_0
    //   408: aload 4
    //   410: ifnull +8 -> 418
    //   413: aload 4
    //   415: invokevirtual 1605	java/io/InputStream:close	()V
    //   418: aload 5
    //   420: ifnull +8 -> 428
    //   423: aload 5
    //   425: invokevirtual 1606	java/io/FileOutputStream:close	()V
    //   428: aload_0
    //   429: athrow
    //   430: astore_1
    //   431: aload_1
    //   432: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   435: goto -17 -> 418
    //   438: astore_1
    //   439: aload_1
    //   440: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   443: goto -15 -> 428
    //   446: astore 6
    //   448: aload_0
    //   449: astore 4
    //   451: aload_1
    //   452: astore 5
    //   454: aload 6
    //   456: astore_0
    //   457: goto -49 -> 408
    //   460: astore 7
    //   462: aload 6
    //   464: astore_0
    //   465: aload 8
    //   467: astore_1
    //   468: goto -148 -> 320
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	471	0	paramUri	Uri
    //   0	471	1	paramString	String
    //   57	255	2	i	int
    //   199	2	3	bool	boolean
    //   14	436	4	localObject1	Object
    //   18	435	5	localObject2	Object
    //   22	255	6	localObject3	Object
    //   446	17	6	localObject4	Object
    //   35	248	7	localObject5	Object
    //   318	32	7	localException1	Exception
    //   460	1	7	localException2	Exception
    //   10	456	8	localObject6	Object
    //   7	265	9	localObject7	Object
    //   4	242	10	localObject8	Object
    //   1	253	11	localObject9	Object
    //   31	7	12	str	String
    // Exception table:
    //   from	to	target	type
    //   208	216	230	java/lang/Exception
    //   220	228	238	java/lang/Exception
    //   288	295	318	java/lang/Exception
    //   295	302	318	java/lang/Exception
    //   307	315	318	java/lang/Exception
    //   349	356	318	java/lang/Exception
    //   360	364	375	java/lang/Exception
    //   368	372	383	java/lang/Exception
    //   335	339	391	java/lang/Exception
    //   343	347	399	java/lang/Exception
    //   24	33	407	finally
    //   54	58	407	finally
    //   70	73	407	finally
    //   85	111	407	finally
    //   123	138	407	finally
    //   150	155	407	finally
    //   167	179	407	finally
    //   191	200	407	finally
    //   257	268	407	finally
    //   278	288	407	finally
    //   326	331	407	finally
    //   413	418	430	java/lang/Exception
    //   423	428	438	java/lang/Exception
    //   288	295	446	finally
    //   295	302	446	finally
    //   307	315	446	finally
    //   349	356	446	finally
    //   24	33	460	java/lang/Exception
    //   54	58	460	java/lang/Exception
    //   70	73	460	java/lang/Exception
    //   85	111	460	java/lang/Exception
    //   123	138	460	java/lang/Exception
    //   150	155	460	java/lang/Exception
    //   167	179	460	java/lang/Exception
    //   191	200	460	java/lang/Exception
    //   257	268	460	java/lang/Exception
    //   278	288	460	java/lang/Exception
  }
  
  private void didWriteData(final MessageObject paramMessageObject, final File paramFile, final boolean paramBoolean1, final boolean paramBoolean2)
  {
    final boolean bool = this.videoConvertFirstWrite;
    if (bool) {
      this.videoConvertFirstWrite = false;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if ((paramBoolean2) || (paramBoolean1)) {}
        synchronized (MediaController.this.videoConvertSync)
        {
          MediaController.access$8202(MediaController.this, false);
          MediaController.this.videoConvertQueue.remove(paramMessageObject);
          MediaController.this.startVideoConvertFromQueue();
          if (paramBoolean2)
          {
            NotificationCenter.getInstance(paramMessageObject.currentAccount).postNotificationName(NotificationCenter.FilePreparingFailed, new Object[] { paramMessageObject, paramFile.toString() });
            return;
          }
        }
        if (bool) {
          NotificationCenter.getInstance(paramMessageObject.currentAccount).postNotificationName(NotificationCenter.FilePreparingStarted, new Object[] { paramMessageObject, paramFile.toString() });
        }
        ??? = NotificationCenter.getInstance(paramMessageObject.currentAccount);
        int i = NotificationCenter.FileNewChunkAvailable;
        MessageObject localMessageObject = paramMessageObject;
        String str = paramFile.toString();
        long l2 = paramFile.length();
        if (paramBoolean1) {}
        for (long l1 = paramFile.length();; l1 = 0L)
        {
          ((NotificationCenter)???).postNotificationName(i, new Object[] { localMessageObject, str, Long.valueOf(l2), Long.valueOf(l1) });
          return;
        }
      }
    });
  }
  
  private int findTrack(MediaExtractor paramMediaExtractor, boolean paramBoolean)
  {
    int j = paramMediaExtractor.getTrackCount();
    int i = 0;
    while (i < j)
    {
      String str = paramMediaExtractor.getTrackFormat(i).getString("mime");
      if (paramBoolean)
      {
        if (!str.startsWith("audio/")) {}
      }
      else {
        while (str.startsWith("video/")) {
          return i;
        }
      }
      i += 1;
    }
    return -5;
  }
  
  public static String getFileName(Uri paramUri)
  {
    localObject3 = null;
    String str = null;
    localObject2 = localObject3;
    if (paramUri.getScheme().equals("content"))
    {
      localObject2 = null;
      localObject1 = null;
    }
    try
    {
      Cursor localCursor = ApplicationLoader.applicationContext.getContentResolver().query(paramUri, new String[] { "_display_name" }, null, null, null);
      localObject1 = localCursor;
      localObject2 = localCursor;
      if (localCursor.moveToFirst())
      {
        localObject1 = localCursor;
        localObject2 = localCursor;
        str = localCursor.getString(localCursor.getColumnIndex("_display_name"));
      }
      localObject2 = str;
      if (localCursor != null)
      {
        localCursor.close();
        localObject2 = str;
      }
    }
    catch (Exception localException)
    {
      for (;;)
      {
        int i;
        localObject2 = localObject1;
        FileLog.e(localException);
        localObject2 = localObject3;
        if (localObject1 != null)
        {
          ((Cursor)localObject1).close();
          localObject2 = localObject3;
        }
      }
    }
    finally
    {
      if (localObject2 == null) {
        break label184;
      }
      ((Cursor)localObject2).close();
    }
    localObject1 = localObject2;
    if (localObject2 == null)
    {
      paramUri = paramUri.getPath();
      i = paramUri.lastIndexOf('/');
      localObject1 = paramUri;
      if (i != -1) {
        localObject1 = paramUri.substring(i + 1);
      }
    }
    return (String)localObject1;
  }
  
  public static MediaController getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          MediaController localMediaController2 = Instance;
          localObject1 = localMediaController2;
          if (localMediaController2 == null) {
            localObject1 = new MediaController();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (MediaController)localObject1;
          return (MediaController)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localMediaController1;
  }
  
  private native long getTotalPcmDuration();
  
  public static boolean isGif(Uri paramUri)
  {
    boolean bool1 = false;
    Object localObject1 = null;
    Uri localUri = null;
    do
    {
      try
      {
        paramUri = ApplicationLoader.applicationContext.getContentResolver().openInputStream(paramUri);
        localUri = paramUri;
        localObject1 = paramUri;
        Object localObject2 = new byte[3];
        localUri = paramUri;
        localObject1 = paramUri;
        if (paramUri.read((byte[])localObject2, 0, 3) != 3) {
          continue;
        }
        localUri = paramUri;
        localObject1 = paramUri;
        localObject2 = new String((byte[])localObject2);
        if (localObject2 == null) {
          continue;
        }
        localUri = paramUri;
        localObject1 = paramUri;
        bool2 = ((String)localObject2).equalsIgnoreCase("gif");
        if (!bool2) {
          continue;
        }
        bool2 = true;
        bool1 = bool2;
      }
      catch (Exception paramUri)
      {
        do
        {
          boolean bool2;
          localObject1 = localUri;
          FileLog.e(paramUri);
        } while (localUri == null);
        try
        {
          localUri.close();
          return false;
        }
        catch (Exception paramUri)
        {
          FileLog.e(paramUri);
          return false;
        }
      }
      finally
      {
        if (localObject1 == null) {
          break label160;
        }
      }
      try
      {
        paramUri.close();
        bool1 = bool2;
        return bool1;
      }
      catch (Exception paramUri)
      {
        FileLog.e(paramUri);
        return true;
      }
    } while (paramUri == null);
    try
    {
      paramUri.close();
      return false;
    }
    catch (Exception paramUri)
    {
      FileLog.e(paramUri);
      return false;
    }
    try
    {
      ((InputStream)localObject1).close();
      label160:
      throw paramUri;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  private boolean isNearToSensor(float paramFloat)
  {
    return (paramFloat < 5.0F) && (paramFloat != this.proximitySensor.getMaximumRange());
  }
  
  public static native int isOpusFile(String paramString);
  
  private static boolean isRecognizedFormat(int paramInt)
  {
    switch (paramInt)
    {
    default: 
      return false;
    }
    return true;
  }
  
  private boolean isSamePlayingMessage(MessageObject paramMessageObject)
  {
    if ((this.playingMessageObject != null) && (this.playingMessageObject.getDialogId() == paramMessageObject.getDialogId()) && (this.playingMessageObject.getId() == paramMessageObject.getId()))
    {
      int i;
      if (this.playingMessageObject.eventId == 0L)
      {
        i = 1;
        if (paramMessageObject.eventId != 0L) {
          break label73;
        }
      }
      label73:
      for (int j = 1;; j = 0)
      {
        if (i != j) {
          break label78;
        }
        return true;
        i = 0;
        break;
      }
    }
    label78:
    return false;
  }
  
  public static boolean isWebp(Uri paramUri)
  {
    boolean bool1 = false;
    Object localObject1 = null;
    Uri localUri = null;
    do
    {
      try
      {
        paramUri = ApplicationLoader.applicationContext.getContentResolver().openInputStream(paramUri);
        localUri = paramUri;
        localObject1 = paramUri;
        Object localObject2 = new byte[12];
        localUri = paramUri;
        localObject1 = paramUri;
        if (paramUri.read((byte[])localObject2, 0, 12) != 12) {
          continue;
        }
        localUri = paramUri;
        localObject1 = paramUri;
        localObject2 = new String((byte[])localObject2);
        if (localObject2 == null) {
          continue;
        }
        localUri = paramUri;
        localObject1 = paramUri;
        localObject2 = ((String)localObject2).toLowerCase();
        localUri = paramUri;
        localObject1 = paramUri;
        if (!((String)localObject2).startsWith("riff")) {
          continue;
        }
        localUri = paramUri;
        localObject1 = paramUri;
        bool2 = ((String)localObject2).endsWith("webp");
        if (!bool2) {
          continue;
        }
        bool2 = true;
        bool1 = bool2;
      }
      catch (Exception paramUri)
      {
        do
        {
          boolean bool2;
          localObject1 = localUri;
          FileLog.e(paramUri);
        } while (localUri == null);
        try
        {
          localUri.close();
          return false;
        }
        catch (Exception paramUri)
        {
          FileLog.e(paramUri);
          return false;
        }
      }
      finally
      {
        if (localObject1 == null) {
          break label191;
        }
      }
      try
      {
        paramUri.close();
        bool1 = bool2;
        return bool1;
      }
      catch (Exception paramUri)
      {
        FileLog.e(paramUri);
        return true;
      }
    } while (paramUri == null);
    try
    {
      paramUri.close();
      return false;
    }
    catch (Exception paramUri)
    {
      FileLog.e(paramUri);
      return false;
    }
    try
    {
      ((InputStream)localObject1).close();
      label191:
      throw paramUri;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public static void loadGalleryPhotosAlbums(int paramInt)
  {
    Thread localThread = new Thread(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: new 29	java/util/ArrayList
        //   3: dup
        //   4: invokespecial 30	java/util/ArrayList:<init>	()V
        //   7: astore 35
        //   9: new 29	java/util/ArrayList
        //   12: dup
        //   13: invokespecial 30	java/util/ArrayList:<init>	()V
        //   16: astore 36
        //   18: new 32	android/util/SparseArray
        //   21: dup
        //   22: invokespecial 33	android/util/SparseArray:<init>	()V
        //   25: astore 37
        //   27: new 32	android/util/SparseArray
        //   30: dup
        //   31: invokespecial 33	android/util/SparseArray:<init>	()V
        //   34: astore 38
        //   36: aconst_null
        //   37: astore 28
        //   39: aconst_null
        //   40: astore 25
        //   42: aconst_null
        //   43: astore 29
        //   45: aconst_null
        //   46: astore 27
        //   48: aconst_null
        //   49: astore 24
        //   51: new 35	java/lang/StringBuilder
        //   54: dup
        //   55: invokespecial 36	java/lang/StringBuilder:<init>	()V
        //   58: getstatic 42	android/os/Environment:DIRECTORY_DCIM	Ljava/lang/String;
        //   61: invokestatic 46	android/os/Environment:getExternalStoragePublicDirectory	(Ljava/lang/String;)Ljava/io/File;
        //   64: invokevirtual 52	java/io/File:getAbsolutePath	()Ljava/lang/String;
        //   67: invokevirtual 56	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   70: ldc 58
        //   72: invokevirtual 56	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   75: invokevirtual 61	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   78: astore 14
        //   80: aload 14
        //   82: astore 24
        //   84: aconst_null
        //   85: astore 31
        //   87: aconst_null
        //   88: astore 32
        //   90: aconst_null
        //   91: astore 26
        //   93: aconst_null
        //   94: astore 30
        //   96: aconst_null
        //   97: astore 33
        //   99: aconst_null
        //   100: astore 34
        //   102: aconst_null
        //   103: astore 15
        //   105: aload 31
        //   107: astore 18
        //   109: aload 27
        //   111: astore 20
        //   113: aload 25
        //   115: astore 22
        //   117: aload 15
        //   119: astore 23
        //   121: aload 33
        //   123: astore 19
        //   125: getstatic 66	android/os/Build$VERSION:SDK_INT	I
        //   128: bipush 23
        //   130: if_icmplt +94 -> 224
        //   133: aload 31
        //   135: astore 18
        //   137: aload 27
        //   139: astore 20
        //   141: aload 25
        //   143: astore 22
        //   145: aload 15
        //   147: astore 23
        //   149: aload 33
        //   151: astore 19
        //   153: aload 32
        //   155: astore 16
        //   157: aload 29
        //   159: astore 14
        //   161: aload 28
        //   163: astore 21
        //   165: aload 34
        //   167: astore 17
        //   169: getstatic 66	android/os/Build$VERSION:SDK_INT	I
        //   172: bipush 23
        //   174: if_icmplt +2141 -> 2315
        //   177: aload 31
        //   179: astore 18
        //   181: aload 27
        //   183: astore 20
        //   185: aload 25
        //   187: astore 22
        //   189: aload 15
        //   191: astore 23
        //   193: aload 33
        //   195: astore 19
        //   197: aload 32
        //   199: astore 16
        //   201: aload 29
        //   203: astore 14
        //   205: aload 28
        //   207: astore 21
        //   209: aload 34
        //   211: astore 17
        //   213: getstatic 72	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
        //   216: ldc 74
        //   218: invokevirtual 80	android/content/Context:checkSelfPermission	(Ljava/lang/String;)I
        //   221: ifne +2094 -> 2315
        //   224: aload 31
        //   226: astore 18
        //   228: aload 27
        //   230: astore 20
        //   232: aload 25
        //   234: astore 22
        //   236: aload 15
        //   238: astore 23
        //   240: aload 33
        //   242: astore 19
        //   244: getstatic 72	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
        //   247: invokevirtual 84	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
        //   250: getstatic 90	android/provider/MediaStore$Images$Media:EXTERNAL_CONTENT_URI	Landroid/net/Uri;
        //   253: invokestatic 94	org/telegram/messenger/MediaController:access$7700	()[Ljava/lang/String;
        //   256: aconst_null
        //   257: aconst_null
        //   258: ldc 96
        //   260: invokestatic 100	android/provider/MediaStore$Images$Media:query	(Landroid/content/ContentResolver;Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
        //   263: astore 15
        //   265: aload 32
        //   267: astore 16
        //   269: aload 29
        //   271: astore 14
        //   273: aload 28
        //   275: astore 21
        //   277: aload 15
        //   279: astore 17
        //   281: aload 15
        //   283: ifnull +2032 -> 2315
        //   286: aload 31
        //   288: astore 18
        //   290: aload 27
        //   292: astore 20
        //   294: aload 25
        //   296: astore 22
        //   298: aload 15
        //   300: astore 23
        //   302: aload 15
        //   304: astore 19
        //   306: aload 15
        //   308: ldc 102
        //   310: invokeinterface 107 2 0
        //   315: istore_1
        //   316: aload 31
        //   318: astore 18
        //   320: aload 27
        //   322: astore 20
        //   324: aload 25
        //   326: astore 22
        //   328: aload 15
        //   330: astore 23
        //   332: aload 15
        //   334: astore 19
        //   336: aload 15
        //   338: ldc 109
        //   340: invokeinterface 107 2 0
        //   345: istore_2
        //   346: aload 31
        //   348: astore 18
        //   350: aload 27
        //   352: astore 20
        //   354: aload 25
        //   356: astore 22
        //   358: aload 15
        //   360: astore 23
        //   362: aload 15
        //   364: astore 19
        //   366: aload 15
        //   368: ldc 111
        //   370: invokeinterface 107 2 0
        //   375: istore_3
        //   376: aload 31
        //   378: astore 18
        //   380: aload 27
        //   382: astore 20
        //   384: aload 25
        //   386: astore 22
        //   388: aload 15
        //   390: astore 23
        //   392: aload 15
        //   394: astore 19
        //   396: aload 15
        //   398: ldc 113
        //   400: invokeinterface 107 2 0
        //   405: istore 4
        //   407: aload 31
        //   409: astore 18
        //   411: aload 27
        //   413: astore 20
        //   415: aload 25
        //   417: astore 22
        //   419: aload 15
        //   421: astore 23
        //   423: aload 15
        //   425: astore 19
        //   427: aload 15
        //   429: ldc 115
        //   431: invokeinterface 107 2 0
        //   436: istore 5
        //   438: aload 31
        //   440: astore 18
        //   442: aload 27
        //   444: astore 20
        //   446: aload 25
        //   448: astore 22
        //   450: aload 15
        //   452: astore 23
        //   454: aload 15
        //   456: astore 19
        //   458: aload 15
        //   460: ldc 117
        //   462: invokeinterface 107 2 0
        //   467: istore 6
        //   469: aconst_null
        //   470: astore 14
        //   472: aconst_null
        //   473: astore 17
        //   475: aload 30
        //   477: astore 25
        //   479: aload 26
        //   481: astore 16
        //   483: aload 15
        //   485: invokeinterface 121 1 0
        //   490: ifeq +1817 -> 2307
        //   493: aload 15
        //   495: iload_1
        //   496: invokeinterface 125 2 0
        //   501: istore 7
        //   503: aload 15
        //   505: iload_2
        //   506: invokeinterface 125 2 0
        //   511: istore 8
        //   513: aload 15
        //   515: iload_3
        //   516: invokeinterface 129 2 0
        //   521: astore 30
        //   523: aload 15
        //   525: iload 4
        //   527: invokeinterface 129 2 0
        //   532: astore 29
        //   534: aload 15
        //   536: iload 5
        //   538: invokeinterface 133 2 0
        //   543: lstore 10
        //   545: aload 15
        //   547: iload 6
        //   549: invokeinterface 125 2 0
        //   554: istore 9
        //   556: aload 29
        //   558: ifnull -75 -> 483
        //   561: aload 29
        //   563: invokevirtual 139	java/lang/String:length	()I
        //   566: ifeq -83 -> 483
        //   569: new 141	org/telegram/messenger/MediaController$PhotoEntry
        //   572: dup
        //   573: iload 8
        //   575: iload 7
        //   577: lload 10
        //   579: aload 29
        //   581: iload 9
        //   583: iconst_0
        //   584: invokespecial 144	org/telegram/messenger/MediaController$PhotoEntry:<init>	(IIJLjava/lang/String;IZ)V
        //   587: astore 28
        //   589: aload 17
        //   591: ifnonnull +2146 -> 2737
        //   594: new 146	org/telegram/messenger/MediaController$AlbumEntry
        //   597: dup
        //   598: iconst_0
        //   599: ldc -108
        //   601: ldc -107
        //   603: invokestatic 154	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
        //   606: aload 28
        //   608: invokespecial 157	org/telegram/messenger/MediaController$AlbumEntry:<init>	(ILjava/lang/String;Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   611: astore 18
        //   613: aload 18
        //   615: astore 19
        //   617: aload 36
        //   619: iconst_0
        //   620: aload 18
        //   622: invokevirtual 161	java/util/ArrayList:add	(ILjava/lang/Object;)V
        //   625: aload 18
        //   627: astore 17
        //   629: aload 14
        //   631: ifnonnull +2103 -> 2734
        //   634: aload 17
        //   636: astore 19
        //   638: new 146	org/telegram/messenger/MediaController$AlbumEntry
        //   641: dup
        //   642: iconst_0
        //   643: ldc -93
        //   645: ldc -92
        //   647: invokestatic 154	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
        //   650: aload 28
        //   652: invokespecial 157	org/telegram/messenger/MediaController$AlbumEntry:<init>	(ILjava/lang/String;Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   655: astore 21
        //   657: aload 16
        //   659: astore 18
        //   661: aload 21
        //   663: astore 20
        //   665: aload 17
        //   667: astore 22
        //   669: aload 15
        //   671: astore 23
        //   673: aload 15
        //   675: astore 19
        //   677: aload 35
        //   679: iconst_0
        //   680: aload 21
        //   682: invokevirtual 161	java/util/ArrayList:add	(ILjava/lang/Object;)V
        //   685: aload 21
        //   687: astore 14
        //   689: aload 16
        //   691: astore 18
        //   693: aload 14
        //   695: astore 20
        //   697: aload 17
        //   699: astore 22
        //   701: aload 15
        //   703: astore 23
        //   705: aload 15
        //   707: astore 19
        //   709: aload 17
        //   711: aload 28
        //   713: invokevirtual 168	org/telegram/messenger/MediaController$AlbumEntry:addPhoto	(Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   716: aload 16
        //   718: astore 18
        //   720: aload 14
        //   722: astore 20
        //   724: aload 17
        //   726: astore 22
        //   728: aload 15
        //   730: astore 23
        //   732: aload 15
        //   734: astore 19
        //   736: aload 14
        //   738: aload 28
        //   740: invokevirtual 168	org/telegram/messenger/MediaController$AlbumEntry:addPhoto	(Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   743: aload 16
        //   745: astore 18
        //   747: aload 14
        //   749: astore 20
        //   751: aload 17
        //   753: astore 22
        //   755: aload 15
        //   757: astore 23
        //   759: aload 15
        //   761: astore 19
        //   763: aload 37
        //   765: iload 8
        //   767: invokevirtual 172	android/util/SparseArray:get	(I)Ljava/lang/Object;
        //   770: checkcast 146	org/telegram/messenger/MediaController$AlbumEntry
        //   773: astore 27
        //   775: aload 16
        //   777: astore 21
        //   779: aload 27
        //   781: astore 26
        //   783: aload 27
        //   785: ifnonnull +167 -> 952
        //   788: aload 16
        //   790: astore 18
        //   792: aload 14
        //   794: astore 20
        //   796: aload 17
        //   798: astore 22
        //   800: aload 15
        //   802: astore 23
        //   804: aload 15
        //   806: astore 19
        //   808: new 146	org/telegram/messenger/MediaController$AlbumEntry
        //   811: dup
        //   812: iload 8
        //   814: aload 30
        //   816: aload 28
        //   818: invokespecial 157	org/telegram/messenger/MediaController$AlbumEntry:<init>	(ILjava/lang/String;Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   821: astore 26
        //   823: aload 16
        //   825: astore 18
        //   827: aload 14
        //   829: astore 20
        //   831: aload 17
        //   833: astore 22
        //   835: aload 15
        //   837: astore 23
        //   839: aload 15
        //   841: astore 19
        //   843: aload 37
        //   845: iload 8
        //   847: aload 26
        //   849: invokevirtual 175	android/util/SparseArray:put	(ILjava/lang/Object;)V
        //   852: aload 16
        //   854: ifnonnull +382 -> 1236
        //   857: aload 24
        //   859: ifnull +377 -> 1236
        //   862: aload 29
        //   864: ifnull +372 -> 1236
        //   867: aload 16
        //   869: astore 18
        //   871: aload 14
        //   873: astore 20
        //   875: aload 17
        //   877: astore 22
        //   879: aload 15
        //   881: astore 23
        //   883: aload 15
        //   885: astore 19
        //   887: aload 29
        //   889: aload 24
        //   891: invokevirtual 179	java/lang/String:startsWith	(Ljava/lang/String;)Z
        //   894: ifeq +342 -> 1236
        //   897: aload 16
        //   899: astore 18
        //   901: aload 14
        //   903: astore 20
        //   905: aload 17
        //   907: astore 22
        //   909: aload 15
        //   911: astore 23
        //   913: aload 15
        //   915: astore 19
        //   917: aload 35
        //   919: iconst_0
        //   920: aload 26
        //   922: invokevirtual 161	java/util/ArrayList:add	(ILjava/lang/Object;)V
        //   925: aload 16
        //   927: astore 18
        //   929: aload 14
        //   931: astore 20
        //   933: aload 17
        //   935: astore 22
        //   937: aload 15
        //   939: astore 23
        //   941: aload 15
        //   943: astore 19
        //   945: iload 8
        //   947: invokestatic 185	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   950: astore 21
        //   952: aload 21
        //   954: astore 18
        //   956: aload 14
        //   958: astore 20
        //   960: aload 17
        //   962: astore 22
        //   964: aload 15
        //   966: astore 23
        //   968: aload 15
        //   970: astore 19
        //   972: aload 26
        //   974: aload 28
        //   976: invokevirtual 168	org/telegram/messenger/MediaController$AlbumEntry:addPhoto	(Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   979: aload 21
        //   981: astore 18
        //   983: aload 14
        //   985: astore 20
        //   987: aload 17
        //   989: astore 22
        //   991: aload 15
        //   993: astore 23
        //   995: aload 15
        //   997: astore 19
        //   999: aload 38
        //   1001: iload 8
        //   1003: invokevirtual 172	android/util/SparseArray:get	(I)Ljava/lang/Object;
        //   1006: checkcast 146	org/telegram/messenger/MediaController$AlbumEntry
        //   1009: astore 27
        //   1011: aload 27
        //   1013: astore 16
        //   1015: aload 25
        //   1017: astore 26
        //   1019: aload 27
        //   1021: ifnonnull +167 -> 1188
        //   1024: aload 21
        //   1026: astore 18
        //   1028: aload 14
        //   1030: astore 20
        //   1032: aload 17
        //   1034: astore 22
        //   1036: aload 15
        //   1038: astore 23
        //   1040: aload 15
        //   1042: astore 19
        //   1044: new 146	org/telegram/messenger/MediaController$AlbumEntry
        //   1047: dup
        //   1048: iload 8
        //   1050: aload 30
        //   1052: aload 28
        //   1054: invokespecial 157	org/telegram/messenger/MediaController$AlbumEntry:<init>	(ILjava/lang/String;Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   1057: astore 16
        //   1059: aload 21
        //   1061: astore 18
        //   1063: aload 14
        //   1065: astore 20
        //   1067: aload 17
        //   1069: astore 22
        //   1071: aload 15
        //   1073: astore 23
        //   1075: aload 15
        //   1077: astore 19
        //   1079: aload 38
        //   1081: iload 8
        //   1083: aload 16
        //   1085: invokevirtual 175	android/util/SparseArray:put	(ILjava/lang/Object;)V
        //   1088: aload 25
        //   1090: ifnonnull +1161 -> 2251
        //   1093: aload 24
        //   1095: ifnull +1156 -> 2251
        //   1098: aload 29
        //   1100: ifnull +1151 -> 2251
        //   1103: aload 21
        //   1105: astore 18
        //   1107: aload 14
        //   1109: astore 20
        //   1111: aload 17
        //   1113: astore 22
        //   1115: aload 15
        //   1117: astore 23
        //   1119: aload 15
        //   1121: astore 19
        //   1123: aload 29
        //   1125: aload 24
        //   1127: invokevirtual 179	java/lang/String:startsWith	(Ljava/lang/String;)Z
        //   1130: ifeq +1121 -> 2251
        //   1133: aload 21
        //   1135: astore 18
        //   1137: aload 14
        //   1139: astore 20
        //   1141: aload 17
        //   1143: astore 22
        //   1145: aload 15
        //   1147: astore 23
        //   1149: aload 15
        //   1151: astore 19
        //   1153: aload 36
        //   1155: iconst_0
        //   1156: aload 16
        //   1158: invokevirtual 161	java/util/ArrayList:add	(ILjava/lang/Object;)V
        //   1161: aload 21
        //   1163: astore 18
        //   1165: aload 14
        //   1167: astore 20
        //   1169: aload 17
        //   1171: astore 22
        //   1173: aload 15
        //   1175: astore 23
        //   1177: aload 15
        //   1179: astore 19
        //   1181: iload 8
        //   1183: invokestatic 185	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   1186: astore 26
        //   1188: aload 21
        //   1190: astore 18
        //   1192: aload 14
        //   1194: astore 20
        //   1196: aload 17
        //   1198: astore 22
        //   1200: aload 15
        //   1202: astore 23
        //   1204: aload 15
        //   1206: astore 19
        //   1208: aload 16
        //   1210: aload 28
        //   1212: invokevirtual 168	org/telegram/messenger/MediaController$AlbumEntry:addPhoto	(Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   1215: aload 21
        //   1217: astore 16
        //   1219: aload 26
        //   1221: astore 25
        //   1223: goto -740 -> 483
        //   1226: astore 14
        //   1228: aload 14
        //   1230: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   1233: goto -1149 -> 84
        //   1236: aload 16
        //   1238: astore 18
        //   1240: aload 14
        //   1242: astore 20
        //   1244: aload 17
        //   1246: astore 22
        //   1248: aload 15
        //   1250: astore 23
        //   1252: aload 15
        //   1254: astore 19
        //   1256: aload 35
        //   1258: aload 26
        //   1260: invokevirtual 194	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   1263: pop
        //   1264: aload 16
        //   1266: astore 21
        //   1268: goto -316 -> 952
        //   1271: astore 15
        //   1273: aload 23
        //   1275: astore 16
        //   1277: aload 22
        //   1279: astore 17
        //   1281: aload 20
        //   1283: astore 14
        //   1285: aload 16
        //   1287: astore 19
        //   1289: aload 15
        //   1291: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   1294: aload 18
        //   1296: astore 22
        //   1298: aload 14
        //   1300: astore 20
        //   1302: aload 17
        //   1304: astore 19
        //   1306: aload 16
        //   1308: astore 15
        //   1310: aload 16
        //   1312: ifnull +1403 -> 2715
        //   1315: aload 16
        //   1317: invokeinterface 197 1 0
        //   1322: aload 18
        //   1324: astore 15
        //   1326: aload 17
        //   1328: astore 21
        //   1330: aload 14
        //   1332: astore 18
        //   1334: aload 16
        //   1336: astore 17
        //   1338: aload 15
        //   1340: astore 19
        //   1342: aload 14
        //   1344: astore 20
        //   1346: aload 16
        //   1348: astore 20
        //   1350: aload 15
        //   1352: astore 26
        //   1354: getstatic 66	android/os/Build$VERSION:SDK_INT	I
        //   1357: bipush 23
        //   1359: if_icmplt +94 -> 1453
        //   1362: aload 14
        //   1364: astore 22
        //   1366: aload 16
        //   1368: astore 25
        //   1370: aload 15
        //   1372: astore 23
        //   1374: aload 14
        //   1376: astore 18
        //   1378: aload 16
        //   1380: astore 17
        //   1382: aload 15
        //   1384: astore 19
        //   1386: aload 14
        //   1388: astore 20
        //   1390: aload 16
        //   1392: astore 20
        //   1394: aload 15
        //   1396: astore 26
        //   1398: getstatic 66	android/os/Build$VERSION:SDK_INT	I
        //   1401: bipush 23
        //   1403: if_icmplt +1020 -> 2423
        //   1406: aload 14
        //   1408: astore 22
        //   1410: aload 16
        //   1412: astore 25
        //   1414: aload 15
        //   1416: astore 23
        //   1418: aload 14
        //   1420: astore 18
        //   1422: aload 16
        //   1424: astore 17
        //   1426: aload 15
        //   1428: astore 19
        //   1430: aload 14
        //   1432: astore 20
        //   1434: aload 16
        //   1436: astore 20
        //   1438: aload 15
        //   1440: astore 26
        //   1442: getstatic 72	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
        //   1445: ldc 74
        //   1447: invokevirtual 80	android/content/Context:checkSelfPermission	(Ljava/lang/String;)I
        //   1450: ifne +973 -> 2423
        //   1453: aload 14
        //   1455: astore 18
        //   1457: aload 16
        //   1459: astore 17
        //   1461: aload 15
        //   1463: astore 19
        //   1465: aload 14
        //   1467: astore 20
        //   1469: aload 16
        //   1471: astore 20
        //   1473: aload 15
        //   1475: astore 26
        //   1477: getstatic 72	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
        //   1480: invokevirtual 84	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
        //   1483: getstatic 200	android/provider/MediaStore$Video$Media:EXTERNAL_CONTENT_URI	Landroid/net/Uri;
        //   1486: invokestatic 203	org/telegram/messenger/MediaController:access$7800	()[Ljava/lang/String;
        //   1489: aconst_null
        //   1490: aconst_null
        //   1491: ldc 96
        //   1493: invokestatic 100	android/provider/MediaStore$Images$Media:query	(Landroid/content/ContentResolver;Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
        //   1496: astore 16
        //   1498: aload 14
        //   1500: astore 22
        //   1502: aload 16
        //   1504: astore 25
        //   1506: aload 15
        //   1508: astore 23
        //   1510: aload 16
        //   1512: ifnull +911 -> 2423
        //   1515: aload 14
        //   1517: astore 18
        //   1519: aload 16
        //   1521: astore 17
        //   1523: aload 15
        //   1525: astore 19
        //   1527: aload 14
        //   1529: astore 20
        //   1531: aload 16
        //   1533: astore 20
        //   1535: aload 15
        //   1537: astore 26
        //   1539: aload 16
        //   1541: ldc 102
        //   1543: invokeinterface 107 2 0
        //   1548: istore_1
        //   1549: aload 14
        //   1551: astore 18
        //   1553: aload 16
        //   1555: astore 17
        //   1557: aload 15
        //   1559: astore 19
        //   1561: aload 14
        //   1563: astore 20
        //   1565: aload 16
        //   1567: astore 20
        //   1569: aload 15
        //   1571: astore 26
        //   1573: aload 16
        //   1575: ldc 109
        //   1577: invokeinterface 107 2 0
        //   1582: istore_2
        //   1583: aload 14
        //   1585: astore 18
        //   1587: aload 16
        //   1589: astore 17
        //   1591: aload 15
        //   1593: astore 19
        //   1595: aload 14
        //   1597: astore 20
        //   1599: aload 16
        //   1601: astore 20
        //   1603: aload 15
        //   1605: astore 26
        //   1607: aload 16
        //   1609: ldc 111
        //   1611: invokeinterface 107 2 0
        //   1616: istore_3
        //   1617: aload 14
        //   1619: astore 18
        //   1621: aload 16
        //   1623: astore 17
        //   1625: aload 15
        //   1627: astore 19
        //   1629: aload 14
        //   1631: astore 20
        //   1633: aload 16
        //   1635: astore 20
        //   1637: aload 15
        //   1639: astore 26
        //   1641: aload 16
        //   1643: ldc 113
        //   1645: invokeinterface 107 2 0
        //   1650: istore 4
        //   1652: aload 14
        //   1654: astore 18
        //   1656: aload 16
        //   1658: astore 17
        //   1660: aload 15
        //   1662: astore 19
        //   1664: aload 14
        //   1666: astore 20
        //   1668: aload 16
        //   1670: astore 20
        //   1672: aload 15
        //   1674: astore 26
        //   1676: aload 16
        //   1678: ldc 115
        //   1680: invokeinterface 107 2 0
        //   1685: istore 5
        //   1687: aload 14
        //   1689: astore 18
        //   1691: aload 16
        //   1693: astore 17
        //   1695: aload 15
        //   1697: astore 19
        //   1699: aload 14
        //   1701: astore 20
        //   1703: aload 16
        //   1705: astore 20
        //   1707: aload 15
        //   1709: astore 26
        //   1711: aload 16
        //   1713: ldc -51
        //   1715: invokeinterface 107 2 0
        //   1720: istore 6
        //   1722: aload 14
        //   1724: astore 22
        //   1726: aload 16
        //   1728: astore 25
        //   1730: aload 15
        //   1732: astore 23
        //   1734: aload 14
        //   1736: astore 18
        //   1738: aload 16
        //   1740: astore 17
        //   1742: aload 15
        //   1744: astore 19
        //   1746: aload 14
        //   1748: astore 20
        //   1750: aload 16
        //   1752: astore 20
        //   1754: aload 15
        //   1756: astore 26
        //   1758: aload 16
        //   1760: invokeinterface 121 1 0
        //   1765: ifeq +658 -> 2423
        //   1768: aload 14
        //   1770: astore 18
        //   1772: aload 16
        //   1774: astore 17
        //   1776: aload 15
        //   1778: astore 19
        //   1780: aload 14
        //   1782: astore 20
        //   1784: aload 16
        //   1786: astore 20
        //   1788: aload 15
        //   1790: astore 26
        //   1792: aload 16
        //   1794: iload_1
        //   1795: invokeinterface 125 2 0
        //   1800: istore 7
        //   1802: aload 14
        //   1804: astore 18
        //   1806: aload 16
        //   1808: astore 17
        //   1810: aload 15
        //   1812: astore 19
        //   1814: aload 14
        //   1816: astore 20
        //   1818: aload 16
        //   1820: astore 20
        //   1822: aload 15
        //   1824: astore 26
        //   1826: aload 16
        //   1828: iload_2
        //   1829: invokeinterface 125 2 0
        //   1834: istore 8
        //   1836: aload 14
        //   1838: astore 18
        //   1840: aload 16
        //   1842: astore 17
        //   1844: aload 15
        //   1846: astore 19
        //   1848: aload 14
        //   1850: astore 20
        //   1852: aload 16
        //   1854: astore 20
        //   1856: aload 15
        //   1858: astore 26
        //   1860: aload 16
        //   1862: iload_3
        //   1863: invokeinterface 129 2 0
        //   1868: astore 25
        //   1870: aload 14
        //   1872: astore 18
        //   1874: aload 16
        //   1876: astore 17
        //   1878: aload 15
        //   1880: astore 19
        //   1882: aload 14
        //   1884: astore 20
        //   1886: aload 16
        //   1888: astore 20
        //   1890: aload 15
        //   1892: astore 26
        //   1894: aload 16
        //   1896: iload 4
        //   1898: invokeinterface 129 2 0
        //   1903: astore 23
        //   1905: aload 14
        //   1907: astore 18
        //   1909: aload 16
        //   1911: astore 17
        //   1913: aload 15
        //   1915: astore 19
        //   1917: aload 14
        //   1919: astore 20
        //   1921: aload 16
        //   1923: astore 20
        //   1925: aload 15
        //   1927: astore 26
        //   1929: aload 16
        //   1931: iload 5
        //   1933: invokeinterface 133 2 0
        //   1938: lstore 10
        //   1940: aload 14
        //   1942: astore 18
        //   1944: aload 16
        //   1946: astore 17
        //   1948: aload 15
        //   1950: astore 19
        //   1952: aload 14
        //   1954: astore 20
        //   1956: aload 16
        //   1958: astore 20
        //   1960: aload 15
        //   1962: astore 26
        //   1964: aload 16
        //   1966: iload 6
        //   1968: invokeinterface 133 2 0
        //   1973: lstore 12
        //   1975: aload 23
        //   1977: ifnull -255 -> 1722
        //   1980: aload 14
        //   1982: astore 18
        //   1984: aload 16
        //   1986: astore 17
        //   1988: aload 15
        //   1990: astore 19
        //   1992: aload 14
        //   1994: astore 20
        //   1996: aload 16
        //   1998: astore 20
        //   2000: aload 15
        //   2002: astore 26
        //   2004: aload 23
        //   2006: invokevirtual 139	java/lang/String:length	()I
        //   2009: ifeq -287 -> 1722
        //   2012: aload 14
        //   2014: astore 18
        //   2016: aload 16
        //   2018: astore 17
        //   2020: aload 15
        //   2022: astore 19
        //   2024: aload 14
        //   2026: astore 20
        //   2028: aload 16
        //   2030: astore 20
        //   2032: aload 15
        //   2034: astore 26
        //   2036: new 141	org/telegram/messenger/MediaController$PhotoEntry
        //   2039: dup
        //   2040: iload 8
        //   2042: iload 7
        //   2044: lload 10
        //   2046: aload 23
        //   2048: lload 12
        //   2050: ldc2_w 206
        //   2053: ldiv
        //   2054: l2i
        //   2055: iconst_1
        //   2056: invokespecial 144	org/telegram/messenger/MediaController$PhotoEntry:<init>	(IIJLjava/lang/String;IZ)V
        //   2059: astore 22
        //   2061: aload 14
        //   2063: ifnonnull +649 -> 2712
        //   2066: aload 14
        //   2068: astore 18
        //   2070: aload 16
        //   2072: astore 17
        //   2074: aload 15
        //   2076: astore 19
        //   2078: aload 14
        //   2080: astore 20
        //   2082: aload 16
        //   2084: astore 20
        //   2086: aload 15
        //   2088: astore 26
        //   2090: new 146	org/telegram/messenger/MediaController$AlbumEntry
        //   2093: dup
        //   2094: iconst_0
        //   2095: ldc -93
        //   2097: ldc -92
        //   2099: invokestatic 154	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
        //   2102: aload 22
        //   2104: invokespecial 157	org/telegram/messenger/MediaController$AlbumEntry:<init>	(ILjava/lang/String;Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   2107: astore 14
        //   2109: aload 14
        //   2111: astore 17
        //   2113: aload 35
        //   2115: iconst_0
        //   2116: aload 14
        //   2118: invokevirtual 161	java/util/ArrayList:add	(ILjava/lang/Object;)V
        //   2121: aload 14
        //   2123: astore 17
        //   2125: aload 14
        //   2127: aload 22
        //   2129: invokevirtual 168	org/telegram/messenger/MediaController$AlbumEntry:addPhoto	(Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   2132: aload 14
        //   2134: astore 17
        //   2136: aload 37
        //   2138: iload 8
        //   2140: invokevirtual 172	android/util/SparseArray:get	(I)Ljava/lang/Object;
        //   2143: checkcast 146	org/telegram/messenger/MediaController$AlbumEntry
        //   2146: astore 18
        //   2148: aload 18
        //   2150: astore 17
        //   2152: aload 18
        //   2154: ifnonnull +262 -> 2416
        //   2157: aload 14
        //   2159: astore 17
        //   2161: new 146	org/telegram/messenger/MediaController$AlbumEntry
        //   2164: dup
        //   2165: iload 8
        //   2167: aload 25
        //   2169: aload 22
        //   2171: invokespecial 157	org/telegram/messenger/MediaController$AlbumEntry:<init>	(ILjava/lang/String;Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   2174: astore 18
        //   2176: aload 14
        //   2178: astore 17
        //   2180: aload 37
        //   2182: iload 8
        //   2184: aload 18
        //   2186: invokevirtual 175	android/util/SparseArray:put	(ILjava/lang/Object;)V
        //   2189: aload 15
        //   2191: ifnonnull +209 -> 2400
        //   2194: aload 24
        //   2196: ifnull +204 -> 2400
        //   2199: aload 23
        //   2201: ifnull +199 -> 2400
        //   2204: aload 14
        //   2206: astore 17
        //   2208: aload 23
        //   2210: aload 24
        //   2212: invokevirtual 179	java/lang/String:startsWith	(Ljava/lang/String;)Z
        //   2215: ifeq +185 -> 2400
        //   2218: aload 14
        //   2220: astore 17
        //   2222: aload 35
        //   2224: iconst_0
        //   2225: aload 18
        //   2227: invokevirtual 161	java/util/ArrayList:add	(ILjava/lang/Object;)V
        //   2230: iload 8
        //   2232: invokestatic 185	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   2235: astore 15
        //   2237: aload 16
        //   2239: astore 17
        //   2241: aload 18
        //   2243: aload 22
        //   2245: invokevirtual 168	org/telegram/messenger/MediaController$AlbumEntry:addPhoto	(Lorg/telegram/messenger/MediaController$PhotoEntry;)V
        //   2248: goto -526 -> 1722
        //   2251: aload 21
        //   2253: astore 18
        //   2255: aload 14
        //   2257: astore 20
        //   2259: aload 17
        //   2261: astore 22
        //   2263: aload 15
        //   2265: astore 23
        //   2267: aload 15
        //   2269: astore 19
        //   2271: aload 36
        //   2273: aload 16
        //   2275: invokevirtual 194	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   2278: pop
        //   2279: aload 25
        //   2281: astore 26
        //   2283: goto -1095 -> 1188
        //   2286: astore 14
        //   2288: aload 19
        //   2290: astore 15
        //   2292: aload 15
        //   2294: ifnull +10 -> 2304
        //   2297: aload 15
        //   2299: invokeinterface 197 1 0
        //   2304: aload 14
        //   2306: athrow
        //   2307: aload 17
        //   2309: astore 21
        //   2311: aload 15
        //   2313: astore 17
        //   2315: aload 16
        //   2317: astore 22
        //   2319: aload 14
        //   2321: astore 20
        //   2323: aload 21
        //   2325: astore 19
        //   2327: aload 17
        //   2329: astore 15
        //   2331: aload 17
        //   2333: ifnull +382 -> 2715
        //   2336: aload 17
        //   2338: invokeinterface 197 1 0
        //   2343: aload 16
        //   2345: astore 15
        //   2347: aload 17
        //   2349: astore 16
        //   2351: goto -1021 -> 1330
        //   2354: astore 15
        //   2356: aload 15
        //   2358: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   2361: aload 16
        //   2363: astore 15
        //   2365: aload 17
        //   2367: astore 16
        //   2369: goto -1039 -> 1330
        //   2372: astore 15
        //   2374: aload 15
        //   2376: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   2379: aload 18
        //   2381: astore 15
        //   2383: aload 17
        //   2385: astore 21
        //   2387: goto -1057 -> 1330
        //   2390: astore 15
        //   2392: aload 15
        //   2394: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   2397: goto -93 -> 2304
        //   2400: aload 14
        //   2402: astore 17
        //   2404: aload 35
        //   2406: aload 18
        //   2408: invokevirtual 194	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   2411: pop
        //   2412: aload 18
        //   2414: astore 17
        //   2416: aload 17
        //   2418: astore 18
        //   2420: goto -183 -> 2237
        //   2423: aload 23
        //   2425: astore 17
        //   2427: aload 22
        //   2429: astore 18
        //   2431: aload 25
        //   2433: ifnull +18 -> 2451
        //   2436: aload 25
        //   2438: invokeinterface 197 1 0
        //   2443: aload 22
        //   2445: astore 18
        //   2447: aload 23
        //   2449: astore 17
        //   2451: iconst_0
        //   2452: istore_1
        //   2453: iload_1
        //   2454: aload 35
        //   2456: invokevirtual 210	java/util/ArrayList:size	()I
        //   2459: if_icmpge +158 -> 2617
        //   2462: aload 35
        //   2464: iload_1
        //   2465: invokevirtual 211	java/util/ArrayList:get	(I)Ljava/lang/Object;
        //   2468: checkcast 146	org/telegram/messenger/MediaController$AlbumEntry
        //   2471: getfield 215	org/telegram/messenger/MediaController$AlbumEntry:photos	Ljava/util/ArrayList;
        //   2474: new 13	org/telegram/messenger/MediaController$29$1
        //   2477: dup
        //   2478: aload_0
        //   2479: invokespecial 218	org/telegram/messenger/MediaController$29$1:<init>	(Lorg/telegram/messenger/MediaController$29;)V
        //   2482: invokestatic 224	java/util/Collections:sort	(Ljava/util/List;Ljava/util/Comparator;)V
        //   2485: iload_1
        //   2486: iconst_1
        //   2487: iadd
        //   2488: istore_1
        //   2489: goto -36 -> 2453
        //   2492: astore 14
        //   2494: aload 14
        //   2496: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   2499: aload 23
        //   2501: astore 17
        //   2503: aload 22
        //   2505: astore 18
        //   2507: goto -56 -> 2451
        //   2510: astore 16
        //   2512: aload 19
        //   2514: astore 15
        //   2516: aload 18
        //   2518: astore 14
        //   2520: aload 16
        //   2522: astore 18
        //   2524: aload 17
        //   2526: astore 16
        //   2528: aload 16
        //   2530: astore 17
        //   2532: aload 18
        //   2534: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   2537: aload 15
        //   2539: astore 17
        //   2541: aload 14
        //   2543: astore 18
        //   2545: aload 16
        //   2547: ifnull -96 -> 2451
        //   2550: aload 16
        //   2552: invokeinterface 197 1 0
        //   2557: aload 15
        //   2559: astore 17
        //   2561: aload 14
        //   2563: astore 18
        //   2565: goto -114 -> 2451
        //   2568: astore 16
        //   2570: aload 16
        //   2572: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   2575: aload 15
        //   2577: astore 17
        //   2579: aload 14
        //   2581: astore 18
        //   2583: goto -132 -> 2451
        //   2586: astore 14
        //   2588: aload 20
        //   2590: astore 16
        //   2592: aload 16
        //   2594: ifnull +10 -> 2604
        //   2597: aload 16
        //   2599: invokeinterface 197 1 0
        //   2604: aload 14
        //   2606: athrow
        //   2607: astore 15
        //   2609: aload 15
        //   2611: invokestatic 191	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   2614: goto -10 -> 2604
        //   2617: aload_0
        //   2618: getfield 18	org/telegram/messenger/MediaController$29:val$guid	I
        //   2621: aload 35
        //   2623: aload 36
        //   2625: aload 17
        //   2627: aload 18
        //   2629: aload 21
        //   2631: iconst_0
        //   2632: invokestatic 228	org/telegram/messenger/MediaController:access$7900	(ILjava/util/ArrayList;Ljava/util/ArrayList;Ljava/lang/Integer;Lorg/telegram/messenger/MediaController$AlbumEntry;Lorg/telegram/messenger/MediaController$AlbumEntry;I)V
        //   2635: return
        //   2636: astore 14
        //   2638: goto -46 -> 2592
        //   2641: astore 14
        //   2643: aload 17
        //   2645: astore 16
        //   2647: goto -55 -> 2592
        //   2650: astore 18
        //   2652: aload 17
        //   2654: astore 14
        //   2656: goto -128 -> 2528
        //   2659: astore 18
        //   2661: goto -133 -> 2528
        //   2664: astore 14
        //   2666: goto -374 -> 2292
        //   2669: astore 14
        //   2671: goto -379 -> 2292
        //   2674: astore 19
        //   2676: aload 16
        //   2678: astore 18
        //   2680: aload 15
        //   2682: astore 16
        //   2684: aload 19
        //   2686: astore 15
        //   2688: goto -1403 -> 1285
        //   2691: astore 20
        //   2693: aload 16
        //   2695: astore 18
        //   2697: aload 19
        //   2699: astore 17
        //   2701: aload 15
        //   2703: astore 16
        //   2705: aload 20
        //   2707: astore 15
        //   2709: goto -1424 -> 1285
        //   2712: goto -591 -> 2121
        //   2715: aload 20
        //   2717: astore 14
        //   2719: aload 19
        //   2721: astore 21
        //   2723: aload 15
        //   2725: astore 16
        //   2727: aload 22
        //   2729: astore 15
        //   2731: goto -1401 -> 1330
        //   2734: goto -2045 -> 689
        //   2737: goto -2108 -> 629
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	2740	0	this	29
        //   315	2174	1	i	int
        //   345	1484	2	j	int
        //   375	1488	3	k	int
        //   405	1492	4	m	int
        //   436	1496	5	n	int
        //   467	1500	6	i1	int
        //   501	1542	7	i2	int
        //   511	1720	8	i3	int
        //   554	28	9	i4	int
        //   543	1502	10	l1	long
        //   1973	76	12	l2	long
        //   78	1115	14	localObject1	Object
        //   1226	15	14	localException1	Exception
        //   1283	973	14	localObject2	Object
        //   2286	115	14	localObject3	Object
        //   2492	3	14	localException2	Exception
        //   2518	62	14	localObject4	Object
        //   2586	19	14	localObject5	Object
        //   2636	1	14	localObject6	Object
        //   2641	1	14	localObject7	Object
        //   2654	1	14	localObject8	Object
        //   2664	1	14	localObject9	Object
        //   2669	1	14	localObject10	Object
        //   2717	1	14	localObject11	Object
        //   103	1150	15	localCursor	Cursor
        //   1271	19	15	localThrowable1	Throwable
        //   1308	1038	15	localObject12	Object
        //   2354	3	15	localException3	Exception
        //   2363	1	15	localObject13	Object
        //   2372	3	15	localException4	Exception
        //   2381	1	15	localObject14	Object
        //   2390	3	15	localException5	Exception
        //   2514	62	15	localObject15	Object
        //   2607	74	15	localException6	Exception
        //   2686	44	15	localObject16	Object
        //   155	2213	16	localObject17	Object
        //   2510	11	16	localThrowable2	Throwable
        //   2526	25	16	localObject18	Object
        //   2568	3	16	localException7	Exception
        //   2590	136	16	localObject19	Object
        //   167	2533	17	localObject20	Object
        //   107	2521	18	localObject21	Object
        //   2650	1	18	localThrowable3	Throwable
        //   2659	1	18	localThrowable4	Throwable
        //   2678	18	18	localObject22	Object
        //   123	2390	19	localObject23	Object
        //   2674	46	19	localThrowable5	Throwable
        //   111	2478	20	localObject24	Object
        //   2691	25	20	localThrowable6	Throwable
        //   163	2559	21	localObject25	Object
        //   115	2613	22	localObject26	Object
        //   119	2381	23	localObject27	Object
        //   49	2162	24	localObject28	Object
        //   40	2397	25	localObject29	Object
        //   91	2191	26	localObject30	Object
        //   46	974	27	localAlbumEntry	MediaController.AlbumEntry
        //   37	1174	28	localPhotoEntry	MediaController.PhotoEntry
        //   43	1081	29	str1	String
        //   94	957	30	str2	String
        //   85	354	31	localObject31	Object
        //   88	178	32	localObject32	Object
        //   97	144	33	localObject33	Object
        //   100	110	34	localObject34	Object
        //   7	2615	35	localArrayList1	ArrayList
        //   16	2608	36	localArrayList2	ArrayList
        //   25	2156	37	localSparseArray1	SparseArray
        //   34	1046	38	localSparseArray2	SparseArray
        // Exception table:
        //   from	to	target	type
        //   51	80	1226	java/lang/Exception
        //   125	133	1271	java/lang/Throwable
        //   169	177	1271	java/lang/Throwable
        //   213	224	1271	java/lang/Throwable
        //   244	265	1271	java/lang/Throwable
        //   306	316	1271	java/lang/Throwable
        //   336	346	1271	java/lang/Throwable
        //   366	376	1271	java/lang/Throwable
        //   396	407	1271	java/lang/Throwable
        //   427	438	1271	java/lang/Throwable
        //   458	469	1271	java/lang/Throwable
        //   677	685	1271	java/lang/Throwable
        //   709	716	1271	java/lang/Throwable
        //   736	743	1271	java/lang/Throwable
        //   763	775	1271	java/lang/Throwable
        //   808	823	1271	java/lang/Throwable
        //   843	852	1271	java/lang/Throwable
        //   887	897	1271	java/lang/Throwable
        //   917	925	1271	java/lang/Throwable
        //   945	952	1271	java/lang/Throwable
        //   972	979	1271	java/lang/Throwable
        //   999	1011	1271	java/lang/Throwable
        //   1044	1059	1271	java/lang/Throwable
        //   1079	1088	1271	java/lang/Throwable
        //   1123	1133	1271	java/lang/Throwable
        //   1153	1161	1271	java/lang/Throwable
        //   1181	1188	1271	java/lang/Throwable
        //   1208	1215	1271	java/lang/Throwable
        //   1256	1264	1271	java/lang/Throwable
        //   2271	2279	1271	java/lang/Throwable
        //   125	133	2286	finally
        //   169	177	2286	finally
        //   213	224	2286	finally
        //   244	265	2286	finally
        //   306	316	2286	finally
        //   336	346	2286	finally
        //   366	376	2286	finally
        //   396	407	2286	finally
        //   427	438	2286	finally
        //   458	469	2286	finally
        //   677	685	2286	finally
        //   709	716	2286	finally
        //   736	743	2286	finally
        //   763	775	2286	finally
        //   808	823	2286	finally
        //   843	852	2286	finally
        //   887	897	2286	finally
        //   917	925	2286	finally
        //   945	952	2286	finally
        //   972	979	2286	finally
        //   999	1011	2286	finally
        //   1044	1059	2286	finally
        //   1079	1088	2286	finally
        //   1123	1133	2286	finally
        //   1153	1161	2286	finally
        //   1181	1188	2286	finally
        //   1208	1215	2286	finally
        //   1256	1264	2286	finally
        //   1289	1294	2286	finally
        //   2271	2279	2286	finally
        //   2336	2343	2354	java/lang/Exception
        //   1315	1322	2372	java/lang/Exception
        //   2297	2304	2390	java/lang/Exception
        //   2436	2443	2492	java/lang/Exception
        //   1354	1362	2510	java/lang/Throwable
        //   1398	1406	2510	java/lang/Throwable
        //   1442	1453	2510	java/lang/Throwable
        //   1477	1498	2510	java/lang/Throwable
        //   1539	1549	2510	java/lang/Throwable
        //   1573	1583	2510	java/lang/Throwable
        //   1607	1617	2510	java/lang/Throwable
        //   1641	1652	2510	java/lang/Throwable
        //   1676	1687	2510	java/lang/Throwable
        //   1711	1722	2510	java/lang/Throwable
        //   1758	1768	2510	java/lang/Throwable
        //   1792	1802	2510	java/lang/Throwable
        //   1826	1836	2510	java/lang/Throwable
        //   1860	1870	2510	java/lang/Throwable
        //   1894	1905	2510	java/lang/Throwable
        //   1929	1940	2510	java/lang/Throwable
        //   1964	1975	2510	java/lang/Throwable
        //   2004	2012	2510	java/lang/Throwable
        //   2036	2061	2510	java/lang/Throwable
        //   2090	2109	2510	java/lang/Throwable
        //   2550	2557	2568	java/lang/Exception
        //   1354	1362	2586	finally
        //   1398	1406	2586	finally
        //   1442	1453	2586	finally
        //   1477	1498	2586	finally
        //   1539	1549	2586	finally
        //   1573	1583	2586	finally
        //   1607	1617	2586	finally
        //   1641	1652	2586	finally
        //   1676	1687	2586	finally
        //   1711	1722	2586	finally
        //   1758	1768	2586	finally
        //   1792	1802	2586	finally
        //   1826	1836	2586	finally
        //   1860	1870	2586	finally
        //   1894	1905	2586	finally
        //   1929	1940	2586	finally
        //   1964	1975	2586	finally
        //   2004	2012	2586	finally
        //   2036	2061	2586	finally
        //   2090	2109	2586	finally
        //   2597	2604	2607	java/lang/Exception
        //   2113	2121	2636	finally
        //   2125	2132	2636	finally
        //   2136	2148	2636	finally
        //   2161	2176	2636	finally
        //   2180	2189	2636	finally
        //   2208	2218	2636	finally
        //   2222	2230	2636	finally
        //   2404	2412	2636	finally
        //   2241	2248	2641	finally
        //   2532	2537	2641	finally
        //   2113	2121	2650	java/lang/Throwable
        //   2125	2132	2650	java/lang/Throwable
        //   2136	2148	2650	java/lang/Throwable
        //   2161	2176	2650	java/lang/Throwable
        //   2180	2189	2650	java/lang/Throwable
        //   2208	2218	2650	java/lang/Throwable
        //   2222	2230	2650	java/lang/Throwable
        //   2404	2412	2650	java/lang/Throwable
        //   2241	2248	2659	java/lang/Throwable
        //   483	556	2664	finally
        //   561	589	2664	finally
        //   594	613	2664	finally
        //   617	625	2669	finally
        //   638	657	2669	finally
        //   483	556	2674	java/lang/Throwable
        //   561	589	2674	java/lang/Throwable
        //   594	613	2674	java/lang/Throwable
        //   617	625	2691	java/lang/Throwable
        //   638	657	2691	java/lang/Throwable
      }
    });
    localThread.setPriority(1);
    localThread.start();
  }
  
  private native int openOpusFile(String paramString);
  
  private void playNextMessageWithoutOrder(boolean paramBoolean)
  {
    ArrayList localArrayList;
    if (SharedConfig.shuffleMusic)
    {
      localArrayList = this.shuffledPlaylist;
      if ((!paramBoolean) || (SharedConfig.repeatMode != 2) || (this.forceLoopCurrentPlaylist)) {
        break label60;
      }
      cleanupPlayer(false, false);
      playMessage((MessageObject)localArrayList.get(this.currentPlaylistNum));
    }
    label60:
    label266:
    label441:
    do
    {
      int i;
      do
      {
        return;
        localArrayList = this.playlist;
        break;
        i = 0;
        if (!SharedConfig.playOrderReversed) {
          break label266;
        }
        this.currentPlaylistNum += 1;
        if (this.currentPlaylistNum >= localArrayList.size())
        {
          this.currentPlaylistNum = 0;
          i = 1;
        }
        if ((i == 0) || (!paramBoolean) || (SharedConfig.repeatMode != 0) || (this.forceLoopCurrentPlaylist)) {
          break label441;
        }
      } while ((this.audioPlayer == null) && (this.audioTrackPlayer == null) && (this.videoPlayer == null));
      if (this.audioPlayer != null) {}
      for (;;)
      {
        try
        {
          this.audioPlayer.releasePlayer();
          this.audioPlayer = null;
          stopProgressTimer();
          this.lastProgress = 0L;
          this.buffersWrited = 0;
          this.isPaused = true;
          this.playingMessageObject.audioProgress = 0.0F;
          this.playingMessageObject.audioProgressSec = 0;
          NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, new Object[] { Integer.valueOf(this.playingMessageObject.getId()), Integer.valueOf(0) });
          NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, new Object[] { Integer.valueOf(this.playingMessageObject.getId()) });
          return;
          this.currentPlaylistNum -= 1;
          if (this.currentPlaylistNum >= 0) {
            break;
          }
          this.currentPlaylistNum = (localArrayList.size() - 1);
          i = 1;
        }
        catch (Exception localException1)
        {
          FileLog.e(localException1);
          continue;
        }
        if (this.audioTrackPlayer != null)
        {
          try
          {
            synchronized (this.playerObjectSync)
            {
              this.audioTrackPlayer.pause();
              this.audioTrackPlayer.flush();
            }
          }
          catch (Exception localException3)
          {
            try
            {
              for (;;)
              {
                this.audioTrackPlayer.release();
                this.audioTrackPlayer = null;
                break;
                localObject2 = finally;
                throw ((Throwable)localObject2);
                localException3 = localException3;
                FileLog.e(localException3);
              }
            }
            catch (Exception localException4)
            {
              for (;;)
              {
                FileLog.e(localException4);
              }
            }
          }
        }
        else if (this.videoPlayer != null)
        {
          this.currentAspectRatioFrameLayout = null;
          this.currentTextureViewContainer = null;
          this.currentAspectRatioFrameLayoutReady = false;
          this.currentTextureView = null;
          this.videoPlayer.releasePlayer();
          this.videoPlayer = null;
          try
          {
            this.baseActivity.getWindow().clearFlags(128);
          }
          catch (Exception localException2)
          {
            FileLog.e(localException2);
          }
        }
      }
    } while ((this.currentPlaylistNum < 0) || (this.currentPlaylistNum >= localException2.size()));
    if (this.playingMessageObject != null) {
      this.playingMessageObject.resetPlayingProgress();
    }
    this.playMusicAgain = true;
    playMessage((MessageObject)localException2.get(this.currentPlaylistNum));
  }
  
  private void processMediaObserver(Uri paramUri)
  {
    final ArrayList localArrayList;
    label237:
    do
    {
      try
      {
        android.graphics.Point localPoint = AndroidUtilities.getRealScreenSize();
        paramUri = ApplicationLoader.applicationContext.getContentResolver().query(paramUri, this.mediaProjections, null, null, "date_added DESC LIMIT 1");
        localArrayList = new ArrayList();
        if (paramUri != null)
        {
          while (paramUri.moveToNext())
          {
            String str1 = paramUri.getString(0);
            Object localObject = paramUri.getString(1);
            String str2 = paramUri.getString(2);
            long l = paramUri.getLong(3);
            String str3 = paramUri.getString(4);
            int j = paramUri.getInt(5);
            int k = paramUri.getInt(6);
            if (((str1 == null) || (!str1.toLowerCase().contains("screenshot"))) && ((localObject == null) || (!((String)localObject).toLowerCase().contains("screenshot"))) && ((str2 == null) || (!str2.toLowerCase().contains("screenshot"))))
            {
              if (str3 != null)
              {
                boolean bool = str3.toLowerCase().contains("screenshot");
                if (!bool) {}
              }
            }
            else
            {
              int i;
              if (j != 0)
              {
                i = k;
                if (k != 0) {
                  break label237;
                }
              }
              try
              {
                localObject = new BitmapFactory.Options();
                ((BitmapFactory.Options)localObject).inJustDecodeBounds = true;
                BitmapFactory.decodeFile(str1, (BitmapFactory.Options)localObject);
                j = ((BitmapFactory.Options)localObject).outWidth;
                i = ((BitmapFactory.Options)localObject).outHeight;
                if ((j <= 0) || (i <= 0) || ((j == localPoint.x) && (i == localPoint.y)) || ((i == localPoint.x) && (j == localPoint.y))) {
                  localArrayList.add(Long.valueOf(l));
                }
              }
              catch (Exception localException)
              {
                localArrayList.add(Long.valueOf(l));
              }
            }
          }
          paramUri.close();
        }
      }
      catch (Exception paramUri)
      {
        FileLog.e(paramUri);
        return;
      }
    } while (localArrayList.isEmpty());
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        NotificationCenter.getInstance(MediaController.this.lastChatAccount).postNotificationName(NotificationCenter.screenshotTook, new Object[0]);
        MediaController.this.checkScreenshots(localArrayList);
      }
    });
  }
  
  private long readAndWriteTracks(MessageObject paramMessageObject, MediaExtractor paramMediaExtractor, MP4Builder paramMP4Builder, MediaCodec.BufferInfo paramBufferInfo, long paramLong1, long paramLong2, File paramFile, boolean paramBoolean)
    throws Exception
  {
    int i5 = findTrack(paramMediaExtractor, false);
    int i1;
    int k;
    int i2;
    int i;
    Object localObject;
    label85:
    int m;
    label146:
    long l1;
    int i4;
    int i6;
    label216:
    int i3;
    if (paramBoolean)
    {
      i1 = findTrack(paramMediaExtractor, true);
      int j = -1;
      k = -1;
      i2 = 0;
      i = 0;
      if (i5 >= 0)
      {
        paramMediaExtractor.selectTrack(i5);
        localObject = paramMediaExtractor.getTrackFormat(i5);
        j = paramMP4Builder.addTrack((MediaFormat)localObject, false);
        i = ((MediaFormat)localObject).getInteger("max-input-size");
        if (paramLong1 <= 0L) {
          break label446;
        }
        paramMediaExtractor.seekTo(paramLong1, 0);
      }
      m = i;
      if (i1 >= 0)
      {
        paramMediaExtractor.selectTrack(i1);
        localObject = paramMediaExtractor.getTrackFormat(i1);
        k = paramMP4Builder.addTrack((MediaFormat)localObject, true);
        m = Math.max(((MediaFormat)localObject).getInteger("max-input-size"), i);
        if (paramLong1 <= 0L) {
          break label455;
        }
        paramMediaExtractor.seekTo(paramLong1, 0);
      }
      localObject = ByteBuffer.allocateDirect(m);
      if ((i1 < 0) && (i5 < 0)) {
        break label785;
      }
      l1 = -1L;
      checkConversionCanceled();
      if (i2 != 0) {
        break label760;
      }
      checkConversionCanceled();
      i = 0;
      i4 = 0;
      paramBufferInfo.size = paramMediaExtractor.readSampleData((ByteBuffer)localObject, 0);
      i6 = paramMediaExtractor.getSampleTrackIndex();
      if (i6 != i5) {
        break label464;
      }
      m = j;
      if (m == -1) {
        break label728;
      }
      if (Build.VERSION.SDK_INT < 21)
      {
        ((ByteBuffer)localObject).position(0);
        ((ByteBuffer)localObject).limit(paramBufferInfo.size);
      }
      if (i6 == i1) {
        break label497;
      }
      byte[] arrayOfByte = ((ByteBuffer)localObject).array();
      if (arrayOfByte == null) {
        break label497;
      }
      i = ((ByteBuffer)localObject).arrayOffset();
      int i7 = i + ((ByteBuffer)localObject).limit();
      i3 = -1;
      label287:
      if (i > i7 - 4) {
        break label497;
      }
      if ((arrayOfByte[i] != 0) || (arrayOfByte[(i + 1)] != 0) || (arrayOfByte[(i + 2)] != 0) || (arrayOfByte[(i + 3)] != 1))
      {
        n = i3;
        if (i != i7 - 4) {}
      }
      else
      {
        if (i3 == -1) {
          break label490;
        }
        if (i == i7 - 4) {
          break label484;
        }
        n = 4;
        label366:
        n = i - i3 - n;
        arrayOfByte[i3] = ((byte)(n >> 24));
        arrayOfByte[(i3 + 1)] = ((byte)(n >> 16));
        arrayOfByte[(i3 + 2)] = ((byte)(n >> 8));
        arrayOfByte[(i3 + 3)] = ((byte)n);
      }
    }
    label446:
    label455:
    label464:
    label484:
    label490:
    for (int n = i;; n = i)
    {
      i += 1;
      i3 = n;
      break label287;
      i1 = -1;
      break;
      paramMediaExtractor.seekTo(0L, 0);
      break label85;
      paramMediaExtractor.seekTo(0L, 0);
      break label146;
      if (i6 == i1)
      {
        m = k;
        break label216;
      }
      m = -1;
      break label216;
      n = 0;
      break label366;
    }
    label497:
    label518:
    long l2;
    long l3;
    if (paramBufferInfo.size >= 0)
    {
      paramBufferInfo.presentationTimeUs = paramMediaExtractor.getSampleTime();
      n = i4;
      i = n;
      l2 = l1;
      if (paramBufferInfo.size > 0)
      {
        i = n;
        l2 = l1;
        if (n == 0)
        {
          l3 = l1;
          if (i6 == i5)
          {
            l3 = l1;
            if (paramLong1 > 0L)
            {
              l3 = l1;
              if (l1 == -1L) {
                l3 = paramBufferInfo.presentationTimeUs;
              }
            }
          }
          if ((paramLong2 >= 0L) && (paramBufferInfo.presentationTimeUs >= paramLong2)) {
            break label718;
          }
          paramBufferInfo.offset = 0;
          paramBufferInfo.flags = paramMediaExtractor.getSampleFlags();
          i = n;
          l2 = l3;
          if (paramMP4Builder.writeSampleData(m, (ByteBuffer)localObject, paramBufferInfo, false))
          {
            didWriteData(paramMessageObject, paramFile, false, false);
            l2 = l3;
            i = n;
          }
        }
      }
      label661:
      m = i;
      l3 = l2;
      if (i == 0)
      {
        paramMediaExtractor.advance();
        l3 = l2;
        m = i;
      }
    }
    for (;;)
    {
      l1 = l3;
      if (m == 0) {
        break;
      }
      i2 = 1;
      l1 = l3;
      break;
      paramBufferInfo.size = 0;
      n = 1;
      break label518;
      label718:
      i = 1;
      l2 = l3;
      break label661;
      label728:
      if (i6 == -1)
      {
        m = 1;
        l3 = l1;
      }
      else
      {
        paramMediaExtractor.advance();
        m = i;
        l3 = l1;
      }
    }
    label760:
    if (i5 >= 0) {
      paramMediaExtractor.unselectTrack(i5);
    }
    if (i1 >= 0) {
      paramMediaExtractor.unselectTrack(i1);
    }
    return l1;
    label785:
    return -1L;
  }
  
  private native void readOpusFile(ByteBuffer paramByteBuffer, int paramInt, int[] paramArrayOfInt);
  
  private void readSms() {}
  
  public static void saveFile(final String paramString1, Context paramContext, int paramInt, final String paramString2, final String paramString3)
  {
    if (paramString1 == null) {}
    final Object localObject1;
    final boolean[] arrayOfBoolean;
    do
    {
      do
      {
        return;
        localObject2 = null;
        localObject1 = localObject2;
        if (paramString1 != null)
        {
          localObject1 = localObject2;
          if (paramString1.length() != 0)
          {
            paramString1 = new File(paramString1);
            localObject1 = paramString1;
            if (!paramString1.exists()) {
              localObject1 = null;
            }
          }
        }
      } while (localObject1 == null);
      arrayOfBoolean = new boolean[1];
      arrayOfBoolean[0] = false;
    } while (!((File)localObject1).exists());
    Object localObject2 = null;
    Object localObject3 = null;
    paramString1 = (String)localObject3;
    if (paramContext != null)
    {
      paramString1 = (String)localObject3;
      if (paramInt == 0) {}
    }
    for (;;)
    {
      try
      {
        paramString1 = new AlertDialog(paramContext, 2);
        FileLog.e(paramContext);
      }
      catch (Exception paramContext)
      {
        try
        {
          paramString1.setMessage(LocaleController.getString("Loading", 2131493762));
          paramString1.setCanceledOnTouchOutside(false);
          paramString1.setCancelable(true);
          paramString1.setOnCancelListener(new DialogInterface.OnCancelListener()
          {
            public void onCancel(DialogInterface paramAnonymousDialogInterface)
            {
              this.val$cancelled[0] = true;
            }
          });
          paramString1.show();
          new Thread(new Runnable()
          {
            /* Error */
            public void run()
            {
              // Byte code:
              //   0: aload_0
              //   1: getfield 30	org/telegram/messenger/MediaController$28:val$type	I
              //   4: ifne +275 -> 279
              //   7: invokestatic 53	org/telegram/messenger/AndroidUtilities:generatePicturePath	()Ljava/io/File;
              //   10: astore 12
              //   12: aload 12
              //   14: invokevirtual 59	java/io/File:exists	()Z
              //   17: ifne +9 -> 26
              //   20: aload 12
              //   22: invokevirtual 62	java/io/File:createNewFile	()Z
              //   25: pop
              //   26: aconst_null
              //   27: astore 16
              //   29: aconst_null
              //   30: astore 15
              //   32: aconst_null
              //   33: astore 19
              //   35: aconst_null
              //   36: astore 18
              //   38: iconst_1
              //   39: istore_2
              //   40: invokestatic 68	java/lang/System:currentTimeMillis	()J
              //   43: lstore_3
              //   44: lload_3
              //   45: ldc2_w 69
              //   48: lsub
              //   49: lstore 5
              //   51: aload 18
              //   53: astore 13
              //   55: aload 19
              //   57: astore 14
              //   59: new 72	java/io/FileInputStream
              //   62: dup
              //   63: aload_0
              //   64: getfield 34	org/telegram/messenger/MediaController$28:val$sourceFile	Ljava/io/File;
              //   67: invokespecial 75	java/io/FileInputStream:<init>	(Ljava/io/File;)V
              //   70: invokevirtual 79	java/io/FileInputStream:getChannel	()Ljava/nio/channels/FileChannel;
              //   73: astore 17
              //   75: aload 18
              //   77: astore 13
              //   79: aload 17
              //   81: astore 15
              //   83: aload 19
              //   85: astore 14
              //   87: aload 17
              //   89: astore 16
              //   91: new 81	java/io/FileOutputStream
              //   94: dup
              //   95: aload 12
              //   97: invokespecial 82	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
              //   100: invokevirtual 83	java/io/FileOutputStream:getChannel	()Ljava/nio/channels/FileChannel;
              //   103: astore 18
              //   105: aload 18
              //   107: astore 13
              //   109: aload 17
              //   111: astore 15
              //   113: aload 18
              //   115: astore 14
              //   117: aload 17
              //   119: astore 16
              //   121: aload 17
              //   123: invokevirtual 88	java/nio/channels/FileChannel:size	()J
              //   126: lstore 9
              //   128: lconst_0
              //   129: lstore_3
              //   130: lload_3
              //   131: lload 9
              //   133: lcmp
              //   134: ifge +32 -> 166
              //   137: aload 18
              //   139: astore 13
              //   141: aload 17
              //   143: astore 15
              //   145: aload 18
              //   147: astore 14
              //   149: aload 17
              //   151: astore 16
              //   153: aload_0
              //   154: getfield 36	org/telegram/messenger/MediaController$28:val$cancelled	[Z
              //   157: iconst_0
              //   158: baload
              //   159: istore 11
              //   161: iload 11
              //   163: ifeq +344 -> 507
              //   166: aload 17
              //   168: ifnull +8 -> 176
              //   171: aload 17
              //   173: invokevirtual 91	java/nio/channels/FileChannel:close	()V
              //   176: iload_2
              //   177: istore_1
              //   178: aload 18
              //   180: ifnull +10 -> 190
              //   183: aload 18
              //   185: invokevirtual 91	java/nio/channels/FileChannel:close	()V
              //   188: iload_2
              //   189: istore_1
              //   190: aload_0
              //   191: getfield 36	org/telegram/messenger/MediaController$28:val$cancelled	[Z
              //   194: iconst_0
              //   195: baload
              //   196: ifeq +11 -> 207
              //   199: aload 12
              //   201: invokevirtual 94	java/io/File:delete	()Z
              //   204: pop
              //   205: iconst_0
              //   206: istore_1
              //   207: iload_1
              //   208: ifeq +52 -> 260
              //   211: aload_0
              //   212: getfield 30	org/telegram/messenger/MediaController$28:val$type	I
              //   215: iconst_2
              //   216: if_icmpne +543 -> 759
              //   219: getstatic 100	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
              //   222: ldc 102
              //   224: invokevirtual 108	android/content/Context:getSystemService	(Ljava/lang/String;)Ljava/lang/Object;
              //   227: checkcast 110	android/app/DownloadManager
              //   230: aload 12
              //   232: invokevirtual 114	java/io/File:getName	()Ljava/lang/String;
              //   235: aload 12
              //   237: invokevirtual 114	java/io/File:getName	()Ljava/lang/String;
              //   240: iconst_0
              //   241: aload_0
              //   242: getfield 40	org/telegram/messenger/MediaController$28:val$mime	Ljava/lang/String;
              //   245: aload 12
              //   247: invokevirtual 117	java/io/File:getAbsolutePath	()Ljava/lang/String;
              //   250: aload 12
              //   252: invokevirtual 120	java/io/File:length	()J
              //   255: iconst_1
              //   256: invokevirtual 124	android/app/DownloadManager:addCompletedDownload	(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;JZ)J
              //   259: pop2
              //   260: aload_0
              //   261: getfield 38	org/telegram/messenger/MediaController$28:val$finalProgress	Lorg/telegram/ui/ActionBar/AlertDialog;
              //   264: ifnull +14 -> 278
              //   267: new 15	org/telegram/messenger/MediaController$28$2
              //   270: dup
              //   271: aload_0
              //   272: invokespecial 127	org/telegram/messenger/MediaController$28$2:<init>	(Lorg/telegram/messenger/MediaController$28;)V
              //   275: invokestatic 131	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
              //   278: return
              //   279: aload_0
              //   280: getfield 30	org/telegram/messenger/MediaController$28:val$type	I
              //   283: iconst_1
              //   284: if_icmpne +11 -> 295
              //   287: invokestatic 134	org/telegram/messenger/AndroidUtilities:generateVideoPath	()Ljava/io/File;
              //   290: astore 12
              //   292: goto -280 -> 12
              //   295: aload_0
              //   296: getfield 30	org/telegram/messenger/MediaController$28:val$type	I
              //   299: iconst_2
              //   300: if_icmpne +158 -> 458
              //   303: getstatic 139	android/os/Environment:DIRECTORY_DOWNLOADS	Ljava/lang/String;
              //   306: invokestatic 143	android/os/Environment:getExternalStoragePublicDirectory	(Ljava/lang/String;)Ljava/io/File;
              //   309: astore 13
              //   311: aload 13
              //   313: invokevirtual 146	java/io/File:mkdir	()Z
              //   316: pop
              //   317: new 55	java/io/File
              //   320: dup
              //   321: aload 13
              //   323: aload_0
              //   324: getfield 32	org/telegram/messenger/MediaController$28:val$name	Ljava/lang/String;
              //   327: invokespecial 149	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
              //   330: astore 14
              //   332: aload 14
              //   334: astore 12
              //   336: aload 14
              //   338: invokevirtual 59	java/io/File:exists	()Z
              //   341: ifeq -329 -> 12
              //   344: aload_0
              //   345: getfield 32	org/telegram/messenger/MediaController$28:val$name	Ljava/lang/String;
              //   348: bipush 46
              //   350: invokevirtual 155	java/lang/String:lastIndexOf	(I)I
              //   353: istore_2
              //   354: iconst_0
              //   355: istore_1
              //   356: aload 14
              //   358: astore 12
              //   360: iload_1
              //   361: bipush 10
              //   363: if_icmpge -351 -> 12
              //   366: iload_2
              //   367: iconst_m1
              //   368: if_icmpeq +101 -> 469
              //   371: new 157	java/lang/StringBuilder
              //   374: dup
              //   375: invokespecial 158	java/lang/StringBuilder:<init>	()V
              //   378: aload_0
              //   379: getfield 32	org/telegram/messenger/MediaController$28:val$name	Ljava/lang/String;
              //   382: iconst_0
              //   383: iload_2
              //   384: invokevirtual 162	java/lang/String:substring	(II)Ljava/lang/String;
              //   387: invokevirtual 166	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
              //   390: ldc -88
              //   392: invokevirtual 166	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
              //   395: iload_1
              //   396: iconst_1
              //   397: iadd
              //   398: invokevirtual 171	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
              //   401: ldc -83
              //   403: invokevirtual 166	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
              //   406: aload_0
              //   407: getfield 32	org/telegram/messenger/MediaController$28:val$name	Ljava/lang/String;
              //   410: iload_2
              //   411: invokevirtual 176	java/lang/String:substring	(I)Ljava/lang/String;
              //   414: invokevirtual 166	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
              //   417: invokevirtual 179	java/lang/StringBuilder:toString	()Ljava/lang/String;
              //   420: astore 12
              //   422: new 55	java/io/File
              //   425: dup
              //   426: aload 13
              //   428: aload 12
              //   430: invokespecial 149	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
              //   433: astore 14
              //   435: aload 14
              //   437: astore 12
              //   439: aload 14
              //   441: invokevirtual 59	java/io/File:exists	()Z
              //   444: ifeq -432 -> 12
              //   447: iload_1
              //   448: iconst_1
              //   449: iadd
              //   450: istore_1
              //   451: aload 14
              //   453: astore 12
              //   455: goto -95 -> 360
              //   458: getstatic 182	android/os/Environment:DIRECTORY_MUSIC	Ljava/lang/String;
              //   461: invokestatic 143	android/os/Environment:getExternalStoragePublicDirectory	(Ljava/lang/String;)Ljava/io/File;
              //   464: astore 13
              //   466: goto -155 -> 311
              //   469: new 157	java/lang/StringBuilder
              //   472: dup
              //   473: invokespecial 158	java/lang/StringBuilder:<init>	()V
              //   476: aload_0
              //   477: getfield 32	org/telegram/messenger/MediaController$28:val$name	Ljava/lang/String;
              //   480: invokevirtual 166	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
              //   483: ldc -88
              //   485: invokevirtual 166	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
              //   488: iload_1
              //   489: iconst_1
              //   490: iadd
              //   491: invokevirtual 171	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
              //   494: ldc -83
              //   496: invokevirtual 166	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
              //   499: invokevirtual 179	java/lang/StringBuilder:toString	()Ljava/lang/String;
              //   502: astore 12
              //   504: goto -82 -> 422
              //   507: aload 18
              //   509: astore 13
              //   511: aload 17
              //   513: astore 15
              //   515: aload 18
              //   517: astore 14
              //   519: aload 17
              //   521: astore 16
              //   523: aload 18
              //   525: aload 17
              //   527: lload_3
              //   528: ldc2_w 183
              //   531: lload 9
              //   533: lload_3
              //   534: lsub
              //   535: invokestatic 190	java/lang/Math:min	(JJ)J
              //   538: invokevirtual 194	java/nio/channels/FileChannel:transferFrom	(Ljava/nio/channels/ReadableByteChannel;JJ)J
              //   541: pop2
              //   542: lload 5
              //   544: lstore 7
              //   546: aload 18
              //   548: astore 13
              //   550: aload 17
              //   552: astore 15
              //   554: aload 18
              //   556: astore 14
              //   558: aload 17
              //   560: astore 16
              //   562: aload_0
              //   563: getfield 38	org/telegram/messenger/MediaController$28:val$finalProgress	Lorg/telegram/ui/ActionBar/AlertDialog;
              //   566: ifnull +94 -> 660
              //   569: lload 5
              //   571: lstore 7
              //   573: aload 18
              //   575: astore 13
              //   577: aload 17
              //   579: astore 15
              //   581: aload 18
              //   583: astore 14
              //   585: aload 17
              //   587: astore 16
              //   589: lload 5
              //   591: invokestatic 68	java/lang/System:currentTimeMillis	()J
              //   594: ldc2_w 69
              //   597: lsub
              //   598: lcmp
              //   599: ifgt +61 -> 660
              //   602: aload 18
              //   604: astore 13
              //   606: aload 17
              //   608: astore 15
              //   610: aload 18
              //   612: astore 14
              //   614: aload 17
              //   616: astore 16
              //   618: invokestatic 68	java/lang/System:currentTimeMillis	()J
              //   621: lstore 7
              //   623: aload 18
              //   625: astore 13
              //   627: aload 17
              //   629: astore 15
              //   631: aload 18
              //   633: astore 14
              //   635: aload 17
              //   637: astore 16
              //   639: new 13	org/telegram/messenger/MediaController$28$1
              //   642: dup
              //   643: aload_0
              //   644: lload_3
              //   645: l2f
              //   646: lload 9
              //   648: l2f
              //   649: fdiv
              //   650: ldc -61
              //   652: fmul
              //   653: f2i
              //   654: invokespecial 198	org/telegram/messenger/MediaController$28$1:<init>	(Lorg/telegram/messenger/MediaController$28;I)V
              //   657: invokestatic 131	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
              //   660: lload_3
              //   661: ldc2_w 183
              //   664: ladd
              //   665: lstore_3
              //   666: lload 7
              //   668: lstore 5
              //   670: goto -540 -> 130
              //   673: astore 17
              //   675: aload 13
              //   677: astore 14
              //   679: aload 15
              //   681: astore 16
              //   683: aload 17
              //   685: invokestatic 204	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
              //   688: iconst_0
              //   689: istore_2
              //   690: aload 15
              //   692: ifnull +8 -> 700
              //   695: aload 15
              //   697: invokevirtual 91	java/nio/channels/FileChannel:close	()V
              //   700: iload_2
              //   701: istore_1
              //   702: aload 13
              //   704: ifnull -514 -> 190
              //   707: aload 13
              //   709: invokevirtual 91	java/nio/channels/FileChannel:close	()V
              //   712: iload_2
              //   713: istore_1
              //   714: goto -524 -> 190
              //   717: astore 13
              //   719: iload_2
              //   720: istore_1
              //   721: goto -531 -> 190
              //   724: astore 12
              //   726: aload 16
              //   728: ifnull +8 -> 736
              //   731: aload 16
              //   733: invokevirtual 91	java/nio/channels/FileChannel:close	()V
              //   736: aload 14
              //   738: ifnull +8 -> 746
              //   741: aload 14
              //   743: invokevirtual 91	java/nio/channels/FileChannel:close	()V
              //   746: aload 12
              //   748: athrow
              //   749: astore 12
              //   751: aload 12
              //   753: invokestatic 204	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
              //   756: goto -496 -> 260
              //   759: aload 12
              //   761: invokestatic 210	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
              //   764: invokestatic 214	org/telegram/messenger/AndroidUtilities:addMediaToGallery	(Landroid/net/Uri;)V
              //   767: goto -507 -> 260
              //   770: astore 13
              //   772: goto -596 -> 176
              //   775: astore 13
              //   777: iload_2
              //   778: istore_1
              //   779: goto -589 -> 190
              //   782: astore 14
              //   784: goto -84 -> 700
              //   787: astore 13
              //   789: goto -53 -> 736
              //   792: astore 13
              //   794: goto -48 -> 746
              // Local variable table:
              //   start	length	slot	name	signature
              //   0	797	0	this	28
              //   177	602	1	i	int
              //   39	739	2	j	int
              //   43	623	3	l1	long
              //   49	620	5	l2	long
              //   544	123	7	l3	long
              //   126	521	9	l4	long
              //   159	3	11	k	int
              //   10	493	12	localObject1	Object
              //   724	23	12	localObject2	Object
              //   749	11	12	localException1	Exception
              //   53	655	13	localObject3	Object
              //   717	1	13	localException2	Exception
              //   770	1	13	localException3	Exception
              //   775	1	13	localException4	Exception
              //   787	1	13	localException5	Exception
              //   792	1	13	localException6	Exception
              //   57	685	14	localObject4	Object
              //   782	1	14	localException7	Exception
              //   30	666	15	localObject5	Object
              //   27	705	16	localObject6	Object
              //   73	563	17	localFileChannel1	java.nio.channels.FileChannel
              //   673	11	17	localException8	Exception
              //   36	596	18	localFileChannel2	java.nio.channels.FileChannel
              //   33	51	19	localObject7	Object
              // Exception table:
              //   from	to	target	type
              //   59	75	673	java/lang/Exception
              //   91	105	673	java/lang/Exception
              //   121	128	673	java/lang/Exception
              //   153	161	673	java/lang/Exception
              //   523	542	673	java/lang/Exception
              //   562	569	673	java/lang/Exception
              //   589	602	673	java/lang/Exception
              //   618	623	673	java/lang/Exception
              //   639	660	673	java/lang/Exception
              //   707	712	717	java/lang/Exception
              //   59	75	724	finally
              //   91	105	724	finally
              //   121	128	724	finally
              //   153	161	724	finally
              //   523	542	724	finally
              //   562	569	724	finally
              //   589	602	724	finally
              //   618	623	724	finally
              //   639	660	724	finally
              //   683	688	724	finally
              //   0	12	749	java/lang/Exception
              //   12	26	749	java/lang/Exception
              //   40	44	749	java/lang/Exception
              //   190	205	749	java/lang/Exception
              //   211	260	749	java/lang/Exception
              //   279	292	749	java/lang/Exception
              //   295	311	749	java/lang/Exception
              //   311	332	749	java/lang/Exception
              //   336	354	749	java/lang/Exception
              //   371	422	749	java/lang/Exception
              //   422	435	749	java/lang/Exception
              //   439	447	749	java/lang/Exception
              //   458	466	749	java/lang/Exception
              //   469	504	749	java/lang/Exception
              //   746	749	749	java/lang/Exception
              //   759	767	749	java/lang/Exception
              //   171	176	770	java/lang/Exception
              //   183	188	775	java/lang/Exception
              //   695	700	782	java/lang/Exception
              //   731	736	787	java/lang/Exception
              //   741	746	792	java/lang/Exception
            }
          }).start();
          return;
        }
        catch (Exception paramContext)
        {
          for (;;) {}
        }
        paramContext = paramContext;
        paramString1 = (String)localObject2;
      }
    }
  }
  
  private native int seekOpusFile(float paramFloat);
  
  private void seekOpusPlayer(final float paramFloat)
  {
    if (paramFloat == 1.0F) {
      return;
    }
    if (!this.isPaused) {
      this.audioTrackPlayer.pause();
    }
    this.audioTrackPlayer.flush();
    this.fileDecodingQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MediaController.this.seekOpusFile(paramFloat);
        synchronized (MediaController.this.playerSync)
        {
          MediaController.this.freePlayerBuffers.addAll(MediaController.this.usedPlayerBuffers);
          MediaController.this.usedPlayerBuffers.clear();
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (!MediaController.this.isPaused)
              {
                MediaController.access$3902(MediaController.this, 3);
                MediaController.access$4102(MediaController.this, ((float)MediaController.this.currentTotalPcmDuration * MediaController.14.this.val$progress));
                if (MediaController.this.audioTrackPlayer != null) {
                  MediaController.this.audioTrackPlayer.play();
                }
                MediaController.access$4302(MediaController.this, (int)((float)MediaController.this.currentTotalPcmDuration / 48.0F * MediaController.14.this.val$progress));
                MediaController.this.checkPlayerQueue();
              }
            }
          });
          return;
        }
      }
    });
  }
  
  @SuppressLint({"NewApi"})
  public static MediaCodecInfo selectCodec(String paramString)
  {
    int k = MediaCodecList.getCodecCount();
    Object localObject1 = null;
    int i = 0;
    while (i < k)
    {
      MediaCodecInfo localMediaCodecInfo = MediaCodecList.getCodecInfoAt(i);
      Object localObject2;
      if (!localMediaCodecInfo.isEncoder())
      {
        localObject2 = localObject1;
        i += 1;
        localObject1 = localObject2;
      }
      else
      {
        String[] arrayOfString = localMediaCodecInfo.getSupportedTypes();
        int m = arrayOfString.length;
        int j = 0;
        for (;;)
        {
          localObject2 = localObject1;
          if (j >= m) {
            break;
          }
          localObject2 = localObject1;
          if (arrayOfString[j].equalsIgnoreCase(paramString))
          {
            localObject1 = localMediaCodecInfo;
            String str = ((MediaCodecInfo)localObject1).getName();
            localObject2 = localObject1;
            if (str != null)
            {
              if (!str.equals("OMX.SEC.avc.enc")) {
                return (MediaCodecInfo)localObject1;
              }
              localObject2 = localObject1;
              if (str.equals("OMX.SEC.AVC.Encoder")) {
                return (MediaCodecInfo)localObject1;
              }
            }
          }
          j += 1;
          localObject1 = localObject2;
        }
      }
    }
    return (MediaCodecInfo)localObject1;
  }
  
  @SuppressLint({"NewApi"})
  public static int selectColorFormat(MediaCodecInfo paramMediaCodecInfo, String paramString)
  {
    paramString = paramMediaCodecInfo.getCapabilitiesForType(paramString);
    int j = 0;
    int i = 0;
    while (i < paramString.colorFormats.length)
    {
      int k = paramString.colorFormats[i];
      if (isRecognizedFormat(k))
      {
        j = k;
        if ((!paramMediaCodecInfo.getName().equals("OMX.SEC.AVC.Encoder")) || (k != 19)) {
          return k;
        }
      }
      i += 1;
    }
    return j;
  }
  
  private void setPlayerVolume()
  {
    for (;;)
    {
      try
      {
        if (this.audioFocus == 1) {
          break label66;
        }
        f = 1.0F;
        if (this.audioPlayer != null)
        {
          this.audioPlayer.setVolume(f);
          return;
        }
        if (this.audioTrackPlayer != null)
        {
          this.audioTrackPlayer.setStereoVolume(f, f);
          return;
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
        return;
      }
      if (this.videoPlayer != null) {
        this.videoPlayer.setVolume(f);
      }
      return;
      label66:
      float f = 0.2F;
    }
  }
  
  private void setUseFrontSpeaker(boolean paramBoolean)
  {
    this.useFrontSpeaker = paramBoolean;
    AudioManager localAudioManager = NotificationsController.audioManager;
    if (this.useFrontSpeaker)
    {
      localAudioManager.setBluetoothScoOn(false);
      localAudioManager.setSpeakerphoneOn(false);
      return;
    }
    localAudioManager.setSpeakerphoneOn(true);
  }
  
  private void startAudioAgain(boolean paramBoolean)
  {
    int i = 0;
    if (this.playingMessageObject == null) {
      return;
    }
    NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.audioRouteChanged, new Object[] { Boolean.valueOf(this.useFrontSpeaker) });
    final Object localObject;
    if (this.videoPlayer != null)
    {
      localObject = this.videoPlayer;
      if (this.useFrontSpeaker) {}
      for (;;)
      {
        ((VideoPlayer)localObject).setStreamType(i);
        if (paramBoolean) {
          break;
        }
        this.videoPlayer.play();
        return;
        i = 3;
      }
      this.videoPlayer.pause();
      return;
    }
    if (this.audioPlayer != null) {}
    for (i = 1;; i = 0)
    {
      localObject = this.playingMessageObject;
      float f = this.playingMessageObject.audioProgress;
      cleanupPlayer(false, true);
      ((MessageObject)localObject).audioProgress = f;
      playMessage((MessageObject)localObject);
      if (!paramBoolean) {
        break;
      }
      if (i == 0) {
        break label163;
      }
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          MediaController.this.pauseMessage(localObject);
        }
      }, 100L);
      return;
    }
    label163:
    pauseMessage((MessageObject)localObject);
  }
  
  private void startProgressTimer(final MessageObject paramMessageObject)
  {
    synchronized (this.progressTimerSync)
    {
      Timer localTimer = this.progressTimer;
      if (localTimer != null) {}
      try
      {
        this.progressTimer.cancel();
        this.progressTimer = null;
        paramMessageObject.getFileName();
        this.progressTimer = new Timer();
        this.progressTimer.schedule(new TimerTask()
        {
          public void run()
          {
            synchronized (MediaController.this.sync)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  long l1;
                  long l2;
                  float f1;
                  if ((MediaController.6.this.val$currentPlayingMessageObject != null) && ((MediaController.this.audioPlayer != null) || (MediaController.this.audioTrackPlayer != null) || (MediaController.this.videoPlayer != null)) && (!MediaController.this.isPaused))
                  {
                    try
                    {
                      if (MediaController.this.ignoreFirstProgress != 0)
                      {
                        MediaController.access$3910(MediaController.this);
                        return;
                      }
                      if (MediaController.this.videoPlayer == null) {
                        break label299;
                      }
                      l1 = MediaController.this.videoPlayer.getDuration();
                      l2 = MediaController.this.videoPlayer.getCurrentPosition();
                      f1 = (float)MediaController.this.videoPlayer.getBufferedPosition() / (float)l1;
                      if (l1 < 0L) {
                        break label294;
                      }
                      f2 = (float)l2 / (float)l1;
                    }
                    catch (Exception localException)
                    {
                      FileLog.e(localException);
                      return;
                    }
                    MediaController.access$4302(MediaController.this, l2);
                    MediaController.6.this.val$currentPlayingMessageObject.audioPlayerDuration = ((int)(l1 / 1000L));
                    MediaController.6.this.val$currentPlayingMessageObject.audioProgress = f2;
                    MediaController.6.this.val$currentPlayingMessageObject.audioProgressSec = ((int)(MediaController.this.lastProgress / 1000L));
                    MediaController.6.this.val$currentPlayingMessageObject.bufferedProgress = f1;
                    NotificationCenter.getInstance(MediaController.6.this.val$currentPlayingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, new Object[] { Integer.valueOf(MediaController.6.this.val$currentPlayingMessageObject.getId()), Float.valueOf(f2) });
                    return;
                    label294:
                    f2 = 0.0F;
                    break label482;
                    label299:
                    if (MediaController.this.audioPlayer != null)
                    {
                      l1 = MediaController.this.audioPlayer.getDuration();
                      l2 = MediaController.this.audioPlayer.getCurrentPosition();
                      if ((l1 == -9223372036854775807L) || (l1 < 0L)) {
                        break label498;
                      }
                    }
                  }
                  label482:
                  label498:
                  for (float f2 = (float)l2 / (float)l1;; f2 = 0.0F)
                  {
                    f1 = (float)MediaController.this.audioPlayer.getBufferedPosition() / (float)l1;
                    if ((l1 != -9223372036854775807L) && (l2 >= 0L))
                    {
                      if (MediaController.this.seekToProgressPending == 0.0F) {
                        break;
                      }
                      return;
                      l1 = 0L;
                      long l3 = (int)((float)MediaController.this.lastPlayPcm / 48.0F);
                      f2 = (float)MediaController.this.lastPlayPcm / (float)MediaController.this.currentTotalPcmDuration;
                      f1 = 0.0F;
                      long l4 = MediaController.this.lastProgress;
                      l2 = l3;
                      if (l3 != l4) {
                        break;
                      }
                    }
                    do
                    {
                      return;
                    } while ((l2 < 0L) || (f2 >= 1.0F));
                    break;
                  }
                }
              });
              return;
            }
          }
        }, 0L, 17L);
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
  }
  
  private native int startRecord(String paramString);
  
  private boolean startVideoConvertFromQueue()
  {
    if (!this.videoConvertQueue.isEmpty()) {
      for (;;)
      {
        Intent localIntent;
        int i;
        synchronized (this.videoConvertSync)
        {
          this.cancelCurrentVideoConversion = false;
          ??? = (MessageObject)this.videoConvertQueue.get(0);
          localIntent = new Intent(ApplicationLoader.applicationContext, VideoEncodingService.class);
          localIntent.putExtra("path", ((MessageObject)???).messageOwner.attachPath);
          localIntent.putExtra("currentAccount", ((MessageObject)???).currentAccount);
          if (((MessageObject)???).messageOwner.media.document != null)
          {
            i = 0;
            if (i < ((MessageObject)???).messageOwner.media.document.attributes.size())
            {
              if (!((TLRPC.DocumentAttribute)((MessageObject)???).messageOwner.media.document.attributes.get(i) instanceof TLRPC.TL_documentAttributeAnimated)) {
                break label173;
              }
              localIntent.putExtra("gif", true);
            }
          }
          if (((MessageObject)???).getId() == 0) {}
        }
        try
        {
          ApplicationLoader.applicationContext.startService(localIntent);
          VideoConvertRunnable.runConversion((MessageObject)???);
          return true;
          localObject2 = finally;
          throw ((Throwable)localObject2);
          label173:
          i += 1;
        }
        catch (Throwable localThrowable)
        {
          for (;;)
          {
            FileLog.e(localThrowable);
          }
        }
      }
    }
    return false;
  }
  
  private void stopProgressTimer()
  {
    synchronized (this.progressTimerSync)
    {
      Timer localTimer = this.progressTimer;
      if (localTimer != null) {}
      try
      {
        this.progressTimer.cancel();
        this.progressTimer = null;
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
  }
  
  private native void stopRecord();
  
  private void stopRecordingInternal(final int paramInt)
  {
    if (paramInt != 0)
    {
      final TLRPC.TL_document localTL_document = this.recordingAudio;
      final File localFile = this.recordingAudioFile;
      this.fileEncodingQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MediaController.this.stopRecord();
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MediaController.25.this.val$audioToSend.date = ConnectionsManager.getInstance(MediaController.this.recordingCurrentAccount).getCurrentTime();
              MediaController.25.this.val$audioToSend.size = ((int)MediaController.25.this.val$recordingAudioFileToSend.length());
              Object localObject = new TLRPC.TL_documentAttributeAudio();
              ((TLRPC.TL_documentAttributeAudio)localObject).voice = true;
              ((TLRPC.TL_documentAttributeAudio)localObject).waveform = MediaController.this.getWaveform2(MediaController.this.recordSamples, MediaController.this.recordSamples.length);
              if (((TLRPC.TL_documentAttributeAudio)localObject).waveform != null) {
                ((TLRPC.TL_documentAttributeAudio)localObject).flags |= 0x4;
              }
              long l = MediaController.this.recordTimeCount;
              ((TLRPC.TL_documentAttributeAudio)localObject).duration = ((int)(MediaController.this.recordTimeCount / 1000L));
              MediaController.25.this.val$audioToSend.attributes.add(localObject);
              if (l > 700L)
              {
                if (MediaController.25.this.val$send == 1) {
                  SendMessagesHelper.getInstance(MediaController.this.recordingCurrentAccount).sendMessage(MediaController.25.this.val$audioToSend, null, MediaController.25.this.val$recordingAudioFileToSend.getAbsolutePath(), MediaController.this.recordDialogId, MediaController.this.recordReplyingMessageObject, null, null, null, null, 0);
                }
                NotificationCenter localNotificationCenter = NotificationCenter.getInstance(MediaController.this.recordingCurrentAccount);
                int i = NotificationCenter.audioDidSent;
                if (MediaController.25.this.val$send == 2)
                {
                  localObject = MediaController.25.this.val$audioToSend;
                  if (MediaController.25.this.val$send != 2) {
                    break label332;
                  }
                }
                label332:
                for (String str = MediaController.25.this.val$recordingAudioFileToSend.getAbsolutePath();; str = null)
                {
                  localNotificationCenter.postNotificationName(i, new Object[] { localObject, str });
                  return;
                  localObject = null;
                  break;
                }
              }
              MediaController.25.this.val$recordingAudioFileToSend.delete();
            }
          });
        }
      });
    }
    try
    {
      if (this.audioRecorder != null)
      {
        this.audioRecorder.release();
        this.audioRecorder = null;
      }
      this.recordingAudio = null;
      this.recordingAudioFile = null;
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
  
  private native int writeFrame(ByteBuffer paramByteBuffer, int paramInt);
  
  public void cancelVideoConvert(MessageObject arg1)
  {
    if (??? == null) {
      synchronized (this.videoConvertSync)
      {
        this.cancelCurrentVideoConversion = true;
        return;
      }
    }
    if (!this.videoConvertQueue.isEmpty())
    {
      int i = 0;
      while (i < this.videoConvertQueue.size())
      {
        MessageObject localMessageObject = (MessageObject)this.videoConvertQueue.get(i);
        if ((localMessageObject.getId() == ???.getId()) && (localMessageObject.currentAccount == ???.currentAccount))
        {
          if (i == 0) {
            synchronized (this.videoConvertSync)
            {
              this.cancelCurrentVideoConversion = true;
              return;
            }
          }
          this.videoConvertQueue.remove(i);
          return;
        }
        i += 1;
      }
    }
  }
  
  protected void checkIsNextMediaFileDownloaded()
  {
    if ((this.playingMessageObject == null) || (!this.playingMessageObject.isMusic())) {
      return;
    }
    checkIsNextMusicFileDownloaded(this.playingMessageObject.currentAccount);
  }
  
  public void cleanup()
  {
    cleanupPlayer(false, true);
    this.audioInfo = null;
    this.playMusicAgain = false;
    int i = 0;
    while (i < 3)
    {
      DownloadController.getInstance(i).cleanup();
      i += 1;
    }
    this.videoConvertQueue.clear();
    this.playlist.clear();
    this.shuffledPlaylist.clear();
    this.generatingWaveform.clear();
    this.voiceMessagesPlaylist = null;
    this.voiceMessagesPlaylistMap = null;
    cancelVideoConvert(null);
  }
  
  public void cleanupPlayer(boolean paramBoolean1, boolean paramBoolean2)
  {
    cleanupPlayer(paramBoolean1, paramBoolean2, false);
  }
  
  public void cleanupPlayer(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    if (this.audioPlayer != null) {}
    for (;;)
    {
      try
      {
        this.audioPlayer.releasePlayer();
        this.audioPlayer = null;
        stopProgressTimer();
        this.lastProgress = 0L;
        this.buffersWrited = 0;
        this.isPaused = false;
        Object localObject1;
        if ((!this.useFrontSpeaker) && (!SharedConfig.raiseToSpeak))
        {
          localObject1 = this.raiseChat;
          stopRaiseToEarSensors(this.raiseChat);
          this.raiseChat = ((ChatActivity)localObject1);
        }
        if (this.playingMessageObject != null)
        {
          if (this.downloadingCurrentMessage) {
            FileLoader.getInstance(this.playingMessageObject.currentAccount).cancelLoadFile(this.playingMessageObject.getDocument());
          }
          localObject1 = this.playingMessageObject;
          if (paramBoolean1)
          {
            this.playingMessageObject.resetPlayingProgress();
            NotificationCenter.getInstance(((MessageObject)localObject1).currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, new Object[] { Integer.valueOf(this.playingMessageObject.getId()), Integer.valueOf(0) });
          }
          this.playingMessageObject = null;
          this.downloadingCurrentMessage = false;
          if (paramBoolean1)
          {
            NotificationsController.audioManager.abandonAudioFocus(this);
            this.hasAudioFocus = 0;
            if (this.voiceMessagesPlaylist != null)
            {
              if ((!paramBoolean3) || (this.voiceMessagesPlaylist.get(0) != localObject1)) {
                break label487;
              }
              this.voiceMessagesPlaylist.remove(0);
              this.voiceMessagesPlaylistMap.remove(((MessageObject)localObject1).getId());
              if (this.voiceMessagesPlaylist.isEmpty())
              {
                this.voiceMessagesPlaylist = null;
                this.voiceMessagesPlaylistMap = null;
              }
            }
            if (this.voiceMessagesPlaylist == null) {
              break label500;
            }
            localObject1 = (MessageObject)this.voiceMessagesPlaylist.get(0);
            playMessage((MessageObject)localObject1);
            if ((!((MessageObject)localObject1).isRoundVideo()) && (this.pipRoundVideoView != null))
            {
              this.pipRoundVideoView.close(true);
              this.pipRoundVideoView = null;
            }
          }
          if (paramBoolean2)
          {
            localObject1 = new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class);
            ApplicationLoader.applicationContext.stopService((Intent)localObject1);
          }
        }
        return;
      }
      catch (Exception localException1)
      {
        FileLog.e(localException1);
        continue;
      }
      if (this.audioTrackPlayer != null)
      {
        try
        {
          synchronized (this.playerObjectSync)
          {
            this.audioTrackPlayer.pause();
            this.audioTrackPlayer.flush();
          }
        }
        catch (Exception localException3)
        {
          try
          {
            for (;;)
            {
              this.audioTrackPlayer.release();
              this.audioTrackPlayer = null;
              break;
              localObject3 = finally;
              throw ((Throwable)localObject3);
              localException3 = localException3;
              FileLog.e(localException3);
            }
          }
          catch (Exception localException4)
          {
            for (;;)
            {
              FileLog.e(localException4);
            }
          }
        }
      }
      else if (this.videoPlayer != null)
      {
        this.currentAspectRatioFrameLayout = null;
        this.currentTextureViewContainer = null;
        this.currentAspectRatioFrameLayoutReady = false;
        this.currentTextureView = null;
        this.videoPlayer.releasePlayer();
        this.videoPlayer = null;
        try
        {
          this.baseActivity.getWindow().clearFlags(128);
        }
        catch (Exception localException2)
        {
          FileLog.e(localException2);
        }
        continue;
        label487:
        this.voiceMessagesPlaylist = null;
        this.voiceMessagesPlaylistMap = null;
        continue;
        label500:
        if (((localException2.isVoice()) || (localException2.isRoundVideo())) && (localException2.getId() != 0)) {
          startRecordingIfFromSpeaker();
        }
        NotificationCenter.getInstance(localException2.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidReset, new Object[] { Integer.valueOf(localException2.getId()), Boolean.valueOf(paramBoolean2) });
        this.pipSwitchingState = 0;
        if (this.pipRoundVideoView != null)
        {
          this.pipRoundVideoView.close(true);
          this.pipRoundVideoView = null;
        }
      }
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if ((paramInt1 == NotificationCenter.FileDidLoaded) || (paramInt1 == NotificationCenter.httpFileDidLoaded))
    {
      paramVarArgs = (String)paramVarArgs[0];
      if ((this.downloadingCurrentMessage) && (this.playingMessageObject != null) && (this.playingMessageObject.currentAccount == paramInt2) && (FileLoader.getAttachFileName(this.playingMessageObject.getDocument()).equals(paramVarArgs)))
      {
        this.playMusicAgain = true;
        playMessage(this.playingMessageObject);
      }
    }
    do
    {
      do
      {
        for (;;)
        {
          return;
          Object localObject;
          if (paramInt1 == NotificationCenter.messagesDeleted)
          {
            paramInt1 = ((Integer)paramVarArgs[1]).intValue();
            paramVarArgs = (ArrayList)paramVarArgs[0];
            if ((this.playingMessageObject != null) && (paramInt1 == this.playingMessageObject.messageOwner.to_id.channel_id) && (paramVarArgs.contains(Integer.valueOf(this.playingMessageObject.getId())))) {
              cleanupPlayer(true, true);
            }
            if ((this.voiceMessagesPlaylist != null) && (!this.voiceMessagesPlaylist.isEmpty()) && (paramInt1 == ((MessageObject)this.voiceMessagesPlaylist.get(0)).messageOwner.to_id.channel_id))
            {
              paramInt1 = 0;
              while (paramInt1 < paramVarArgs.size())
              {
                localObject = (Integer)paramVarArgs.get(paramInt1);
                MessageObject localMessageObject = (MessageObject)this.voiceMessagesPlaylistMap.get(((Integer)localObject).intValue());
                this.voiceMessagesPlaylistMap.remove(((Integer)localObject).intValue());
                if (localMessageObject != null) {
                  this.voiceMessagesPlaylist.remove(localMessageObject);
                }
                paramInt1 += 1;
              }
            }
          }
          else
          {
            long l;
            if (paramInt1 == NotificationCenter.removeAllMessagesFromDialog)
            {
              l = ((Long)paramVarArgs[0]).longValue();
              if ((this.playingMessageObject != null) && (this.playingMessageObject.getDialogId() == l)) {
                cleanupPlayer(false, true);
              }
            }
            else if (paramInt1 == NotificationCenter.musicDidLoaded)
            {
              l = ((Long)paramVarArgs[0]).longValue();
              if ((this.playingMessageObject != null) && (this.playingMessageObject.isMusic()) && (this.playingMessageObject.getDialogId() == l))
              {
                paramVarArgs = (ArrayList)paramVarArgs[1];
                this.playlist.addAll(0, paramVarArgs);
                if (SharedConfig.shuffleMusic)
                {
                  buildShuffledPlayList();
                  this.currentPlaylistNum = 0;
                  return;
                }
                this.currentPlaylistNum += paramVarArgs.size();
              }
            }
            else
            {
              if (paramInt1 != NotificationCenter.didReceivedNewMessages) {
                break;
              }
              if ((this.voiceMessagesPlaylist != null) && (!this.voiceMessagesPlaylist.isEmpty()))
              {
                localObject = (MessageObject)this.voiceMessagesPlaylist.get(0);
                if (((Long)paramVarArgs[0]).longValue() == ((MessageObject)localObject).getDialogId())
                {
                  paramVarArgs = (ArrayList)paramVarArgs[1];
                  paramInt1 = 0;
                  while (paramInt1 < paramVarArgs.size())
                  {
                    localObject = (MessageObject)paramVarArgs.get(paramInt1);
                    if (((((MessageObject)localObject).isVoice()) || (((MessageObject)localObject).isRoundVideo())) && ((!this.voiceMessagesPlaylistUnread) || ((((MessageObject)localObject).isContentUnread()) && (!((MessageObject)localObject).isOut()))))
                    {
                      this.voiceMessagesPlaylist.add(localObject);
                      this.voiceMessagesPlaylistMap.put(((MessageObject)localObject).getId(), localObject);
                    }
                    paramInt1 += 1;
                  }
                }
              }
            }
          }
        }
      } while (paramInt1 != NotificationCenter.playerDidStartPlaying);
      paramVarArgs = (VideoPlayer)paramVarArgs[0];
    } while (getInstance().isCurrentPlayer(paramVarArgs));
    getInstance().pauseMessage(getInstance().getPlayingMessageObject());
  }
  
  public boolean findMessageInPlaylistAndPlay(MessageObject paramMessageObject)
  {
    int i = this.playlist.indexOf(paramMessageObject);
    if (i == -1) {
      return playMessage(paramMessageObject);
    }
    playMessageAtIndex(i);
    return true;
  }
  
  public void generateWaveform(MessageObject paramMessageObject)
  {
    final String str1 = paramMessageObject.getId() + "_" + paramMessageObject.getDialogId();
    final String str2 = FileLoader.getPathToMessage(paramMessageObject.messageOwner).getAbsolutePath();
    if (this.generatingWaveform.containsKey(str1)) {
      return;
    }
    this.generatingWaveform.put(str1, paramMessageObject);
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MessageObject localMessageObject = (MessageObject)MediaController.this.generatingWaveform.remove(MediaController.24.this.val$id);
            if (localMessageObject == null) {}
            while (this.val$waveform == null) {
              return;
            }
            int i = 0;
            for (;;)
            {
              Object localObject;
              if (i < localMessageObject.getDocument().attributes.size())
              {
                localObject = (TLRPC.DocumentAttribute)localMessageObject.getDocument().attributes.get(i);
                if ((localObject instanceof TLRPC.TL_documentAttributeAudio))
                {
                  ((TLRPC.DocumentAttribute)localObject).waveform = this.val$waveform;
                  ((TLRPC.DocumentAttribute)localObject).flags |= 0x4;
                }
              }
              else
              {
                localObject = new TLRPC.TL_messages_messages();
                ((TLRPC.TL_messages_messages)localObject).messages.add(localMessageObject.messageOwner);
                MessagesStorage.getInstance(localMessageObject.currentAccount).putMessages((TLRPC.messages_Messages)localObject, localMessageObject.getDialogId(), -1, 0, false);
                localObject = new ArrayList();
                ((ArrayList)localObject).add(localMessageObject);
                NotificationCenter.getInstance(localMessageObject.currentAccount).postNotificationName(NotificationCenter.replaceMessagesObjects, new Object[] { Long.valueOf(localMessageObject.getDialogId()), localObject });
                return;
              }
              i += 1;
            }
          }
        });
      }
    });
  }
  
  public AudioInfo getAudioInfo()
  {
    return this.audioInfo;
  }
  
  public MessageObject getPlayingMessageObject()
  {
    return this.playingMessageObject;
  }
  
  public int getPlayingMessageObjectNum()
  {
    return this.currentPlaylistNum;
  }
  
  public ArrayList<MessageObject> getPlaylist()
  {
    return this.playlist;
  }
  
  public native byte[] getWaveform(String paramString);
  
  public native byte[] getWaveform2(short[] paramArrayOfShort, int paramInt);
  
  public boolean isCurrentPlayer(VideoPlayer paramVideoPlayer)
  {
    return (this.videoPlayer == paramVideoPlayer) || (this.audioPlayer == paramVideoPlayer);
  }
  
  public boolean isDownloadingCurrentMessage()
  {
    return this.downloadingCurrentMessage;
  }
  
  public boolean isMessagePaused()
  {
    return (this.isPaused) || (this.downloadingCurrentMessage);
  }
  
  public boolean isPlayingMessage(MessageObject paramMessageObject)
  {
    boolean bool2 = true;
    boolean bool1 = true;
    if (((this.audioTrackPlayer == null) && (this.audioPlayer == null) && (this.videoPlayer == null)) || (paramMessageObject == null) || (this.playingMessageObject == null)) {}
    do
    {
      return false;
      if ((this.playingMessageObject.eventId != 0L) && (this.playingMessageObject.eventId == paramMessageObject.eventId))
      {
        if (!this.downloadingCurrentMessage) {}
        for (;;)
        {
          return bool1;
          bool1 = false;
        }
      }
    } while (!isSamePlayingMessage(paramMessageObject));
    if (!this.downloadingCurrentMessage) {}
    for (bool1 = bool2;; bool1 = false) {
      return bool1;
    }
  }
  
  protected boolean isRecordingAudio()
  {
    return (this.recordStartRunnable != null) || (this.recordingAudio != null);
  }
  
  public boolean isRecordingOrListeningByProximity()
  {
    return (this.proximityTouched) && ((isRecordingAudio()) || ((this.playingMessageObject != null) && ((this.playingMessageObject.isVoice()) || (this.playingMessageObject.isRoundVideo()))));
  }
  
  public boolean isRoundVideoDrawingReady()
  {
    return (this.currentAspectRatioFrameLayout != null) && (this.currentAspectRatioFrameLayout.isDrawingReady());
  }
  
  public void onAccuracyChanged(Sensor paramSensor, int paramInt) {}
  
  public void onAudioFocusChange(int paramInt)
  {
    if (paramInt == -1)
    {
      if ((isPlayingMessage(getPlayingMessageObject())) && (!isMessagePaused())) {
        pauseMessage(this.playingMessageObject);
      }
      this.hasAudioFocus = 0;
      this.audioFocus = 0;
    }
    for (;;)
    {
      setPlayerVolume();
      return;
      if (paramInt == 1)
      {
        this.audioFocus = 2;
        if (this.resumeAudioOnFocusGain)
        {
          this.resumeAudioOnFocusGain = false;
          if ((isPlayingMessage(getPlayingMessageObject())) && (isMessagePaused())) {
            playMessage(getPlayingMessageObject());
          }
        }
      }
      else if (paramInt == -3)
      {
        this.audioFocus = 1;
      }
      else if (paramInt == -2)
      {
        this.audioFocus = 0;
        if ((isPlayingMessage(getPlayingMessageObject())) && (!isMessagePaused()))
        {
          pauseMessage(this.playingMessageObject);
          this.resumeAudioOnFocusGain = true;
        }
      }
    }
  }
  
  public void onSensorChanged(SensorEvent paramSensorEvent)
  {
    if ((!this.sensorsStarted) || (VoIPService.getSharedInstance() != null)) {}
    label101:
    label237:
    label338:
    label392:
    label617:
    label1160:
    label1166:
    label1190:
    label1253:
    label1402:
    label1408:
    label1503:
    label1746:
    for (;;)
    {
      return;
      float f;
      int i;
      int j;
      boolean bool;
      if (paramSensorEvent.sensor == this.proximitySensor)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("proximity changed to " + paramSensorEvent.values[0]);
        }
        if (this.lastProximityValue == -100.0F)
        {
          this.lastProximityValue = paramSensorEvent.values[0];
          if (this.proximityHasDifferentValues) {
            this.proximityTouched = isNearToSensor(paramSensorEvent.values[0]);
          }
          if ((paramSensorEvent.sensor == this.linearSensor) || (paramSensorEvent.sensor == this.gravitySensor) || (paramSensorEvent.sensor == this.accelerometerSensor))
          {
            f = this.gravity[0] * this.linearAcceleration[0] + this.gravity[1] * this.linearAcceleration[1] + this.gravity[2] * this.linearAcceleration[2];
            if ((this.raisedToBack != 6) && (((f > 0.0F) && (this.previousAccValue > 0.0F)) || ((f < 0.0F) && (this.previousAccValue < 0.0F))))
            {
              if (f <= 0.0F) {
                break label1166;
              }
              if (f <= 15.0F) {
                break label1160;
              }
              i = 1;
              j = 1;
              if ((this.raisedToTopSign == 0) || (this.raisedToTopSign == j)) {
                break label1253;
              }
              if ((this.raisedToTop != 6) || (i == 0)) {
                break label1190;
              }
              if (this.raisedToBack < 6)
              {
                this.raisedToBack += 1;
                if (this.raisedToBack == 6)
                {
                  this.raisedToTop = 0;
                  this.raisedToTopSign = 0;
                  this.countLess = 0;
                  this.timeSinceRaise = System.currentTimeMillis();
                  if ((BuildVars.LOGS_ENABLED) && (BuildVars.DEBUG_PRIVATE_VERSION)) {
                    FileLog.d("motion detected");
                  }
                }
              }
            }
            this.previousAccValue = f;
            if ((this.gravityFast[1] <= 2.5F) || (Math.abs(this.gravityFast[2]) >= 4.0F) || (Math.abs(this.gravityFast[0]) <= 1.5F)) {
              break label1402;
            }
            bool = true;
            this.accelerometerVertical = bool;
          }
          if ((this.raisedToBack != 6) || (!this.accelerometerVertical) || (!this.proximityTouched) || (NotificationsController.audioManager.isWiredHeadsetOn())) {
            break label1503;
          }
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("sensor values reached");
          }
          if ((this.playingMessageObject != null) || (this.recordStartRunnable != null) || (this.recordingAudio != null) || (PhotoViewer.getInstance().isVisible()) || (!ApplicationLoader.isScreenOn) || (this.inputFieldHasText) || (!this.allowStartRecord) || (this.raiseChat == null) || (this.callInProgress)) {
            break label1408;
          }
          if (!this.raiseToEarRecord)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("start record");
            }
            this.useFrontSpeaker = true;
            if (!this.raiseChat.playFirstUnreadVoiceMessage())
            {
              this.raiseToEarRecord = true;
              this.useFrontSpeaker = false;
              startRecording(this.raiseChat.getCurrentAccount(), this.raiseChat.getDialogId(), null);
            }
            if (this.useFrontSpeaker) {
              setUseFrontSpeaker(true);
            }
            this.ignoreOnPause = true;
            if ((this.proximityHasDifferentValues) && (this.proximityWakeLock != null) && (!this.proximityWakeLock.isHeld())) {
              this.proximityWakeLock.acquire();
            }
          }
          this.raisedToBack = 0;
          this.raisedToTop = 0;
          this.raisedToTopSign = 0;
          this.countLess = 0;
        }
      }
      for (;;)
      {
        if ((this.timeSinceRaise == 0L) || (this.raisedToBack != 6) || (Math.abs(System.currentTimeMillis() - this.timeSinceRaise) <= 1000L)) {
          break label1746;
        }
        this.raisedToBack = 0;
        this.raisedToTop = 0;
        this.raisedToTopSign = 0;
        this.countLess = 0;
        this.timeSinceRaise = 0L;
        return;
        if (this.lastProximityValue == paramSensorEvent.values[0]) {
          break;
        }
        this.proximityHasDifferentValues = true;
        break;
        if (paramSensorEvent.sensor == this.accelerometerSensor)
        {
          if (this.lastTimestamp == 0L) {}
          for (double d = 0.9800000190734863D;; d = 1.0D / (1.0D + (paramSensorEvent.timestamp - this.lastTimestamp) / 1.0E9D))
          {
            this.lastTimestamp = paramSensorEvent.timestamp;
            this.gravity[0] = ((float)(this.gravity[0] * d + (1.0D - d) * paramSensorEvent.values[0]));
            this.gravity[1] = ((float)(this.gravity[1] * d + (1.0D - d) * paramSensorEvent.values[1]));
            this.gravity[2] = ((float)(this.gravity[2] * d + (1.0D - d) * paramSensorEvent.values[2]));
            this.gravityFast[0] = (0.8F * this.gravity[0] + 0.19999999F * paramSensorEvent.values[0]);
            this.gravityFast[1] = (0.8F * this.gravity[1] + 0.19999999F * paramSensorEvent.values[1]);
            this.gravityFast[2] = (0.8F * this.gravity[2] + 0.19999999F * paramSensorEvent.values[2]);
            this.linearAcceleration[0] = (paramSensorEvent.values[0] - this.gravity[0]);
            this.linearAcceleration[1] = (paramSensorEvent.values[1] - this.gravity[1]);
            this.linearAcceleration[2] = (paramSensorEvent.values[2] - this.gravity[2]);
            break;
          }
        }
        if (paramSensorEvent.sensor == this.linearSensor)
        {
          this.linearAcceleration[0] = paramSensorEvent.values[0];
          this.linearAcceleration[1] = paramSensorEvent.values[1];
          this.linearAcceleration[2] = paramSensorEvent.values[2];
          break label101;
        }
        if (paramSensorEvent.sensor != this.gravitySensor) {
          break label101;
        }
        float[] arrayOfFloat1 = this.gravityFast;
        float[] arrayOfFloat2 = this.gravity;
        f = paramSensorEvent.values[0];
        arrayOfFloat2[0] = f;
        arrayOfFloat1[0] = f;
        arrayOfFloat1 = this.gravityFast;
        arrayOfFloat2 = this.gravity;
        f = paramSensorEvent.values[1];
        arrayOfFloat2[1] = f;
        arrayOfFloat1[1] = f;
        arrayOfFloat1 = this.gravityFast;
        arrayOfFloat2 = this.gravity;
        f = paramSensorEvent.values[2];
        arrayOfFloat2[2] = f;
        arrayOfFloat1[2] = f;
        break label101;
        i = 0;
        break label237;
        if (f < -15.0F) {}
        for (i = 1;; i = 0)
        {
          j = 2;
          break;
        }
        if (i == 0) {
          this.countLess += 1;
        }
        if ((this.countLess != 10) && (this.raisedToTop == 6) && (this.raisedToBack == 0)) {
          break label338;
        }
        this.raisedToTop = 0;
        this.raisedToTopSign = 0;
        this.raisedToBack = 0;
        this.countLess = 0;
        break label338;
        if ((i != 0) && (this.raisedToBack == 0) && ((this.raisedToTopSign == 0) || (this.raisedToTopSign == j)))
        {
          if ((this.raisedToTop >= 6) || (this.proximityTouched)) {
            break label338;
          }
          this.raisedToTopSign = j;
          this.raisedToTop += 1;
          if (this.raisedToTop != 6) {
            break label338;
          }
          this.countLess = 0;
          break label338;
        }
        if (i == 0) {
          this.countLess += 1;
        }
        if ((this.raisedToTopSign == j) && (this.countLess != 10) && (this.raisedToTop == 6) && (this.raisedToBack == 0)) {
          break label338;
        }
        this.raisedToBack = 0;
        this.raisedToTop = 0;
        this.raisedToTopSign = 0;
        this.countLess = 0;
        break label338;
        bool = false;
        break label392;
        if ((this.playingMessageObject == null) || ((!this.playingMessageObject.isVoice()) && (!this.playingMessageObject.isRoundVideo())) || (this.useFrontSpeaker)) {
          break label617;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("start listen");
        }
        if ((this.proximityHasDifferentValues) && (this.proximityWakeLock != null) && (!this.proximityWakeLock.isHeld())) {
          this.proximityWakeLock.acquire();
        }
        setUseFrontSpeaker(true);
        startAudioAgain(false);
        this.ignoreOnPause = true;
        break label617;
        if (this.proximityTouched)
        {
          if ((this.playingMessageObject != null) && ((this.playingMessageObject.isVoice()) || (this.playingMessageObject.isRoundVideo())) && (!this.useFrontSpeaker))
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("start listen by proximity only");
            }
            if ((this.proximityHasDifferentValues) && (this.proximityWakeLock != null) && (!this.proximityWakeLock.isHeld())) {
              this.proximityWakeLock.acquire();
            }
            setUseFrontSpeaker(true);
            startAudioAgain(false);
            this.ignoreOnPause = true;
          }
        }
        else if (!this.proximityTouched) {
          if (this.raiseToEarRecord)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("stop record");
            }
            stopRecording(2);
            this.raiseToEarRecord = false;
            this.ignoreOnPause = false;
            if ((this.proximityHasDifferentValues) && (this.proximityWakeLock != null) && (this.proximityWakeLock.isHeld())) {
              this.proximityWakeLock.release();
            }
          }
          else if (this.useFrontSpeaker)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("stop listen");
            }
            this.useFrontSpeaker = false;
            startAudioAgain(true);
            this.ignoreOnPause = false;
            if ((this.proximityHasDifferentValues) && (this.proximityWakeLock != null) && (this.proximityWakeLock.isHeld())) {
              this.proximityWakeLock.release();
            }
          }
        }
      }
    }
  }
  
  public boolean pauseMessage(MessageObject paramMessageObject)
  {
    if (((this.audioTrackPlayer == null) && (this.audioPlayer == null) && (this.videoPlayer == null)) || (paramMessageObject == null) || (this.playingMessageObject == null) || (!isSamePlayingMessage(paramMessageObject))) {
      return false;
    }
    stopProgressTimer();
    for (;;)
    {
      try
      {
        if (this.audioPlayer != null)
        {
          this.audioPlayer.pause();
          this.isPaused = true;
          NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, new Object[] { Integer.valueOf(this.playingMessageObject.getId()) });
          return true;
        }
        if (this.audioTrackPlayer != null)
        {
          this.audioTrackPlayer.pause();
          continue;
        }
        if (this.videoPlayer == null) {
          continue;
        }
      }
      catch (Exception paramMessageObject)
      {
        FileLog.e(paramMessageObject);
        this.isPaused = false;
        return false;
      }
      this.videoPlayer.pause();
    }
  }
  
  /* Error */
  public boolean playMessage(final MessageObject paramMessageObject)
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +5 -> 6
    //   4: iconst_0
    //   5: ireturn
    //   6: aload_0
    //   7: getfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   10: ifnonnull +17 -> 27
    //   13: aload_0
    //   14: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   17: ifnonnull +10 -> 27
    //   20: aload_0
    //   21: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   24: ifnull +40 -> 64
    //   27: aload_0
    //   28: aload_1
    //   29: invokespecial 2179	org/telegram/messenger/MediaController:isSamePlayingMessage	(Lorg/telegram/messenger/MessageObject;)Z
    //   32: ifeq +32 -> 64
    //   35: aload_0
    //   36: getfield 411	org/telegram/messenger/MediaController:isPaused	Z
    //   39: ifeq +9 -> 48
    //   42: aload_0
    //   43: aload_1
    //   44: invokevirtual 2335	org/telegram/messenger/MediaController:resumeAudio	(Lorg/telegram/messenger/MessageObject;)Z
    //   47: pop
    //   48: getstatic 2037	org/telegram/messenger/SharedConfig:raiseToSpeak	Z
    //   51: ifne +11 -> 62
    //   54: aload_0
    //   55: aload_0
    //   56: getfield 2039	org/telegram/messenger/MediaController:raiseChat	Lorg/telegram/ui/ChatActivity;
    //   59: invokevirtual 2338	org/telegram/messenger/MediaController:startRaiseToEarSensors	(Lorg/telegram/ui/ChatActivity;)V
    //   62: iconst_1
    //   63: ireturn
    //   64: aload_1
    //   65: invokevirtual 2128	org/telegram/messenger/MessageObject:isOut	()Z
    //   68: ifne +21 -> 89
    //   71: aload_1
    //   72: invokevirtual 2125	org/telegram/messenger/MessageObject:isContentUnread	()Z
    //   75: ifeq +14 -> 89
    //   78: aload_1
    //   79: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   82: invokestatic 2343	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   85: aload_1
    //   86: invokevirtual 2346	org/telegram/messenger/MessagesController:markMessageContentAsRead	(Lorg/telegram/messenger/MessageObject;)V
    //   89: aload_0
    //   90: getfield 1771	org/telegram/messenger/MediaController:playMusicAgain	Z
    //   93: ifne +328 -> 421
    //   96: iconst_1
    //   97: istore 4
    //   99: aload_0
    //   100: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   103: ifnull +28 -> 131
    //   106: iconst_0
    //   107: istore 5
    //   109: iload 5
    //   111: istore 4
    //   113: aload_0
    //   114: getfield 1771	org/telegram/messenger/MediaController:playMusicAgain	Z
    //   117: ifne +14 -> 131
    //   120: aload_0
    //   121: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   124: invokevirtual 1769	org/telegram/messenger/MessageObject:resetPlayingProgress	()V
    //   127: iload 5
    //   129: istore 4
    //   131: aload_0
    //   132: iload 4
    //   134: iconst_0
    //   135: invokevirtual 1711	org/telegram/messenger/MediaController:cleanupPlayer	(ZZ)V
    //   138: aload_0
    //   139: iconst_0
    //   140: putfield 1771	org/telegram/messenger/MediaController:playMusicAgain	Z
    //   143: aload_0
    //   144: fconst_0
    //   145: putfield 685	org/telegram/messenger/MediaController:seekToProgressPending	F
    //   148: aconst_null
    //   149: astore 11
    //   151: iconst_0
    //   152: istore 5
    //   154: iload 5
    //   156: istore 4
    //   158: aload 11
    //   160: astore 10
    //   162: aload_1
    //   163: getfield 986	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   166: getfield 991	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   169: ifnull +63 -> 232
    //   172: iload 5
    //   174: istore 4
    //   176: aload 11
    //   178: astore 10
    //   180: aload_1
    //   181: getfield 986	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   184: getfield 991	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   187: invokevirtual 1028	java/lang/String:length	()I
    //   190: ifle +42 -> 232
    //   193: new 998	java/io/File
    //   196: dup
    //   197: aload_1
    //   198: getfield 986	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   201: getfield 991	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   204: invokespecial 999	java/io/File:<init>	(Ljava/lang/String;)V
    //   207: astore 10
    //   209: aload 10
    //   211: invokevirtual 1002	java/io/File:exists	()Z
    //   214: istore 5
    //   216: iload 5
    //   218: istore 4
    //   220: iload 5
    //   222: ifne +10 -> 232
    //   225: aconst_null
    //   226: astore 10
    //   228: iload 5
    //   230: istore 4
    //   232: aload 10
    //   234: ifnull +193 -> 427
    //   237: aload 10
    //   239: astore 11
    //   241: getstatic 2349	org/telegram/messenger/SharedConfig:streamMedia	Z
    //   244: ifeq +195 -> 439
    //   247: aload_1
    //   248: invokevirtual 1005	org/telegram/messenger/MessageObject:isMusic	()Z
    //   251: ifeq +188 -> 439
    //   254: aload_1
    //   255: invokevirtual 1115	org/telegram/messenger/MessageObject:getDialogId	()J
    //   258: l2i
    //   259: ifeq +180 -> 439
    //   262: iconst_1
    //   263: istore_2
    //   264: iload 4
    //   266: istore 5
    //   268: aload 11
    //   270: ifnull +207 -> 477
    //   273: iload 4
    //   275: istore 5
    //   277: aload 11
    //   279: aload 10
    //   281: if_acmpeq +196 -> 477
    //   284: aload 11
    //   286: invokevirtual 1002	java/io/File:exists	()Z
    //   289: istore 4
    //   291: iload 4
    //   293: istore 5
    //   295: iload 4
    //   297: ifne +180 -> 477
    //   300: iload 4
    //   302: istore 5
    //   304: iload_2
    //   305: ifne +172 -> 477
    //   308: aload_1
    //   309: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   312: invokestatic 1010	org/telegram/messenger/FileLoader:getInstance	(I)Lorg/telegram/messenger/FileLoader;
    //   315: aload_1
    //   316: invokevirtual 1014	org/telegram/messenger/MessageObject:getDocument	()Lorg/telegram/tgnet/TLRPC$Document;
    //   319: iconst_0
    //   320: iconst_0
    //   321: invokevirtual 1018	org/telegram/messenger/FileLoader:loadFile	(Lorg/telegram/tgnet/TLRPC$Document;ZI)V
    //   324: aload_0
    //   325: iconst_1
    //   326: putfield 2045	org/telegram/messenger/MediaController:downloadingCurrentMessage	Z
    //   329: aload_0
    //   330: iconst_0
    //   331: putfield 411	org/telegram/messenger/MediaController:isPaused	Z
    //   334: aload_0
    //   335: lconst_0
    //   336: putfield 417	org/telegram/messenger/MediaController:lastProgress	J
    //   339: aload_0
    //   340: lconst_0
    //   341: putfield 690	org/telegram/messenger/MediaController:lastPlayPcm	J
    //   344: aload_0
    //   345: aconst_null
    //   346: putfield 2024	org/telegram/messenger/MediaController:audioInfo	Lorg/telegram/messenger/audioinfo/AudioInfo;
    //   349: aload_0
    //   350: aload_1
    //   351: putfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   354: aload_0
    //   355: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   358: invokevirtual 1005	org/telegram/messenger/MessageObject:isMusic	()Z
    //   361: ifeq +91 -> 452
    //   364: new 1967	android/content/Intent
    //   367: dup
    //   368: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   371: ldc_w 2063
    //   374: invokespecial 1972	android/content/Intent:<init>	(Landroid/content/Context;Ljava/lang/Class;)V
    //   377: astore_1
    //   378: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   381: aload_1
    //   382: invokevirtual 2008	android/content/Context:startService	(Landroid/content/Intent;)Landroid/content/ComponentName;
    //   385: pop
    //   386: aload_0
    //   387: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   390: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   393: invokestatic 1736	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   396: getstatic 1746	org/telegram/messenger/NotificationCenter:messagePlayingPlayStateChanged	I
    //   399: iconst_1
    //   400: anewarray 4	java/lang/Object
    //   403: dup
    //   404: iconst_0
    //   405: aload_0
    //   406: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   409: invokevirtual 1124	org/telegram/messenger/MessageObject:getId	()I
    //   412: invokestatic 1555	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   415: aastore
    //   416: invokevirtual 1743	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   419: iconst_1
    //   420: ireturn
    //   421: iconst_0
    //   422: istore 4
    //   424: goto -325 -> 99
    //   427: aload_1
    //   428: getfield 986	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   431: invokestatic 1022	org/telegram/messenger/FileLoader:getPathToMessage	(Lorg/telegram/tgnet/TLRPC$Message;)Ljava/io/File;
    //   434: astore 11
    //   436: goto -195 -> 241
    //   439: iconst_0
    //   440: istore_2
    //   441: goto -177 -> 264
    //   444: astore_1
    //   445: aload_1
    //   446: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   449: goto -63 -> 386
    //   452: new 1967	android/content/Intent
    //   455: dup
    //   456: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   459: ldc_w 2063
    //   462: invokespecial 1972	android/content/Intent:<init>	(Landroid/content/Context;Ljava/lang/Class;)V
    //   465: astore_1
    //   466: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   469: aload_1
    //   470: invokevirtual 2067	android/content/Context:stopService	(Landroid/content/Intent;)Z
    //   473: pop
    //   474: goto -88 -> 386
    //   477: aload_0
    //   478: iconst_0
    //   479: putfield 2045	org/telegram/messenger/MediaController:downloadingCurrentMessage	Z
    //   482: aload_1
    //   483: invokevirtual 1005	org/telegram/messenger/MessageObject:isMusic	()Z
    //   486: ifeq +387 -> 873
    //   489: aload_0
    //   490: aload_1
    //   491: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   494: invokespecial 2021	org/telegram/messenger/MediaController:checkIsNextMusicFileDownloaded	(I)V
    //   497: aload_0
    //   498: getfield 771	org/telegram/messenger/MediaController:currentAspectRatioFrameLayout	Lorg/telegram/messenger/exoplayer2/ui/AspectRatioFrameLayout;
    //   501: ifnull +16 -> 517
    //   504: aload_0
    //   505: iconst_0
    //   506: putfield 774	org/telegram/messenger/MediaController:isDrawingWasReady	Z
    //   509: aload_0
    //   510: getfield 771	org/telegram/messenger/MediaController:currentAspectRatioFrameLayout	Lorg/telegram/messenger/exoplayer2/ui/AspectRatioFrameLayout;
    //   513: iconst_0
    //   514: invokevirtual 2352	org/telegram/messenger/exoplayer2/ui/AspectRatioFrameLayout:setDrawingReady	(Z)V
    //   517: aload_1
    //   518: invokevirtual 921	org/telegram/messenger/MessageObject:isRoundVideo	()Z
    //   521: ifeq +399 -> 920
    //   524: aload_0
    //   525: getfield 429	org/telegram/messenger/MediaController:playlist	Ljava/util/ArrayList;
    //   528: invokevirtual 884	java/util/ArrayList:clear	()V
    //   531: aload_0
    //   532: getfield 431	org/telegram/messenger/MediaController:shuffledPlaylist	Ljava/util/ArrayList;
    //   535: invokevirtual 884	java/util/ArrayList:clear	()V
    //   538: aload_0
    //   539: new 1716	org/telegram/ui/Components/VideoPlayer
    //   542: dup
    //   543: invokespecial 2353	org/telegram/ui/Components/VideoPlayer:<init>	()V
    //   546: putfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   549: aload_0
    //   550: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   553: new 36	org/telegram/messenger/MediaController$16
    //   556: dup
    //   557: aload_0
    //   558: invokespecial 2354	org/telegram/messenger/MediaController$16:<init>	(Lorg/telegram/messenger/MediaController;)V
    //   561: invokevirtual 2358	org/telegram/ui/Components/VideoPlayer:setDelegate	(Lorg/telegram/ui/Components/VideoPlayer$VideoPlayerDelegate;)V
    //   564: aload_0
    //   565: iconst_0
    //   566: putfield 749	org/telegram/messenger/MediaController:currentAspectRatioFrameLayoutReady	Z
    //   569: aload_0
    //   570: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   573: ifnonnull +20 -> 593
    //   576: aload_1
    //   577: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   580: invokestatic 2343	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   583: aload_1
    //   584: invokevirtual 1115	org/telegram/messenger/MessageObject:getDialogId	()J
    //   587: invokevirtual 2362	org/telegram/messenger/MessagesController:isDialogCreated	(J)Z
    //   590: ifne +304 -> 894
    //   593: aload_0
    //   594: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   597: ifnonnull +33 -> 630
    //   600: aload_0
    //   601: new 2059	org/telegram/ui/Components/PipRoundVideoView
    //   604: dup
    //   605: invokespecial 2363	org/telegram/ui/Components/PipRoundVideoView:<init>	()V
    //   608: putfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   611: aload_0
    //   612: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   615: aload_0
    //   616: getfield 746	org/telegram/messenger/MediaController:baseActivity	Landroid/app/Activity;
    //   619: new 40	org/telegram/messenger/MediaController$17
    //   622: dup
    //   623: aload_0
    //   624: invokespecial 2364	org/telegram/messenger/MediaController$17:<init>	(Lorg/telegram/messenger/MediaController;)V
    //   627: invokevirtual 2367	org/telegram/ui/Components/PipRoundVideoView:show	(Landroid/app/Activity;Ljava/lang/Runnable;)V
    //   630: aload_0
    //   631: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   634: ifnull +17 -> 651
    //   637: aload_0
    //   638: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   641: aload_0
    //   642: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   645: invokevirtual 2371	org/telegram/ui/Components/PipRoundVideoView:getTextureView	()Landroid/view/TextureView;
    //   648: invokevirtual 2375	org/telegram/ui/Components/VideoPlayer:setTextureView	(Landroid/view/TextureView;)V
    //   651: aload_0
    //   652: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   655: aload 11
    //   657: invokestatic 1577	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   660: ldc_w 2377
    //   663: invokevirtual 2381	org/telegram/ui/Components/VideoPlayer:preparePlayer	(Landroid/net/Uri;Ljava/lang/String;)V
    //   666: aload_0
    //   667: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   670: astore 10
    //   672: aload_0
    //   673: getfield 923	org/telegram/messenger/MediaController:useFrontSpeaker	Z
    //   676: ifeq +239 -> 915
    //   679: iconst_0
    //   680: istore_2
    //   681: aload 10
    //   683: iload_2
    //   684: invokevirtual 1935	org/telegram/ui/Components/VideoPlayer:setStreamType	(I)V
    //   687: aload_0
    //   688: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   691: invokevirtual 1938	org/telegram/ui/Components/VideoPlayer:play	()V
    //   694: aload_0
    //   695: aload_1
    //   696: invokespecial 2383	org/telegram/messenger/MediaController:checkAudioFocus	(Lorg/telegram/messenger/MessageObject;)V
    //   699: aload_0
    //   700: invokespecial 2200	org/telegram/messenger/MediaController:setPlayerVolume	()V
    //   703: aload_0
    //   704: iconst_0
    //   705: putfield 411	org/telegram/messenger/MediaController:isPaused	Z
    //   708: aload_0
    //   709: lconst_0
    //   710: putfield 417	org/telegram/messenger/MediaController:lastProgress	J
    //   713: aload_0
    //   714: lconst_0
    //   715: putfield 690	org/telegram/messenger/MediaController:lastPlayPcm	J
    //   718: aload_0
    //   719: aload_1
    //   720: putfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   723: getstatic 2037	org/telegram/messenger/SharedConfig:raiseToSpeak	Z
    //   726: ifne +11 -> 737
    //   729: aload_0
    //   730: aload_0
    //   731: getfield 2039	org/telegram/messenger/MediaController:raiseChat	Lorg/telegram/ui/ChatActivity;
    //   734: invokevirtual 2338	org/telegram/messenger/MediaController:startRaiseToEarSensors	(Lorg/telegram/ui/ChatActivity;)V
    //   737: aload_0
    //   738: aload_0
    //   739: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   742: invokespecial 2385	org/telegram/messenger/MediaController:startProgressTimer	(Lorg/telegram/messenger/MessageObject;)V
    //   745: aload_1
    //   746: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   749: invokestatic 1736	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   752: getstatic 2388	org/telegram/messenger/NotificationCenter:messagePlayingDidStarted	I
    //   755: iconst_1
    //   756: anewarray 4	java/lang/Object
    //   759: dup
    //   760: iconst_0
    //   761: aload_1
    //   762: aastore
    //   763: invokevirtual 1743	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   766: aload_0
    //   767: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   770: ifnull +911 -> 1681
    //   773: aload_0
    //   774: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   777: getfield 1725	org/telegram/messenger/MessageObject:audioProgress	F
    //   780: fconst_0
    //   781: fcmpl
    //   782: ifeq +57 -> 839
    //   785: aload_0
    //   786: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   789: invokevirtual 2391	org/telegram/ui/Components/VideoPlayer:getDuration	()J
    //   792: lstore 8
    //   794: lload 8
    //   796: lstore 6
    //   798: lload 8
    //   800: ldc2_w 2392
    //   803: lcmp
    //   804: ifne +13 -> 817
    //   807: aload_0
    //   808: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   811: invokevirtual 2395	org/telegram/messenger/MessageObject:getDuration	()I
    //   814: i2l
    //   815: lstore 6
    //   817: lload 6
    //   819: l2f
    //   820: aload_0
    //   821: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   824: getfield 1725	org/telegram/messenger/MessageObject:audioProgress	F
    //   827: fmul
    //   828: f2i
    //   829: istore_2
    //   830: aload_0
    //   831: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   834: iload_2
    //   835: i2l
    //   836: invokevirtual 2397	org/telegram/ui/Components/VideoPlayer:seekTo	(J)V
    //   839: aload_0
    //   840: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   843: invokevirtual 1005	org/telegram/messenger/MessageObject:isMusic	()Z
    //   846: ifeq +1018 -> 1864
    //   849: new 1967	android/content/Intent
    //   852: dup
    //   853: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   856: ldc_w 2063
    //   859: invokespecial 1972	android/content/Intent:<init>	(Landroid/content/Context;Ljava/lang/Class;)V
    //   862: astore_1
    //   863: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   866: aload_1
    //   867: invokevirtual 2008	android/content/Context:startService	(Landroid/content/Intent;)Landroid/content/ComponentName;
    //   870: pop
    //   871: iconst_1
    //   872: ireturn
    //   873: aload_0
    //   874: aload_1
    //   875: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   878: invokespecial 2399	org/telegram/messenger/MediaController:checkIsNextVoiceFileDownloaded	(I)V
    //   881: goto -384 -> 497
    //   884: astore 10
    //   886: aload_0
    //   887: aconst_null
    //   888: putfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   891: goto -261 -> 630
    //   894: aload_0
    //   895: getfield 783	org/telegram/messenger/MediaController:currentTextureView	Landroid/view/TextureView;
    //   898: ifnull -247 -> 651
    //   901: aload_0
    //   902: getfield 674	org/telegram/messenger/MediaController:videoPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   905: aload_0
    //   906: getfield 783	org/telegram/messenger/MediaController:currentTextureView	Landroid/view/TextureView;
    //   909: invokevirtual 2375	org/telegram/ui/Components/VideoPlayer:setTextureView	(Landroid/view/TextureView;)V
    //   912: goto -261 -> 651
    //   915: iconst_3
    //   916: istore_2
    //   917: goto -236 -> 681
    //   920: aload_1
    //   921: invokevirtual 1005	org/telegram/messenger/MessageObject:isMusic	()Z
    //   924: ifne +251 -> 1175
    //   927: aload 11
    //   929: invokevirtual 1609	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   932: invokestatic 2401	org/telegram/messenger/MediaController:isOpusFile	(Ljava/lang/String;)I
    //   935: iconst_1
    //   936: if_icmpne +239 -> 1175
    //   939: aload_0
    //   940: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   943: ifnull +16 -> 959
    //   946: aload_0
    //   947: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   950: iconst_1
    //   951: invokevirtual 2061	org/telegram/ui/Components/PipRoundVideoView:close	(Z)V
    //   954: aload_0
    //   955: aconst_null
    //   956: putfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   959: aload_0
    //   960: getfield 429	org/telegram/messenger/MediaController:playlist	Ljava/util/ArrayList;
    //   963: invokevirtual 884	java/util/ArrayList:clear	()V
    //   966: aload_0
    //   967: getfield 431	org/telegram/messenger/MediaController:shuffledPlaylist	Ljava/util/ArrayList;
    //   970: invokevirtual 884	java/util/ArrayList:clear	()V
    //   973: aload_0
    //   974: getfield 433	org/telegram/messenger/MediaController:playerObjectSync	Ljava/lang/Object;
    //   977: astore 10
    //   979: aload 10
    //   981: monitorenter
    //   982: aload_0
    //   983: iconst_3
    //   984: putfield 423	org/telegram/messenger/MediaController:ignoreFirstProgress	I
    //   987: new 2403	java/util/concurrent/CountDownLatch
    //   990: dup
    //   991: iconst_1
    //   992: invokespecial 2404	java/util/concurrent/CountDownLatch:<init>	(I)V
    //   995: astore 12
    //   997: iconst_1
    //   998: anewarray 1929	java/lang/Boolean
    //   1001: astore 13
    //   1003: aload_0
    //   1004: getfield 475	org/telegram/messenger/MediaController:fileDecodingQueue	Lorg/telegram/messenger/DispatchQueue;
    //   1007: new 42	org/telegram/messenger/MediaController$18
    //   1010: dup
    //   1011: aload_0
    //   1012: aload 13
    //   1014: aload 11
    //   1016: aload 12
    //   1018: invokespecial 2407	org/telegram/messenger/MediaController$18:<init>	(Lorg/telegram/messenger/MediaController;[Ljava/lang/Boolean;Ljava/io/File;Ljava/util/concurrent/CountDownLatch;)V
    //   1021: invokevirtual 480	org/telegram/messenger/DispatchQueue:postRunnable	(Ljava/lang/Runnable;)V
    //   1024: aload 12
    //   1026: invokevirtual 2410	java/util/concurrent/CountDownLatch:await	()V
    //   1029: aload 13
    //   1031: iconst_0
    //   1032: aaload
    //   1033: invokevirtual 2413	java/lang/Boolean:booleanValue	()Z
    //   1036: istore 4
    //   1038: iload 4
    //   1040: ifne +14 -> 1054
    //   1043: aload 10
    //   1045: monitorexit
    //   1046: iconst_0
    //   1047: ireturn
    //   1048: astore_1
    //   1049: aload 10
    //   1051: monitorexit
    //   1052: aload_1
    //   1053: athrow
    //   1054: aload_0
    //   1055: aload_0
    //   1056: invokespecial 2415	org/telegram/messenger/MediaController:getTotalPcmDuration	()J
    //   1059: putfield 694	org/telegram/messenger/MediaController:currentTotalPcmDuration	J
    //   1062: aload_0
    //   1063: getfield 923	org/telegram/messenger/MediaController:useFrontSpeaker	Z
    //   1066: ifeq +823 -> 1889
    //   1069: iconst_0
    //   1070: istore_2
    //   1071: aload_0
    //   1072: new 1748	android/media/AudioTrack
    //   1075: dup
    //   1076: iload_2
    //   1077: ldc_w 2416
    //   1080: iconst_4
    //   1081: iconst_2
    //   1082: aload_0
    //   1083: getfield 419	org/telegram/messenger/MediaController:playerBufferSize	I
    //   1086: iconst_1
    //   1087: invokespecial 2419	android/media/AudioTrack:<init>	(IIIIII)V
    //   1090: putfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   1093: aload_0
    //   1094: getfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   1097: fconst_1
    //   1098: fconst_1
    //   1099: invokevirtual 1916	android/media/AudioTrack:setStereoVolume	(FF)I
    //   1102: pop
    //   1103: aload_0
    //   1104: getfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   1107: new 44	org/telegram/messenger/MediaController$19
    //   1110: dup
    //   1111: aload_0
    //   1112: invokespecial 2420	org/telegram/messenger/MediaController$19:<init>	(Lorg/telegram/messenger/MediaController;)V
    //   1115: invokevirtual 2424	android/media/AudioTrack:setPlaybackPositionUpdateListener	(Landroid/media/AudioTrack$OnPlaybackPositionUpdateListener;)V
    //   1118: aload_0
    //   1119: getfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   1122: invokevirtual 2425	android/media/AudioTrack:play	()V
    //   1125: aload 10
    //   1127: monitorexit
    //   1128: goto -434 -> 694
    //   1131: astore_1
    //   1132: aload_1
    //   1133: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1136: aload_0
    //   1137: getfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   1140: ifnull +30 -> 1170
    //   1143: aload_0
    //   1144: getfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   1147: invokevirtual 1755	android/media/AudioTrack:release	()V
    //   1150: aload_0
    //   1151: aconst_null
    //   1152: putfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   1155: aload_0
    //   1156: iconst_0
    //   1157: putfield 411	org/telegram/messenger/MediaController:isPaused	Z
    //   1160: aload_0
    //   1161: aconst_null
    //   1162: putfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1165: aload_0
    //   1166: iconst_0
    //   1167: putfield 2045	org/telegram/messenger/MediaController:downloadingCurrentMessage	Z
    //   1170: aload 10
    //   1172: monitorexit
    //   1173: iconst_0
    //   1174: ireturn
    //   1175: aload_0
    //   1176: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   1179: ifnull +16 -> 1195
    //   1182: aload_0
    //   1183: getfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   1186: iconst_1
    //   1187: invokevirtual 2061	org/telegram/ui/Components/PipRoundVideoView:close	(Z)V
    //   1190: aload_0
    //   1191: aconst_null
    //   1192: putfield 787	org/telegram/messenger/MediaController:pipRoundVideoView	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   1195: aload_0
    //   1196: new 1716	org/telegram/ui/Components/VideoPlayer
    //   1199: dup
    //   1200: invokespecial 2353	org/telegram/ui/Components/VideoPlayer:<init>	()V
    //   1203: putfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1206: aload_0
    //   1207: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1210: astore 12
    //   1212: aload_0
    //   1213: getfield 923	org/telegram/messenger/MediaController:useFrontSpeaker	Z
    //   1216: ifeq +195 -> 1411
    //   1219: iconst_0
    //   1220: istore_2
    //   1221: aload 12
    //   1223: iload_2
    //   1224: invokevirtual 1935	org/telegram/ui/Components/VideoPlayer:setStreamType	(I)V
    //   1227: aload_0
    //   1228: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1231: new 48	org/telegram/messenger/MediaController$20
    //   1234: dup
    //   1235: aload_0
    //   1236: aload_1
    //   1237: invokespecial 2426	org/telegram/messenger/MediaController$20:<init>	(Lorg/telegram/messenger/MediaController;Lorg/telegram/messenger/MessageObject;)V
    //   1240: invokevirtual 2358	org/telegram/ui/Components/VideoPlayer:setDelegate	(Lorg/telegram/ui/Components/VideoPlayer$VideoPlayerDelegate;)V
    //   1243: iload 5
    //   1245: ifeq +171 -> 1416
    //   1248: aload_1
    //   1249: getfield 2429	org/telegram/messenger/MessageObject:mediaExists	Z
    //   1252: ifne +22 -> 1274
    //   1255: aload 11
    //   1257: aload 10
    //   1259: if_acmpeq +15 -> 1274
    //   1262: new 50	org/telegram/messenger/MediaController$21
    //   1265: dup
    //   1266: aload_0
    //   1267: aload_1
    //   1268: invokespecial 2430	org/telegram/messenger/MediaController$21:<init>	(Lorg/telegram/messenger/MediaController;Lorg/telegram/messenger/MessageObject;)V
    //   1271: invokestatic 500	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
    //   1274: aload_0
    //   1275: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1278: aload 11
    //   1280: invokestatic 1577	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   1283: ldc_w 2377
    //   1286: invokevirtual 2381	org/telegram/ui/Components/VideoPlayer:preparePlayer	(Landroid/net/Uri;Ljava/lang/String;)V
    //   1289: aload_0
    //   1290: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1293: invokevirtual 1938	org/telegram/ui/Components/VideoPlayer:play	()V
    //   1296: aload_1
    //   1297: invokevirtual 918	org/telegram/messenger/MessageObject:isVoice	()Z
    //   1300: ifeq +291 -> 1591
    //   1303: aload_0
    //   1304: aconst_null
    //   1305: putfield 2024	org/telegram/messenger/MediaController:audioInfo	Lorg/telegram/messenger/audioinfo/AudioInfo;
    //   1308: aload_0
    //   1309: getfield 429	org/telegram/messenger/MediaController:playlist	Ljava/util/ArrayList;
    //   1312: invokevirtual 884	java/util/ArrayList:clear	()V
    //   1315: aload_0
    //   1316: getfield 431	org/telegram/messenger/MediaController:shuffledPlaylist	Ljava/util/ArrayList;
    //   1319: invokevirtual 884	java/util/ArrayList:clear	()V
    //   1322: goto -628 -> 694
    //   1325: astore 10
    //   1327: aload 10
    //   1329: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1332: aload_1
    //   1333: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   1336: invokestatic 1736	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   1339: astore_1
    //   1340: getstatic 1746	org/telegram/messenger/NotificationCenter:messagePlayingPlayStateChanged	I
    //   1343: istore_3
    //   1344: aload_0
    //   1345: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1348: ifnull +265 -> 1613
    //   1351: aload_0
    //   1352: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1355: invokevirtual 1124	org/telegram/messenger/MessageObject:getId	()I
    //   1358: istore_2
    //   1359: aload_1
    //   1360: iload_3
    //   1361: iconst_1
    //   1362: anewarray 4	java/lang/Object
    //   1365: dup
    //   1366: iconst_0
    //   1367: iload_2
    //   1368: invokestatic 1555	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   1371: aastore
    //   1372: invokevirtual 1743	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   1375: aload_0
    //   1376: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1379: ifnull +30 -> 1409
    //   1382: aload_0
    //   1383: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1386: invokevirtual 1719	org/telegram/ui/Components/VideoPlayer:releasePlayer	()V
    //   1389: aload_0
    //   1390: aconst_null
    //   1391: putfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1394: aload_0
    //   1395: iconst_0
    //   1396: putfield 411	org/telegram/messenger/MediaController:isPaused	Z
    //   1399: aload_0
    //   1400: aconst_null
    //   1401: putfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1404: aload_0
    //   1405: iconst_0
    //   1406: putfield 2045	org/telegram/messenger/MediaController:downloadingCurrentMessage	Z
    //   1409: iconst_0
    //   1410: ireturn
    //   1411: iconst_3
    //   1412: istore_2
    //   1413: goto -192 -> 1221
    //   1416: aload_1
    //   1417: invokevirtual 1014	org/telegram/messenger/MessageObject:getDocument	()Lorg/telegram/tgnet/TLRPC$Document;
    //   1420: astore 10
    //   1422: new 1245	java/lang/StringBuilder
    //   1425: dup
    //   1426: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   1429: ldc_w 2432
    //   1432: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1435: aload_1
    //   1436: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   1439: invokevirtual 1292	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   1442: ldc_w 2434
    //   1445: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1448: aload 10
    //   1450: getfield 2437	org/telegram/tgnet/TLRPC$Document:id	J
    //   1453: invokevirtual 1255	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   1456: ldc_w 2439
    //   1459: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1462: aload 10
    //   1464: getfield 2442	org/telegram/tgnet/TLRPC$Document:access_hash	J
    //   1467: invokevirtual 1255	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   1470: ldc_w 2444
    //   1473: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1476: aload 10
    //   1478: getfield 2447	org/telegram/tgnet/TLRPC$Document:dc_id	I
    //   1481: invokevirtual 1292	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   1484: ldc_w 2449
    //   1487: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1490: aload 10
    //   1492: getfield 2450	org/telegram/tgnet/TLRPC$Document:size	I
    //   1495: invokevirtual 1292	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   1498: ldc_w 2452
    //   1501: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1504: aload 10
    //   1506: getfield 2455	org/telegram/tgnet/TLRPC$Document:mime_type	Ljava/lang/String;
    //   1509: ldc_w 2457
    //   1512: invokestatic 2463	java/net/URLEncoder:encode	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   1515: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1518: ldc_w 2465
    //   1521: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1524: aload 10
    //   1526: invokestatic 2469	org/telegram/messenger/FileLoader:getDocumentFileName	(Lorg/telegram/tgnet/TLRPC$Document;)Ljava/lang/String;
    //   1529: ldc_w 2457
    //   1532: invokestatic 2463	java/net/URLEncoder:encode	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   1535: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1538: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1541: astore 10
    //   1543: new 1245	java/lang/StringBuilder
    //   1546: dup
    //   1547: invokespecial 1246	java/lang/StringBuilder:<init>	()V
    //   1550: ldc_w 2471
    //   1553: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1556: aload_1
    //   1557: invokevirtual 1955	org/telegram/messenger/MessageObject:getFileName	()Ljava/lang/String;
    //   1560: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1563: aload 10
    //   1565: invokevirtual 1252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1568: invokevirtual 1258	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1571: invokestatic 2475	android/net/Uri:parse	(Ljava/lang/String;)Landroid/net/Uri;
    //   1574: astore 10
    //   1576: aload_0
    //   1577: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1580: aload 10
    //   1582: ldc_w 2377
    //   1585: invokevirtual 2381	org/telegram/ui/Components/VideoPlayer:preparePlayer	(Landroid/net/Uri;Ljava/lang/String;)V
    //   1588: goto -299 -> 1289
    //   1591: aload_0
    //   1592: aload 11
    //   1594: invokestatic 2480	org/telegram/messenger/audioinfo/AudioInfo:getAudioInfo	(Ljava/io/File;)Lorg/telegram/messenger/audioinfo/AudioInfo;
    //   1597: putfield 2024	org/telegram/messenger/MediaController:audioInfo	Lorg/telegram/messenger/audioinfo/AudioInfo;
    //   1600: goto -906 -> 694
    //   1603: astore 10
    //   1605: aload 10
    //   1607: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1610: goto -916 -> 694
    //   1613: iconst_0
    //   1614: istore_2
    //   1615: goto -256 -> 1359
    //   1618: astore 10
    //   1620: aload_0
    //   1621: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1624: fconst_0
    //   1625: putfield 1725	org/telegram/messenger/MessageObject:audioProgress	F
    //   1628: aload_0
    //   1629: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1632: iconst_0
    //   1633: putfield 1728	org/telegram/messenger/MessageObject:audioProgressSec	I
    //   1636: aload_1
    //   1637: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   1640: invokestatic 1736	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   1643: getstatic 1739	org/telegram/messenger/NotificationCenter:messagePlayingProgressDidChanged	I
    //   1646: iconst_2
    //   1647: anewarray 4	java/lang/Object
    //   1650: dup
    //   1651: iconst_0
    //   1652: aload_0
    //   1653: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1656: invokevirtual 1124	org/telegram/messenger/MessageObject:getId	()I
    //   1659: invokestatic 1555	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   1662: aastore
    //   1663: dup
    //   1664: iconst_1
    //   1665: iconst_0
    //   1666: invokestatic 1555	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   1669: aastore
    //   1670: invokevirtual 1743	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   1673: aload 10
    //   1675: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1678: goto -839 -> 839
    //   1681: aload_0
    //   1682: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1685: ifnull +126 -> 1811
    //   1688: aload_0
    //   1689: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1692: getfield 1725	org/telegram/messenger/MessageObject:audioProgress	F
    //   1695: fconst_0
    //   1696: fcmpl
    //   1697: ifeq -858 -> 839
    //   1700: aload_0
    //   1701: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1704: invokevirtual 2391	org/telegram/ui/Components/VideoPlayer:getDuration	()J
    //   1707: lstore 8
    //   1709: lload 8
    //   1711: lstore 6
    //   1713: lload 8
    //   1715: ldc2_w 2392
    //   1718: lcmp
    //   1719: ifne +13 -> 1732
    //   1722: aload_0
    //   1723: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1726: invokevirtual 2395	org/telegram/messenger/MessageObject:getDuration	()I
    //   1729: i2l
    //   1730: lstore 6
    //   1732: lload 6
    //   1734: l2f
    //   1735: aload_0
    //   1736: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1739: getfield 1725	org/telegram/messenger/MessageObject:audioProgress	F
    //   1742: fmul
    //   1743: f2i
    //   1744: istore_2
    //   1745: aload_0
    //   1746: getfield 413	org/telegram/messenger/MediaController:audioPlayer	Lorg/telegram/ui/Components/VideoPlayer;
    //   1749: iload_2
    //   1750: i2l
    //   1751: invokevirtual 2397	org/telegram/ui/Components/VideoPlayer:seekTo	(J)V
    //   1754: goto -915 -> 839
    //   1757: astore 10
    //   1759: aload_0
    //   1760: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1763: invokevirtual 1769	org/telegram/messenger/MessageObject:resetPlayingProgress	()V
    //   1766: aload_1
    //   1767: getfield 1731	org/telegram/messenger/MessageObject:currentAccount	I
    //   1770: invokestatic 1736	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   1773: getstatic 1739	org/telegram/messenger/NotificationCenter:messagePlayingProgressDidChanged	I
    //   1776: iconst_2
    //   1777: anewarray 4	java/lang/Object
    //   1780: dup
    //   1781: iconst_0
    //   1782: aload_0
    //   1783: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1786: invokevirtual 1124	org/telegram/messenger/MessageObject:getId	()I
    //   1789: invokestatic 1555	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   1792: aastore
    //   1793: dup
    //   1794: iconst_1
    //   1795: iconst_0
    //   1796: invokestatic 1555	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   1799: aastore
    //   1800: invokevirtual 1743	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   1803: aload 10
    //   1805: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1808: goto -969 -> 839
    //   1811: aload_0
    //   1812: getfield 415	org/telegram/messenger/MediaController:audioTrackPlayer	Landroid/media/AudioTrack;
    //   1815: ifnull -976 -> 839
    //   1818: aload_0
    //   1819: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1822: getfield 1725	org/telegram/messenger/MessageObject:audioProgress	F
    //   1825: fconst_1
    //   1826: fcmpl
    //   1827: ifne +11 -> 1838
    //   1830: aload_0
    //   1831: getfield 649	org/telegram/messenger/MediaController:playingMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1834: fconst_0
    //   1835: putfield 1725	org/telegram/messenger/MessageObject:audioProgress	F
    //   1838: aload_0
    //   1839: getfield 475	org/telegram/messenger/MediaController:fileDecodingQueue	Lorg/telegram/messenger/DispatchQueue;
    //   1842: new 52	org/telegram/messenger/MediaController$22
    //   1845: dup
    //   1846: aload_0
    //   1847: invokespecial 2481	org/telegram/messenger/MediaController$22:<init>	(Lorg/telegram/messenger/MediaController;)V
    //   1850: invokevirtual 480	org/telegram/messenger/DispatchQueue:postRunnable	(Ljava/lang/Runnable;)V
    //   1853: goto -1014 -> 839
    //   1856: astore_1
    //   1857: aload_1
    //   1858: invokestatic 547	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1861: goto -990 -> 871
    //   1864: new 1967	android/content/Intent
    //   1867: dup
    //   1868: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   1871: ldc_w 2063
    //   1874: invokespecial 1972	android/content/Intent:<init>	(Landroid/content/Context;Ljava/lang/Class;)V
    //   1877: astore_1
    //   1878: getstatic 514	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   1881: aload_1
    //   1882: invokevirtual 2067	android/content/Context:stopService	(Landroid/content/Intent;)Z
    //   1885: pop
    //   1886: goto -1015 -> 871
    //   1889: iconst_3
    //   1890: istore_2
    //   1891: goto -820 -> 1071
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1894	0	this	MediaController
    //   0	1894	1	paramMessageObject	MessageObject
    //   263	1628	2	i	int
    //   1343	18	3	j	int
    //   97	942	4	bool1	boolean
    //   107	1137	5	bool2	boolean
    //   796	937	6	l1	long
    //   792	922	8	l2	long
    //   160	522	10	localObject1	Object
    //   884	1	10	localException1	Exception
    //   1325	3	10	localException2	Exception
    //   1420	161	10	localObject3	Object
    //   1603	3	10	localException3	Exception
    //   1618	56	10	localException4	Exception
    //   1757	47	10	localException5	Exception
    //   149	1444	11	localObject4	Object
    //   995	227	12	localObject5	Object
    //   1001	29	13	arrayOfBoolean	Boolean[]
    // Exception table:
    //   from	to	target	type
    //   378	386	444	java/lang/Throwable
    //   600	630	884	java/lang/Exception
    //   982	1038	1048	finally
    //   1043	1046	1048	finally
    //   1049	1052	1048	finally
    //   1054	1069	1048	finally
    //   1071	1125	1048	finally
    //   1125	1128	1048	finally
    //   1132	1170	1048	finally
    //   1170	1173	1048	finally
    //   982	1038	1131	java/lang/Exception
    //   1054	1069	1131	java/lang/Exception
    //   1071	1125	1131	java/lang/Exception
    //   1195	1219	1325	java/lang/Exception
    //   1221	1243	1325	java/lang/Exception
    //   1248	1255	1325	java/lang/Exception
    //   1262	1274	1325	java/lang/Exception
    //   1274	1289	1325	java/lang/Exception
    //   1289	1322	1325	java/lang/Exception
    //   1416	1588	1325	java/lang/Exception
    //   1605	1610	1325	java/lang/Exception
    //   1591	1600	1603	java/lang/Exception
    //   773	794	1618	java/lang/Exception
    //   807	817	1618	java/lang/Exception
    //   817	839	1618	java/lang/Exception
    //   1688	1709	1757	java/lang/Exception
    //   1722	1732	1757	java/lang/Exception
    //   1732	1754	1757	java/lang/Exception
    //   863	871	1856	java/lang/Throwable
  }
  
  public void playMessageAtIndex(int paramInt)
  {
    if ((this.currentPlaylistNum < 0) || (this.currentPlaylistNum >= this.playlist.size())) {
      return;
    }
    this.currentPlaylistNum = paramInt;
    this.playMusicAgain = true;
    if (this.playingMessageObject != null) {
      this.playingMessageObject.resetPlayingProgress();
    }
    playMessage((MessageObject)this.playlist.get(this.currentPlaylistNum));
  }
  
  public void playNextMessage()
  {
    playNextMessageWithoutOrder(false);
  }
  
  public void playPreviousMessage()
  {
    ArrayList localArrayList;
    if (SharedConfig.shuffleMusic)
    {
      localArrayList = this.shuffledPlaylist;
      if ((!localArrayList.isEmpty()) && (this.currentPlaylistNum >= 0) && (this.currentPlaylistNum < localArrayList.size())) {
        break label45;
      }
    }
    for (;;)
    {
      return;
      localArrayList = this.playlist;
      break;
      label45:
      MessageObject localMessageObject = (MessageObject)localArrayList.get(this.currentPlaylistNum);
      if (localMessageObject.audioProgressSec > 10)
      {
        seekToProgress(localMessageObject, 0.0F);
        return;
      }
      if (SharedConfig.playOrderReversed)
      {
        this.currentPlaylistNum -= 1;
        if (this.currentPlaylistNum < 0) {
          this.currentPlaylistNum = (localArrayList.size() - 1);
        }
      }
      while ((this.currentPlaylistNum >= 0) && (this.currentPlaylistNum < localArrayList.size()))
      {
        this.playMusicAgain = true;
        playMessage((MessageObject)localArrayList.get(this.currentPlaylistNum));
        return;
        this.currentPlaylistNum += 1;
        if (this.currentPlaylistNum >= localArrayList.size()) {
          this.currentPlaylistNum = 0;
        }
      }
    }
  }
  
  public boolean resumeAudio(MessageObject paramMessageObject)
  {
    if (((this.audioTrackPlayer == null) && (this.audioPlayer == null) && (this.videoPlayer == null)) || (paramMessageObject == null) || (this.playingMessageObject == null) || (!isSamePlayingMessage(paramMessageObject))) {
      return false;
    }
    for (;;)
    {
      try
      {
        startProgressTimer(this.playingMessageObject);
        if (this.audioPlayer != null)
        {
          this.audioPlayer.play();
          checkAudioFocus(paramMessageObject);
          this.isPaused = false;
          NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, new Object[] { Integer.valueOf(this.playingMessageObject.getId()) });
          return true;
        }
        if (this.audioTrackPlayer != null)
        {
          this.audioTrackPlayer.play();
          checkPlayerQueue();
          continue;
        }
        if (this.videoPlayer == null) {
          continue;
        }
      }
      catch (Exception paramMessageObject)
      {
        FileLog.e(paramMessageObject);
        return false;
      }
      this.videoPlayer.play();
    }
  }
  
  public void scheduleVideoConvert(MessageObject paramMessageObject)
  {
    scheduleVideoConvert(paramMessageObject, false);
  }
  
  public boolean scheduleVideoConvert(MessageObject paramMessageObject, boolean paramBoolean)
  {
    if ((paramMessageObject == null) || (paramMessageObject.videoEditedInfo == null)) {}
    while ((paramBoolean) && (!this.videoConvertQueue.isEmpty())) {
      return false;
    }
    if (paramBoolean) {
      new File(paramMessageObject.messageOwner.attachPath).delete();
    }
    this.videoConvertQueue.add(paramMessageObject);
    if (this.videoConvertQueue.size() == 1) {
      startVideoConvertFromQueue();
    }
    return true;
  }
  
  public boolean seekToProgress(MessageObject paramMessageObject, float paramFloat)
  {
    if (((this.audioTrackPlayer == null) && (this.audioPlayer == null) && (this.videoPlayer == null)) || (paramMessageObject == null) || (this.playingMessageObject == null) || (!isSamePlayingMessage(paramMessageObject))) {
      return false;
    }
    try
    {
      if (this.audioPlayer != null)
      {
        long l = this.audioPlayer.getDuration();
        if (l == -9223372036854775807L)
        {
          this.seekToProgressPending = paramFloat;
        }
        else
        {
          int i = (int)((float)l * paramFloat);
          this.audioPlayer.seekTo(i);
          this.lastProgress = i;
        }
      }
    }
    catch (Exception paramMessageObject)
    {
      FileLog.e(paramMessageObject);
      return false;
    }
    if (this.audioTrackPlayer != null) {
      seekOpusPlayer(paramFloat);
    } else if (this.videoPlayer != null) {
      this.videoPlayer.seekTo(((float)this.videoPlayer.getDuration() * paramFloat));
    }
    return true;
  }
  
  public void setAllowStartRecord(boolean paramBoolean)
  {
    this.allowStartRecord = paramBoolean;
  }
  
  public void setBaseActivity(Activity paramActivity, boolean paramBoolean)
  {
    if (paramBoolean) {
      this.baseActivity = paramActivity;
    }
    while (this.baseActivity != paramActivity) {
      return;
    }
    this.baseActivity = null;
  }
  
  public void setCurrentRoundVisible(boolean paramBoolean)
  {
    if (this.currentAspectRatioFrameLayout == null) {}
    for (;;)
    {
      return;
      if (paramBoolean)
      {
        if (this.pipRoundVideoView != null)
        {
          this.pipSwitchingState = 2;
          this.pipRoundVideoView.close(true);
          this.pipRoundVideoView = null;
          return;
        }
        if (this.currentAspectRatioFrameLayout == null) {
          continue;
        }
        if (this.currentAspectRatioFrameLayout.getParent() == null) {
          this.currentTextureViewContainer.addView(this.currentAspectRatioFrameLayout);
        }
        this.videoPlayer.setTextureView(this.currentTextureView);
        return;
      }
      if (this.currentAspectRatioFrameLayout.getParent() != null)
      {
        this.pipSwitchingState = 1;
        this.currentTextureViewContainer.removeView(this.currentAspectRatioFrameLayout);
        return;
      }
      if (this.pipRoundVideoView == null) {}
      try
      {
        this.pipRoundVideoView = new PipRoundVideoView();
        this.pipRoundVideoView.show(this.baseActivity, new Runnable()
        {
          public void run()
          {
            MediaController.this.cleanupPlayer(true, true);
          }
        });
        if (this.pipRoundVideoView == null) {
          continue;
        }
        this.videoPlayer.setTextureView(this.pipRoundVideoView.getTextureView());
        return;
      }
      catch (Exception localException)
      {
        for (;;)
        {
          this.pipRoundVideoView = null;
        }
      }
    }
  }
  
  public void setFeedbackView(View paramView, boolean paramBoolean)
  {
    if (paramBoolean) {
      this.feedbackView = paramView;
    }
    while (this.feedbackView != paramView) {
      return;
    }
    this.feedbackView = null;
  }
  
  public void setFlagSecure(BaseFragment paramBaseFragment, boolean paramBoolean)
  {
    if (paramBoolean) {}
    try
    {
      paramBaseFragment.getParentActivity().getWindow().setFlags(8192, 8192);
      this.flagSecureFragment = paramBaseFragment;
      do
      {
        return;
      } while (this.flagSecureFragment != paramBaseFragment);
      try
      {
        paramBaseFragment.getParentActivity().getWindow().clearFlags(8192);
        this.flagSecureFragment = null;
        return;
      }
      catch (Exception paramBaseFragment)
      {
        for (;;) {}
      }
    }
    catch (Exception localException)
    {
      for (;;) {}
    }
  }
  
  public void setInputFieldHasText(boolean paramBoolean)
  {
    this.inputFieldHasText = paramBoolean;
  }
  
  public void setLastVisibleMessageIds(int paramInt1, long paramLong1, long paramLong2, TLRPC.User paramUser, TLRPC.EncryptedChat paramEncryptedChat, ArrayList<Long> paramArrayList, int paramInt2)
  {
    this.lastChatEnterTime = paramLong1;
    this.lastChatLeaveTime = paramLong2;
    this.lastChatAccount = paramInt1;
    this.lastSecretChat = paramEncryptedChat;
    this.lastUser = paramUser;
    this.lastMessageId = paramInt2;
    this.lastChatVisibleMessages = paramArrayList;
  }
  
  public boolean setPlaylist(ArrayList<MessageObject> paramArrayList, MessageObject paramMessageObject)
  {
    return setPlaylist(paramArrayList, paramMessageObject, true);
  }
  
  public boolean setPlaylist(ArrayList<MessageObject> paramArrayList, MessageObject paramMessageObject, boolean paramBoolean)
  {
    boolean bool2 = true;
    if (this.playingMessageObject == paramMessageObject) {
      return playMessage(paramMessageObject);
    }
    if (!paramBoolean)
    {
      bool1 = true;
      this.forceLoopCurrentPlaylist = bool1;
      if (this.playlist.isEmpty()) {
        break label114;
      }
    }
    label114:
    for (boolean bool1 = bool2;; bool1 = false)
    {
      this.playMusicAgain = bool1;
      this.playlist.clear();
      int i = paramArrayList.size() - 1;
      while (i >= 0)
      {
        MessageObject localMessageObject = (MessageObject)paramArrayList.get(i);
        if (localMessageObject.isMusic()) {
          this.playlist.add(localMessageObject);
        }
        i -= 1;
      }
      bool1 = false;
      break;
    }
    this.currentPlaylistNum = this.playlist.indexOf(paramMessageObject);
    if (this.currentPlaylistNum == -1)
    {
      this.playlist.clear();
      this.shuffledPlaylist.clear();
      this.currentPlaylistNum = this.playlist.size();
      this.playlist.add(paramMessageObject);
    }
    if (paramMessageObject.isMusic())
    {
      if (SharedConfig.shuffleMusic)
      {
        buildShuffledPlayList();
        this.currentPlaylistNum = 0;
      }
      if (paramBoolean) {
        DataQuery.getInstance(paramMessageObject.currentAccount).loadMusic(paramMessageObject.getDialogId(), ((MessageObject)this.playlist.get(0)).getIdWithChannel());
      }
    }
    return playMessage(paramMessageObject);
  }
  
  public void setReplyingMessage(MessageObject paramMessageObject)
  {
    this.recordReplyingMessageObject = paramMessageObject;
  }
  
  public void setTextureView(TextureView paramTextureView, AspectRatioFrameLayout paramAspectRatioFrameLayout, FrameLayout paramFrameLayout, boolean paramBoolean)
  {
    boolean bool = true;
    if (paramTextureView == null) {}
    do
    {
      return;
      if ((!paramBoolean) && (this.currentTextureView == paramTextureView))
      {
        this.pipSwitchingState = 1;
        this.currentTextureView = null;
        this.currentAspectRatioFrameLayout = null;
        this.currentTextureViewContainer = null;
        return;
      }
    } while ((this.videoPlayer == null) || (paramTextureView == this.currentTextureView));
    if ((paramAspectRatioFrameLayout != null) && (paramAspectRatioFrameLayout.isDrawingReady()))
    {
      paramBoolean = bool;
      label72:
      this.isDrawingWasReady = paramBoolean;
      this.currentTextureView = paramTextureView;
      if (this.pipRoundVideoView == null) {
        break label175;
      }
      this.videoPlayer.setTextureView(this.pipRoundVideoView.getTextureView());
    }
    for (;;)
    {
      this.currentAspectRatioFrameLayout = paramAspectRatioFrameLayout;
      this.currentTextureViewContainer = paramFrameLayout;
      if ((!this.currentAspectRatioFrameLayoutReady) || (this.currentAspectRatioFrameLayout == null)) {
        break;
      }
      if (this.currentAspectRatioFrameLayout != null) {
        this.currentAspectRatioFrameLayout.setAspectRatio(this.currentAspectRatioFrameLayoutRatio, this.currentAspectRatioFrameLayoutRotation);
      }
      if (this.currentTextureViewContainer.getVisibility() == 0) {
        break;
      }
      this.currentTextureViewContainer.setVisibility(0);
      return;
      paramBoolean = false;
      break label72;
      label175:
      this.videoPlayer.setTextureView(this.currentTextureView);
    }
  }
  
  public void setVoiceMessagesPlaylist(ArrayList<MessageObject> paramArrayList, boolean paramBoolean)
  {
    this.voiceMessagesPlaylist = paramArrayList;
    if (this.voiceMessagesPlaylist != null)
    {
      this.voiceMessagesPlaylistUnread = paramBoolean;
      this.voiceMessagesPlaylistMap = new SparseArray();
      int i = 0;
      while (i < this.voiceMessagesPlaylist.size())
      {
        paramArrayList = (MessageObject)this.voiceMessagesPlaylist.get(i);
        this.voiceMessagesPlaylistMap.put(paramArrayList.getId(), paramArrayList);
        i += 1;
      }
    }
  }
  
  public void startMediaObserver()
  {
    ApplicationLoader.applicationHandler.removeCallbacks(this.stopMediaObserverRunnable);
    this.startObserverToken += 1;
    try
    {
      if (this.internalObserver == null)
      {
        localContentResolver = ApplicationLoader.applicationContext.getContentResolver();
        localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        localObject = new ExternalObserver();
        this.externalObserver = ((ExternalObserver)localObject);
        localContentResolver.registerContentObserver(localUri, false, (ContentObserver)localObject);
      }
    }
    catch (Exception localException1)
    {
      for (;;)
      {
        try
        {
          ContentResolver localContentResolver;
          Uri localUri;
          Object localObject;
          if (this.externalObserver == null)
          {
            localContentResolver = ApplicationLoader.applicationContext.getContentResolver();
            localUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            localObject = new InternalObserver();
            this.internalObserver = ((InternalObserver)localObject);
            localContentResolver.registerContentObserver(localUri, false, (ContentObserver)localObject);
          }
          return;
        }
        catch (Exception localException2)
        {
          FileLog.e(localException2);
        }
        localException1 = localException1;
        FileLog.e(localException1);
      }
    }
  }
  
  public void startRaiseToEarSensors(ChatActivity paramChatActivity)
  {
    if ((paramChatActivity == null) || ((this.accelerometerSensor == null) && ((this.gravitySensor == null) || (this.linearAcceleration == null))) || (this.proximitySensor == null)) {}
    do
    {
      return;
      this.raiseChat = paramChatActivity;
    } while (((!SharedConfig.raiseToSpeak) && ((this.playingMessageObject == null) || ((!this.playingMessageObject.isVoice()) && (!this.playingMessageObject.isRoundVideo())))) || (this.sensorsStarted));
    paramChatActivity = this.gravity;
    float[] arrayOfFloat = this.gravity;
    this.gravity[2] = 0.0F;
    arrayOfFloat[1] = 0.0F;
    paramChatActivity[0] = 0.0F;
    paramChatActivity = this.linearAcceleration;
    arrayOfFloat = this.linearAcceleration;
    this.linearAcceleration[2] = 0.0F;
    arrayOfFloat[1] = 0.0F;
    paramChatActivity[0] = 0.0F;
    paramChatActivity = this.gravityFast;
    arrayOfFloat = this.gravityFast;
    this.gravityFast[2] = 0.0F;
    arrayOfFloat[1] = 0.0F;
    paramChatActivity[0] = 0.0F;
    this.lastTimestamp = 0L;
    this.previousAccValue = 0.0F;
    this.raisedToTop = 0;
    this.raisedToTopSign = 0;
    this.countLess = 0;
    this.raisedToBack = 0;
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (MediaController.this.gravitySensor != null) {
          MediaController.this.sensorManager.registerListener(MediaController.this, MediaController.this.gravitySensor, 30000);
        }
        if (MediaController.this.linearSensor != null) {
          MediaController.this.sensorManager.registerListener(MediaController.this, MediaController.this.linearSensor, 30000);
        }
        if (MediaController.this.accelerometerSensor != null) {
          MediaController.this.sensorManager.registerListener(MediaController.this, MediaController.this.accelerometerSensor, 30000);
        }
        MediaController.this.sensorManager.registerListener(MediaController.this, MediaController.this.proximitySensor, 3);
      }
    });
    this.sensorsStarted = true;
  }
  
  public void startRecording(final int paramInt, final long paramLong, MessageObject paramMessageObject)
  {
    int j = 0;
    int i = j;
    if (this.playingMessageObject != null)
    {
      i = j;
      if (isPlayingMessage(this.playingMessageObject))
      {
        i = j;
        if (!isMessagePaused())
        {
          i = 1;
          pauseMessage(this.playingMessageObject);
        }
      }
    }
    try
    {
      this.feedbackView.performHapticFeedback(3, 2);
      DispatchQueue localDispatchQueue = this.recordQueue;
      paramMessageObject = new Runnable()
      {
        public void run()
        {
          if (MediaController.this.audioRecorder != null)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MediaController.access$3102(MediaController.this, null);
                NotificationCenter.getInstance(MediaController.23.this.val$currentAccount).postNotificationName(NotificationCenter.recordStartError, new Object[0]);
              }
            });
            return;
          }
          MediaController.access$3202(MediaController.this, new TLRPC.TL_document());
          MediaController.this.recordingAudio.dc_id = Integer.MIN_VALUE;
          MediaController.this.recordingAudio.id = SharedConfig.getLastLocalId();
          MediaController.this.recordingAudio.user_id = UserConfig.getInstance(paramInt).getClientUserId();
          MediaController.this.recordingAudio.mime_type = "audio/ogg";
          MediaController.this.recordingAudio.thumb = new TLRPC.TL_photoSizeEmpty();
          MediaController.this.recordingAudio.thumb.type = "s";
          SharedConfig.saveConfig();
          MediaController.access$7002(MediaController.this, new File(FileLoader.getDirectory(4), FileLoader.getAttachFileName(MediaController.this.recordingAudio)));
          try
          {
            if (MediaController.this.startRecord(MediaController.this.recordingAudioFile.getAbsolutePath()) == 0)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MediaController.access$3102(MediaController.this, null);
                  NotificationCenter.getInstance(MediaController.23.this.val$currentAccount).postNotificationName(NotificationCenter.recordStartError, new Object[0]);
                }
              });
              return;
            }
          }
          catch (Exception localException1)
          {
            FileLog.e(localException1);
            MediaController.access$3202(MediaController.this, null);
            MediaController.this.stopRecord();
            MediaController.this.recordingAudioFile.delete();
            MediaController.access$7002(MediaController.this, null);
          }
          try
          {
            MediaController.this.audioRecorder.release();
            MediaController.access$002(MediaController.this, null);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MediaController.access$3102(MediaController.this, null);
                NotificationCenter.getInstance(MediaController.23.this.val$currentAccount).postNotificationName(NotificationCenter.recordStartError, new Object[0]);
              }
            });
            return;
            MediaController.access$002(MediaController.this, new AudioRecord(1, 16000, 16, 2, MediaController.this.recordBufferSize * 10));
            MediaController.access$1102(MediaController.this, System.currentTimeMillis());
            MediaController.access$702(MediaController.this, 0L);
            MediaController.access$302(MediaController.this, 0L);
            MediaController.access$7202(MediaController.this, paramLong);
            MediaController.access$1202(MediaController.this, paramInt);
            MediaController.access$7302(MediaController.this, this.val$reply_to_msg);
            MediaController.this.fileBuffer.rewind();
            MediaController.this.audioRecorder.startRecording();
            MediaController.this.recordQueue.postRunnable(MediaController.this.recordRunnable);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MediaController.access$3102(MediaController.this, null);
                NotificationCenter.getInstance(MediaController.23.this.val$currentAccount).postNotificationName(NotificationCenter.recordStarted, new Object[0]);
              }
            });
            return;
          }
          catch (Exception localException2)
          {
            for (;;)
            {
              FileLog.e(localException2);
            }
          }
        }
      };
      this.recordStartRunnable = paramMessageObject;
      if (i != 0) {}
      for (paramLong = 500L;; paramLong = 50L)
      {
        localDispatchQueue.postRunnable(paramMessageObject, paramLong);
        return;
      }
    }
    catch (Exception localException)
    {
      for (;;) {}
    }
  }
  
  public void startRecordingIfFromSpeaker()
  {
    if ((!this.useFrontSpeaker) || (this.raiseChat == null) || (!this.allowStartRecord)) {
      return;
    }
    this.raiseToEarRecord = true;
    startRecording(this.raiseChat.getCurrentAccount(), this.raiseChat.getDialogId(), null);
    this.ignoreOnPause = true;
  }
  
  public void startSmsObserver()
  {
    try
    {
      if (this.smsObserver == null)
      {
        ContentResolver localContentResolver = ApplicationLoader.applicationContext.getContentResolver();
        Uri localUri = Uri.parse("content://sms");
        SmsObserver localSmsObserver = new SmsObserver();
        this.smsObserver = localSmsObserver;
        localContentResolver.registerContentObserver(localUri, false, localSmsObserver);
      }
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          try
          {
            if (MediaController.this.smsObserver != null)
            {
              ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.smsObserver);
              MediaController.access$4402(MediaController.this, null);
            }
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
          }
        }
      }, 300000L);
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public void stopAudio()
  {
    if (((this.audioTrackPlayer == null) && (this.audioPlayer == null) && (this.videoPlayer == null)) || (this.playingMessageObject == null)) {
      return;
    }
    try
    {
      if (this.audioPlayer != null) {
        this.audioPlayer.pause();
      }
    }
    catch (Exception localException1)
    {
      try
      {
        if (this.audioPlayer != null)
        {
          this.audioPlayer.releasePlayer();
          this.audioPlayer = null;
        }
        for (;;)
        {
          stopProgressTimer();
          this.playingMessageObject = null;
          this.downloadingCurrentMessage = false;
          this.isPaused = false;
          Intent localIntent = new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class);
          ApplicationLoader.applicationContext.stopService(localIntent);
          return;
          if (this.audioTrackPlayer != null)
          {
            this.audioTrackPlayer.pause();
            this.audioTrackPlayer.flush();
            break;
            localException1 = localException1;
            FileLog.e(localException1);
            break;
          }
          if (this.videoPlayer == null) {
            break;
          }
          this.videoPlayer.pause();
          break;
          if (this.audioTrackPlayer != null) {
            synchronized (this.playerObjectSync)
            {
              this.audioTrackPlayer.release();
              this.audioTrackPlayer = null;
            }
          }
        }
      }
      catch (Exception localException2)
      {
        for (;;)
        {
          FileLog.e(localException2);
          continue;
          if (this.videoPlayer != null)
          {
            this.currentAspectRatioFrameLayout = null;
            this.currentTextureViewContainer = null;
            this.currentAspectRatioFrameLayoutReady = false;
            this.currentTextureView = null;
            this.videoPlayer.releasePlayer();
            this.videoPlayer = null;
            try
            {
              this.baseActivity.getWindow().clearFlags(128);
            }
            catch (Exception localException3)
            {
              FileLog.e(localException3);
            }
          }
        }
      }
    }
  }
  
  public void stopMediaObserver()
  {
    if (this.stopMediaObserverRunnable == null) {
      this.stopMediaObserverRunnable = new StopMediaObserverRunnable(null);
    }
    this.stopMediaObserverRunnable.currentObserverToken = this.startObserverToken;
    ApplicationLoader.applicationHandler.postDelayed(this.stopMediaObserverRunnable, 5000L);
  }
  
  public void stopRaiseToEarSensors(ChatActivity paramChatActivity)
  {
    if (this.ignoreOnPause) {
      this.ignoreOnPause = false;
    }
    do
    {
      do
      {
        return;
        stopRecording(0);
      } while ((!this.sensorsStarted) || (this.ignoreOnPause) || ((this.accelerometerSensor == null) && ((this.gravitySensor == null) || (this.linearAcceleration == null))) || (this.proximitySensor == null) || (this.raiseChat != paramChatActivity));
      this.raiseChat = null;
      this.sensorsStarted = false;
      this.accelerometerVertical = false;
      this.proximityTouched = false;
      this.raiseToEarRecord = false;
      this.useFrontSpeaker = false;
      Utilities.globalQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          if (MediaController.this.linearSensor != null) {
            MediaController.this.sensorManager.unregisterListener(MediaController.this, MediaController.this.linearSensor);
          }
          if (MediaController.this.gravitySensor != null) {
            MediaController.this.sensorManager.unregisterListener(MediaController.this, MediaController.this.gravitySensor);
          }
          if (MediaController.this.accelerometerSensor != null) {
            MediaController.this.sensorManager.unregisterListener(MediaController.this, MediaController.this.accelerometerSensor);
          }
          MediaController.this.sensorManager.unregisterListener(MediaController.this, MediaController.this.proximitySensor);
        }
      });
    } while ((!this.proximityHasDifferentValues) || (this.proximityWakeLock == null) || (!this.proximityWakeLock.isHeld()));
    this.proximityWakeLock.release();
  }
  
  public void stopRecording(final int paramInt)
  {
    if (this.recordStartRunnable != null)
    {
      this.recordQueue.cancelRunnable(this.recordStartRunnable);
      this.recordStartRunnable = null;
    }
    this.recordQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (MediaController.this.audioRecorder == null) {
          return;
        }
        try
        {
          MediaController.access$1302(MediaController.this, paramInt);
          MediaController.this.audioRecorder.stop();
          if (paramInt == 0) {
            MediaController.this.stopRecordingInternal(0);
          }
        }
        catch (Exception localException1)
        {
          try
          {
            do
            {
              MediaController.this.feedbackView.performHapticFeedback(3, 2);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  int i = 1;
                  NotificationCenter localNotificationCenter = NotificationCenter.getInstance(MediaController.this.recordingCurrentAccount);
                  int j = NotificationCenter.recordStopped;
                  if (MediaController.26.this.val$send == 2) {}
                  for (;;)
                  {
                    localNotificationCenter.postNotificationName(j, new Object[] { Integer.valueOf(i) });
                    return;
                    i = 0;
                  }
                }
              });
              return;
              localException1 = localException1;
              FileLog.e(localException1);
            } while (MediaController.this.recordingAudioFile == null);
            MediaController.this.recordingAudioFile.delete();
          }
          catch (Exception localException2)
          {
            for (;;) {}
          }
        }
      }
    });
  }
  
  public void toggleShuffleMusic(int paramInt)
  {
    boolean bool = SharedConfig.shuffleMusic;
    SharedConfig.toggleShuffleMusic(paramInt);
    if (bool != SharedConfig.shuffleMusic)
    {
      if (!SharedConfig.shuffleMusic) {
        break label31;
      }
      buildShuffledPlayList();
      this.currentPlaylistNum = 0;
    }
    label31:
    do
    {
      do
      {
        return;
      } while (this.playingMessageObject == null);
      this.currentPlaylistNum = this.playlist.indexOf(this.playingMessageObject);
    } while (this.currentPlaylistNum != -1);
    this.playlist.clear();
    this.shuffledPlaylist.clear();
    cleanupPlayer(true, true);
  }
  
  public static class AlbumEntry
  {
    public int bucketId;
    public String bucketName;
    public MediaController.PhotoEntry coverPhoto;
    public ArrayList<MediaController.PhotoEntry> photos = new ArrayList();
    public SparseArray<MediaController.PhotoEntry> photosByIds = new SparseArray();
    
    public AlbumEntry(int paramInt, String paramString, MediaController.PhotoEntry paramPhotoEntry)
    {
      this.bucketId = paramInt;
      this.bucketName = paramString;
      this.coverPhoto = paramPhotoEntry;
    }
    
    public void addPhoto(MediaController.PhotoEntry paramPhotoEntry)
    {
      this.photos.add(paramPhotoEntry);
      this.photosByIds.put(paramPhotoEntry.imageId, paramPhotoEntry);
    }
  }
  
  private class AudioBuffer
  {
    ByteBuffer buffer;
    byte[] bufferBytes;
    int finished;
    long pcmOffset;
    int size;
    
    public AudioBuffer(int paramInt)
    {
      this.buffer = ByteBuffer.allocateDirect(paramInt);
      this.bufferBytes = new byte[paramInt];
    }
  }
  
  public static class AudioEntry
  {
    public String author;
    public int duration;
    public String genre;
    public long id;
    public MessageObject messageObject;
    public String path;
    public String title;
  }
  
  private class ExternalObserver
    extends ContentObserver
  {
    public ExternalObserver()
    {
      super();
    }
    
    public void onChange(boolean paramBoolean)
    {
      super.onChange(paramBoolean);
      MediaController.this.processMediaObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }
  }
  
  private class GalleryObserverExternal
    extends ContentObserver
  {
    public GalleryObserverExternal()
    {
      super();
    }
    
    public void onChange(boolean paramBoolean)
    {
      super.onChange(paramBoolean);
      if (MediaController.refreshGalleryRunnable != null) {
        AndroidUtilities.cancelRunOnUIThread(MediaController.refreshGalleryRunnable);
      }
      AndroidUtilities.runOnUIThread(MediaController.access$1702(new Runnable()
      {
        public void run()
        {
          MediaController.access$1702(null);
          MediaController.loadGalleryPhotosAlbums(0);
        }
      }), 2000L);
    }
  }
  
  private class GalleryObserverInternal
    extends ContentObserver
  {
    public GalleryObserverInternal()
    {
      super();
    }
    
    private void scheduleReloadRunnable()
    {
      AndroidUtilities.runOnUIThread(MediaController.access$1702(new Runnable()
      {
        public void run()
        {
          if (PhotoViewer.getInstance().isVisible())
          {
            MediaController.GalleryObserverInternal.this.scheduleReloadRunnable();
            return;
          }
          MediaController.access$1702(null);
          MediaController.loadGalleryPhotosAlbums(0);
        }
      }), 2000L);
    }
    
    public void onChange(boolean paramBoolean)
    {
      super.onChange(paramBoolean);
      if (MediaController.refreshGalleryRunnable != null) {
        AndroidUtilities.cancelRunOnUIThread(MediaController.refreshGalleryRunnable);
      }
      scheduleReloadRunnable();
    }
  }
  
  private class InternalObserver
    extends ContentObserver
  {
    public InternalObserver()
    {
      super();
    }
    
    public void onChange(boolean paramBoolean)
    {
      super.onChange(paramBoolean);
      MediaController.this.processMediaObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    }
  }
  
  public static class PhotoEntry
  {
    public int bucketId;
    public CharSequence caption;
    public long dateTaken;
    public int duration;
    public VideoEditedInfo editedInfo;
    public ArrayList<TLRPC.MessageEntity> entities;
    public int imageId;
    public String imagePath;
    public boolean isCropped;
    public boolean isFiltered;
    public boolean isMuted;
    public boolean isPainted;
    public boolean isVideo;
    public int orientation;
    public String path;
    public MediaController.SavedFilterState savedFilterState;
    public ArrayList<TLRPC.InputDocument> stickers = new ArrayList();
    public String thumbPath;
    public int ttl;
    
    public PhotoEntry(int paramInt1, int paramInt2, long paramLong, String paramString, int paramInt3, boolean paramBoolean)
    {
      this.bucketId = paramInt1;
      this.imageId = paramInt2;
      this.dateTaken = paramLong;
      this.path = paramString;
      if (paramBoolean) {
        this.duration = paramInt3;
      }
      for (;;)
      {
        this.isVideo = paramBoolean;
        return;
        this.orientation = paramInt3;
      }
    }
    
    public void reset()
    {
      this.isFiltered = false;
      this.isPainted = false;
      this.isCropped = false;
      this.ttl = 0;
      this.imagePath = null;
      if (!this.isVideo) {
        this.thumbPath = null;
      }
      this.editedInfo = null;
      this.caption = null;
      this.entities = null;
      this.savedFilterState = null;
      this.stickers.clear();
    }
  }
  
  public static class SavedFilterState
  {
    public float blurAngle;
    public float blurExcludeBlurSize;
    public org.telegram.ui.Components.Point blurExcludePoint;
    public float blurExcludeSize;
    public int blurType;
    public float contrastValue;
    public PhotoFilterView.CurvesToolValue curvesToolValue = new PhotoFilterView.CurvesToolValue();
    public float enhanceValue;
    public float exposureValue;
    public float fadeValue;
    public float grainValue;
    public float highlightsValue;
    public float saturationValue;
    public float shadowsValue;
    public float sharpenValue;
    public int tintHighlightsColor;
    public int tintShadowsColor;
    public float vignetteValue;
    public float warmthValue;
  }
  
  public static class SearchImage
  {
    public CharSequence caption;
    public int date;
    public TLRPC.Document document;
    public ArrayList<TLRPC.MessageEntity> entities;
    public int height;
    public String id;
    public String imagePath;
    public String imageUrl;
    public boolean isCropped;
    public boolean isFiltered;
    public boolean isPainted;
    public String localUrl;
    public MediaController.SavedFilterState savedFilterState;
    public int size;
    public ArrayList<TLRPC.InputDocument> stickers = new ArrayList();
    public String thumbPath;
    public String thumbUrl;
    public int ttl;
    public int type;
    public int width;
    
    public void reset()
    {
      this.isFiltered = false;
      this.isPainted = false;
      this.isCropped = false;
      this.ttl = 0;
      this.imagePath = null;
      this.thumbPath = null;
      this.caption = null;
      this.entities = null;
      this.savedFilterState = null;
      this.stickers.clear();
    }
  }
  
  private class SmsObserver
    extends ContentObserver
  {
    public SmsObserver()
    {
      super();
    }
    
    public void onChange(boolean paramBoolean)
    {
      MediaController.this.readSms();
    }
  }
  
  private final class StopMediaObserverRunnable
    implements Runnable
  {
    public int currentObserverToken = 0;
    
    private StopMediaObserverRunnable() {}
    
    public void run()
    {
      if (this.currentObserverToken == MediaController.this.startObserverToken) {}
      try
      {
        if (MediaController.this.internalObserver != null)
        {
          ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.internalObserver);
          MediaController.access$2002(MediaController.this, null);
        }
      }
      catch (Exception localException1)
      {
        for (;;)
        {
          try
          {
            if (MediaController.this.externalObserver != null)
            {
              ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.externalObserver);
              MediaController.access$2102(MediaController.this, null);
            }
            return;
          }
          catch (Exception localException2)
          {
            FileLog.e(localException2);
          }
          localException1 = localException1;
          FileLog.e(localException1);
        }
      }
    }
  }
  
  private static class VideoConvertRunnable
    implements Runnable
  {
    private MessageObject messageObject;
    
    private VideoConvertRunnable(MessageObject paramMessageObject)
    {
      this.messageObject = paramMessageObject;
    }
    
    public static void runConversion(MessageObject paramMessageObject)
    {
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            Thread localThread = new Thread(new MediaController.VideoConvertRunnable(this.val$obj, null), "VideoConvertRunnable");
            localThread.start();
            localThread.join();
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
          }
        }
      }).start();
    }
    
    public void run()
    {
      MediaController.getInstance().convertVideo(this.messageObject);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/MediaController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */