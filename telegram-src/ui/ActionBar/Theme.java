package org.telegram.ui.ActionBar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.StateSet;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.time.SunDate;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.ThemeEditorView;

public class Theme
{
  public static final int ACTION_BAR_AUDIO_SELECTOR_COLOR = 788529152;
  public static final int ACTION_BAR_MEDIA_PICKER_COLOR = -13421773;
  public static final int ACTION_BAR_PHOTO_VIEWER_COLOR = 2130706432;
  public static final int ACTION_BAR_PICKER_SELECTOR_COLOR = -12763843;
  public static final int ACTION_BAR_PLAYER_COLOR = -1;
  public static final int ACTION_BAR_VIDEO_EDIT_COLOR = -16777216;
  public static final int ACTION_BAR_WHITE_SELECTOR_COLOR = 1090519039;
  public static final int ARTICLE_VIEWER_MEDIA_PROGRESS_COLOR = -1;
  public static final int AUTO_NIGHT_TYPE_AUTOMATIC = 2;
  public static final int AUTO_NIGHT_TYPE_NONE = 0;
  public static final int AUTO_NIGHT_TYPE_SCHEDULED = 1;
  private static Field BitmapDrawable_mColorFilter;
  private static final int LIGHT_SENSOR_THEME_SWITCH_DELAY = 1800;
  private static final int LIGHT_SENSOR_THEME_SWITCH_NEAR_DELAY = 12000;
  private static final int LIGHT_SENSOR_THEME_SWITCH_NEAR_THRESHOLD = 12000;
  private static final float MAXIMUM_LUX_BREAKPOINT = 500.0F;
  private static Method StateListDrawable_getStateDrawableMethod;
  private static SensorEventListener ambientSensorListener;
  public static float autoNightBrighnessThreshold = 0.0F;
  public static String autoNightCityName;
  public static int autoNightDayEndTime = 0;
  public static int autoNightDayStartTime = 0;
  public static int autoNightLastSunCheckDay = 0;
  public static double autoNightLocationLatitude = 0.0D;
  public static double autoNightLocationLongitude = 0.0D;
  public static boolean autoNightScheduleByLocation = false;
  public static int autoNightSunriseTime = 0;
  public static int autoNightSunsetTime = 0;
  public static Paint avatar_backgroundPaint;
  public static Drawable avatar_broadcastDrawable;
  public static Drawable avatar_photoDrawable;
  public static Drawable avatar_savedDrawable;
  private static boolean canStartHolidayAnimation = false;
  public static Paint chat_actionBackgroundPaint;
  public static TextPaint chat_actionTextPaint;
  public static TextPaint chat_adminPaint;
  public static Drawable[] chat_attachButtonDrawables;
  public static TextPaint chat_audioPerformerPaint;
  public static TextPaint chat_audioTimePaint;
  public static TextPaint chat_audioTitlePaint;
  public static TextPaint chat_botButtonPaint;
  public static Drawable chat_botInlineDrawable;
  public static Drawable chat_botLinkDrawalbe;
  public static Paint chat_botProgressPaint;
  public static Paint chat_composeBackgroundPaint;
  public static Drawable chat_composeShadowDrawable;
  public static Drawable[] chat_contactDrawable;
  public static TextPaint chat_contactNamePaint;
  public static TextPaint chat_contactPhonePaint;
  public static TextPaint chat_contextResult_descriptionTextPaint;
  public static Drawable chat_contextResult_shadowUnderSwitchDrawable;
  public static TextPaint chat_contextResult_titleTextPaint;
  public static Drawable[] chat_cornerInner;
  public static Drawable[] chat_cornerOuter;
  public static Paint chat_deleteProgressPaint;
  public static Paint chat_docBackPaint;
  public static TextPaint chat_docNamePaint;
  public static TextPaint chat_durationPaint;
  public static CombinedDrawable[][] chat_fileMiniStatesDrawable;
  public static Drawable[][] chat_fileStatesDrawable;
  public static TextPaint chat_forwardNamePaint;
  public static TextPaint chat_gamePaint;
  public static Drawable chat_goIconDrawable;
  public static TextPaint chat_infoPaint;
  public static Drawable chat_inlineResultAudio;
  public static Drawable chat_inlineResultFile;
  public static Drawable chat_inlineResultLocation;
  public static TextPaint chat_instantViewPaint;
  public static Paint chat_instantViewRectPaint;
  public static Drawable[][] chat_ivStatesDrawable;
  public static TextPaint chat_livePaint;
  public static TextPaint chat_locationAddressPaint;
  public static Drawable[] chat_locationDrawable;
  public static TextPaint chat_locationTitlePaint;
  public static Drawable chat_lockIconDrawable;
  public static Drawable chat_msgAvatarLiveLocationDrawable;
  public static TextPaint chat_msgBotButtonPaint;
  public static Drawable chat_msgBroadcastDrawable;
  public static Drawable chat_msgBroadcastMediaDrawable;
  public static Drawable chat_msgCallDownGreenDrawable;
  public static Drawable chat_msgCallDownRedDrawable;
  public static Drawable chat_msgCallUpGreenDrawable;
  public static Drawable chat_msgCallUpRedDrawable;
  public static Drawable chat_msgErrorDrawable;
  public static Paint chat_msgErrorPaint;
  public static TextPaint chat_msgGameTextPaint;
  public static Drawable chat_msgInCallDrawable;
  public static Drawable chat_msgInCallSelectedDrawable;
  public static Drawable chat_msgInClockDrawable;
  public static Drawable chat_msgInDrawable;
  public static Drawable chat_msgInInstantDrawable;
  public static Drawable chat_msgInMediaDrawable;
  public static Drawable chat_msgInMediaSelectedDrawable;
  public static Drawable chat_msgInMediaShadowDrawable;
  public static Drawable chat_msgInMenuDrawable;
  public static Drawable chat_msgInMenuSelectedDrawable;
  public static Drawable chat_msgInSelectedClockDrawable;
  public static Drawable chat_msgInSelectedDrawable;
  public static Drawable chat_msgInShadowDrawable;
  public static Drawable chat_msgInViewsDrawable;
  public static Drawable chat_msgInViewsSelectedDrawable;
  public static Drawable chat_msgMediaBroadcastDrawable;
  public static Drawable chat_msgMediaCheckDrawable;
  public static Drawable chat_msgMediaClockDrawable;
  public static Drawable chat_msgMediaHalfCheckDrawable;
  public static Drawable chat_msgMediaMenuDrawable;
  public static Drawable chat_msgMediaViewsDrawable;
  public static Drawable chat_msgOutBroadcastDrawable;
  public static Drawable chat_msgOutCallDrawable;
  public static Drawable chat_msgOutCallSelectedDrawable;
  public static Drawable chat_msgOutCheckDrawable;
  public static Drawable chat_msgOutCheckSelectedDrawable;
  public static Drawable chat_msgOutClockDrawable;
  public static Drawable chat_msgOutDrawable;
  public static Drawable chat_msgOutHalfCheckDrawable;
  public static Drawable chat_msgOutHalfCheckSelectedDrawable;
  public static Drawable chat_msgOutInstantDrawable;
  public static Drawable chat_msgOutLocationDrawable;
  public static Drawable chat_msgOutMediaDrawable;
  public static Drawable chat_msgOutMediaSelectedDrawable;
  public static Drawable chat_msgOutMediaShadowDrawable;
  public static Drawable chat_msgOutMenuDrawable;
  public static Drawable chat_msgOutMenuSelectedDrawable;
  public static Drawable chat_msgOutSelectedClockDrawable;
  public static Drawable chat_msgOutSelectedDrawable;
  public static Drawable chat_msgOutShadowDrawable;
  public static Drawable chat_msgOutViewsDrawable;
  public static Drawable chat_msgOutViewsSelectedDrawable;
  public static Drawable chat_msgStickerCheckDrawable;
  public static Drawable chat_msgStickerClockDrawable;
  public static Drawable chat_msgStickerHalfCheckDrawable;
  public static Drawable chat_msgStickerViewsDrawable;
  public static TextPaint chat_msgTextPaint;
  public static TextPaint chat_msgTextPaintOneEmoji;
  public static TextPaint chat_msgTextPaintThreeEmoji;
  public static TextPaint chat_msgTextPaintTwoEmoji;
  public static Drawable chat_muteIconDrawable;
  public static TextPaint chat_namePaint;
  public static Drawable[][] chat_photoStatesDrawables;
  public static Paint chat_radialProgress2Paint;
  public static Paint chat_radialProgressPaint;
  public static Drawable chat_replyIconDrawable;
  public static Paint chat_replyLinePaint;
  public static TextPaint chat_replyNamePaint;
  public static TextPaint chat_replyTextPaint;
  public static Drawable chat_roundVideoShadow;
  public static Drawable chat_shareDrawable;
  public static Drawable chat_shareIconDrawable;
  public static TextPaint chat_shipmentPaint;
  public static Paint chat_statusPaint;
  public static Paint chat_statusRecordPaint;
  public static Drawable chat_systemDrawable;
  public static Paint chat_textSearchSelectionPaint;
  public static Paint chat_timeBackgroundPaint;
  public static TextPaint chat_timePaint;
  public static Paint chat_urlPaint;
  public static Paint checkboxSquare_backgroundPaint;
  public static Paint checkboxSquare_checkPaint;
  public static Paint checkboxSquare_eraserPaint;
  public static PorterDuffColorFilter colorFilter;
  public static PorterDuffColorFilter colorPressedFilter;
  private static int currentColor = 0;
  private static HashMap<String, Integer> currentColors;
  private static ThemeInfo currentDayTheme;
  private static ThemeInfo currentNightTheme;
  private static int currentSelectedColor = 0;
  private static ThemeInfo currentTheme;
  private static HashMap<String, Integer> defaultColors;
  private static ThemeInfo defaultTheme;
  public static Drawable dialogs_botDrawable;
  public static Drawable dialogs_broadcastDrawable;
  public static Drawable dialogs_checkDrawable;
  public static Drawable dialogs_clockDrawable;
  public static Paint dialogs_countGrayPaint;
  public static Paint dialogs_countPaint;
  public static TextPaint dialogs_countTextPaint;
  public static Drawable dialogs_errorDrawable;
  public static Paint dialogs_errorPaint;
  public static Drawable dialogs_groupDrawable;
  public static Drawable dialogs_halfCheckDrawable;
  private static Drawable dialogs_holidayDrawable;
  private static int dialogs_holidayDrawableOffsetX = 0;
  private static int dialogs_holidayDrawableOffsetY = 0;
  public static Drawable dialogs_lockDrawable;
  public static Drawable dialogs_mentionDrawable;
  public static TextPaint dialogs_messagePaint;
  public static TextPaint dialogs_messagePrintingPaint;
  public static Drawable dialogs_muteDrawable;
  public static TextPaint dialogs_nameEncryptedPaint;
  public static TextPaint dialogs_namePaint;
  public static TextPaint dialogs_offlinePaint;
  public static TextPaint dialogs_onlinePaint;
  public static Drawable dialogs_pinnedDrawable;
  public static Paint dialogs_pinnedPaint;
  public static Paint dialogs_tabletSeletedPaint;
  public static TextPaint dialogs_timePaint;
  public static Drawable dialogs_verifiedCheckDrawable;
  public static Drawable dialogs_verifiedDrawable;
  public static Paint dividerPaint;
  private static HashMap<String, String> fallbackKeys;
  private static boolean isCustomTheme = false;
  public static final String key_actionBarActionModeDefault = "actionBarActionModeDefault";
  public static final String key_actionBarActionModeDefaultIcon = "actionBarActionModeDefaultIcon";
  public static final String key_actionBarActionModeDefaultSelector = "actionBarActionModeDefaultSelector";
  public static final String key_actionBarActionModeDefaultTop = "actionBarActionModeDefaultTop";
  public static final String key_actionBarDefault = "actionBarDefault";
  public static final String key_actionBarDefaultIcon = "actionBarDefaultIcon";
  public static final String key_actionBarDefaultSearch = "actionBarDefaultSearch";
  public static final String key_actionBarDefaultSearchPlaceholder = "actionBarDefaultSearchPlaceholder";
  public static final String key_actionBarDefaultSelector = "actionBarDefaultSelector";
  public static final String key_actionBarDefaultSubmenuBackground = "actionBarDefaultSubmenuBackground";
  public static final String key_actionBarDefaultSubmenuItem = "actionBarDefaultSubmenuItem";
  public static final String key_actionBarDefaultSubtitle = "actionBarDefaultSubtitle";
  public static final String key_actionBarDefaultTitle = "actionBarDefaultTitle";
  public static final String key_actionBarWhiteSelector = "actionBarWhiteSelector";
  public static final String key_avatar_actionBarIconBlue = "avatar_actionBarIconBlue";
  public static final String key_avatar_actionBarIconCyan = "avatar_actionBarIconCyan";
  public static final String key_avatar_actionBarIconGreen = "avatar_actionBarIconGreen";
  public static final String key_avatar_actionBarIconOrange = "avatar_actionBarIconOrange";
  public static final String key_avatar_actionBarIconPink = "avatar_actionBarIconPink";
  public static final String key_avatar_actionBarIconRed = "avatar_actionBarIconRed";
  public static final String key_avatar_actionBarIconViolet = "avatar_actionBarIconViolet";
  public static final String key_avatar_actionBarSelectorBlue = "avatar_actionBarSelectorBlue";
  public static final String key_avatar_actionBarSelectorCyan = "avatar_actionBarSelectorCyan";
  public static final String key_avatar_actionBarSelectorGreen = "avatar_actionBarSelectorGreen";
  public static final String key_avatar_actionBarSelectorOrange = "avatar_actionBarSelectorOrange";
  public static final String key_avatar_actionBarSelectorPink = "avatar_actionBarSelectorPink";
  public static final String key_avatar_actionBarSelectorRed = "avatar_actionBarSelectorRed";
  public static final String key_avatar_actionBarSelectorViolet = "avatar_actionBarSelectorViolet";
  public static final String key_avatar_backgroundActionBarBlue = "avatar_backgroundActionBarBlue";
  public static final String key_avatar_backgroundActionBarCyan = "avatar_backgroundActionBarCyan";
  public static final String key_avatar_backgroundActionBarGreen = "avatar_backgroundActionBarGreen";
  public static final String key_avatar_backgroundActionBarOrange = "avatar_backgroundActionBarOrange";
  public static final String key_avatar_backgroundActionBarPink = "avatar_backgroundActionBarPink";
  public static final String key_avatar_backgroundActionBarRed = "avatar_backgroundActionBarRed";
  public static final String key_avatar_backgroundActionBarViolet = "avatar_backgroundActionBarViolet";
  public static final String key_avatar_backgroundBlue = "avatar_backgroundBlue";
  public static final String key_avatar_backgroundCyan = "avatar_backgroundCyan";
  public static final String key_avatar_backgroundGreen = "avatar_backgroundGreen";
  public static final String key_avatar_backgroundGroupCreateSpanBlue = "avatar_backgroundGroupCreateSpanBlue";
  public static final String key_avatar_backgroundInProfileBlue = "avatar_backgroundInProfileBlue";
  public static final String key_avatar_backgroundInProfileCyan = "avatar_backgroundInProfileCyan";
  public static final String key_avatar_backgroundInProfileGreen = "avatar_backgroundInProfileGreen";
  public static final String key_avatar_backgroundInProfileOrange = "avatar_backgroundInProfileOrange";
  public static final String key_avatar_backgroundInProfilePink = "avatar_backgroundInProfilePink";
  public static final String key_avatar_backgroundInProfileRed = "avatar_backgroundInProfileRed";
  public static final String key_avatar_backgroundInProfileViolet = "avatar_backgroundInProfileViolet";
  public static final String key_avatar_backgroundOrange = "avatar_backgroundOrange";
  public static final String key_avatar_backgroundPink = "avatar_backgroundPink";
  public static final String key_avatar_backgroundRed = "avatar_backgroundRed";
  public static final String key_avatar_backgroundSaved = "avatar_backgroundSaved";
  public static final String key_avatar_backgroundViolet = "avatar_backgroundViolet";
  public static final String key_avatar_nameInMessageBlue = "avatar_nameInMessageBlue";
  public static final String key_avatar_nameInMessageCyan = "avatar_nameInMessageCyan";
  public static final String key_avatar_nameInMessageGreen = "avatar_nameInMessageGreen";
  public static final String key_avatar_nameInMessageOrange = "avatar_nameInMessageOrange";
  public static final String key_avatar_nameInMessagePink = "avatar_nameInMessagePink";
  public static final String key_avatar_nameInMessageRed = "avatar_nameInMessageRed";
  public static final String key_avatar_nameInMessageViolet = "avatar_nameInMessageViolet";
  public static final String key_avatar_subtitleInProfileBlue = "avatar_subtitleInProfileBlue";
  public static final String key_avatar_subtitleInProfileCyan = "avatar_subtitleInProfileCyan";
  public static final String key_avatar_subtitleInProfileGreen = "avatar_subtitleInProfileGreen";
  public static final String key_avatar_subtitleInProfileOrange = "avatar_subtitleInProfileOrange";
  public static final String key_avatar_subtitleInProfilePink = "avatar_subtitleInProfilePink";
  public static final String key_avatar_subtitleInProfileRed = "avatar_subtitleInProfileRed";
  public static final String key_avatar_subtitleInProfileViolet = "avatar_subtitleInProfileViolet";
  public static final String key_avatar_text = "avatar_text";
  public static final String key_calls_callReceivedGreenIcon = "calls_callReceivedGreenIcon";
  public static final String key_calls_callReceivedRedIcon = "calls_callReceivedRedIcon";
  public static final String key_calls_ratingStar = "calls_ratingStar";
  public static final String key_calls_ratingStarSelected = "calls_ratingStarSelected";
  public static final String key_changephoneinfo_image = "changephoneinfo_image";
  public static final String key_chat_addContact = "chat_addContact";
  public static final String key_chat_adminSelectedText = "chat_adminSelectedText";
  public static final String key_chat_adminText = "chat_adminText";
  public static final String key_chat_botButtonText = "chat_botButtonText";
  public static final String key_chat_botKeyboardButtonBackground = "chat_botKeyboardButtonBackground";
  public static final String key_chat_botKeyboardButtonBackgroundPressed = "chat_botKeyboardButtonBackgroundPressed";
  public static final String key_chat_botKeyboardButtonText = "chat_botKeyboardButtonText";
  public static final String key_chat_botProgress = "chat_botProgress";
  public static final String key_chat_botSwitchToInlineText = "chat_botSwitchToInlineText";
  public static final String key_chat_editDoneIcon = "chat_editDoneIcon";
  public static final String key_chat_emojiPanelBackground = "chat_emojiPanelBackground";
  public static final String key_chat_emojiPanelBackspace = "chat_emojiPanelBackspace";
  public static final String key_chat_emojiPanelEmptyText = "chat_emojiPanelEmptyText";
  public static final String key_chat_emojiPanelIcon = "chat_emojiPanelIcon";
  public static final String key_chat_emojiPanelIconSelected = "chat_emojiPanelIconSelected";
  public static final String key_chat_emojiPanelIconSelector = "chat_emojiPanelIconSelector";
  public static final String key_chat_emojiPanelMasksIcon = "chat_emojiPanelMasksIcon";
  public static final String key_chat_emojiPanelMasksIconSelected = "chat_emojiPanelMasksIconSelected";
  public static final String key_chat_emojiPanelNewTrending = "chat_emojiPanelNewTrending";
  public static final String key_chat_emojiPanelShadowLine = "chat_emojiPanelShadowLine";
  public static final String key_chat_emojiPanelStickerPackSelector = "chat_emojiPanelStickerPackSelector";
  public static final String key_chat_emojiPanelStickerSetName = "chat_emojiPanelStickerSetName";
  public static final String key_chat_emojiPanelStickerSetNameIcon = "chat_emojiPanelStickerSetNameIcon";
  public static final String key_chat_emojiPanelTrendingDescription = "chat_emojiPanelTrendingDescription";
  public static final String key_chat_emojiPanelTrendingTitle = "chat_emojiPanelTrendingTitle";
  public static final String key_chat_emojiSearchBackground = "chat_emojiSearchBackground";
  public static final String key_chat_fieldOverlayText = "chat_fieldOverlayText";
  public static final String key_chat_gifSaveHintBackground = "chat_gifSaveHintBackground";
  public static final String key_chat_gifSaveHintText = "chat_gifSaveHintText";
  public static final String key_chat_goDownButton = "chat_goDownButton";
  public static final String key_chat_goDownButtonCounter = "chat_goDownButtonCounter";
  public static final String key_chat_goDownButtonCounterBackground = "chat_goDownButtonCounterBackground";
  public static final String key_chat_goDownButtonIcon = "chat_goDownButtonIcon";
  public static final String key_chat_goDownButtonShadow = "chat_goDownButtonShadow";
  public static final String key_chat_inAudioCacheSeekbar = "chat_inAudioCacheSeekbar";
  public static final String key_chat_inAudioDurationSelectedText = "chat_inAudioDurationSelectedText";
  public static final String key_chat_inAudioDurationText = "chat_inAudioDurationText";
  public static final String key_chat_inAudioPerfomerText = "chat_inAudioPerfomerText";
  public static final String key_chat_inAudioProgress = "chat_inAudioProgress";
  public static final String key_chat_inAudioSeekbar = "chat_inAudioSeekbar";
  public static final String key_chat_inAudioSeekbarFill = "chat_inAudioSeekbarFill";
  public static final String key_chat_inAudioSeekbarSelected = "chat_inAudioSeekbarSelected";
  public static final String key_chat_inAudioSelectedProgress = "chat_inAudioSelectedProgress";
  public static final String key_chat_inAudioTitleText = "chat_inAudioTitleText";
  public static final String key_chat_inBubble = "chat_inBubble";
  public static final String key_chat_inBubbleSelected = "chat_inBubbleSelected";
  public static final String key_chat_inBubbleShadow = "chat_inBubbleShadow";
  public static final String key_chat_inContactBackground = "chat_inContactBackground";
  public static final String key_chat_inContactIcon = "chat_inContactIcon";
  public static final String key_chat_inContactNameText = "chat_inContactNameText";
  public static final String key_chat_inContactPhoneText = "chat_inContactPhoneText";
  public static final String key_chat_inFileBackground = "chat_inFileBackground";
  public static final String key_chat_inFileBackgroundSelected = "chat_inFileBackgroundSelected";
  public static final String key_chat_inFileIcon = "chat_inFileIcon";
  public static final String key_chat_inFileInfoSelectedText = "chat_inFileInfoSelectedText";
  public static final String key_chat_inFileInfoText = "chat_inFileInfoText";
  public static final String key_chat_inFileNameText = "chat_inFileNameText";
  public static final String key_chat_inFileProgress = "chat_inFileProgress";
  public static final String key_chat_inFileProgressSelected = "chat_inFileProgressSelected";
  public static final String key_chat_inFileSelectedIcon = "chat_inFileSelectedIcon";
  public static final String key_chat_inForwardedNameText = "chat_inForwardedNameText";
  public static final String key_chat_inInstant = "chat_inInstant";
  public static final String key_chat_inInstantSelected = "chat_inInstantSelected";
  public static final String key_chat_inLoader = "chat_inLoader";
  public static final String key_chat_inLoaderPhoto = "chat_inLoaderPhoto";
  public static final String key_chat_inLoaderPhotoIcon = "chat_inLoaderPhotoIcon";
  public static final String key_chat_inLoaderPhotoIconSelected = "chat_inLoaderPhotoIconSelected";
  public static final String key_chat_inLoaderPhotoSelected = "chat_inLoaderPhotoSelected";
  public static final String key_chat_inLoaderSelected = "chat_inLoaderSelected";
  public static final String key_chat_inLocationBackground = "chat_inLocationBackground";
  public static final String key_chat_inLocationIcon = "chat_inLocationIcon";
  public static final String key_chat_inMenu = "chat_inMenu";
  public static final String key_chat_inMenuSelected = "chat_inMenuSelected";
  public static final String key_chat_inPreviewInstantSelectedText = "chat_inPreviewInstantSelectedText";
  public static final String key_chat_inPreviewInstantText = "chat_inPreviewInstantText";
  public static final String key_chat_inPreviewLine = "chat_inPreviewLine";
  public static final String key_chat_inReplyLine = "chat_inReplyLine";
  public static final String key_chat_inReplyMediaMessageSelectedText = "chat_inReplyMediaMessageSelectedText";
  public static final String key_chat_inReplyMediaMessageText = "chat_inReplyMediaMessageText";
  public static final String key_chat_inReplyMessageText = "chat_inReplyMessageText";
  public static final String key_chat_inReplyNameText = "chat_inReplyNameText";
  public static final String key_chat_inSentClock = "chat_inSentClock";
  public static final String key_chat_inSentClockSelected = "chat_inSentClockSelected";
  public static final String key_chat_inSiteNameText = "chat_inSiteNameText";
  public static final String key_chat_inTimeSelectedText = "chat_inTimeSelectedText";
  public static final String key_chat_inTimeText = "chat_inTimeText";
  public static final String key_chat_inVenueInfoSelectedText = "chat_inVenueInfoSelectedText";
  public static final String key_chat_inVenueInfoText = "chat_inVenueInfoText";
  public static final String key_chat_inVenueNameText = "chat_inVenueNameText";
  public static final String key_chat_inViaBotNameText = "chat_inViaBotNameText";
  public static final String key_chat_inViews = "chat_inViews";
  public static final String key_chat_inViewsSelected = "chat_inViewsSelected";
  public static final String key_chat_inVoiceSeekbar = "chat_inVoiceSeekbar";
  public static final String key_chat_inVoiceSeekbarFill = "chat_inVoiceSeekbarFill";
  public static final String key_chat_inVoiceSeekbarSelected = "chat_inVoiceSeekbarSelected";
  public static final String key_chat_inlineResultIcon = "chat_inlineResultIcon";
  public static final String key_chat_linkSelectBackground = "chat_linkSelectBackground";
  public static final String key_chat_lockIcon = "chat_lockIcon";
  public static final String key_chat_mediaBroadcast = "chat_mediaBroadcast";
  public static final String key_chat_mediaInfoText = "chat_mediaInfoText";
  public static final String key_chat_mediaLoaderPhoto = "chat_mediaLoaderPhoto";
  public static final String key_chat_mediaLoaderPhotoIcon = "chat_mediaLoaderPhotoIcon";
  public static final String key_chat_mediaLoaderPhotoIconSelected = "chat_mediaLoaderPhotoIconSelected";
  public static final String key_chat_mediaLoaderPhotoSelected = "chat_mediaLoaderPhotoSelected";
  public static final String key_chat_mediaMenu = "chat_mediaMenu";
  public static final String key_chat_mediaProgress = "chat_mediaProgress";
  public static final String key_chat_mediaSentCheck = "chat_mediaSentCheck";
  public static final String key_chat_mediaSentClock = "chat_mediaSentClock";
  public static final String key_chat_mediaTimeBackground = "chat_mediaTimeBackground";
  public static final String key_chat_mediaTimeText = "chat_mediaTimeText";
  public static final String key_chat_mediaViews = "chat_mediaViews";
  public static final String key_chat_messageLinkIn = "chat_messageLinkIn";
  public static final String key_chat_messageLinkOut = "chat_messageLinkOut";
  public static final String key_chat_messagePanelBackground = "chat_messagePanelBackground";
  public static final String key_chat_messagePanelCancelInlineBot = "chat_messagePanelCancelInlineBot";
  public static final String key_chat_messagePanelHint = "chat_messagePanelHint";
  public static final String key_chat_messagePanelIcons = "chat_messagePanelIcons";
  public static final String key_chat_messagePanelSend = "chat_messagePanelSend";
  public static final String key_chat_messagePanelShadow = "chat_messagePanelShadow";
  public static final String key_chat_messagePanelText = "chat_messagePanelText";
  public static final String key_chat_messagePanelVoiceBackground = "chat_messagePanelVoiceBackground";
  public static final String key_chat_messagePanelVoiceDelete = "chat_messagePanelVoiceDelete";
  public static final String key_chat_messagePanelVoiceDuration = "chat_messagePanelVoiceDuration";
  public static final String key_chat_messagePanelVoiceLock = "key_chat_messagePanelVoiceLock";
  public static final String key_chat_messagePanelVoiceLockBackground = "key_chat_messagePanelVoiceLockBackground";
  public static final String key_chat_messagePanelVoiceLockShadow = "key_chat_messagePanelVoiceLockShadow";
  public static final String key_chat_messagePanelVoicePressed = "chat_messagePanelVoicePressed";
  public static final String key_chat_messagePanelVoiceShadow = "chat_messagePanelVoiceShadow";
  public static final String key_chat_messageTextIn = "chat_messageTextIn";
  public static final String key_chat_messageTextOut = "chat_messageTextOut";
  public static final String key_chat_muteIcon = "chat_muteIcon";
  public static final String key_chat_outAudioCacheSeekbar = "chat_outAudioCacheSeekbar";
  public static final String key_chat_outAudioDurationSelectedText = "chat_outAudioDurationSelectedText";
  public static final String key_chat_outAudioDurationText = "chat_outAudioDurationText";
  public static final String key_chat_outAudioPerfomerText = "chat_outAudioPerfomerText";
  public static final String key_chat_outAudioProgress = "chat_outAudioProgress";
  public static final String key_chat_outAudioSeekbar = "chat_outAudioSeekbar";
  public static final String key_chat_outAudioSeekbarFill = "chat_outAudioSeekbarFill";
  public static final String key_chat_outAudioSeekbarSelected = "chat_outAudioSeekbarSelected";
  public static final String key_chat_outAudioSelectedProgress = "chat_outAudioSelectedProgress";
  public static final String key_chat_outAudioTitleText = "chat_outAudioTitleText";
  public static final String key_chat_outBroadcast = "chat_outBroadcast";
  public static final String key_chat_outBubble = "chat_outBubble";
  public static final String key_chat_outBubbleSelected = "chat_outBubbleSelected";
  public static final String key_chat_outBubbleShadow = "chat_outBubbleShadow";
  public static final String key_chat_outContactBackground = "chat_outContactBackground";
  public static final String key_chat_outContactIcon = "chat_outContactIcon";
  public static final String key_chat_outContactNameText = "chat_outContactNameText";
  public static final String key_chat_outContactPhoneText = "chat_outContactPhoneText";
  public static final String key_chat_outFileBackground = "chat_outFileBackground";
  public static final String key_chat_outFileBackgroundSelected = "chat_outFileBackgroundSelected";
  public static final String key_chat_outFileIcon = "chat_outFileIcon";
  public static final String key_chat_outFileInfoSelectedText = "chat_outFileInfoSelectedText";
  public static final String key_chat_outFileInfoText = "chat_outFileInfoText";
  public static final String key_chat_outFileNameText = "chat_outFileNameText";
  public static final String key_chat_outFileProgress = "chat_outFileProgress";
  public static final String key_chat_outFileProgressSelected = "chat_outFileProgressSelected";
  public static final String key_chat_outFileSelectedIcon = "chat_outFileSelectedIcon";
  public static final String key_chat_outForwardedNameText = "chat_outForwardedNameText";
  public static final String key_chat_outInstant = "chat_outInstant";
  public static final String key_chat_outInstantSelected = "chat_outInstantSelected";
  public static final String key_chat_outLoader = "chat_outLoader";
  public static final String key_chat_outLoaderPhoto = "chat_outLoaderPhoto";
  public static final String key_chat_outLoaderPhotoIcon = "chat_outLoaderPhotoIcon";
  public static final String key_chat_outLoaderPhotoIconSelected = "chat_outLoaderPhotoIconSelected";
  public static final String key_chat_outLoaderPhotoSelected = "chat_outLoaderPhotoSelected";
  public static final String key_chat_outLoaderSelected = "chat_outLoaderSelected";
  public static final String key_chat_outLocationBackground = "chat_outLocationBackground";
  public static final String key_chat_outLocationIcon = "chat_outLocationIcon";
  public static final String key_chat_outMenu = "chat_outMenu";
  public static final String key_chat_outMenuSelected = "chat_outMenuSelected";
  public static final String key_chat_outPreviewInstantSelectedText = "chat_outPreviewInstantSelectedText";
  public static final String key_chat_outPreviewInstantText = "chat_outPreviewInstantText";
  public static final String key_chat_outPreviewLine = "chat_outPreviewLine";
  public static final String key_chat_outReplyLine = "chat_outReplyLine";
  public static final String key_chat_outReplyMediaMessageSelectedText = "chat_outReplyMediaMessageSelectedText";
  public static final String key_chat_outReplyMediaMessageText = "chat_outReplyMediaMessageText";
  public static final String key_chat_outReplyMessageText = "chat_outReplyMessageText";
  public static final String key_chat_outReplyNameText = "chat_outReplyNameText";
  public static final String key_chat_outSentCheck = "chat_outSentCheck";
  public static final String key_chat_outSentCheckSelected = "chat_outSentCheckSelected";
  public static final String key_chat_outSentClock = "chat_outSentClock";
  public static final String key_chat_outSentClockSelected = "chat_outSentClockSelected";
  public static final String key_chat_outSiteNameText = "chat_outSiteNameText";
  public static final String key_chat_outTimeSelectedText = "chat_outTimeSelectedText";
  public static final String key_chat_outTimeText = "chat_outTimeText";
  public static final String key_chat_outVenueInfoSelectedText = "chat_outVenueInfoSelectedText";
  public static final String key_chat_outVenueInfoText = "chat_outVenueInfoText";
  public static final String key_chat_outVenueNameText = "chat_outVenueNameText";
  public static final String key_chat_outViaBotNameText = "chat_outViaBotNameText";
  public static final String key_chat_outViews = "chat_outViews";
  public static final String key_chat_outViewsSelected = "chat_outViewsSelected";
  public static final String key_chat_outVoiceSeekbar = "chat_outVoiceSeekbar";
  public static final String key_chat_outVoiceSeekbarFill = "chat_outVoiceSeekbarFill";
  public static final String key_chat_outVoiceSeekbarSelected = "chat_outVoiceSeekbarSelected";
  public static final String key_chat_previewDurationText = "chat_previewDurationText";
  public static final String key_chat_previewGameText = "chat_previewGameText";
  public static final String key_chat_recordTime = "chat_recordTime";
  public static final String key_chat_recordVoiceCancel = "chat_recordVoiceCancel";
  public static final String key_chat_recordedVoiceBackground = "chat_recordedVoiceBackground";
  public static final String key_chat_recordedVoiceDot = "chat_recordedVoiceDot";
  public static final String key_chat_recordedVoicePlayPause = "chat_recordedVoicePlayPause";
  public static final String key_chat_recordedVoicePlayPausePressed = "chat_recordedVoicePlayPausePressed";
  public static final String key_chat_recordedVoiceProgress = "chat_recordedVoiceProgress";
  public static final String key_chat_recordedVoiceProgressInner = "chat_recordedVoiceProgressInner";
  public static final String key_chat_replyPanelClose = "chat_replyPanelClose";
  public static final String key_chat_replyPanelIcons = "chat_replyPanelIcons";
  public static final String key_chat_replyPanelLine = "chat_replyPanelLine";
  public static final String key_chat_replyPanelMessage = "chat_replyPanelMessage";
  public static final String key_chat_replyPanelName = "chat_replyPanelName";
  public static final String key_chat_reportSpam = "chat_reportSpam";
  public static final String key_chat_searchPanelIcons = "chat_searchPanelIcons";
  public static final String key_chat_searchPanelText = "chat_searchPanelText";
  public static final String key_chat_secretChatStatusText = "chat_secretChatStatusText";
  public static final String key_chat_secretTimeText = "chat_secretTimeText";
  public static final String key_chat_secretTimerBackground = "chat_secretTimerBackground";
  public static final String key_chat_secretTimerText = "chat_secretTimerText";
  public static final String key_chat_selectedBackground = "chat_selectedBackground";
  public static final String key_chat_sentError = "chat_sentError";
  public static final String key_chat_sentErrorIcon = "chat_sentErrorIcon";
  public static final String key_chat_serviceBackground = "chat_serviceBackground";
  public static final String key_chat_serviceBackgroundSelected = "chat_serviceBackgroundSelected";
  public static final String key_chat_serviceIcon = "chat_serviceIcon";
  public static final String key_chat_serviceLink = "chat_serviceLink";
  public static final String key_chat_serviceText = "chat_serviceText";
  public static final String key_chat_stickerNameText = "chat_stickerNameText";
  public static final String key_chat_stickerReplyLine = "chat_stickerReplyLine";
  public static final String key_chat_stickerReplyMessageText = "chat_stickerReplyMessageText";
  public static final String key_chat_stickerReplyNameText = "chat_stickerReplyNameText";
  public static final String key_chat_stickerViaBotNameText = "chat_stickerViaBotNameText";
  public static final String key_chat_stickersHintPanel = "chat_stickersHintPanel";
  public static final String key_chat_textSelectBackground = "chat_textSelectBackground";
  public static final String key_chat_topPanelBackground = "chat_topPanelBackground";
  public static final String key_chat_topPanelClose = "chat_topPanelClose";
  public static final String key_chat_topPanelLine = "chat_topPanelLine";
  public static final String key_chat_topPanelMessage = "chat_topPanelMessage";
  public static final String key_chat_topPanelTitle = "chat_topPanelTitle";
  public static final String key_chat_unreadMessagesStartArrowIcon = "chat_unreadMessagesStartArrowIcon";
  public static final String key_chat_unreadMessagesStartBackground = "chat_unreadMessagesStartBackground";
  public static final String key_chat_unreadMessagesStartText = "chat_unreadMessagesStartText";
  public static final String key_chat_wallpaper = "chat_wallpaper";
  public static final String key_chats_actionBackground = "chats_actionBackground";
  public static final String key_chats_actionIcon = "chats_actionIcon";
  public static final String key_chats_actionMessage = "chats_actionMessage";
  public static final String key_chats_actionPressedBackground = "chats_actionPressedBackground";
  public static final String key_chats_attachMessage = "chats_attachMessage";
  public static final String key_chats_date = "chats_date";
  public static final String key_chats_draft = "chats_draft";
  public static final String key_chats_menuBackground = "chats_menuBackground";
  public static final String key_chats_menuCloud = "chats_menuCloud";
  public static final String key_chats_menuCloudBackgroundCats = "chats_menuCloudBackgroundCats";
  public static final String key_chats_menuItemCheck = "chats_menuItemCheck";
  public static final String key_chats_menuItemIcon = "chats_menuItemIcon";
  public static final String key_chats_menuItemText = "chats_menuItemText";
  public static final String key_chats_menuName = "chats_menuName";
  public static final String key_chats_menuPhone = "chats_menuPhone";
  public static final String key_chats_menuPhoneCats = "chats_menuPhoneCats";
  public static final String key_chats_menuTopShadow = "chats_menuTopShadow";
  public static final String key_chats_message = "chats_message";
  public static final String key_chats_muteIcon = "chats_muteIcon";
  public static final String key_chats_name = "chats_name";
  public static final String key_chats_nameIcon = "chats_nameIcon";
  public static final String key_chats_nameMessage = "chats_nameMessage";
  public static final String key_chats_pinnedIcon = "chats_pinnedIcon";
  public static final String key_chats_pinnedOverlay = "chats_pinnedOverlay";
  public static final String key_chats_secretIcon = "chats_secretIcon";
  public static final String key_chats_secretName = "chats_secretName";
  public static final String key_chats_sentCheck = "chats_sentCheck";
  public static final String key_chats_sentClock = "chats_sentClock";
  public static final String key_chats_sentError = "chats_sentError";
  public static final String key_chats_sentErrorIcon = "chats_sentErrorIcon";
  public static final String key_chats_tabletSelectedOverlay = "chats_tabletSelectedOverlay";
  public static final String key_chats_unreadCounter = "chats_unreadCounter";
  public static final String key_chats_unreadCounterMuted = "chats_unreadCounterMuted";
  public static final String key_chats_unreadCounterText = "chats_unreadCounterText";
  public static final String key_chats_verifiedBackground = "chats_verifiedBackground";
  public static final String key_chats_verifiedCheck = "chats_verifiedCheck";
  public static final String key_checkbox = "checkbox";
  public static final String key_checkboxCheck = "checkboxCheck";
  public static final String key_checkboxSquareBackground = "checkboxSquareBackground";
  public static final String key_checkboxSquareCheck = "checkboxSquareCheck";
  public static final String key_checkboxSquareDisabled = "checkboxSquareDisabled";
  public static final String key_checkboxSquareUnchecked = "checkboxSquareUnchecked";
  public static final String key_contacts_inviteBackground = "contacts_inviteBackground";
  public static final String key_contacts_inviteText = "contacts_inviteText";
  public static final String key_contextProgressInner1 = "contextProgressInner1";
  public static final String key_contextProgressInner2 = "contextProgressInner2";
  public static final String key_contextProgressInner3 = "contextProgressInner3";
  public static final String key_contextProgressOuter1 = "contextProgressOuter1";
  public static final String key_contextProgressOuter2 = "contextProgressOuter2";
  public static final String key_contextProgressOuter3 = "contextProgressOuter3";
  public static final String key_dialogBackground = "dialogBackground";
  public static final String key_dialogBackgroundGray = "dialogBackgroundGray";
  public static final String key_dialogBadgeBackground = "dialogBadgeBackground";
  public static final String key_dialogBadgeText = "dialogBadgeText";
  public static final String key_dialogButton = "dialogButton";
  public static final String key_dialogButtonSelector = "dialogButtonSelector";
  public static final String key_dialogCheckboxSquareBackground = "dialogCheckboxSquareBackground";
  public static final String key_dialogCheckboxSquareCheck = "dialogCheckboxSquareCheck";
  public static final String key_dialogCheckboxSquareDisabled = "dialogCheckboxSquareDisabled";
  public static final String key_dialogCheckboxSquareUnchecked = "dialogCheckboxSquareUnchecked";
  public static final String key_dialogGrayLine = "dialogGrayLine";
  public static final String key_dialogIcon = "dialogIcon";
  public static final String key_dialogInputField = "dialogInputField";
  public static final String key_dialogInputFieldActivated = "dialogInputFieldActivated";
  public static final String key_dialogLineProgress = "dialogLineProgress";
  public static final String key_dialogLineProgressBackground = "dialogLineProgressBackground";
  public static final String key_dialogLinkSelection = "dialogLinkSelection";
  public static final String key_dialogProgressCircle = "dialogProgressCircle";
  public static final String key_dialogRadioBackground = "dialogRadioBackground";
  public static final String key_dialogRadioBackgroundChecked = "dialogRadioBackgroundChecked";
  public static final String key_dialogRoundCheckBox = "dialogRoundCheckBox";
  public static final String key_dialogRoundCheckBoxCheck = "dialogRoundCheckBoxCheck";
  public static final String key_dialogScrollGlow = "dialogScrollGlow";
  public static final String key_dialogTextBlack = "dialogTextBlack";
  public static final String key_dialogTextBlue = "dialogTextBlue";
  public static final String key_dialogTextBlue2 = "dialogTextBlue2";
  public static final String key_dialogTextBlue3 = "dialogTextBlue3";
  public static final String key_dialogTextBlue4 = "dialogTextBlue4";
  public static final String key_dialogTextGray = "dialogTextGray";
  public static final String key_dialogTextGray2 = "dialogTextGray2";
  public static final String key_dialogTextGray3 = "dialogTextGray3";
  public static final String key_dialogTextGray4 = "dialogTextGray4";
  public static final String key_dialogTextHint = "dialogTextHint";
  public static final String key_dialogTextLink = "dialogTextLink";
  public static final String key_dialogTextRed = "dialogTextRed";
  public static final String key_dialogTopBackground = "dialogTopBackground";
  public static final String key_dialog_liveLocationProgress = "location_liveLocationProgress";
  public static final String key_divider = "divider";
  public static final String key_emptyListPlaceholder = "emptyListPlaceholder";
  public static final String key_fastScrollActive = "fastScrollActive";
  public static final String key_fastScrollInactive = "fastScrollInactive";
  public static final String key_fastScrollText = "fastScrollText";
  public static final String key_featuredStickers_addButton = "featuredStickers_addButton";
  public static final String key_featuredStickers_addButtonPressed = "featuredStickers_addButtonPressed";
  public static final String key_featuredStickers_addedIcon = "featuredStickers_addedIcon";
  public static final String key_featuredStickers_buttonProgress = "featuredStickers_buttonProgress";
  public static final String key_featuredStickers_buttonText = "featuredStickers_buttonText";
  public static final String key_featuredStickers_delButton = "featuredStickers_delButton";
  public static final String key_featuredStickers_delButtonPressed = "featuredStickers_delButtonPressed";
  public static final String key_featuredStickers_unread = "featuredStickers_unread";
  public static final String key_files_folderIcon = "files_folderIcon";
  public static final String key_files_folderIconBackground = "files_folderIconBackground";
  public static final String key_files_iconText = "files_iconText";
  public static final String key_graySection = "graySection";
  public static final String key_groupcreate_checkbox = "groupcreate_checkbox";
  public static final String key_groupcreate_checkboxCheck = "groupcreate_checkboxCheck";
  public static final String key_groupcreate_cursor = "groupcreate_cursor";
  public static final String key_groupcreate_hintText = "groupcreate_hintText";
  public static final String key_groupcreate_offlineText = "groupcreate_offlineText";
  public static final String key_groupcreate_onlineText = "groupcreate_onlineText";
  public static final String key_groupcreate_sectionShadow = "groupcreate_sectionShadow";
  public static final String key_groupcreate_sectionText = "groupcreate_sectionText";
  public static final String key_groupcreate_spanBackground = "groupcreate_spanBackground";
  public static final String key_groupcreate_spanText = "groupcreate_spanText";
  public static final String key_inappPlayerBackground = "inappPlayerBackground";
  public static final String key_inappPlayerClose = "inappPlayerClose";
  public static final String key_inappPlayerPerformer = "inappPlayerPerformer";
  public static final String key_inappPlayerPlayPause = "inappPlayerPlayPause";
  public static final String key_inappPlayerTitle = "inappPlayerTitle";
  public static final String key_listSelector = "listSelectorSDK21";
  public static final String key_location_liveLocationProgress = "location_liveLocationProgress";
  public static final String key_location_markerX = "location_markerX";
  public static final String key_location_placeLocationBackground = "location_placeLocationBackground";
  public static final String key_location_sendLiveLocationBackground = "location_sendLiveLocationBackground";
  public static final String key_location_sendLocationBackground = "location_sendLocationBackground";
  public static final String key_location_sendLocationIcon = "location_sendLocationIcon";
  public static final String key_login_progressInner = "login_progressInner";
  public static final String key_login_progressOuter = "login_progressOuter";
  public static final String key_musicPicker_buttonBackground = "musicPicker_buttonBackground";
  public static final String key_musicPicker_buttonIcon = "musicPicker_buttonIcon";
  public static final String key_musicPicker_checkbox = "musicPicker_checkbox";
  public static final String key_musicPicker_checkboxCheck = "musicPicker_checkboxCheck";
  public static final String key_picker_badge = "picker_badge";
  public static final String key_picker_badgeText = "picker_badgeText";
  public static final String key_picker_disabledButton = "picker_disabledButton";
  public static final String key_picker_enabledButton = "picker_enabledButton";
  public static final String key_player_actionBar = "player_actionBar";
  public static final String key_player_actionBarItems = "player_actionBarItems";
  public static final String key_player_actionBarSelector = "player_actionBarSelector";
  public static final String key_player_actionBarSubtitle = "player_actionBarSubtitle";
  public static final String key_player_actionBarTitle = "player_actionBarTitle";
  public static final String key_player_actionBarTop = "player_actionBarTop";
  public static final String key_player_background = "player_background";
  public static final String key_player_button = "player_button";
  public static final String key_player_buttonActive = "player_buttonActive";
  public static final String key_player_placeholder = "player_placeholder";
  public static final String key_player_placeholderBackground = "player_placeholderBackground";
  public static final String key_player_progress = "player_progress";
  public static final String key_player_progressBackground = "player_progressBackground";
  public static final String key_player_progressCachedBackground = "key_player_progressCachedBackground";
  public static final String key_player_time = "player_time";
  public static final String key_profile_actionBackground = "profile_actionBackground";
  public static final String key_profile_actionIcon = "profile_actionIcon";
  public static final String key_profile_actionPressedBackground = "profile_actionPressedBackground";
  public static final String key_profile_adminIcon = "profile_adminIcon";
  public static final String key_profile_creatorIcon = "profile_creatorIcon";
  public static final String key_profile_title = "profile_title";
  public static final String key_profile_verifiedBackground = "profile_verifiedBackground";
  public static final String key_profile_verifiedCheck = "profile_verifiedCheck";
  public static final String key_progressCircle = "progressCircle";
  public static final String key_radioBackground = "radioBackground";
  public static final String key_radioBackgroundChecked = "radioBackgroundChecked";
  public static final String key_returnToCallBackground = "returnToCallBackground";
  public static final String key_returnToCallText = "returnToCallText";
  public static final String key_sessions_devicesImage = "sessions_devicesImage";
  public static final String key_sharedMedia_linkPlaceholder = "sharedMedia_linkPlaceholder";
  public static final String key_sharedMedia_linkPlaceholderText = "sharedMedia_linkPlaceholderText";
  public static final String key_sharedMedia_startStopLoadIcon = "sharedMedia_startStopLoadIcon";
  public static final String key_stickers_menu = "stickers_menu";
  public static final String key_stickers_menuSelector = "stickers_menuSelector";
  public static final String key_switchThumb = "switchThumb";
  public static final String key_switchThumbChecked = "switchThumbChecked";
  public static final String key_switchTrack = "switchTrack";
  public static final String key_switchTrackChecked = "switchTrackChecked";
  public static final String key_windowBackgroundGray = "windowBackgroundGray";
  public static final String key_windowBackgroundGrayShadow = "windowBackgroundGrayShadow";
  public static final String key_windowBackgroundWhite = "windowBackgroundWhite";
  public static final String key_windowBackgroundWhiteBlackText = "windowBackgroundWhiteBlackText";
  public static final String key_windowBackgroundWhiteBlueHeader = "windowBackgroundWhiteBlueHeader";
  public static final String key_windowBackgroundWhiteBlueText = "windowBackgroundWhiteBlueText";
  public static final String key_windowBackgroundWhiteBlueText2 = "windowBackgroundWhiteBlueText2";
  public static final String key_windowBackgroundWhiteBlueText3 = "windowBackgroundWhiteBlueText3";
  public static final String key_windowBackgroundWhiteBlueText4 = "windowBackgroundWhiteBlueText4";
  public static final String key_windowBackgroundWhiteBlueText5 = "windowBackgroundWhiteBlueText5";
  public static final String key_windowBackgroundWhiteBlueText6 = "windowBackgroundWhiteBlueText6";
  public static final String key_windowBackgroundWhiteBlueText7 = "windowBackgroundWhiteBlueText7";
  public static final String key_windowBackgroundWhiteGrayIcon = "windowBackgroundWhiteGrayIcon";
  public static final String key_windowBackgroundWhiteGrayLine = "windowBackgroundWhiteGrayLine";
  public static final String key_windowBackgroundWhiteGrayText = "windowBackgroundWhiteGrayText";
  public static final String key_windowBackgroundWhiteGrayText2 = "windowBackgroundWhiteGrayText2";
  public static final String key_windowBackgroundWhiteGrayText3 = "windowBackgroundWhiteGrayText3";
  public static final String key_windowBackgroundWhiteGrayText4 = "windowBackgroundWhiteGrayText4";
  public static final String key_windowBackgroundWhiteGrayText5 = "windowBackgroundWhiteGrayText5";
  public static final String key_windowBackgroundWhiteGrayText6 = "windowBackgroundWhiteGrayText6";
  public static final String key_windowBackgroundWhiteGrayText7 = "windowBackgroundWhiteGrayText7";
  public static final String key_windowBackgroundWhiteGrayText8 = "windowBackgroundWhiteGrayText8";
  public static final String key_windowBackgroundWhiteGreenText = "windowBackgroundWhiteGreenText";
  public static final String key_windowBackgroundWhiteGreenText2 = "windowBackgroundWhiteGreenText2";
  public static final String key_windowBackgroundWhiteHintText = "windowBackgroundWhiteHintText";
  public static final String key_windowBackgroundWhiteInputField = "windowBackgroundWhiteInputField";
  public static final String key_windowBackgroundWhiteInputFieldActivated = "windowBackgroundWhiteInputFieldActivated";
  public static final String key_windowBackgroundWhiteLinkSelection = "windowBackgroundWhiteLinkSelection";
  public static final String key_windowBackgroundWhiteLinkText = "windowBackgroundWhiteLinkText";
  public static final String key_windowBackgroundWhiteRedText = "windowBackgroundWhiteRedText";
  public static final String key_windowBackgroundWhiteRedText2 = "windowBackgroundWhiteRedText2";
  public static final String key_windowBackgroundWhiteRedText3 = "windowBackgroundWhiteRedText3";
  public static final String key_windowBackgroundWhiteRedText4 = "windowBackgroundWhiteRedText4";
  public static final String key_windowBackgroundWhiteRedText5 = "windowBackgroundWhiteRedText5";
  public static final String key_windowBackgroundWhiteRedText6 = "windowBackgroundWhiteRedText6";
  public static final String key_windowBackgroundWhiteValueText = "windowBackgroundWhiteValueText";
  public static String[] keys_avatar_actionBarIcon;
  public static String[] keys_avatar_actionBarSelector;
  public static String[] keys_avatar_background;
  public static String[] keys_avatar_backgroundActionBar;
  public static String[] keys_avatar_backgroundInProfile;
  public static String[] keys_avatar_nameInMessage;
  public static String[] keys_avatar_subtitleInProfile;
  private static float lastBrightnessValue;
  private static long lastHolidayCheckTime;
  private static long lastThemeSwitchTime;
  private static Sensor lightSensor;
  private static boolean lightSensorRegistered;
  public static Paint linkSelectionPaint;
  public static Drawable listSelector;
  private static Paint maskPaint;
  private static ArrayList<ThemeInfo> otherThemes;
  private static ThemeInfo previousTheme;
  public static TextPaint profile_aboutTextPaint;
  public static Drawable profile_verifiedCheckDrawable;
  public static Drawable profile_verifiedDrawable;
  public static int selectedAutoNightType;
  private static int selectedColor;
  private static SensorManager sensorManager;
  private static int serviceMessageColor;
  private static int serviceSelectedMessageColor;
  private static Runnable switchDayBrightnessRunnable;
  private static boolean switchDayRunnableScheduled;
  private static Runnable switchNightBrightnessRunnable;
  private static boolean switchNightRunnableScheduled;
  private static final Object sync = new Object();
  private static Drawable themedWallpaper;
  private static int themedWallpaperFileOffset;
  public static ArrayList<ThemeInfo> themes;
  private static HashMap<String, ThemeInfo> themesDict;
  private static Drawable wallpaper;
  private static final Object wallpaperSync = new Object();
  
  static
  {
    lastBrightnessValue = 1.0F;
    switchDayBrightnessRunnable = new Runnable()
    {
      public void run()
      {
        Theme.access$002(false);
        Theme.applyDayNightThemeMaybe(false);
      }
    };
    switchNightBrightnessRunnable = new Runnable()
    {
      public void run()
      {
        Theme.access$202(false);
        Theme.applyDayNightThemeMaybe(true);
      }
    };
    selectedAutoNightType = 0;
    autoNightBrighnessThreshold = 0.25F;
    autoNightDayStartTime = 1320;
    autoNightDayEndTime = 480;
    autoNightSunsetTime = 1320;
    autoNightLastSunCheckDay = -1;
    autoNightSunriseTime = 480;
    autoNightCityName = "";
    autoNightLocationLatitude = 10000.0D;
    autoNightLocationLongitude = 10000.0D;
    maskPaint = new Paint(1);
    chat_attachButtonDrawables = new Drawable[8];
    chat_locationDrawable = new Drawable[2];
    chat_contactDrawable = new Drawable[2];
    chat_cornerOuter = new Drawable[4];
    chat_cornerInner = new Drawable[4];
    chat_fileStatesDrawable = (Drawable[][])Array.newInstance(Drawable.class, new int[] { 10, 2 });
    chat_fileMiniStatesDrawable = (CombinedDrawable[][])Array.newInstance(CombinedDrawable.class, new int[] { 6, 2 });
    chat_ivStatesDrawable = (Drawable[][])Array.newInstance(Drawable.class, new int[] { 4, 2 });
    chat_photoStatesDrawables = (Drawable[][])Array.newInstance(Drawable.class, new int[] { 13, 2 });
    keys_avatar_background = new String[] { "avatar_backgroundRed", "avatar_backgroundOrange", "avatar_backgroundViolet", "avatar_backgroundGreen", "avatar_backgroundCyan", "avatar_backgroundBlue", "avatar_backgroundPink" };
    keys_avatar_backgroundInProfile = new String[] { "avatar_backgroundInProfileRed", "avatar_backgroundInProfileOrange", "avatar_backgroundInProfileViolet", "avatar_backgroundInProfileGreen", "avatar_backgroundInProfileCyan", "avatar_backgroundInProfileBlue", "avatar_backgroundInProfilePink" };
    keys_avatar_backgroundActionBar = new String[] { "avatar_backgroundActionBarRed", "avatar_backgroundActionBarOrange", "avatar_backgroundActionBarViolet", "avatar_backgroundActionBarGreen", "avatar_backgroundActionBarCyan", "avatar_backgroundActionBarBlue", "avatar_backgroundActionBarPink" };
    keys_avatar_subtitleInProfile = new String[] { "avatar_subtitleInProfileRed", "avatar_subtitleInProfileOrange", "avatar_subtitleInProfileViolet", "avatar_subtitleInProfileGreen", "avatar_subtitleInProfileCyan", "avatar_subtitleInProfileBlue", "avatar_subtitleInProfilePink" };
    keys_avatar_nameInMessage = new String[] { "avatar_nameInMessageRed", "avatar_nameInMessageOrange", "avatar_nameInMessageViolet", "avatar_nameInMessageGreen", "avatar_nameInMessageCyan", "avatar_nameInMessageBlue", "avatar_nameInMessagePink" };
    keys_avatar_actionBarSelector = new String[] { "avatar_actionBarSelectorRed", "avatar_actionBarSelectorOrange", "avatar_actionBarSelectorViolet", "avatar_actionBarSelectorGreen", "avatar_actionBarSelectorCyan", "avatar_actionBarSelectorBlue", "avatar_actionBarSelectorPink" };
    keys_avatar_actionBarIcon = new String[] { "avatar_actionBarIconRed", "avatar_actionBarIconOrange", "avatar_actionBarIconViolet", "avatar_actionBarIconGreen", "avatar_actionBarIconCyan", "avatar_actionBarIconBlue", "avatar_actionBarIconPink" };
    defaultColors = new HashMap();
    fallbackKeys = new HashMap();
    defaultColors.put("dialogBackground", Integer.valueOf(-1));
    defaultColors.put("dialogBackgroundGray", Integer.valueOf(-986896));
    defaultColors.put("dialogTextBlack", Integer.valueOf(-14606047));
    defaultColors.put("dialogTextLink", Integer.valueOf(-14255946));
    defaultColors.put("dialogLinkSelection", Integer.valueOf(862104035));
    defaultColors.put("dialogTextRed", Integer.valueOf(-3319206));
    defaultColors.put("dialogTextBlue", Integer.valueOf(-13660983));
    defaultColors.put("dialogTextBlue2", Integer.valueOf(-12940081));
    defaultColors.put("dialogTextBlue3", Integer.valueOf(-12664327));
    defaultColors.put("dialogTextBlue4", Integer.valueOf(-15095832));
    defaultColors.put("dialogTextGray", Integer.valueOf(-13333567));
    defaultColors.put("dialogTextGray2", Integer.valueOf(-9079435));
    defaultColors.put("dialogTextGray3", Integer.valueOf(-6710887));
    defaultColors.put("dialogTextGray4", Integer.valueOf(-5000269));
    defaultColors.put("dialogTextHint", Integer.valueOf(-6842473));
    defaultColors.put("dialogIcon", Integer.valueOf(-7697782));
    defaultColors.put("dialogGrayLine", Integer.valueOf(-2960686));
    defaultColors.put("dialogTopBackground", Integer.valueOf(-9456923));
    defaultColors.put("dialogInputField", Integer.valueOf(-2368549));
    defaultColors.put("dialogInputFieldActivated", Integer.valueOf(-13129232));
    defaultColors.put("dialogCheckboxSquareBackground", Integer.valueOf(-12345121));
    defaultColors.put("dialogCheckboxSquareCheck", Integer.valueOf(-1));
    defaultColors.put("dialogCheckboxSquareUnchecked", Integer.valueOf(-9211021));
    defaultColors.put("dialogCheckboxSquareDisabled", Integer.valueOf(-5197648));
    defaultColors.put("dialogRadioBackground", Integer.valueOf(-5000269));
    defaultColors.put("dialogRadioBackgroundChecked", Integer.valueOf(-13129232));
    defaultColors.put("dialogProgressCircle", Integer.valueOf(-11371101));
    defaultColors.put("dialogLineProgress", Integer.valueOf(-11371101));
    defaultColors.put("dialogLineProgressBackground", Integer.valueOf(-2368549));
    defaultColors.put("dialogButton", Integer.valueOf(-11955764));
    defaultColors.put("dialogButtonSelector", Integer.valueOf(251658240));
    defaultColors.put("dialogScrollGlow", Integer.valueOf(-657673));
    defaultColors.put("dialogRoundCheckBox", Integer.valueOf(-12664327));
    defaultColors.put("dialogRoundCheckBoxCheck", Integer.valueOf(-1));
    defaultColors.put("dialogBadgeBackground", Integer.valueOf(-12664327));
    defaultColors.put("dialogBadgeText", Integer.valueOf(-1));
    defaultColors.put("windowBackgroundWhite", Integer.valueOf(-1));
    defaultColors.put("progressCircle", Integer.valueOf(-11371101));
    defaultColors.put("windowBackgroundWhiteGrayIcon", Integer.valueOf(-9211021));
    defaultColors.put("windowBackgroundWhiteBlueText", Integer.valueOf(-12876608));
    defaultColors.put("windowBackgroundWhiteBlueText2", Integer.valueOf(-13333567));
    defaultColors.put("windowBackgroundWhiteBlueText3", Integer.valueOf(-14255946));
    defaultColors.put("windowBackgroundWhiteBlueText4", Integer.valueOf(-11697229));
    defaultColors.put("windowBackgroundWhiteBlueText5", Integer.valueOf(-11759926));
    defaultColors.put("windowBackgroundWhiteBlueText6", Integer.valueOf(-12940081));
    defaultColors.put("windowBackgroundWhiteBlueText7", Integer.valueOf(-13141330));
    defaultColors.put("windowBackgroundWhiteGreenText", Integer.valueOf(-14248148));
    defaultColors.put("windowBackgroundWhiteGreenText2", Integer.valueOf(-13129447));
    defaultColors.put("windowBackgroundWhiteRedText", Integer.valueOf(-3319206));
    defaultColors.put("windowBackgroundWhiteRedText2", Integer.valueOf(-2404015));
    defaultColors.put("windowBackgroundWhiteRedText3", Integer.valueOf(-2995895));
    defaultColors.put("windowBackgroundWhiteRedText4", Integer.valueOf(-3198928));
    defaultColors.put("windowBackgroundWhiteRedText5", Integer.valueOf(-1229511));
    defaultColors.put("windowBackgroundWhiteRedText6", Integer.valueOf(-39322));
    defaultColors.put("windowBackgroundWhiteGrayText", Integer.valueOf(-5723992));
    defaultColors.put("windowBackgroundWhiteGrayText2", Integer.valueOf(-7697782));
    defaultColors.put("windowBackgroundWhiteGrayText3", Integer.valueOf(-6710887));
    defaultColors.put("windowBackgroundWhiteGrayText4", Integer.valueOf(-8355712));
    defaultColors.put("windowBackgroundWhiteGrayText5", Integer.valueOf(-6052957));
    defaultColors.put("windowBackgroundWhiteGrayText6", Integer.valueOf(-9079435));
    defaultColors.put("windowBackgroundWhiteGrayText7", Integer.valueOf(-3750202));
    defaultColors.put("windowBackgroundWhiteGrayText8", Integer.valueOf(-9605774));
    defaultColors.put("windowBackgroundWhiteGrayLine", Integer.valueOf(-2368549));
    defaultColors.put("windowBackgroundWhiteBlackText", Integer.valueOf(-14606047));
    defaultColors.put("windowBackgroundWhiteHintText", Integer.valueOf(-6842473));
    defaultColors.put("windowBackgroundWhiteValueText", Integer.valueOf(-13660983));
    defaultColors.put("windowBackgroundWhiteLinkText", Integer.valueOf(-14255946));
    defaultColors.put("windowBackgroundWhiteLinkSelection", Integer.valueOf(862104035));
    defaultColors.put("windowBackgroundWhiteBlueHeader", Integer.valueOf(-12676913));
    defaultColors.put("windowBackgroundWhiteInputField", Integer.valueOf(-2368549));
    defaultColors.put("windowBackgroundWhiteInputFieldActivated", Integer.valueOf(-13129232));
    defaultColors.put("switchThumb", Integer.valueOf(-1184275));
    defaultColors.put("switchTrack", Integer.valueOf(-3684409));
    defaultColors.put("switchThumbChecked", Integer.valueOf(-12211217));
    defaultColors.put("switchTrackChecked", Integer.valueOf(-6236422));
    defaultColors.put("checkboxSquareBackground", Integer.valueOf(-12345121));
    defaultColors.put("checkboxSquareCheck", Integer.valueOf(-1));
    defaultColors.put("checkboxSquareUnchecked", Integer.valueOf(-9211021));
    defaultColors.put("checkboxSquareDisabled", Integer.valueOf(-5197648));
    defaultColors.put("listSelectorSDK21", Integer.valueOf(251658240));
    defaultColors.put("radioBackground", Integer.valueOf(-5000269));
    defaultColors.put("radioBackgroundChecked", Integer.valueOf(-13129232));
    defaultColors.put("windowBackgroundGray", Integer.valueOf(-986896));
    defaultColors.put("windowBackgroundGrayShadow", Integer.valueOf(-16777216));
    defaultColors.put("emptyListPlaceholder", Integer.valueOf(-6974059));
    defaultColors.put("divider", Integer.valueOf(-2500135));
    defaultColors.put("graySection", Integer.valueOf(-855310));
    defaultColors.put("contextProgressInner1", Integer.valueOf(-4202506));
    defaultColors.put("contextProgressOuter1", Integer.valueOf(-13920542));
    defaultColors.put("contextProgressInner2", Integer.valueOf(-4202506));
    defaultColors.put("contextProgressOuter2", Integer.valueOf(-1));
    defaultColors.put("contextProgressInner3", Integer.valueOf(-5000269));
    defaultColors.put("contextProgressOuter3", Integer.valueOf(-1));
    defaultColors.put("fastScrollActive", Integer.valueOf(-11361317));
    defaultColors.put("fastScrollInactive", Integer.valueOf(-10263709));
    defaultColors.put("fastScrollText", Integer.valueOf(-1));
    defaultColors.put("avatar_text", Integer.valueOf(-1));
    defaultColors.put("avatar_backgroundSaved", Integer.valueOf(-10043398));
    defaultColors.put("avatar_backgroundRed", Integer.valueOf(-1743531));
    defaultColors.put("avatar_backgroundOrange", Integer.valueOf(-881592));
    defaultColors.put("avatar_backgroundViolet", Integer.valueOf(-7436818));
    defaultColors.put("avatar_backgroundGreen", Integer.valueOf(-8992691));
    defaultColors.put("avatar_backgroundCyan", Integer.valueOf(-10502443));
    defaultColors.put("avatar_backgroundBlue", Integer.valueOf(-11232035));
    defaultColors.put("avatar_backgroundPink", Integer.valueOf(-887654));
    defaultColors.put("avatar_backgroundGroupCreateSpanBlue", Integer.valueOf(-4204822));
    defaultColors.put("avatar_backgroundInProfileRed", Integer.valueOf(-2592923));
    defaultColors.put("avatar_backgroundInProfileOrange", Integer.valueOf(-615071));
    defaultColors.put("avatar_backgroundInProfileViolet", Integer.valueOf(-7570990));
    defaultColors.put("avatar_backgroundInProfileGreen", Integer.valueOf(-9981091));
    defaultColors.put("avatar_backgroundInProfileCyan", Integer.valueOf(-11099461));
    defaultColors.put("avatar_backgroundInProfileBlue", Integer.valueOf(-11500111));
    defaultColors.put("avatar_backgroundInProfilePink", Integer.valueOf(-819290));
    defaultColors.put("avatar_backgroundActionBarRed", Integer.valueOf(-3514282));
    defaultColors.put("avatar_backgroundActionBarOrange", Integer.valueOf(-947900));
    defaultColors.put("avatar_backgroundActionBarViolet", Integer.valueOf(-8557884));
    defaultColors.put("avatar_backgroundActionBarGreen", Integer.valueOf(-11099828));
    defaultColors.put("avatar_backgroundActionBarCyan", Integer.valueOf(-12283220));
    defaultColors.put("avatar_backgroundActionBarBlue", Integer.valueOf(-10907718));
    defaultColors.put("avatar_backgroundActionBarPink", Integer.valueOf(-10907718));
    defaultColors.put("avatar_subtitleInProfileRed", Integer.valueOf(-406587));
    defaultColors.put("avatar_subtitleInProfileOrange", Integer.valueOf(-139832));
    defaultColors.put("avatar_subtitleInProfileViolet", Integer.valueOf(-3291923));
    defaultColors.put("avatar_subtitleInProfileGreen", Integer.valueOf(-4133446));
    defaultColors.put("avatar_subtitleInProfileCyan", Integer.valueOf(-4660496));
    defaultColors.put("avatar_subtitleInProfileBlue", Integer.valueOf(-2626822));
    defaultColors.put("avatar_subtitleInProfilePink", Integer.valueOf(-2626822));
    defaultColors.put("avatar_nameInMessageRed", Integer.valueOf(-3516848));
    defaultColors.put("avatar_nameInMessageOrange", Integer.valueOf(-2589911));
    defaultColors.put("avatar_nameInMessageViolet", Integer.valueOf(-11627828));
    defaultColors.put("avatar_nameInMessageGreen", Integer.valueOf(-11488718));
    defaultColors.put("avatar_nameInMessageCyan", Integer.valueOf(-12406360));
    defaultColors.put("avatar_nameInMessageBlue", Integer.valueOf(-11627828));
    defaultColors.put("avatar_nameInMessagePink", Integer.valueOf(-11627828));
    defaultColors.put("avatar_actionBarSelectorRed", Integer.valueOf(-4437183));
    defaultColors.put("avatar_actionBarSelectorOrange", Integer.valueOf(-1674199));
    defaultColors.put("avatar_actionBarSelectorViolet", Integer.valueOf(-9216066));
    defaultColors.put("avatar_actionBarSelectorGreen", Integer.valueOf(-12020419));
    defaultColors.put("avatar_actionBarSelectorCyan", Integer.valueOf(-13007715));
    defaultColors.put("avatar_actionBarSelectorBlue", Integer.valueOf(-11959891));
    defaultColors.put("avatar_actionBarSelectorPink", Integer.valueOf(-11959891));
    defaultColors.put("avatar_actionBarIconRed", Integer.valueOf(-1));
    defaultColors.put("avatar_actionBarIconOrange", Integer.valueOf(-1));
    defaultColors.put("avatar_actionBarIconViolet", Integer.valueOf(-1));
    defaultColors.put("avatar_actionBarIconGreen", Integer.valueOf(-1));
    defaultColors.put("avatar_actionBarIconCyan", Integer.valueOf(-1));
    defaultColors.put("avatar_actionBarIconBlue", Integer.valueOf(-1));
    defaultColors.put("avatar_actionBarIconPink", Integer.valueOf(-1));
    defaultColors.put("actionBarDefault", Integer.valueOf(-11371101));
    defaultColors.put("actionBarDefaultIcon", Integer.valueOf(-1));
    defaultColors.put("actionBarActionModeDefault", Integer.valueOf(-1));
    defaultColors.put("actionBarActionModeDefaultTop", Integer.valueOf(-1728053248));
    defaultColors.put("actionBarActionModeDefaultIcon", Integer.valueOf(-9211021));
    defaultColors.put("actionBarDefaultTitle", Integer.valueOf(-1));
    defaultColors.put("actionBarDefaultSubtitle", Integer.valueOf(-2758409));
    defaultColors.put("actionBarDefaultSelector", Integer.valueOf(-12554860));
    defaultColors.put("actionBarWhiteSelector", Integer.valueOf(788529152));
    defaultColors.put("actionBarDefaultSearch", Integer.valueOf(-1));
    defaultColors.put("actionBarDefaultSearchPlaceholder", Integer.valueOf(-1996488705));
    defaultColors.put("actionBarDefaultSubmenuItem", Integer.valueOf(-14606047));
    defaultColors.put("actionBarDefaultSubmenuBackground", Integer.valueOf(-1));
    defaultColors.put("actionBarActionModeDefaultSelector", Integer.valueOf(-986896));
    defaultColors.put("chats_unreadCounter", Integer.valueOf(-11613090));
    defaultColors.put("chats_unreadCounterMuted", Integer.valueOf(-3684409));
    defaultColors.put("chats_unreadCounterText", Integer.valueOf(-1));
    defaultColors.put("chats_name", Integer.valueOf(-14606047));
    defaultColors.put("chats_secretName", Integer.valueOf(-16734706));
    defaultColors.put("chats_secretIcon", Integer.valueOf(-15093466));
    defaultColors.put("chats_nameIcon", Integer.valueOf(-14408668));
    defaultColors.put("chats_pinnedIcon", Integer.valueOf(-5723992));
    defaultColors.put("chats_message", Integer.valueOf(-7368817));
    defaultColors.put("chats_draft", Integer.valueOf(-2274503));
    defaultColors.put("chats_nameMessage", Integer.valueOf(-11697229));
    defaultColors.put("chats_attachMessage", Integer.valueOf(-11697229));
    defaultColors.put("chats_actionMessage", Integer.valueOf(-11697229));
    defaultColors.put("chats_date", Integer.valueOf(-6710887));
    defaultColors.put("chats_pinnedOverlay", Integer.valueOf(134217728));
    defaultColors.put("chats_tabletSelectedOverlay", Integer.valueOf(251658240));
    defaultColors.put("chats_sentCheck", Integer.valueOf(-12146122));
    defaultColors.put("chats_sentClock", Integer.valueOf(-9061026));
    defaultColors.put("chats_sentError", Integer.valueOf(-2796974));
    defaultColors.put("chats_sentErrorIcon", Integer.valueOf(-1));
    defaultColors.put("chats_verifiedBackground", Integer.valueOf(-13391642));
    defaultColors.put("chats_verifiedCheck", Integer.valueOf(-1));
    defaultColors.put("chats_muteIcon", Integer.valueOf(-5723992));
    defaultColors.put("chats_menuBackground", Integer.valueOf(-1));
    defaultColors.put("chats_menuItemText", Integer.valueOf(-12303292));
    defaultColors.put("chats_menuItemCheck", Integer.valueOf(-10907718));
    defaultColors.put("chats_menuItemIcon", Integer.valueOf(-9211021));
    defaultColors.put("chats_menuName", Integer.valueOf(-1));
    defaultColors.put("chats_menuPhone", Integer.valueOf(-1));
    defaultColors.put("chats_menuPhoneCats", Integer.valueOf(-4004353));
    defaultColors.put("chats_menuCloud", Integer.valueOf(-1));
    defaultColors.put("chats_menuCloudBackgroundCats", Integer.valueOf(-12420183));
    defaultColors.put("chats_actionIcon", Integer.valueOf(-1));
    defaultColors.put("chats_actionBackground", Integer.valueOf(-9788978));
    defaultColors.put("chats_actionPressedBackground", Integer.valueOf(-11038014));
    defaultColors.put("chat_lockIcon", Integer.valueOf(-1));
    defaultColors.put("chat_muteIcon", Integer.valueOf(-5124893));
    defaultColors.put("chat_inBubble", Integer.valueOf(-1));
    defaultColors.put("chat_inBubbleSelected", Integer.valueOf(-1902337));
    defaultColors.put("chat_inBubbleShadow", Integer.valueOf(-14862509));
    defaultColors.put("chat_outBubble", Integer.valueOf(-1048610));
    defaultColors.put("chat_outBubbleSelected", Integer.valueOf(-2820676));
    defaultColors.put("chat_outBubbleShadow", Integer.valueOf(-14781172));
    defaultColors.put("chat_messageTextIn", Integer.valueOf(-16777216));
    defaultColors.put("chat_messageTextOut", Integer.valueOf(-16777216));
    defaultColors.put("chat_messageLinkIn", Integer.valueOf(-14255946));
    defaultColors.put("chat_messageLinkOut", Integer.valueOf(-14255946));
    defaultColors.put("chat_serviceText", Integer.valueOf(-1));
    defaultColors.put("chat_serviceLink", Integer.valueOf(-1));
    defaultColors.put("chat_serviceIcon", Integer.valueOf(-1));
    defaultColors.put("chat_mediaTimeBackground", Integer.valueOf(1711276032));
    defaultColors.put("chat_outSentCheck", Integer.valueOf(-10637232));
    defaultColors.put("chat_outSentCheckSelected", Integer.valueOf(-10637232));
    defaultColors.put("chat_outSentClock", Integer.valueOf(-9061026));
    defaultColors.put("chat_outSentClockSelected", Integer.valueOf(-9061026));
    defaultColors.put("chat_inSentClock", Integer.valueOf(-6182221));
    defaultColors.put("chat_inSentClockSelected", Integer.valueOf(-7094838));
    defaultColors.put("chat_mediaSentCheck", Integer.valueOf(-1));
    defaultColors.put("chat_mediaSentClock", Integer.valueOf(-1));
    defaultColors.put("chat_inViews", Integer.valueOf(-6182221));
    defaultColors.put("chat_inViewsSelected", Integer.valueOf(-7094838));
    defaultColors.put("chat_outViews", Integer.valueOf(-9522601));
    defaultColors.put("chat_outViewsSelected", Integer.valueOf(-9522601));
    defaultColors.put("chat_mediaViews", Integer.valueOf(-1));
    defaultColors.put("chat_inMenu", Integer.valueOf(-4801083));
    defaultColors.put("chat_inMenuSelected", Integer.valueOf(-6766130));
    defaultColors.put("chat_outMenu", Integer.valueOf(-7221634));
    defaultColors.put("chat_outMenuSelected", Integer.valueOf(-7221634));
    defaultColors.put("chat_mediaMenu", Integer.valueOf(-1));
    defaultColors.put("chat_outInstant", Integer.valueOf(-11162801));
    defaultColors.put("chat_outInstantSelected", Integer.valueOf(-12019389));
    defaultColors.put("chat_inInstant", Integer.valueOf(-12940081));
    defaultColors.put("chat_inInstantSelected", Integer.valueOf(-13600331));
    defaultColors.put("chat_sentError", Integer.valueOf(-2411211));
    defaultColors.put("chat_sentErrorIcon", Integer.valueOf(-1));
    defaultColors.put("chat_selectedBackground", Integer.valueOf(1714664933));
    defaultColors.put("chat_previewDurationText", Integer.valueOf(-1));
    defaultColors.put("chat_previewGameText", Integer.valueOf(-1));
    defaultColors.put("chat_inPreviewInstantText", Integer.valueOf(-12940081));
    defaultColors.put("chat_outPreviewInstantText", Integer.valueOf(-11162801));
    defaultColors.put("chat_inPreviewInstantSelectedText", Integer.valueOf(-13600331));
    defaultColors.put("chat_outPreviewInstantSelectedText", Integer.valueOf(-12019389));
    defaultColors.put("chat_secretTimeText", Integer.valueOf(-1776928));
    defaultColors.put("chat_stickerNameText", Integer.valueOf(-1));
    defaultColors.put("chat_botButtonText", Integer.valueOf(-1));
    defaultColors.put("chat_botProgress", Integer.valueOf(-1));
    defaultColors.put("chat_inForwardedNameText", Integer.valueOf(-13072697));
    defaultColors.put("chat_outForwardedNameText", Integer.valueOf(-11162801));
    defaultColors.put("chat_inViaBotNameText", Integer.valueOf(-12940081));
    defaultColors.put("chat_outViaBotNameText", Integer.valueOf(-11162801));
    defaultColors.put("chat_stickerViaBotNameText", Integer.valueOf(-1));
    defaultColors.put("chat_inReplyLine", Integer.valueOf(-10903592));
    defaultColors.put("chat_outReplyLine", Integer.valueOf(-9520791));
    defaultColors.put("chat_stickerReplyLine", Integer.valueOf(-1));
    defaultColors.put("chat_inReplyNameText", Integer.valueOf(-12940081));
    defaultColors.put("chat_outReplyNameText", Integer.valueOf(-11162801));
    defaultColors.put("chat_stickerReplyNameText", Integer.valueOf(-1));
    defaultColors.put("chat_inReplyMessageText", Integer.valueOf(-16777216));
    defaultColors.put("chat_outReplyMessageText", Integer.valueOf(-16777216));
    defaultColors.put("chat_inReplyMediaMessageText", Integer.valueOf(-6182221));
    defaultColors.put("chat_outReplyMediaMessageText", Integer.valueOf(-10112933));
    defaultColors.put("chat_inReplyMediaMessageSelectedText", Integer.valueOf(-7752511));
    defaultColors.put("chat_outReplyMediaMessageSelectedText", Integer.valueOf(-10112933));
    defaultColors.put("chat_stickerReplyMessageText", Integer.valueOf(-1));
    defaultColors.put("chat_inPreviewLine", Integer.valueOf(-9390872));
    defaultColors.put("chat_outPreviewLine", Integer.valueOf(-7812741));
    defaultColors.put("chat_inSiteNameText", Integer.valueOf(-12940081));
    defaultColors.put("chat_outSiteNameText", Integer.valueOf(-11162801));
    defaultColors.put("chat_inContactNameText", Integer.valueOf(-11625772));
    defaultColors.put("chat_outContactNameText", Integer.valueOf(-11162801));
    defaultColors.put("chat_inContactPhoneText", Integer.valueOf(-13683656));
    defaultColors.put("chat_outContactPhoneText", Integer.valueOf(-13286860));
    defaultColors.put("chat_mediaProgress", Integer.valueOf(-1));
    defaultColors.put("chat_inAudioProgress", Integer.valueOf(-1));
    defaultColors.put("chat_outAudioProgress", Integer.valueOf(-1048610));
    defaultColors.put("chat_inAudioSelectedProgress", Integer.valueOf(-1902337));
    defaultColors.put("chat_outAudioSelectedProgress", Integer.valueOf(-2820676));
    defaultColors.put("chat_mediaTimeText", Integer.valueOf(-1));
    defaultColors.put("chat_inTimeText", Integer.valueOf(-6182221));
    defaultColors.put("chat_outTimeText", Integer.valueOf(-9391780));
    defaultColors.put("chat_adminText", Integer.valueOf(-4143413));
    defaultColors.put("chat_adminSelectedText", Integer.valueOf(-7752511));
    defaultColors.put("chat_inTimeSelectedText", Integer.valueOf(-7752511));
    defaultColors.put("chat_outTimeSelectedText", Integer.valueOf(-9391780));
    defaultColors.put("chat_inAudioPerfomerText", Integer.valueOf(-13683656));
    defaultColors.put("chat_outAudioPerfomerText", Integer.valueOf(-13286860));
    defaultColors.put("chat_inAudioTitleText", Integer.valueOf(-11625772));
    defaultColors.put("chat_outAudioTitleText", Integer.valueOf(-11162801));
    defaultColors.put("chat_inAudioDurationText", Integer.valueOf(-6182221));
    defaultColors.put("chat_outAudioDurationText", Integer.valueOf(-10112933));
    defaultColors.put("chat_inAudioDurationSelectedText", Integer.valueOf(-7752511));
    defaultColors.put("chat_outAudioDurationSelectedText", Integer.valueOf(-10112933));
    defaultColors.put("chat_inAudioSeekbar", Integer.valueOf(-1774864));
    defaultColors.put("chat_inAudioCacheSeekbar", Integer.valueOf(1071966960));
    defaultColors.put("chat_outAudioSeekbar", Integer.valueOf(-4463700));
    defaultColors.put("chat_outAudioCacheSeekbar", Integer.valueOf(1069278124));
    defaultColors.put("chat_inAudioSeekbarSelected", Integer.valueOf(-4399384));
    defaultColors.put("chat_outAudioSeekbarSelected", Integer.valueOf(-5644906));
    defaultColors.put("chat_inAudioSeekbarFill", Integer.valueOf(-9259544));
    defaultColors.put("chat_outAudioSeekbarFill", Integer.valueOf(-8863118));
    defaultColors.put("chat_inVoiceSeekbar", Integer.valueOf(-2169365));
    defaultColors.put("chat_outVoiceSeekbar", Integer.valueOf(-4463700));
    defaultColors.put("chat_inVoiceSeekbarSelected", Integer.valueOf(-4399384));
    defaultColors.put("chat_outVoiceSeekbarSelected", Integer.valueOf(-5644906));
    defaultColors.put("chat_inVoiceSeekbarFill", Integer.valueOf(-9259544));
    defaultColors.put("chat_outVoiceSeekbarFill", Integer.valueOf(-8863118));
    defaultColors.put("chat_inFileProgress", Integer.valueOf(-1314571));
    defaultColors.put("chat_outFileProgress", Integer.valueOf(-2427453));
    defaultColors.put("chat_inFileProgressSelected", Integer.valueOf(-3413258));
    defaultColors.put("chat_outFileProgressSelected", Integer.valueOf(-3806041));
    defaultColors.put("chat_inFileNameText", Integer.valueOf(-11625772));
    defaultColors.put("chat_outFileNameText", Integer.valueOf(-11162801));
    defaultColors.put("chat_inFileInfoText", Integer.valueOf(-6182221));
    defaultColors.put("chat_outFileInfoText", Integer.valueOf(-10112933));
    defaultColors.put("chat_inFileInfoSelectedText", Integer.valueOf(-7752511));
    defaultColors.put("chat_outFileInfoSelectedText", Integer.valueOf(-10112933));
    defaultColors.put("chat_inFileBackground", Integer.valueOf(-1314571));
    defaultColors.put("chat_outFileBackground", Integer.valueOf(-2427453));
    defaultColors.put("chat_inFileBackgroundSelected", Integer.valueOf(-3413258));
    defaultColors.put("chat_outFileBackgroundSelected", Integer.valueOf(-3806041));
    defaultColors.put("chat_inVenueNameText", Integer.valueOf(-11625772));
    defaultColors.put("chat_outVenueNameText", Integer.valueOf(-11162801));
    defaultColors.put("chat_inVenueInfoText", Integer.valueOf(-6182221));
    defaultColors.put("chat_outVenueInfoText", Integer.valueOf(-10112933));
    defaultColors.put("chat_inVenueInfoSelectedText", Integer.valueOf(-7752511));
    defaultColors.put("chat_outVenueInfoSelectedText", Integer.valueOf(-10112933));
    defaultColors.put("chat_mediaInfoText", Integer.valueOf(-1));
    defaultColors.put("chat_linkSelectBackground", Integer.valueOf(862104035));
    defaultColors.put("chat_textSelectBackground", Integer.valueOf(1717742051));
    defaultColors.put("chat_emojiPanelBackground", Integer.valueOf(-657673));
    defaultColors.put("chat_emojiSearchBackground", Integer.valueOf(-1578003));
    defaultColors.put("chat_emojiPanelShadowLine", Integer.valueOf(-1907225));
    defaultColors.put("chat_emojiPanelEmptyText", Integer.valueOf(-5723992));
    defaultColors.put("chat_emojiPanelIcon", Integer.valueOf(-5723992));
    defaultColors.put("chat_emojiPanelIconSelected", Integer.valueOf(-13920542));
    defaultColors.put("chat_emojiPanelStickerPackSelector", Integer.valueOf(-1907225));
    defaultColors.put("chat_emojiPanelIconSelector", Integer.valueOf(-13920542));
    defaultColors.put("chat_emojiPanelBackspace", Integer.valueOf(-5723992));
    defaultColors.put("chat_emojiPanelMasksIcon", Integer.valueOf(-1));
    defaultColors.put("chat_emojiPanelMasksIconSelected", Integer.valueOf(-10305560));
    defaultColors.put("chat_emojiPanelTrendingTitle", Integer.valueOf(-14606047));
    defaultColors.put("chat_emojiPanelStickerSetName", Integer.valueOf(-8156010));
    defaultColors.put("chat_emojiPanelStickerSetNameIcon", Integer.valueOf(-5130564));
    defaultColors.put("chat_emojiPanelTrendingDescription", Integer.valueOf(-7697782));
    defaultColors.put("chat_botKeyboardButtonText", Integer.valueOf(-13220017));
    defaultColors.put("chat_botKeyboardButtonBackground", Integer.valueOf(-1775639));
    defaultColors.put("chat_botKeyboardButtonBackgroundPressed", Integer.valueOf(-3354156));
    defaultColors.put("chat_unreadMessagesStartArrowIcon", Integer.valueOf(-6113849));
    defaultColors.put("chat_unreadMessagesStartText", Integer.valueOf(-11102772));
    defaultColors.put("chat_unreadMessagesStartBackground", Integer.valueOf(-1));
    defaultColors.put("chat_editDoneIcon", Integer.valueOf(-11420173));
    defaultColors.put("chat_inFileIcon", Integer.valueOf(-6113849));
    defaultColors.put("chat_inFileSelectedIcon", Integer.valueOf(-7883067));
    defaultColors.put("chat_outFileIcon", Integer.valueOf(-8011912));
    defaultColors.put("chat_outFileSelectedIcon", Integer.valueOf(-8011912));
    defaultColors.put("chat_inLocationBackground", Integer.valueOf(-1314571));
    defaultColors.put("chat_inLocationIcon", Integer.valueOf(-6113849));
    defaultColors.put("chat_outLocationBackground", Integer.valueOf(-2427453));
    defaultColors.put("chat_outLocationIcon", Integer.valueOf(-7880840));
    defaultColors.put("chat_inContactBackground", Integer.valueOf(-9259544));
    defaultColors.put("chat_inContactIcon", Integer.valueOf(-1));
    defaultColors.put("chat_outContactBackground", Integer.valueOf(-8863118));
    defaultColors.put("chat_outContactIcon", Integer.valueOf(-1048610));
    defaultColors.put("chat_outBroadcast", Integer.valueOf(-12146122));
    defaultColors.put("chat_mediaBroadcast", Integer.valueOf(-1));
    defaultColors.put("chat_searchPanelIcons", Integer.valueOf(-10639908));
    defaultColors.put("chat_searchPanelText", Integer.valueOf(-11625772));
    defaultColors.put("chat_secretChatStatusText", Integer.valueOf(-8421505));
    defaultColors.put("chat_fieldOverlayText", Integer.valueOf(-12940081));
    defaultColors.put("chat_stickersHintPanel", Integer.valueOf(-1));
    defaultColors.put("chat_replyPanelIcons", Integer.valueOf(-11032346));
    defaultColors.put("chat_replyPanelClose", Integer.valueOf(-5723992));
    defaultColors.put("chat_replyPanelName", Integer.valueOf(-12940081));
    defaultColors.put("chat_replyPanelMessage", Integer.valueOf(-14540254));
    defaultColors.put("chat_replyPanelLine", Integer.valueOf(-1513240));
    defaultColors.put("chat_messagePanelBackground", Integer.valueOf(-1));
    defaultColors.put("chat_messagePanelText", Integer.valueOf(-16777216));
    defaultColors.put("chat_messagePanelHint", Integer.valueOf(-5066062));
    defaultColors.put("chat_messagePanelShadow", Integer.valueOf(-16777216));
    defaultColors.put("chat_messagePanelIcons", Integer.valueOf(-5723992));
    defaultColors.put("chat_recordedVoicePlayPause", Integer.valueOf(-1));
    defaultColors.put("chat_recordedVoicePlayPausePressed", Integer.valueOf(-2495749));
    defaultColors.put("chat_recordedVoiceDot", Integer.valueOf(-2468275));
    defaultColors.put("chat_recordedVoiceBackground", Integer.valueOf(-11165981));
    defaultColors.put("chat_recordedVoiceProgress", Integer.valueOf(-6107400));
    defaultColors.put("chat_recordedVoiceProgressInner", Integer.valueOf(-1));
    defaultColors.put("chat_recordVoiceCancel", Integer.valueOf(-6710887));
    defaultColors.put("chat_messagePanelSend", Integer.valueOf(-10309397));
    defaultColors.put("key_chat_messagePanelVoiceLock", Integer.valueOf(-5987164));
    defaultColors.put("key_chat_messagePanelVoiceLockBackground", Integer.valueOf(-1));
    defaultColors.put("key_chat_messagePanelVoiceLockShadow", Integer.valueOf(-16777216));
    defaultColors.put("chat_recordTime", Integer.valueOf(-11711413));
    defaultColors.put("chat_emojiPanelNewTrending", Integer.valueOf(-11688214));
    defaultColors.put("chat_gifSaveHintText", Integer.valueOf(-1));
    defaultColors.put("chat_gifSaveHintBackground", Integer.valueOf(-871296751));
    defaultColors.put("chat_goDownButton", Integer.valueOf(-1));
    defaultColors.put("chat_goDownButtonShadow", Integer.valueOf(-16777216));
    defaultColors.put("chat_goDownButtonIcon", Integer.valueOf(-5723992));
    defaultColors.put("chat_goDownButtonCounter", Integer.valueOf(-1));
    defaultColors.put("chat_goDownButtonCounterBackground", Integer.valueOf(-11689240));
    defaultColors.put("chat_messagePanelCancelInlineBot", Integer.valueOf(-5395027));
    defaultColors.put("chat_messagePanelVoicePressed", Integer.valueOf(-1));
    defaultColors.put("chat_messagePanelVoiceBackground", Integer.valueOf(-11037236));
    defaultColors.put("chat_messagePanelVoiceShadow", Integer.valueOf(218103808));
    defaultColors.put("chat_messagePanelVoiceDelete", Integer.valueOf(-9211021));
    defaultColors.put("chat_messagePanelVoiceDuration", Integer.valueOf(-1));
    defaultColors.put("chat_inlineResultIcon", Integer.valueOf(-11037236));
    defaultColors.put("chat_topPanelBackground", Integer.valueOf(-1));
    defaultColors.put("chat_topPanelClose", Integer.valueOf(-5723992));
    defaultColors.put("chat_topPanelLine", Integer.valueOf(-9658414));
    defaultColors.put("chat_topPanelTitle", Integer.valueOf(-12940081));
    defaultColors.put("chat_topPanelMessage", Integer.valueOf(-6710887));
    defaultColors.put("chat_reportSpam", Integer.valueOf(-3188393));
    defaultColors.put("chat_addContact", Integer.valueOf(-11894091));
    defaultColors.put("chat_inLoader", Integer.valueOf(-9259544));
    defaultColors.put("chat_inLoaderSelected", Integer.valueOf(-10114080));
    defaultColors.put("chat_outLoader", Integer.valueOf(-8863118));
    defaultColors.put("chat_outLoaderSelected", Integer.valueOf(-9783964));
    defaultColors.put("chat_inLoaderPhoto", Integer.valueOf(-6113080));
    defaultColors.put("chat_inLoaderPhotoSelected", Integer.valueOf(-6113849));
    defaultColors.put("chat_inLoaderPhotoIcon", Integer.valueOf(-197380));
    defaultColors.put("chat_inLoaderPhotoIconSelected", Integer.valueOf(-1314571));
    defaultColors.put("chat_outLoaderPhoto", Integer.valueOf(-8011912));
    defaultColors.put("chat_outLoaderPhotoSelected", Integer.valueOf(-8538000));
    defaultColors.put("chat_outLoaderPhotoIcon", Integer.valueOf(-2427453));
    defaultColors.put("chat_outLoaderPhotoIconSelected", Integer.valueOf(-4134748));
    defaultColors.put("chat_mediaLoaderPhoto", Integer.valueOf(1711276032));
    defaultColors.put("chat_mediaLoaderPhotoSelected", Integer.valueOf(2130706432));
    defaultColors.put("chat_mediaLoaderPhotoIcon", Integer.valueOf(-1));
    defaultColors.put("chat_mediaLoaderPhotoIconSelected", Integer.valueOf(-2500135));
    defaultColors.put("chat_secretTimerBackground", Integer.valueOf(-868326258));
    defaultColors.put("chat_secretTimerText", Integer.valueOf(-1));
    defaultColors.put("profile_creatorIcon", Integer.valueOf(-11888682));
    defaultColors.put("profile_adminIcon", Integer.valueOf(-8026747));
    defaultColors.put("profile_actionIcon", Integer.valueOf(-9211021));
    defaultColors.put("profile_actionBackground", Integer.valueOf(-1));
    defaultColors.put("profile_actionPressedBackground", Integer.valueOf(-855310));
    defaultColors.put("profile_verifiedBackground", Integer.valueOf(-5056776));
    defaultColors.put("profile_verifiedCheck", Integer.valueOf(-11959368));
    defaultColors.put("profile_title", Integer.valueOf(-1));
    defaultColors.put("player_actionBar", Integer.valueOf(-1));
    defaultColors.put("player_actionBarSelector", Integer.valueOf(788529152));
    defaultColors.put("player_actionBarTitle", Integer.valueOf(-13683656));
    defaultColors.put("player_actionBarTop", Integer.valueOf(-1728053248));
    defaultColors.put("player_actionBarSubtitle", Integer.valueOf(-7697782));
    defaultColors.put("player_actionBarItems", Integer.valueOf(-7697782));
    defaultColors.put("player_background", Integer.valueOf(-1));
    defaultColors.put("player_time", Integer.valueOf(-7564650));
    defaultColors.put("player_progressBackground", Integer.valueOf(419430400));
    defaultColors.put("key_player_progressCachedBackground", Integer.valueOf(419430400));
    defaultColors.put("player_progress", Integer.valueOf(-14438417));
    defaultColors.put("player_placeholder", Integer.valueOf(-5723992));
    defaultColors.put("player_placeholderBackground", Integer.valueOf(-986896));
    defaultColors.put("player_button", Integer.valueOf(-13421773));
    defaultColors.put("player_buttonActive", Integer.valueOf(-11753238));
    defaultColors.put("files_folderIcon", Integer.valueOf(-6710887));
    defaultColors.put("files_folderIconBackground", Integer.valueOf(-986896));
    defaultColors.put("files_iconText", Integer.valueOf(-1));
    defaultColors.put("sessions_devicesImage", Integer.valueOf(-6908266));
    defaultColors.put("location_markerX", Integer.valueOf(-8355712));
    defaultColors.put("location_sendLocationBackground", Integer.valueOf(-9592620));
    defaultColors.put("location_sendLiveLocationBackground", Integer.valueOf(-39836));
    defaultColors.put("location_sendLocationIcon", Integer.valueOf(-1));
    defaultColors.put("location_liveLocationProgress", Integer.valueOf(-13262875));
    defaultColors.put("location_placeLocationBackground", Integer.valueOf(-11753238));
    defaultColors.put("location_liveLocationProgress", Integer.valueOf(-13262875));
    defaultColors.put("calls_callReceivedGreenIcon", Integer.valueOf(-16725933));
    defaultColors.put("calls_callReceivedRedIcon", Integer.valueOf(-47032));
    defaultColors.put("featuredStickers_addedIcon", Integer.valueOf(-11491093));
    defaultColors.put("featuredStickers_buttonProgress", Integer.valueOf(-1));
    defaultColors.put("featuredStickers_addButton", Integer.valueOf(-11491093));
    defaultColors.put("featuredStickers_addButtonPressed", Integer.valueOf(-12346402));
    defaultColors.put("featuredStickers_delButton", Integer.valueOf(-2533545));
    defaultColors.put("featuredStickers_delButtonPressed", Integer.valueOf(-3782327));
    defaultColors.put("featuredStickers_buttonText", Integer.valueOf(-1));
    defaultColors.put("featuredStickers_unread", Integer.valueOf(-11688214));
    defaultColors.put("inappPlayerPerformer", Integer.valueOf(-13683656));
    defaultColors.put("inappPlayerTitle", Integer.valueOf(-13683656));
    defaultColors.put("inappPlayerBackground", Integer.valueOf(-1));
    defaultColors.put("inappPlayerPlayPause", Integer.valueOf(-10309397));
    defaultColors.put("inappPlayerClose", Integer.valueOf(-5723992));
    defaultColors.put("returnToCallBackground", Integer.valueOf(-12279325));
    defaultColors.put("returnToCallText", Integer.valueOf(-1));
    defaultColors.put("sharedMedia_startStopLoadIcon", Integer.valueOf(-13196562));
    defaultColors.put("sharedMedia_linkPlaceholder", Integer.valueOf(-986896));
    defaultColors.put("sharedMedia_linkPlaceholderText", Integer.valueOf(-1));
    defaultColors.put("checkbox", Integer.valueOf(-10567099));
    defaultColors.put("checkboxCheck", Integer.valueOf(-1));
    defaultColors.put("stickers_menu", Integer.valueOf(-4801083));
    defaultColors.put("stickers_menuSelector", Integer.valueOf(788529152));
    defaultColors.put("changephoneinfo_image", Integer.valueOf(-5723992));
    defaultColors.put("groupcreate_hintText", Integer.valueOf(-6182221));
    defaultColors.put("groupcreate_cursor", Integer.valueOf(-11361317));
    defaultColors.put("groupcreate_sectionShadow", Integer.valueOf(-16777216));
    defaultColors.put("groupcreate_sectionText", Integer.valueOf(-8617336));
    defaultColors.put("groupcreate_onlineText", Integer.valueOf(-12545331));
    defaultColors.put("groupcreate_offlineText", Integer.valueOf(-8156010));
    defaultColors.put("groupcreate_checkbox", Integer.valueOf(-10567099));
    defaultColors.put("groupcreate_checkboxCheck", Integer.valueOf(-1));
    defaultColors.put("groupcreate_spanText", Integer.valueOf(-14606047));
    defaultColors.put("groupcreate_spanBackground", Integer.valueOf(-855310));
    defaultColors.put("contacts_inviteBackground", Integer.valueOf(-11157919));
    defaultColors.put("contacts_inviteText", Integer.valueOf(-1));
    defaultColors.put("login_progressInner", Integer.valueOf(-1971470));
    defaultColors.put("login_progressOuter", Integer.valueOf(-10313520));
    defaultColors.put("musicPicker_checkbox", Integer.valueOf(-14043401));
    defaultColors.put("musicPicker_checkboxCheck", Integer.valueOf(-1));
    defaultColors.put("musicPicker_buttonBackground", Integer.valueOf(-10702870));
    defaultColors.put("musicPicker_buttonIcon", Integer.valueOf(-1));
    defaultColors.put("picker_enabledButton", Integer.valueOf(-15095832));
    defaultColors.put("picker_disabledButton", Integer.valueOf(-6710887));
    defaultColors.put("picker_badge", Integer.valueOf(-14043401));
    defaultColors.put("picker_badgeText", Integer.valueOf(-1));
    defaultColors.put("chat_botSwitchToInlineText", Integer.valueOf(-12348980));
    defaultColors.put("calls_ratingStar", Integer.valueOf(Integer.MIN_VALUE));
    defaultColors.put("calls_ratingStarSelected", Integer.valueOf(-11888682));
    fallbackKeys.put("chat_adminText", "chat_inTimeText");
    fallbackKeys.put("chat_adminSelectedText", "chat_inTimeSelectedText");
    fallbackKeys.put("key_player_progressCachedBackground", "player_progressBackground");
    fallbackKeys.put("chat_inAudioCacheSeekbar", "chat_inAudioSeekbar");
    fallbackKeys.put("chat_outAudioCacheSeekbar", "chat_outAudioSeekbar");
    fallbackKeys.put("chat_emojiSearchBackground", "chat_emojiPanelStickerPackSelector");
    themes = new ArrayList();
    otherThemes = new ArrayList();
    themesDict = new HashMap();
    currentColors = new HashMap();
    Object localObject1 = new ThemeInfo();
    ((ThemeInfo)localObject1).name = "Default";
    localObject3 = themes;
    defaultTheme = (ThemeInfo)localObject1;
    currentTheme = (ThemeInfo)localObject1;
    currentDayTheme = (ThemeInfo)localObject1;
    ((ArrayList)localObject3).add(localObject1);
    themesDict.put("Default", defaultTheme);
    localObject1 = new ThemeInfo();
    ((ThemeInfo)localObject1).name = "Dark";
    ((ThemeInfo)localObject1).assetName = "dark.attheme";
    localObject3 = themes;
    currentNightTheme = (ThemeInfo)localObject1;
    ((ArrayList)localObject3).add(localObject1);
    themesDict.put("Dark", localObject1);
    localObject1 = new ThemeInfo();
    ((ThemeInfo)localObject1).name = "Blue";
    ((ThemeInfo)localObject1).assetName = "bluebubbles.attheme";
    themes.add(localObject1);
    themesDict.put("Blue", localObject1);
    localObject1 = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", 0);
    localObject3 = ((SharedPreferences)localObject1).getString("themes2", null);
    int i;
    Object localObject4;
    ThemeInfo localThemeInfo;
    if (!TextUtils.isEmpty((CharSequence)localObject3))
    {
      try
      {
        localObject1 = new JSONArray((String)localObject3);
        i = 0;
        while (i < ((JSONArray)localObject1).length())
        {
          localObject3 = ThemeInfo.createWithJson(((JSONArray)localObject1).getJSONObject(i));
          if (localObject3 != null)
          {
            otherThemes.add(localObject3);
            themes.add(localObject3);
            themesDict.put(((ThemeInfo)localObject3).name, localObject3);
          }
          i += 1;
        }
        sortThemes();
      }
      catch (Exception localException1)
      {
        FileLog.e(localException1);
      }
      localObject4 = null;
      localThemeInfo = null;
      localObject3 = localObject4;
    }
    for (;;)
    {
      try
      {
        localSharedPreferences = MessagesController.getGlobalMainSettings();
        localObject3 = localObject4;
        String str = localSharedPreferences.getString("theme", null);
        if (str != null)
        {
          localObject3 = localObject4;
          localThemeInfo = (ThemeInfo)themesDict.get(str);
        }
        localObject3 = localThemeInfo;
        localObject4 = localSharedPreferences.getString("nighttheme", null);
        if (localObject4 != null)
        {
          localObject3 = localThemeInfo;
          localObject4 = (ThemeInfo)themesDict.get(localObject4);
          if (localObject4 != null)
          {
            localObject3 = localThemeInfo;
            currentNightTheme = (ThemeInfo)localObject4;
          }
        }
        localObject3 = localThemeInfo;
        selectedAutoNightType = localSharedPreferences.getInt("selectedAutoNightType", 0);
        localObject3 = localThemeInfo;
        autoNightScheduleByLocation = localSharedPreferences.getBoolean("autoNightScheduleByLocation", false);
        localObject3 = localThemeInfo;
        autoNightBrighnessThreshold = localSharedPreferences.getFloat("autoNightBrighnessThreshold", 0.25F);
        localObject3 = localThemeInfo;
        autoNightDayStartTime = localSharedPreferences.getInt("autoNightDayStartTime", 1320);
        localObject3 = localThemeInfo;
        autoNightDayEndTime = localSharedPreferences.getInt("autoNightDayEndTime", 480);
        localObject3 = localThemeInfo;
        autoNightSunsetTime = localSharedPreferences.getInt("autoNightSunsetTime", 1320);
        localObject3 = localThemeInfo;
        autoNightSunriseTime = localSharedPreferences.getInt("autoNightSunriseTime", 480);
        localObject3 = localThemeInfo;
        autoNightCityName = localSharedPreferences.getString("autoNightCityName", "");
        localObject3 = localThemeInfo;
        long l = localSharedPreferences.getLong("autoNightLocationLatitude3", 10000L);
        if (l == 10000L) {
          continue;
        }
        localObject3 = localThemeInfo;
        autoNightLocationLatitude = Double.longBitsToDouble(l);
        localObject3 = localThemeInfo;
        l = localSharedPreferences.getLong("autoNightLocationLongitude3", 10000L);
        if (l == 10000L) {
          continue;
        }
        localObject3 = localThemeInfo;
        autoNightLocationLongitude = Double.longBitsToDouble(l);
      }
      catch (Exception localException2)
      {
        SharedPreferences localSharedPreferences;
        FileLog.e(localException2);
        Object localObject2 = localObject3;
        continue;
        localObject3 = localObject2;
        autoNightLocationLongitude = 10000.0D;
        continue;
        currentDayTheme = (ThemeInfo)localObject2;
        continue;
      }
      localObject3 = localThemeInfo;
      autoNightLastSunCheckDay = localSharedPreferences.getInt("autoNightLastSunCheckDay", -1);
      if (localThemeInfo != null) {
        continue;
      }
      localThemeInfo = defaultTheme;
      applyTheme(localThemeInfo, false, false, false);
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run() {}
      });
      ambientSensorListener = new SensorEventListener()
      {
        public void onAccuracyChanged(Sensor paramAnonymousSensor, int paramAnonymousInt) {}
        
        public void onSensorChanged(SensorEvent paramAnonymousSensorEvent)
        {
          float f2 = paramAnonymousSensorEvent.values[0];
          float f1 = f2;
          if (f2 <= 0.0F) {
            f1 = 0.1F;
          }
          if ((ApplicationLoader.mainInterfacePaused) || (!ApplicationLoader.isScreenOn)) {}
          label127:
          do
          {
            return;
            if (f1 > 500.0F) {
              Theme.access$502(1.0F);
            }
            for (;;)
            {
              if (Theme.lastBrightnessValue > Theme.autoNightBrighnessThreshold) {
                break label127;
              }
              if (MediaController.getInstance().isRecordingOrListeningByProximity()) {
                break;
              }
              if (Theme.switchDayRunnableScheduled)
              {
                Theme.access$002(false);
                AndroidUtilities.cancelRunOnUIThread(Theme.switchDayBrightnessRunnable);
              }
              if (Theme.switchNightRunnableScheduled) {
                break;
              }
              Theme.access$202(true);
              AndroidUtilities.runOnUIThread(Theme.switchNightBrightnessRunnable, Theme.access$800());
              return;
              Theme.access$502((float)Math.ceil(9.932299613952637D * Math.log(f1) + 27.05900001525879D) / 100.0F);
            }
            if (Theme.switchNightRunnableScheduled)
            {
              Theme.access$202(false);
              AndroidUtilities.cancelRunOnUIThread(Theme.switchNightBrightnessRunnable);
            }
          } while (Theme.switchDayRunnableScheduled);
          Theme.access$002(true);
          AndroidUtilities.runOnUIThread(Theme.switchDayBrightnessRunnable, Theme.access$800());
        }
      };
      return;
      localObject3 = localThemeInfo.getString("themes", null);
      if (!TextUtils.isEmpty((CharSequence)localObject3))
      {
        localObject3 = ((String)localObject3).split("&");
        i = 0;
        if (i < localObject3.length)
        {
          localObject4 = ThemeInfo.createWithString(localObject3[i]);
          if (localObject4 != null)
          {
            otherThemes.add(localObject4);
            themes.add(localObject4);
            themesDict.put(((ThemeInfo)localObject4).name, localObject4);
          }
          i += 1;
          continue;
        }
      }
      saveOtherThemes();
      localThemeInfo.edit().remove("themes").commit();
      break;
      localObject3 = localThemeInfo;
      autoNightLocationLatitude = 10000.0D;
    }
  }
  
  public static void applyChatServiceMessageColor()
  {
    if (chat_actionBackgroundPaint == null) {}
    Object localObject2;
    do
    {
      return;
      localObject2 = (Integer)currentColors.get("chat_serviceBackground");
      Integer localInteger = (Integer)currentColors.get("chat_serviceBackgroundSelected");
      Object localObject1 = localObject2;
      if (localObject2 == null) {
        localObject1 = Integer.valueOf(serviceMessageColor);
      }
      localObject2 = localInteger;
      if (localInteger == null) {
        localObject2 = Integer.valueOf(serviceSelectedMessageColor);
      }
      if (currentColor != ((Integer)localObject1).intValue())
      {
        chat_actionBackgroundPaint.setColor(((Integer)localObject1).intValue());
        colorFilter = new PorterDuffColorFilter(((Integer)localObject1).intValue(), PorterDuff.Mode.MULTIPLY);
        currentColor = ((Integer)localObject1).intValue();
        if (chat_cornerOuter[0] != null)
        {
          int i = 0;
          while (i < 4)
          {
            chat_cornerOuter[i].setColorFilter(colorFilter);
            chat_cornerInner[i].setColorFilter(colorFilter);
            i += 1;
          }
        }
      }
    } while (currentSelectedColor == ((Integer)localObject2).intValue());
    currentSelectedColor = ((Integer)localObject2).intValue();
    colorPressedFilter = new PorterDuffColorFilter(((Integer)localObject2).intValue(), PorterDuff.Mode.MULTIPLY);
  }
  
  public static void applyChatTheme(boolean paramBoolean)
  {
    if (chat_msgTextPaint == null) {}
    while ((chat_msgInDrawable == null) || (paramBoolean)) {
      return;
    }
    chat_gamePaint.setColor(getColor("chat_previewGameText"));
    chat_durationPaint.setColor(getColor("chat_previewDurationText"));
    chat_botButtonPaint.setColor(getColor("chat_botButtonText"));
    chat_urlPaint.setColor(getColor("chat_linkSelectBackground"));
    chat_botProgressPaint.setColor(getColor("chat_botProgress"));
    chat_deleteProgressPaint.setColor(getColor("chat_secretTimeText"));
    chat_textSearchSelectionPaint.setColor(getColor("chat_textSelectBackground"));
    chat_msgErrorPaint.setColor(getColor("chat_sentError"));
    chat_statusPaint.setColor(getColor("actionBarDefaultSubtitle"));
    chat_statusRecordPaint.setColor(getColor("actionBarDefaultSubtitle"));
    chat_actionTextPaint.setColor(getColor("chat_serviceText"));
    chat_actionTextPaint.linkColor = getColor("chat_serviceLink");
    chat_contextResult_titleTextPaint.setColor(getColor("windowBackgroundWhiteBlackText"));
    chat_composeBackgroundPaint.setColor(getColor("chat_messagePanelBackground"));
    chat_timeBackgroundPaint.setColor(getColor("chat_mediaTimeBackground"));
    setDrawableColorByKey(chat_msgInDrawable, "chat_inBubble");
    setDrawableColorByKey(chat_msgInSelectedDrawable, "chat_inBubbleSelected");
    setDrawableColorByKey(chat_msgInShadowDrawable, "chat_inBubbleShadow");
    setDrawableColorByKey(chat_msgOutDrawable, "chat_outBubble");
    setDrawableColorByKey(chat_msgOutSelectedDrawable, "chat_outBubbleSelected");
    setDrawableColorByKey(chat_msgOutShadowDrawable, "chat_outBubbleShadow");
    setDrawableColorByKey(chat_msgInMediaDrawable, "chat_inBubble");
    setDrawableColorByKey(chat_msgInMediaSelectedDrawable, "chat_inBubbleSelected");
    setDrawableColorByKey(chat_msgInMediaShadowDrawable, "chat_inBubbleShadow");
    setDrawableColorByKey(chat_msgOutMediaDrawable, "chat_outBubble");
    setDrawableColorByKey(chat_msgOutMediaSelectedDrawable, "chat_outBubbleSelected");
    setDrawableColorByKey(chat_msgOutMediaShadowDrawable, "chat_outBubbleShadow");
    setDrawableColorByKey(chat_msgOutCheckDrawable, "chat_outSentCheck");
    setDrawableColorByKey(chat_msgOutCheckSelectedDrawable, "chat_outSentCheckSelected");
    setDrawableColorByKey(chat_msgOutHalfCheckDrawable, "chat_outSentCheck");
    setDrawableColorByKey(chat_msgOutHalfCheckSelectedDrawable, "chat_outSentCheckSelected");
    setDrawableColorByKey(chat_msgOutClockDrawable, "chat_outSentClock");
    setDrawableColorByKey(chat_msgOutSelectedClockDrawable, "chat_outSentClockSelected");
    setDrawableColorByKey(chat_msgInClockDrawable, "chat_inSentClock");
    setDrawableColorByKey(chat_msgInSelectedClockDrawable, "chat_inSentClockSelected");
    setDrawableColorByKey(chat_msgMediaCheckDrawable, "chat_mediaSentCheck");
    setDrawableColorByKey(chat_msgMediaHalfCheckDrawable, "chat_mediaSentCheck");
    setDrawableColorByKey(chat_msgMediaClockDrawable, "chat_mediaSentClock");
    setDrawableColorByKey(chat_msgStickerCheckDrawable, "chat_serviceText");
    setDrawableColorByKey(chat_msgStickerHalfCheckDrawable, "chat_serviceText");
    setDrawableColorByKey(chat_msgStickerClockDrawable, "chat_serviceText");
    setDrawableColorByKey(chat_msgStickerViewsDrawable, "chat_serviceText");
    setDrawableColorByKey(chat_shareIconDrawable, "chat_serviceIcon");
    setDrawableColorByKey(chat_replyIconDrawable, "chat_serviceIcon");
    setDrawableColorByKey(chat_goIconDrawable, "chat_serviceIcon");
    setDrawableColorByKey(chat_botInlineDrawable, "chat_serviceIcon");
    setDrawableColorByKey(chat_botLinkDrawalbe, "chat_serviceIcon");
    setDrawableColorByKey(chat_msgInViewsDrawable, "chat_inViews");
    setDrawableColorByKey(chat_msgInViewsSelectedDrawable, "chat_inViewsSelected");
    setDrawableColorByKey(chat_msgOutViewsDrawable, "chat_outViews");
    setDrawableColorByKey(chat_msgOutViewsSelectedDrawable, "chat_outViewsSelected");
    setDrawableColorByKey(chat_msgMediaViewsDrawable, "chat_mediaViews");
    setDrawableColorByKey(chat_msgInMenuDrawable, "chat_inMenu");
    setDrawableColorByKey(chat_msgInMenuSelectedDrawable, "chat_inMenuSelected");
    setDrawableColorByKey(chat_msgOutMenuDrawable, "chat_outMenu");
    setDrawableColorByKey(chat_msgOutMenuSelectedDrawable, "chat_outMenuSelected");
    setDrawableColorByKey(chat_msgMediaMenuDrawable, "chat_mediaMenu");
    setDrawableColorByKey(chat_msgOutInstantDrawable, "chat_outInstant");
    setDrawableColorByKey(chat_msgInInstantDrawable, "chat_inInstant");
    setDrawableColorByKey(chat_msgErrorDrawable, "chat_sentErrorIcon");
    setDrawableColorByKey(chat_muteIconDrawable, "chat_muteIcon");
    setDrawableColorByKey(chat_lockIconDrawable, "chat_lockIcon");
    setDrawableColorByKey(chat_msgBroadcastDrawable, "chat_outBroadcast");
    setDrawableColorByKey(chat_msgBroadcastMediaDrawable, "chat_mediaBroadcast");
    setDrawableColorByKey(chat_inlineResultFile, "chat_inlineResultIcon");
    setDrawableColorByKey(chat_inlineResultAudio, "chat_inlineResultIcon");
    setDrawableColorByKey(chat_inlineResultLocation, "chat_inlineResultIcon");
    setDrawableColorByKey(chat_msgInCallDrawable, "chat_inInstant");
    setDrawableColorByKey(chat_msgInCallSelectedDrawable, "chat_inInstantSelected");
    setDrawableColorByKey(chat_msgOutCallDrawable, "chat_outInstant");
    setDrawableColorByKey(chat_msgOutCallSelectedDrawable, "chat_outInstantSelected");
    setDrawableColorByKey(chat_msgCallUpRedDrawable, "calls_callReceivedRedIcon");
    setDrawableColorByKey(chat_msgCallUpGreenDrawable, "calls_callReceivedGreenIcon");
    setDrawableColorByKey(chat_msgCallDownRedDrawable, "calls_callReceivedRedIcon");
    setDrawableColorByKey(chat_msgCallDownGreenDrawable, "calls_callReceivedGreenIcon");
    int i = 0;
    while (i < 2)
    {
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[i][0], getColor("chat_outLoader"), false);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[i][0], getColor("chat_outBubble"), true);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[i][1], getColor("chat_outLoaderSelected"), false);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[i][1], getColor("chat_outBubbleSelected"), true);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[(i + 2)][0], getColor("chat_inLoader"), false);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[(i + 2)][0], getColor("chat_inBubble"), true);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[(i + 2)][1], getColor("chat_inLoaderSelected"), false);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[(i + 2)][1], getColor("chat_inBubbleSelected"), true);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[(i + 4)][0], getColor("chat_mediaLoaderPhoto"), false);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[(i + 4)][0], getColor("chat_mediaLoaderPhotoIcon"), true);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[(i + 4)][1], getColor("chat_mediaLoaderPhotoSelected"), false);
      setCombinedDrawableColor(chat_fileMiniStatesDrawable[(i + 4)][1], getColor("chat_mediaLoaderPhotoIconSelected"), true);
      i += 1;
    }
    i = 0;
    while (i < 5)
    {
      setCombinedDrawableColor(chat_fileStatesDrawable[i][0], getColor("chat_outLoader"), false);
      setCombinedDrawableColor(chat_fileStatesDrawable[i][0], getColor("chat_outBubble"), true);
      setCombinedDrawableColor(chat_fileStatesDrawable[i][1], getColor("chat_outLoaderSelected"), false);
      setCombinedDrawableColor(chat_fileStatesDrawable[i][1], getColor("chat_outBubbleSelected"), true);
      setCombinedDrawableColor(chat_fileStatesDrawable[(i + 5)][0], getColor("chat_inLoader"), false);
      setCombinedDrawableColor(chat_fileStatesDrawable[(i + 5)][0], getColor("chat_inBubble"), true);
      setCombinedDrawableColor(chat_fileStatesDrawable[(i + 5)][1], getColor("chat_inLoaderSelected"), false);
      setCombinedDrawableColor(chat_fileStatesDrawable[(i + 5)][1], getColor("chat_inBubbleSelected"), true);
      i += 1;
    }
    i = 0;
    while (i < 4)
    {
      setCombinedDrawableColor(chat_photoStatesDrawables[i][0], getColor("chat_mediaLoaderPhoto"), false);
      setCombinedDrawableColor(chat_photoStatesDrawables[i][0], getColor("chat_mediaLoaderPhotoIcon"), true);
      setCombinedDrawableColor(chat_photoStatesDrawables[i][1], getColor("chat_mediaLoaderPhotoSelected"), false);
      setCombinedDrawableColor(chat_photoStatesDrawables[i][1], getColor("chat_mediaLoaderPhotoIconSelected"), true);
      i += 1;
    }
    i = 0;
    while (i < 2)
    {
      setCombinedDrawableColor(chat_photoStatesDrawables[(i + 7)][0], getColor("chat_outLoaderPhoto"), false);
      setCombinedDrawableColor(chat_photoStatesDrawables[(i + 7)][0], getColor("chat_outLoaderPhotoIcon"), true);
      setCombinedDrawableColor(chat_photoStatesDrawables[(i + 7)][1], getColor("chat_outLoaderPhotoSelected"), false);
      setCombinedDrawableColor(chat_photoStatesDrawables[(i + 7)][1], getColor("chat_outLoaderPhotoIconSelected"), true);
      setCombinedDrawableColor(chat_photoStatesDrawables[(i + 10)][0], getColor("chat_inLoaderPhoto"), false);
      setCombinedDrawableColor(chat_photoStatesDrawables[(i + 10)][0], getColor("chat_inLoaderPhotoIcon"), true);
      setCombinedDrawableColor(chat_photoStatesDrawables[(i + 10)][1], getColor("chat_inLoaderPhotoSelected"), false);
      setCombinedDrawableColor(chat_photoStatesDrawables[(i + 10)][1], getColor("chat_inLoaderPhotoIconSelected"), true);
      i += 1;
    }
    setDrawableColorByKey(chat_photoStatesDrawables[9][0], "chat_outFileIcon");
    setDrawableColorByKey(chat_photoStatesDrawables[9][1], "chat_outFileSelectedIcon");
    setDrawableColorByKey(chat_photoStatesDrawables[12][0], "chat_inFileIcon");
    setDrawableColorByKey(chat_photoStatesDrawables[12][1], "chat_inFileSelectedIcon");
    setCombinedDrawableColor(chat_contactDrawable[0], getColor("chat_inContactBackground"), false);
    setCombinedDrawableColor(chat_contactDrawable[0], getColor("chat_inContactIcon"), true);
    setCombinedDrawableColor(chat_contactDrawable[1], getColor("chat_outContactBackground"), false);
    setCombinedDrawableColor(chat_contactDrawable[1], getColor("chat_outContactIcon"), true);
    setCombinedDrawableColor(chat_locationDrawable[0], getColor("chat_inLocationBackground"), false);
    setCombinedDrawableColor(chat_locationDrawable[0], getColor("chat_inLocationIcon"), true);
    setCombinedDrawableColor(chat_locationDrawable[1], getColor("chat_outLocationBackground"), false);
    setCombinedDrawableColor(chat_locationDrawable[1], getColor("chat_outLocationIcon"), true);
    setDrawableColorByKey(chat_composeShadowDrawable, "chat_messagePanelShadow");
    applyChatServiceMessageColor();
  }
  
  public static void applyCommonTheme()
  {
    if (dividerPaint == null) {
      return;
    }
    dividerPaint.setColor(getColor("divider"));
    linkSelectionPaint.setColor(getColor("windowBackgroundWhiteLinkSelection"));
    setDrawableColorByKey(avatar_broadcastDrawable, "avatar_text");
    setDrawableColorByKey(avatar_savedDrawable, "avatar_text");
    setDrawableColorByKey(avatar_photoDrawable, "avatar_text");
  }
  
  private static void applyDayNightThemeMaybe(boolean paramBoolean)
  {
    if (paramBoolean) {
      if (currentTheme != currentNightTheme)
      {
        lastThemeSwitchTime = SystemClock.uptimeMillis();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, new Object[] { currentNightTheme });
      }
    }
    while (currentTheme == currentDayTheme) {
      return;
    }
    lastThemeSwitchTime = SystemClock.uptimeMillis();
    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, new Object[] { currentDayTheme });
  }
  
  public static void applyDialogsTheme()
  {
    if (dialogs_namePaint == null) {
      return;
    }
    dialogs_namePaint.setColor(getColor("chats_name"));
    dialogs_nameEncryptedPaint.setColor(getColor("chats_secretName"));
    TextPaint localTextPaint1 = dialogs_messagePaint;
    TextPaint localTextPaint2 = dialogs_messagePaint;
    int i = getColor("chats_message");
    localTextPaint2.linkColor = i;
    localTextPaint1.setColor(i);
    dialogs_tabletSeletedPaint.setColor(getColor("chats_tabletSelectedOverlay"));
    dialogs_pinnedPaint.setColor(getColor("chats_pinnedOverlay"));
    dialogs_timePaint.setColor(getColor("chats_date"));
    dialogs_countTextPaint.setColor(getColor("chats_unreadCounterText"));
    dialogs_messagePrintingPaint.setColor(getColor("chats_actionMessage"));
    dialogs_countPaint.setColor(getColor("chats_unreadCounter"));
    dialogs_countGrayPaint.setColor(getColor("chats_unreadCounterMuted"));
    dialogs_errorPaint.setColor(getColor("chats_sentError"));
    dialogs_onlinePaint.setColor(getColor("windowBackgroundWhiteBlueText3"));
    dialogs_offlinePaint.setColor(getColor("windowBackgroundWhiteGrayText3"));
    setDrawableColorByKey(dialogs_lockDrawable, "chats_secretIcon");
    setDrawableColorByKey(dialogs_checkDrawable, "chats_sentCheck");
    setDrawableColorByKey(dialogs_halfCheckDrawable, "chats_sentCheck");
    setDrawableColorByKey(dialogs_clockDrawable, "chats_sentClock");
    setDrawableColorByKey(dialogs_errorDrawable, "chats_sentErrorIcon");
    setDrawableColorByKey(dialogs_groupDrawable, "chats_nameIcon");
    setDrawableColorByKey(dialogs_broadcastDrawable, "chats_nameIcon");
    setDrawableColorByKey(dialogs_botDrawable, "chats_nameIcon");
    setDrawableColorByKey(dialogs_pinnedDrawable, "chats_pinnedIcon");
    setDrawableColorByKey(dialogs_muteDrawable, "chats_muteIcon");
    setDrawableColorByKey(dialogs_verifiedDrawable, "chats_verifiedBackground");
    setDrawableColorByKey(dialogs_verifiedCheckDrawable, "chats_verifiedCheck");
  }
  
  public static void applyPreviousTheme()
  {
    if (previousTheme == null) {
      return;
    }
    applyTheme(previousTheme, true, false, false);
    previousTheme = null;
    checkAutoNightThemeConditions();
  }
  
  public static void applyProfileTheme()
  {
    if (profile_verifiedDrawable == null) {
      return;
    }
    profile_aboutTextPaint.setColor(getColor("windowBackgroundWhiteBlackText"));
    profile_aboutTextPaint.linkColor = getColor("windowBackgroundWhiteLinkText");
    setDrawableColorByKey(profile_verifiedDrawable, "profile_verifiedBackground");
    setDrawableColorByKey(profile_verifiedCheckDrawable, "profile_verifiedCheck");
  }
  
  public static void applyTheme(ThemeInfo paramThemeInfo)
  {
    applyTheme(paramThemeInfo, true, true, false);
  }
  
  public static void applyTheme(ThemeInfo paramThemeInfo, boolean paramBoolean)
  {
    applyTheme(paramThemeInfo, true, true, paramBoolean);
  }
  
  public static void applyTheme(ThemeInfo paramThemeInfo, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    if (paramThemeInfo == null) {
      return;
    }
    Object localObject = ThemeEditorView.getInstance();
    if (localObject != null) {
      ((ThemeEditorView)localObject).destroy();
    }
    for (;;)
    {
      try
      {
        if ((paramThemeInfo.pathToFile == null) && (paramThemeInfo.assetName == null)) {
          break label177;
        }
        if ((!paramBoolean3) && (paramBoolean1))
        {
          localObject = MessagesController.getGlobalMainSettings().edit();
          ((SharedPreferences.Editor)localObject).putString("theme", paramThemeInfo.name);
          if (paramBoolean2) {
            ((SharedPreferences.Editor)localObject).remove("overrideThemeWallpaper");
          }
          ((SharedPreferences.Editor)localObject).commit();
        }
        if (paramThemeInfo.assetName != null)
        {
          currentColors = getThemeFileValues(null, paramThemeInfo.assetName);
          currentTheme = paramThemeInfo;
          if (!paramBoolean3) {
            currentDayTheme = currentTheme;
          }
          reloadWallpaper();
          applyCommonTheme();
          applyDialogsTheme();
          applyProfileTheme();
          applyChatTheme(false);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme, new Object[] { Boolean.valueOf(this.val$nightTheme) });
            }
          });
          return;
        }
      }
      catch (Exception paramThemeInfo)
      {
        FileLog.e(paramThemeInfo);
        return;
      }
      currentColors = getThemeFileValues(new File(paramThemeInfo.pathToFile), null);
      continue;
      label177:
      if ((!paramBoolean3) && (paramBoolean1))
      {
        localObject = MessagesController.getGlobalMainSettings().edit();
        ((SharedPreferences.Editor)localObject).remove("theme");
        if (paramBoolean2) {
          ((SharedPreferences.Editor)localObject).remove("overrideThemeWallpaper");
        }
        ((SharedPreferences.Editor)localObject).commit();
      }
      currentColors.clear();
      wallpaper = null;
      themedWallpaper = null;
    }
  }
  
  public static ThemeInfo applyThemeFile(File paramFile, String paramString, boolean paramBoolean)
  {
    boolean bool = true;
    for (;;)
    {
      try
      {
        if ((paramString.equals("Default")) || (paramString.equals("Dark")) || (paramString.equals("Blue"))) {
          break label185;
        }
        File localFile = new File(ApplicationLoader.getFilesDirFixed(), paramString);
        if (!AndroidUtilities.copyFile(paramFile, localFile)) {
          return null;
        }
        int i = 0;
        ThemeInfo localThemeInfo = (ThemeInfo)themesDict.get(paramString);
        paramFile = localThemeInfo;
        if (localThemeInfo == null)
        {
          i = 1;
          paramFile = new ThemeInfo();
          paramFile.name = paramString;
          paramFile.pathToFile = localFile.getAbsolutePath();
        }
        if (paramBoolean) {
          break label171;
        }
        previousTheme = null;
        if (i == 0) {
          break label187;
        }
        themes.add(paramFile);
        themesDict.put(paramFile.name, paramFile);
        otherThemes.add(paramFile);
        sortThemes();
        saveOtherThemes();
      }
      catch (Exception paramFile)
      {
        FileLog.e(paramFile);
        return null;
      }
      applyTheme(paramFile, paramBoolean, true, false);
      return paramFile;
      label171:
      previousTheme = currentTheme;
      label185:
      label187:
      while (paramBoolean)
      {
        paramBoolean = false;
        break;
        return null;
      }
      paramBoolean = bool;
    }
  }
  
  private static void calcBackgroundColor(Drawable paramDrawable, int paramInt)
  {
    if (paramInt != 2)
    {
      paramDrawable = AndroidUtilities.calcDrawableColor(paramDrawable);
      serviceMessageColor = paramDrawable[0];
      serviceSelectedMessageColor = paramDrawable[1];
    }
  }
  
  public static boolean canStartHolidayAnimation()
  {
    return canStartHolidayAnimation;
  }
  
  public static void checkAutoNightThemeConditions()
  {
    checkAutoNightThemeConditions(false);
  }
  
  public static void checkAutoNightThemeConditions(boolean paramBoolean)
  {
    if (previousTheme != null) {
      return;
    }
    if (paramBoolean)
    {
      if (switchNightRunnableScheduled)
      {
        switchNightRunnableScheduled = false;
        AndroidUtilities.cancelRunOnUIThread(switchNightBrightnessRunnable);
      }
      if (switchDayRunnableScheduled)
      {
        switchDayRunnableScheduled = false;
        AndroidUtilities.cancelRunOnUIThread(switchDayBrightnessRunnable);
      }
    }
    if (selectedAutoNightType != 2)
    {
      if (switchNightRunnableScheduled)
      {
        switchNightRunnableScheduled = false;
        AndroidUtilities.cancelRunOnUIThread(switchNightBrightnessRunnable);
      }
      if (switchDayRunnableScheduled)
      {
        switchDayRunnableScheduled = false;
        AndroidUtilities.cancelRunOnUIThread(switchDayBrightnessRunnable);
      }
      if (lightSensorRegistered)
      {
        lastBrightnessValue = 1.0F;
        sensorManager.unregisterListener(ambientSensorListener, lightSensor);
        lightSensorRegistered = false;
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("light sensor unregistered");
        }
      }
    }
    int i = 0;
    int k;
    int j;
    if (selectedAutoNightType == 1)
    {
      Object localObject = Calendar.getInstance();
      ((Calendar)localObject).setTimeInMillis(System.currentTimeMillis());
      k = ((Calendar)localObject).get(11) * 60 + ((Calendar)localObject).get(12);
      if (autoNightScheduleByLocation)
      {
        i = ((Calendar)localObject).get(5);
        if ((autoNightLastSunCheckDay != i) && (autoNightLocationLatitude != 10000.0D) && (autoNightLocationLongitude != 10000.0D))
        {
          localObject = SunDate.calculateSunriseSunset(autoNightLocationLatitude, autoNightLocationLongitude);
          autoNightSunriseTime = localObject[0];
          autoNightSunsetTime = localObject[1];
          autoNightLastSunCheckDay = i;
          saveAutoNightThemeConfig();
        }
        j = autoNightSunsetTime;
        i = autoNightSunriseTime;
        label241:
        if (j >= i) {
          break label300;
        }
        if ((j > k) || (k > i)) {
          break label295;
        }
        i = 2;
        label258:
        if (i != 0) {
          if (i != 2) {
            break label456;
          }
        }
      }
    }
    label295:
    label300:
    label456:
    for (boolean bool = true;; bool = false)
    {
      applyDayNightThemeMaybe(bool);
      if (!paramBoolean) {
        break;
      }
      lastThemeSwitchTime = 0L;
      return;
      j = autoNightDayStartTime;
      i = autoNightDayEndTime;
      break label241;
      i = 1;
      break label258;
      if (((j <= k) && (k <= 1440)) || ((k >= 0) && (k <= i)))
      {
        i = 2;
        break label258;
      }
      i = 1;
      break label258;
      if (selectedAutoNightType == 2)
      {
        if (lightSensor == null)
        {
          sensorManager = (SensorManager)ApplicationLoader.applicationContext.getSystemService("sensor");
          lightSensor = sensorManager.getDefaultSensor(5);
        }
        if ((!lightSensorRegistered) && (lightSensor != null))
        {
          sensorManager.registerListener(ambientSensorListener, lightSensor, 500000);
          lightSensorRegistered = true;
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("light sensor registered");
          }
        }
        if (lastBrightnessValue <= autoNightBrighnessThreshold)
        {
          if (switchNightRunnableScheduled) {
            break label258;
          }
          i = 2;
          break label258;
        }
        if (switchDayRunnableScheduled) {
          break label258;
        }
        i = 1;
        break label258;
      }
      if (selectedAutoNightType != 0) {
        break label258;
      }
      i = 1;
      break label258;
    }
  }
  
  public static void createChatResources(Context paramContext, boolean paramBoolean)
  {
    Object localObject2;
    synchronized (sync)
    {
      if (chat_msgTextPaint == null)
      {
        chat_msgTextPaint = new TextPaint(1);
        chat_msgGameTextPaint = new TextPaint(1);
        chat_msgTextPaintOneEmoji = new TextPaint(1);
        chat_msgTextPaintTwoEmoji = new TextPaint(1);
        chat_msgTextPaintThreeEmoji = new TextPaint(1);
        chat_msgBotButtonPaint = new TextPaint(1);
        chat_msgBotButtonPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      }
      if ((!paramBoolean) && (chat_msgInDrawable == null))
      {
        chat_infoPaint = new TextPaint(1);
        chat_docNamePaint = new TextPaint(1);
        chat_docNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_docBackPaint = new Paint(1);
        chat_deleteProgressPaint = new Paint(1);
        chat_botProgressPaint = new Paint(1);
        chat_botProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        chat_botProgressPaint.setStyle(Paint.Style.STROKE);
        chat_locationTitlePaint = new TextPaint(1);
        chat_locationTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_locationAddressPaint = new TextPaint(1);
        chat_urlPaint = new Paint();
        chat_textSearchSelectionPaint = new Paint();
        chat_radialProgressPaint = new Paint(1);
        chat_radialProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        chat_radialProgressPaint.setStyle(Paint.Style.STROKE);
        chat_radialProgressPaint.setColor(-1610612737);
        chat_radialProgress2Paint = new Paint(1);
        chat_radialProgress2Paint.setStrokeCap(Paint.Cap.ROUND);
        chat_radialProgress2Paint.setStyle(Paint.Style.STROKE);
        chat_audioTimePaint = new TextPaint(1);
        chat_livePaint = new TextPaint(1);
        chat_livePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_audioTitlePaint = new TextPaint(1);
        chat_audioTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_audioPerformerPaint = new TextPaint(1);
        chat_botButtonPaint = new TextPaint(1);
        chat_botButtonPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_contactNamePaint = new TextPaint(1);
        chat_contactNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_contactPhonePaint = new TextPaint(1);
        chat_durationPaint = new TextPaint(1);
        chat_gamePaint = new TextPaint(1);
        chat_gamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_shipmentPaint = new TextPaint(1);
        chat_timePaint = new TextPaint(1);
        chat_adminPaint = new TextPaint(1);
        chat_namePaint = new TextPaint(1);
        chat_namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_forwardNamePaint = new TextPaint(1);
        chat_replyNamePaint = new TextPaint(1);
        chat_replyNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_replyTextPaint = new TextPaint(1);
        chat_instantViewPaint = new TextPaint(1);
        chat_instantViewPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_instantViewRectPaint = new Paint(1);
        chat_instantViewRectPaint.setStyle(Paint.Style.STROKE);
        chat_replyLinePaint = new Paint();
        chat_msgErrorPaint = new Paint(1);
        chat_statusPaint = new Paint(1);
        chat_statusRecordPaint = new Paint(1);
        chat_statusRecordPaint.setStyle(Paint.Style.STROKE);
        chat_statusRecordPaint.setStrokeCap(Paint.Cap.ROUND);
        chat_actionTextPaint = new TextPaint(1);
        chat_actionTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_actionBackgroundPaint = new Paint(1);
        chat_timeBackgroundPaint = new Paint(1);
        chat_contextResult_titleTextPaint = new TextPaint(1);
        chat_contextResult_titleTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        chat_contextResult_descriptionTextPaint = new TextPaint(1);
        chat_composeBackgroundPaint = new Paint();
        ??? = paramContext.getResources();
        chat_msgInDrawable = ((Resources)???).getDrawable(2131165513).mutate();
        chat_msgInSelectedDrawable = ((Resources)???).getDrawable(2131165513).mutate();
        chat_msgOutDrawable = ((Resources)???).getDrawable(2131165517).mutate();
        chat_msgOutSelectedDrawable = ((Resources)???).getDrawable(2131165517).mutate();
        chat_msgInMediaDrawable = ((Resources)???).getDrawable(2131165523).mutate();
        chat_msgInMediaSelectedDrawable = ((Resources)???).getDrawable(2131165523).mutate();
        chat_msgOutMediaDrawable = ((Resources)???).getDrawable(2131165523).mutate();
        chat_msgOutMediaSelectedDrawable = ((Resources)???).getDrawable(2131165523).mutate();
        chat_msgOutCheckDrawable = ((Resources)???).getDrawable(2131165509).mutate();
        chat_msgOutCheckSelectedDrawable = ((Resources)???).getDrawable(2131165509).mutate();
        chat_msgMediaCheckDrawable = ((Resources)???).getDrawable(2131165509).mutate();
        chat_msgStickerCheckDrawable = ((Resources)???).getDrawable(2131165509).mutate();
        chat_msgOutHalfCheckDrawable = ((Resources)???).getDrawable(2131165512).mutate();
        chat_msgOutHalfCheckSelectedDrawable = ((Resources)???).getDrawable(2131165512).mutate();
        chat_msgMediaHalfCheckDrawable = ((Resources)???).getDrawable(2131165512).mutate();
        chat_msgStickerHalfCheckDrawable = ((Resources)???).getDrawable(2131165512).mutate();
        chat_msgOutClockDrawable = ((Resources)???).getDrawable(2131165510).mutate();
        chat_msgOutSelectedClockDrawable = ((Resources)???).getDrawable(2131165510).mutate();
        chat_msgInClockDrawable = ((Resources)???).getDrawable(2131165510).mutate();
        chat_msgInSelectedClockDrawable = ((Resources)???).getDrawable(2131165510).mutate();
        chat_msgMediaClockDrawable = ((Resources)???).getDrawable(2131165510).mutate();
        chat_msgStickerClockDrawable = ((Resources)???).getDrawable(2131165510).mutate();
        chat_msgInViewsDrawable = ((Resources)???).getDrawable(2131165531).mutate();
        chat_msgInViewsSelectedDrawable = ((Resources)???).getDrawable(2131165531).mutate();
        chat_msgOutViewsDrawable = ((Resources)???).getDrawable(2131165531).mutate();
        chat_msgOutViewsSelectedDrawable = ((Resources)???).getDrawable(2131165531).mutate();
        chat_msgMediaViewsDrawable = ((Resources)???).getDrawable(2131165531).mutate();
        chat_msgStickerViewsDrawable = ((Resources)???).getDrawable(2131165531).mutate();
        chat_msgInMenuDrawable = ((Resources)???).getDrawable(2131165508).mutate();
        chat_msgInMenuSelectedDrawable = ((Resources)???).getDrawable(2131165508).mutate();
        chat_msgOutMenuDrawable = ((Resources)???).getDrawable(2131165508).mutate();
        chat_msgOutMenuSelectedDrawable = ((Resources)???).getDrawable(2131165508).mutate();
        chat_msgMediaMenuDrawable = ((Resources)???).getDrawable(2131165692);
        chat_msgInInstantDrawable = ((Resources)???).getDrawable(2131165515).mutate();
        chat_msgOutInstantDrawable = ((Resources)???).getDrawable(2131165515).mutate();
        chat_msgErrorDrawable = ((Resources)???).getDrawable(2131165532);
        chat_muteIconDrawable = ((Resources)???).getDrawable(2131165461).mutate();
        chat_lockIconDrawable = ((Resources)???).getDrawable(2131165385);
        chat_msgBroadcastDrawable = ((Resources)???).getDrawable(2131165254).mutate();
        chat_msgBroadcastMediaDrawable = ((Resources)???).getDrawable(2131165254).mutate();
        chat_msgInCallDrawable = ((Resources)???).getDrawable(2131165372).mutate();
        chat_msgInCallSelectedDrawable = ((Resources)???).getDrawable(2131165372).mutate();
        chat_msgOutCallDrawable = ((Resources)???).getDrawable(2131165372).mutate();
        chat_msgOutCallSelectedDrawable = ((Resources)???).getDrawable(2131165372).mutate();
        chat_msgCallUpRedDrawable = ((Resources)???).getDrawable(2131165368).mutate();
        chat_msgCallUpGreenDrawable = ((Resources)???).getDrawable(2131165368).mutate();
        chat_msgCallDownRedDrawable = ((Resources)???).getDrawable(2131165371).mutate();
        chat_msgCallDownGreenDrawable = ((Resources)???).getDrawable(2131165371).mutate();
        chat_msgAvatarLiveLocationDrawable = ((Resources)???).getDrawable(2131165468).mutate();
        chat_inlineResultFile = ((Resources)???).getDrawable(2131165243);
        chat_inlineResultAudio = ((Resources)???).getDrawable(2131165249);
        chat_inlineResultLocation = ((Resources)???).getDrawable(2131165248);
        chat_msgInShadowDrawable = ((Resources)???).getDrawable(2131165514);
        chat_msgOutShadowDrawable = ((Resources)???).getDrawable(2131165518);
        chat_msgInMediaShadowDrawable = ((Resources)???).getDrawable(2131165524);
        chat_msgOutMediaShadowDrawable = ((Resources)???).getDrawable(2131165524);
        chat_botLinkDrawalbe = ((Resources)???).getDrawable(2131165247);
        chat_botInlineDrawable = ((Resources)???).getDrawable(2131165246);
        chat_systemDrawable = ((Resources)???).getDrawable(2131165671);
        chat_contextResult_shadowUnderSwitchDrawable = ((Resources)???).getDrawable(2131165342).mutate();
        chat_attachButtonDrawables[0] = ((Resources)???).getDrawable(2131165198);
        chat_attachButtonDrawables[1] = ((Resources)???).getDrawable(2131165207);
        chat_attachButtonDrawables[2] = ((Resources)???).getDrawable(2131165221);
        chat_attachButtonDrawables[3] = ((Resources)???).getDrawable(2131165195);
        chat_attachButtonDrawables[4] = ((Resources)???).getDrawable(2131165204);
        chat_attachButtonDrawables[5] = ((Resources)???).getDrawable(2131165201);
        chat_attachButtonDrawables[6] = ((Resources)???).getDrawable(2131165214);
        chat_attachButtonDrawables[7] = ((Resources)???).getDrawable(2131165211);
        chat_cornerOuter[0] = ((Resources)???).getDrawable(2131165305);
        chat_cornerOuter[1] = ((Resources)???).getDrawable(2131165306);
        chat_cornerOuter[2] = ((Resources)???).getDrawable(2131165304);
        chat_cornerOuter[3] = ((Resources)???).getDrawable(2131165303);
        chat_cornerInner[0] = ((Resources)???).getDrawable(2131165302);
        chat_cornerInner[1] = ((Resources)???).getDrawable(2131165301);
        chat_cornerInner[2] = ((Resources)???).getDrawable(2131165300);
        chat_cornerInner[3] = ((Resources)???).getDrawable(2131165299);
        chat_shareDrawable = ((Resources)???).getDrawable(2131165639);
        chat_shareIconDrawable = ((Resources)???).getDrawable(2131165638);
        chat_replyIconDrawable = ((Resources)???).getDrawable(2131165313);
        chat_goIconDrawable = ((Resources)???).getDrawable(2131165502);
        chat_ivStatesDrawable[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), 2131165530, 1);
        chat_ivStatesDrawable[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), 2131165530, 1);
        chat_ivStatesDrawable[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), 2131165529, 1);
        chat_ivStatesDrawable[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), 2131165529, 1);
        chat_ivStatesDrawable[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), 2131165528, 1);
        chat_ivStatesDrawable[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), 2131165528, 1);
        chat_ivStatesDrawable[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), 2131165525, 2);
        chat_ivStatesDrawable[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), 2131165525, 2);
        chat_fileMiniStatesDrawable[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165222);
        chat_fileMiniStatesDrawable[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165222);
        chat_fileMiniStatesDrawable[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165223);
        chat_fileMiniStatesDrawable[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165223);
        chat_fileMiniStatesDrawable[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165222);
        chat_fileMiniStatesDrawable[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165222);
        chat_fileMiniStatesDrawable[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165223);
        chat_fileMiniStatesDrawable[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165223);
        chat_fileMiniStatesDrawable[4][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165696);
        chat_fileMiniStatesDrawable[4][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165696);
        chat_fileMiniStatesDrawable[5][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165697);
        chat_fileMiniStatesDrawable[5][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(22.0F), 2131165697);
        chat_fileStatesDrawable[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165530);
        chat_fileStatesDrawable[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165530);
        chat_fileStatesDrawable[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165529);
        chat_fileStatesDrawable[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165529);
        chat_fileStatesDrawable[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165528);
        chat_fileStatesDrawable[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165528);
        chat_fileStatesDrawable[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165526);
        chat_fileStatesDrawable[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165526);
        chat_fileStatesDrawable[4][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165525);
        chat_fileStatesDrawable[4][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165525);
        chat_fileStatesDrawable[5][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165530);
        chat_fileStatesDrawable[5][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165530);
        chat_fileStatesDrawable[6][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165529);
        chat_fileStatesDrawable[6][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165529);
        chat_fileStatesDrawable[7][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165528);
        chat_fileStatesDrawable[7][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165528);
        chat_fileStatesDrawable[8][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165526);
        chat_fileStatesDrawable[8][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165526);
        chat_fileStatesDrawable[9][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165525);
        chat_fileStatesDrawable[9][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165525);
        chat_photoStatesDrawables[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165528);
        chat_photoStatesDrawables[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165528);
        chat_photoStatesDrawables[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165525);
        chat_photoStatesDrawables[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165525);
        chat_photoStatesDrawables[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165527);
        chat_photoStatesDrawables[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165527);
        chat_photoStatesDrawables[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165530);
        chat_photoStatesDrawables[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165530);
        localObject2 = chat_photoStatesDrawables[4];
        Drawable[] arrayOfDrawable = chat_photoStatesDrawables[4];
        Drawable localDrawable = ((Resources)???).getDrawable(2131165257);
        arrayOfDrawable[1] = localDrawable;
        localObject2[0] = localDrawable;
        localObject2 = chat_photoStatesDrawables[5];
        arrayOfDrawable = chat_photoStatesDrawables[5];
        localDrawable = ((Resources)???).getDrawable(2131165274);
        arrayOfDrawable[1] = localDrawable;
        localObject2[0] = localDrawable;
        localObject2 = chat_photoStatesDrawables[6];
        arrayOfDrawable = chat_photoStatesDrawables[6];
        localDrawable = ((Resources)???).getDrawable(2131165591);
        arrayOfDrawable[1] = localDrawable;
        localObject2[0] = localDrawable;
        chat_photoStatesDrawables[7][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165528);
        chat_photoStatesDrawables[7][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165528);
        chat_photoStatesDrawables[8][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165525);
        chat_photoStatesDrawables[8][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165525);
        chat_photoStatesDrawables[9][0] = ((Resources)???).getDrawable(2131165309).mutate();
        chat_photoStatesDrawables[9][1] = ((Resources)???).getDrawable(2131165309).mutate();
        chat_photoStatesDrawables[10][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165528);
        chat_photoStatesDrawables[10][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165528);
        chat_photoStatesDrawables[11][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165525);
        chat_photoStatesDrawables[11][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), 2131165525);
        chat_photoStatesDrawables[12][0] = ((Resources)???).getDrawable(2131165309).mutate();
        chat_photoStatesDrawables[12][1] = ((Resources)???).getDrawable(2131165309).mutate();
        chat_contactDrawable[0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165511);
        chat_contactDrawable[1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44.0F), 2131165511);
        chat_locationDrawable[0] = createRoundRectDrawableWithIcon(AndroidUtilities.dp(2.0F), 2131165516);
        chat_locationDrawable[1] = createRoundRectDrawableWithIcon(AndroidUtilities.dp(2.0F), 2131165516);
        chat_composeShadowDrawable = paramContext.getResources().getDrawable(2131165298);
      }
    }
    for (;;)
    {
      try
      {
        int i = AndroidUtilities.roundMessageSize + AndroidUtilities.dp(6.0F);
        paramContext = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        ??? = new Canvas(paramContext);
        localObject2 = new Paint(1);
        ((Paint)localObject2).setShadowLayer(AndroidUtilities.dp(4.0F), 0.0F, 0.0F, 1593835520);
        ((Canvas)???).drawCircle(i / 2, i / 2, AndroidUtilities.roundMessageSize / 2 - AndroidUtilities.dp(1.0F), (Paint)localObject2);
      }
      catch (Throwable paramContext)
      {
        continue;
      }
      try
      {
        ((Canvas)???).setBitmap(null);
        chat_roundVideoShadow = new BitmapDrawable(paramContext);
        applyChatTheme(paramBoolean);
        chat_msgTextPaintOneEmoji.setTextSize(AndroidUtilities.dp(28.0F));
        chat_msgTextPaintTwoEmoji.setTextSize(AndroidUtilities.dp(24.0F));
        chat_msgTextPaintThreeEmoji.setTextSize(AndroidUtilities.dp(20.0F));
        chat_msgTextPaint.setTextSize(AndroidUtilities.dp(SharedConfig.fontSize));
        chat_msgGameTextPaint.setTextSize(AndroidUtilities.dp(14.0F));
        chat_msgBotButtonPaint.setTextSize(AndroidUtilities.dp(15.0F));
        if ((!paramBoolean) && (chat_botProgressPaint != null))
        {
          chat_botProgressPaint.setStrokeWidth(AndroidUtilities.dp(2.0F));
          chat_infoPaint.setTextSize(AndroidUtilities.dp(12.0F));
          chat_docNamePaint.setTextSize(AndroidUtilities.dp(15.0F));
          chat_locationTitlePaint.setTextSize(AndroidUtilities.dp(15.0F));
          chat_locationAddressPaint.setTextSize(AndroidUtilities.dp(13.0F));
          chat_audioTimePaint.setTextSize(AndroidUtilities.dp(12.0F));
          chat_livePaint.setTextSize(AndroidUtilities.dp(12.0F));
          chat_audioTitlePaint.setTextSize(AndroidUtilities.dp(16.0F));
          chat_audioPerformerPaint.setTextSize(AndroidUtilities.dp(15.0F));
          chat_botButtonPaint.setTextSize(AndroidUtilities.dp(15.0F));
          chat_contactNamePaint.setTextSize(AndroidUtilities.dp(15.0F));
          chat_contactPhonePaint.setTextSize(AndroidUtilities.dp(13.0F));
          chat_durationPaint.setTextSize(AndroidUtilities.dp(12.0F));
          chat_timePaint.setTextSize(AndroidUtilities.dp(12.0F));
          chat_adminPaint.setTextSize(AndroidUtilities.dp(13.0F));
          chat_namePaint.setTextSize(AndroidUtilities.dp(14.0F));
          chat_forwardNamePaint.setTextSize(AndroidUtilities.dp(14.0F));
          chat_replyNamePaint.setTextSize(AndroidUtilities.dp(14.0F));
          chat_replyTextPaint.setTextSize(AndroidUtilities.dp(14.0F));
          chat_gamePaint.setTextSize(AndroidUtilities.dp(13.0F));
          chat_shipmentPaint.setTextSize(AndroidUtilities.dp(13.0F));
          chat_instantViewPaint.setTextSize(AndroidUtilities.dp(13.0F));
          chat_instantViewRectPaint.setStrokeWidth(AndroidUtilities.dp(1.0F));
          chat_statusRecordPaint.setStrokeWidth(AndroidUtilities.dp(2.0F));
          chat_actionTextPaint.setTextSize(AndroidUtilities.dp(Math.max(16, SharedConfig.fontSize) - 2));
          chat_contextResult_titleTextPaint.setTextSize(AndroidUtilities.dp(15.0F));
          chat_contextResult_descriptionTextPaint.setTextSize(AndroidUtilities.dp(13.0F));
          chat_radialProgressPaint.setStrokeWidth(AndroidUtilities.dp(3.0F));
          chat_radialProgress2Paint.setStrokeWidth(AndroidUtilities.dp(2.0F));
        }
        return;
        paramContext = finally;
        throw paramContext;
      }
      catch (Exception localException) {}
    }
  }
  
  public static Drawable createCircleDrawable(int paramInt1, int paramInt2)
  {
    Object localObject = new OvalShape();
    ((OvalShape)localObject).resize(paramInt1, paramInt1);
    localObject = new ShapeDrawable((Shape)localObject);
    ((ShapeDrawable)localObject).getPaint().setColor(paramInt2);
    return (Drawable)localObject;
  }
  
  public static CombinedDrawable createCircleDrawableWithIcon(int paramInt1, int paramInt2)
  {
    return createCircleDrawableWithIcon(paramInt1, paramInt2, 0);
  }
  
  public static CombinedDrawable createCircleDrawableWithIcon(int paramInt1, int paramInt2, int paramInt3)
  {
    if (paramInt2 != 0) {}
    for (Drawable localDrawable = ApplicationLoader.applicationContext.getResources().getDrawable(paramInt2).mutate();; localDrawable = null) {
      return createCircleDrawableWithIcon(paramInt1, localDrawable, paramInt3);
    }
  }
  
  public static CombinedDrawable createCircleDrawableWithIcon(int paramInt1, Drawable paramDrawable, int paramInt2)
  {
    Object localObject = new OvalShape();
    ((OvalShape)localObject).resize(paramInt1, paramInt1);
    localObject = new ShapeDrawable((Shape)localObject);
    Paint localPaint = ((ShapeDrawable)localObject).getPaint();
    localPaint.setColor(-1);
    if (paramInt2 == 1)
    {
      localPaint.setStyle(Paint.Style.STROKE);
      localPaint.setStrokeWidth(AndroidUtilities.dp(2.0F));
    }
    for (;;)
    {
      paramDrawable = new CombinedDrawable((Drawable)localObject, paramDrawable);
      paramDrawable.setCustomSize(paramInt1, paramInt1);
      return paramDrawable;
      if (paramInt2 == 2) {
        localPaint.setAlpha(0);
      }
    }
  }
  
  public static void createCommonResources(Context paramContext)
  {
    if (dividerPaint == null)
    {
      dividerPaint = new Paint();
      dividerPaint.setStrokeWidth(1.0F);
      avatar_backgroundPaint = new Paint(1);
      checkboxSquare_checkPaint = new Paint(1);
      checkboxSquare_checkPaint.setStyle(Paint.Style.STROKE);
      checkboxSquare_checkPaint.setStrokeWidth(AndroidUtilities.dp(2.0F));
      checkboxSquare_eraserPaint = new Paint(1);
      checkboxSquare_eraserPaint.setColor(0);
      checkboxSquare_eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      checkboxSquare_backgroundPaint = new Paint(1);
      linkSelectionPaint = new Paint();
      paramContext = paramContext.getResources();
      avatar_broadcastDrawable = paramContext.getDrawable(2131165255);
      avatar_savedDrawable = paramContext.getDrawable(2131165242);
      avatar_photoDrawable = paramContext.getDrawable(2131165589);
      applyCommonTheme();
    }
  }
  
  public static void createDialogsResources(Context paramContext)
  {
    createCommonResources(paramContext);
    if (dialogs_namePaint == null)
    {
      paramContext = paramContext.getResources();
      dialogs_namePaint = new TextPaint(1);
      dialogs_namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      dialogs_nameEncryptedPaint = new TextPaint(1);
      dialogs_nameEncryptedPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      dialogs_messagePaint = new TextPaint(1);
      dialogs_messagePrintingPaint = new TextPaint(1);
      dialogs_timePaint = new TextPaint(1);
      dialogs_countTextPaint = new TextPaint(1);
      dialogs_countTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      dialogs_onlinePaint = new TextPaint(1);
      dialogs_offlinePaint = new TextPaint(1);
      dialogs_tabletSeletedPaint = new Paint();
      dialogs_pinnedPaint = new Paint();
      dialogs_countPaint = new Paint(1);
      dialogs_countGrayPaint = new Paint(1);
      dialogs_errorPaint = new Paint(1);
      dialogs_lockDrawable = paramContext.getDrawable(2131165463);
      dialogs_checkDrawable = paramContext.getDrawable(2131165457);
      dialogs_halfCheckDrawable = paramContext.getDrawable(2131165460);
      dialogs_clockDrawable = paramContext.getDrawable(2131165510).mutate();
      dialogs_errorDrawable = paramContext.getDrawable(2131165465);
      dialogs_groupDrawable = paramContext.getDrawable(2131165459);
      dialogs_broadcastDrawable = paramContext.getDrawable(2131165456);
      dialogs_muteDrawable = paramContext.getDrawable(2131165461).mutate();
      dialogs_verifiedDrawable = paramContext.getDrawable(2131165685);
      dialogs_verifiedCheckDrawable = paramContext.getDrawable(2131165686);
      dialogs_mentionDrawable = paramContext.getDrawable(2131165490);
      dialogs_botDrawable = paramContext.getDrawable(2131165455);
      dialogs_pinnedDrawable = paramContext.getDrawable(2131165462);
      applyDialogsTheme();
    }
    dialogs_namePaint.setTextSize(AndroidUtilities.dp(17.0F));
    dialogs_nameEncryptedPaint.setTextSize(AndroidUtilities.dp(17.0F));
    dialogs_messagePaint.setTextSize(AndroidUtilities.dp(16.0F));
    dialogs_messagePrintingPaint.setTextSize(AndroidUtilities.dp(16.0F));
    dialogs_timePaint.setTextSize(AndroidUtilities.dp(13.0F));
    dialogs_countTextPaint.setTextSize(AndroidUtilities.dp(13.0F));
    dialogs_onlinePaint.setTextSize(AndroidUtilities.dp(16.0F));
    dialogs_offlinePaint.setTextSize(AndroidUtilities.dp(16.0F));
  }
  
  public static Drawable createEditTextDrawable(Context paramContext, boolean paramBoolean)
  {
    Object localObject = paramContext.getResources();
    Drawable localDrawable = ((Resources)localObject).getDrawable(2131165629).mutate();
    if (paramBoolean)
    {
      paramContext = "dialogInputField";
      localDrawable.setColorFilter(new PorterDuffColorFilter(getColor(paramContext), PorterDuff.Mode.MULTIPLY));
      localObject = ((Resources)localObject).getDrawable(2131165630).mutate();
      if (!paramBoolean) {
        break label138;
      }
    }
    label138:
    for (paramContext = "dialogInputFieldActivated";; paramContext = "windowBackgroundWhiteInputFieldActivated")
    {
      ((Drawable)localObject).setColorFilter(new PorterDuffColorFilter(getColor(paramContext), PorterDuff.Mode.MULTIPLY));
      paramContext = new StateListDrawable()
      {
        public boolean selectDrawable(int paramAnonymousInt)
        {
          if (Build.VERSION.SDK_INT < 21)
          {
            Drawable localDrawable = Theme.getStateDrawable(this, paramAnonymousInt);
            ColorFilter localColorFilter = null;
            if ((localDrawable instanceof BitmapDrawable)) {
              localColorFilter = ((BitmapDrawable)localDrawable).getPaint().getColorFilter();
            }
            for (;;)
            {
              boolean bool = super.selectDrawable(paramAnonymousInt);
              if (localColorFilter != null) {
                localDrawable.setColorFilter(localColorFilter);
              }
              return bool;
              if ((localDrawable instanceof NinePatchDrawable)) {
                localColorFilter = ((NinePatchDrawable)localDrawable).getPaint().getColorFilter();
              }
            }
          }
          return super.selectDrawable(paramAnonymousInt);
        }
      };
      paramContext.addState(new int[] { 16842910, 16842908 }, (Drawable)localObject);
      paramContext.addState(new int[] { 16842908 }, (Drawable)localObject);
      paramContext.addState(StateSet.WILD_CARD, localDrawable);
      return paramContext;
      paramContext = "windowBackgroundWhiteInputField";
      break;
    }
  }
  
  public static Drawable createEmojiIconSelectorDrawable(Context paramContext, int paramInt1, int paramInt2, int paramInt3)
  {
    Object localObject = paramContext.getResources();
    paramContext = ((Resources)localObject).getDrawable(paramInt1).mutate();
    if (paramInt2 != 0) {
      paramContext.setColorFilter(new PorterDuffColorFilter(paramInt2, PorterDuff.Mode.MULTIPLY));
    }
    localObject = ((Resources)localObject).getDrawable(paramInt1).mutate();
    if (paramInt3 != 0) {
      ((Drawable)localObject).setColorFilter(new PorterDuffColorFilter(paramInt3, PorterDuff.Mode.MULTIPLY));
    }
    StateListDrawable local4 = new StateListDrawable()
    {
      public boolean selectDrawable(int paramAnonymousInt)
      {
        if (Build.VERSION.SDK_INT < 21)
        {
          Drawable localDrawable = Theme.getStateDrawable(this, paramAnonymousInt);
          ColorFilter localColorFilter = null;
          if ((localDrawable instanceof BitmapDrawable)) {
            localColorFilter = ((BitmapDrawable)localDrawable).getPaint().getColorFilter();
          }
          for (;;)
          {
            boolean bool = super.selectDrawable(paramAnonymousInt);
            if (localColorFilter != null) {
              localDrawable.setColorFilter(localColorFilter);
            }
            return bool;
            if ((localDrawable instanceof NinePatchDrawable)) {
              localColorFilter = ((NinePatchDrawable)localDrawable).getPaint().getColorFilter();
            }
          }
        }
        return super.selectDrawable(paramAnonymousInt);
      }
    };
    local4.setEnterFadeDuration(1);
    local4.setExitFadeDuration(200);
    local4.addState(new int[] { 16842913 }, (Drawable)localObject);
    local4.addState(new int[0], paramContext);
    return local4;
  }
  
  public static void createProfileResources(Context paramContext)
  {
    if (profile_verifiedDrawable == null)
    {
      profile_aboutTextPaint = new TextPaint(1);
      paramContext = paramContext.getResources();
      profile_verifiedDrawable = paramContext.getDrawable(2131165685).mutate();
      profile_verifiedCheckDrawable = paramContext.getDrawable(2131165686).mutate();
      applyProfileTheme();
    }
    profile_aboutTextPaint.setTextSize(AndroidUtilities.dp(16.0F));
  }
  
  public static Drawable createRoundRectDrawable(int paramInt1, int paramInt2)
  {
    ShapeDrawable localShapeDrawable = new ShapeDrawable(new RoundRectShape(new float[] { paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1 }, null, null));
    localShapeDrawable.getPaint().setColor(paramInt2);
    return localShapeDrawable;
  }
  
  public static Drawable createRoundRectDrawableWithIcon(int paramInt1, int paramInt2)
  {
    ShapeDrawable localShapeDrawable = new ShapeDrawable(new RoundRectShape(new float[] { paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1 }, null, null));
    localShapeDrawable.getPaint().setColor(-1);
    return new CombinedDrawable(localShapeDrawable, ApplicationLoader.applicationContext.getResources().getDrawable(paramInt2).mutate());
  }
  
  public static Drawable createSelectorDrawable(int paramInt)
  {
    return createSelectorDrawable(paramInt, 1);
  }
  
  public static Drawable createSelectorDrawable(int paramInt1, int paramInt2)
  {
    if (Build.VERSION.SDK_INT >= 21)
    {
      localObject = null;
      if (paramInt2 == 1)
      {
        maskPaint.setColor(-1);
        localObject = new Drawable()
        {
          public void draw(Canvas paramAnonymousCanvas)
          {
            Rect localRect = getBounds();
            paramAnonymousCanvas.drawCircle(localRect.centerX(), localRect.centerY(), AndroidUtilities.dp(18.0F), Theme.maskPaint);
          }
          
          public int getOpacity()
          {
            return 0;
          }
          
          public void setAlpha(int paramAnonymousInt) {}
          
          public void setColorFilter(ColorFilter paramAnonymousColorFilter) {}
        };
      }
      for (;;)
      {
        return new RippleDrawable(new ColorStateList(new int[][] { StateSet.WILD_CARD }, new int[] { paramInt1 }), null, (Drawable)localObject);
        if (paramInt2 == 2) {
          localObject = new ColorDrawable(-1);
        }
      }
    }
    Object localObject = new StateListDrawable();
    ColorDrawable localColorDrawable = new ColorDrawable(paramInt1);
    ((StateListDrawable)localObject).addState(new int[] { 16842919 }, localColorDrawable);
    localColorDrawable = new ColorDrawable(paramInt1);
    ((StateListDrawable)localObject).addState(new int[] { 16842913 }, localColorDrawable);
    ((StateListDrawable)localObject).addState(StateSet.WILD_CARD, new ColorDrawable(0));
    return (Drawable)localObject;
  }
  
  public static Drawable createSimpleSelectorCircleDrawable(int paramInt1, int paramInt2, int paramInt3)
  {
    Object localObject = new OvalShape();
    ((OvalShape)localObject).resize(paramInt1, paramInt1);
    ShapeDrawable localShapeDrawable = new ShapeDrawable((Shape)localObject);
    localShapeDrawable.getPaint().setColor(paramInt2);
    localObject = new ShapeDrawable((Shape)localObject);
    if (Build.VERSION.SDK_INT >= 21)
    {
      ((ShapeDrawable)localObject).getPaint().setColor(-1);
      return new RippleDrawable(new ColorStateList(new int[][] { StateSet.WILD_CARD }, new int[] { paramInt3 }), localShapeDrawable, (Drawable)localObject);
    }
    ((ShapeDrawable)localObject).getPaint().setColor(paramInt3);
    StateListDrawable localStateListDrawable = new StateListDrawable();
    localStateListDrawable.addState(new int[] { 16842919 }, (Drawable)localObject);
    localStateListDrawable.addState(new int[] { 16842908 }, (Drawable)localObject);
    localStateListDrawable.addState(StateSet.WILD_CARD, localShapeDrawable);
    return localStateListDrawable;
  }
  
  public static Drawable createSimpleSelectorDrawable(Context paramContext, int paramInt1, int paramInt2, int paramInt3)
  {
    Object localObject = paramContext.getResources();
    paramContext = ((Resources)localObject).getDrawable(paramInt1).mutate();
    if (paramInt2 != 0) {
      paramContext.setColorFilter(new PorterDuffColorFilter(paramInt2, PorterDuff.Mode.MULTIPLY));
    }
    localObject = ((Resources)localObject).getDrawable(paramInt1).mutate();
    if (paramInt3 != 0) {
      ((Drawable)localObject).setColorFilter(new PorterDuffColorFilter(paramInt3, PorterDuff.Mode.MULTIPLY));
    }
    StateListDrawable local6 = new StateListDrawable()
    {
      public boolean selectDrawable(int paramAnonymousInt)
      {
        if (Build.VERSION.SDK_INT < 21)
        {
          Drawable localDrawable = Theme.getStateDrawable(this, paramAnonymousInt);
          ColorFilter localColorFilter = null;
          if ((localDrawable instanceof BitmapDrawable)) {
            localColorFilter = ((BitmapDrawable)localDrawable).getPaint().getColorFilter();
          }
          for (;;)
          {
            boolean bool = super.selectDrawable(paramAnonymousInt);
            if (localColorFilter != null) {
              localDrawable.setColorFilter(localColorFilter);
            }
            return bool;
            if ((localDrawable instanceof NinePatchDrawable)) {
              localColorFilter = ((NinePatchDrawable)localDrawable).getPaint().getColorFilter();
            }
          }
        }
        return super.selectDrawable(paramAnonymousInt);
      }
    };
    local6.addState(new int[] { 16842919 }, (Drawable)localObject);
    local6.addState(new int[] { 16842913 }, (Drawable)localObject);
    local6.addState(StateSet.WILD_CARD, paramContext);
    return local6;
  }
  
  public static Drawable createSimpleSelectorRoundRectDrawable(int paramInt1, int paramInt2, int paramInt3)
  {
    ShapeDrawable localShapeDrawable1 = new ShapeDrawable(new RoundRectShape(new float[] { paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1 }, null, null));
    localShapeDrawable1.getPaint().setColor(paramInt2);
    ShapeDrawable localShapeDrawable2 = new ShapeDrawable(new RoundRectShape(new float[] { paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1, paramInt1 }, null, null));
    localShapeDrawable2.getPaint().setColor(paramInt3);
    StateListDrawable localStateListDrawable = new StateListDrawable();
    localStateListDrawable.addState(new int[] { 16842919 }, localShapeDrawable2);
    localStateListDrawable.addState(new int[] { 16842913 }, localShapeDrawable2);
    localStateListDrawable.addState(StateSet.WILD_CARD, localShapeDrawable1);
    return localStateListDrawable;
  }
  
  public static boolean deleteTheme(ThemeInfo paramThemeInfo)
  {
    if (paramThemeInfo.pathToFile == null) {
      return false;
    }
    boolean bool = false;
    if (currentTheme == paramThemeInfo)
    {
      applyTheme(defaultTheme, true, false, false);
      bool = true;
    }
    otherThemes.remove(paramThemeInfo);
    themesDict.remove(paramThemeInfo.name);
    themes.remove(paramThemeInfo);
    new File(paramThemeInfo.pathToFile).delete();
    saveOtherThemes();
    return bool;
  }
  
  public static void destroyResources()
  {
    int i = 0;
    while (i < chat_attachButtonDrawables.length)
    {
      if (chat_attachButtonDrawables[i] != null) {
        chat_attachButtonDrawables[i].setCallback(null);
      }
      i += 1;
    }
  }
  
  /* Error */
  public static File getAssetFile(String paramString)
  {
    // Byte code:
    //   0: new 2810	java/io/File
    //   3: dup
    //   4: invokestatic 2823	org/telegram/messenger/ApplicationLoader:getFilesDirFixed	()Ljava/io/File;
    //   7: aload_0
    //   8: invokespecial 2826	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   11: astore 5
    //   13: getstatic 2264	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   16: invokevirtual 3333	android/content/Context:getAssets	()Landroid/content/res/AssetManager;
    //   19: aload_0
    //   20: invokevirtual 3339	android/content/res/AssetManager:open	(Ljava/lang/String;)Ljava/io/InputStream;
    //   23: astore_3
    //   24: aload_3
    //   25: invokevirtual 3344	java/io/InputStream:available	()I
    //   28: i2l
    //   29: lstore_1
    //   30: aload_3
    //   31: invokevirtual 3347	java/io/InputStream:close	()V
    //   34: aload 5
    //   36: invokevirtual 3350	java/io/File:exists	()Z
    //   39: ifeq +19 -> 58
    //   42: lload_1
    //   43: lconst_0
    //   44: lcmp
    //   45: ifeq +49 -> 94
    //   48: aload 5
    //   50: invokevirtual 3352	java/io/File:length	()J
    //   53: lload_1
    //   54: lcmp
    //   55: ifeq +39 -> 94
    //   58: aconst_null
    //   59: astore 4
    //   61: aconst_null
    //   62: astore_3
    //   63: getstatic 2264	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   66: invokevirtual 3333	android/content/Context:getAssets	()Landroid/content/res/AssetManager;
    //   69: aload_0
    //   70: invokevirtual 3339	android/content/res/AssetManager:open	(Ljava/lang/String;)Ljava/io/InputStream;
    //   73: astore_0
    //   74: aload_0
    //   75: astore_3
    //   76: aload_0
    //   77: astore 4
    //   79: aload_0
    //   80: aload 5
    //   82: invokestatic 3355	org/telegram/messenger/AndroidUtilities:copyFile	(Ljava/io/InputStream;Ljava/io/File;)Z
    //   85: pop
    //   86: aload_0
    //   87: ifnull +7 -> 94
    //   90: aload_0
    //   91: invokevirtual 3347	java/io/InputStream:close	()V
    //   94: aload 5
    //   96: areturn
    //   97: astore_3
    //   98: lconst_0
    //   99: lstore_1
    //   100: aload_3
    //   101: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   104: goto -70 -> 34
    //   107: astore_0
    //   108: aload_3
    //   109: astore 4
    //   111: aload_0
    //   112: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   115: aload_3
    //   116: ifnull -22 -> 94
    //   119: aload_3
    //   120: invokevirtual 3347	java/io/InputStream:close	()V
    //   123: aload 5
    //   125: areturn
    //   126: astore_0
    //   127: aload 5
    //   129: areturn
    //   130: astore_0
    //   131: aload 4
    //   133: ifnull +8 -> 141
    //   136: aload 4
    //   138: invokevirtual 3347	java/io/InputStream:close	()V
    //   141: aload_0
    //   142: athrow
    //   143: astore_0
    //   144: aload 5
    //   146: areturn
    //   147: astore_3
    //   148: goto -7 -> 141
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	151	0	paramString	String
    //   29	71	1	l	long
    //   23	53	3	localObject1	Object
    //   97	23	3	localException1	Exception
    //   147	1	3	localException2	Exception
    //   59	78	4	localObject2	Object
    //   11	134	5	localFile	File
    // Exception table:
    //   from	to	target	type
    //   13	34	97	java/lang/Exception
    //   63	74	107	java/lang/Exception
    //   79	86	107	java/lang/Exception
    //   119	123	126	java/lang/Exception
    //   63	74	130	finally
    //   79	86	130	finally
    //   111	115	130	finally
    //   90	94	143	java/lang/Exception
    //   136	141	147	java/lang/Exception
  }
  
  private static long getAutoNightSwitchThemeDelay()
  {
    long l1 = 12000L;
    long l2 = SystemClock.uptimeMillis();
    if (Math.abs(lastThemeSwitchTime - l2) >= 12000L) {
      l1 = 1800L;
    }
    return l1;
  }
  
  public static Drawable getCachedWallpaper()
  {
    synchronized (wallpaperSync)
    {
      if (themedWallpaper != null)
      {
        localDrawable = themedWallpaper;
        return localDrawable;
      }
      Drawable localDrawable = wallpaper;
      return localDrawable;
    }
  }
  
  public static int getColor(String paramString)
  {
    return getColor(paramString, null);
  }
  
  public static int getColor(String paramString, boolean[] paramArrayOfBoolean)
  {
    Integer localInteger = (Integer)currentColors.get(paramString);
    Object localObject = localInteger;
    if (localInteger == null)
    {
      localObject = (String)fallbackKeys.get(paramString);
      if (localObject != null) {
        localInteger = (Integer)currentColors.get(localObject);
      }
      localObject = localInteger;
      if (localInteger == null)
      {
        if (paramArrayOfBoolean != null) {
          paramArrayOfBoolean[0] = true;
        }
        if (paramString.equals("chat_serviceBackground")) {
          return serviceMessageColor;
        }
        if (paramString.equals("chat_serviceBackgroundSelected")) {
          return serviceSelectedMessageColor;
        }
        return getDefaultColor(paramString);
      }
    }
    return ((Integer)localObject).intValue();
  }
  
  public static Integer getColorOrNull(String paramString)
  {
    Integer localInteger2 = (Integer)currentColors.get(paramString);
    Integer localInteger1 = localInteger2;
    if (localInteger2 == null)
    {
      if ((String)fallbackKeys.get(paramString) != null) {
        localInteger2 = (Integer)currentColors.get(paramString);
      }
      localInteger1 = localInteger2;
      if (localInteger2 == null) {
        localInteger1 = (Integer)defaultColors.get(paramString);
      }
    }
    return localInteger1;
  }
  
  public static Drawable getCurrentHolidayDrawable()
  {
    int j;
    int k;
    if (System.currentTimeMillis() - lastHolidayCheckTime >= 60000L)
    {
      lastHolidayCheckTime = System.currentTimeMillis();
      Calendar localCalendar = Calendar.getInstance();
      localCalendar.setTimeInMillis(System.currentTimeMillis());
      j = localCalendar.get(2);
      k = localCalendar.get(5);
      i = localCalendar.get(12);
      int m = localCalendar.get(11);
      if ((j != 0) || (k != 1) || (i > 10) || (m != 0)) {
        break label160;
      }
      canStartHolidayAnimation = true;
      if (dialogs_holidayDrawable == null) {
        if (j == 11) {
          if (!BuildVars.DEBUG_PRIVATE_VERSION) {
            break label167;
          }
        }
      }
    }
    label160:
    label167:
    for (int i = 29;; i = 31)
    {
      if ((k < i) || (k > 31))
      {
        if ((j != 0) || (k != 1)) {}
      }
      else
      {
        dialogs_holidayDrawable = ApplicationLoader.applicationContext.getResources().getDrawable(2131165537);
        dialogs_holidayDrawableOffsetX = -AndroidUtilities.dp(3.0F);
        dialogs_holidayDrawableOffsetY = 0;
      }
      return dialogs_holidayDrawable;
      canStartHolidayAnimation = false;
      break;
    }
  }
  
  public static int getCurrentHolidayDrawableXOffset()
  {
    return dialogs_holidayDrawableOffsetX;
  }
  
  public static int getCurrentHolidayDrawableYOffset()
  {
    return dialogs_holidayDrawableOffsetY;
  }
  
  public static ThemeInfo getCurrentNightTheme()
  {
    return currentNightTheme;
  }
  
  public static String getCurrentNightThemeName()
  {
    Object localObject;
    if (currentNightTheme == null) {
      localObject = "";
    }
    String str;
    do
    {
      return (String)localObject;
      str = currentNightTheme.getName();
      localObject = str;
    } while (!str.toLowerCase().endsWith(".attheme"));
    return str.substring(0, str.lastIndexOf('.'));
  }
  
  public static ThemeInfo getCurrentTheme()
  {
    if (currentDayTheme != null) {
      return currentDayTheme;
    }
    return defaultTheme;
  }
  
  public static String getCurrentThemeName()
  {
    String str2 = currentDayTheme.getName();
    String str1 = str2;
    if (str2.toLowerCase().endsWith(".attheme")) {
      str1 = str2.substring(0, str2.lastIndexOf('.'));
    }
    return str1;
  }
  
  public static int getDefaultColor(String paramString)
  {
    Integer localInteger = (Integer)defaultColors.get(paramString);
    if (localInteger == null)
    {
      if (paramString.equals("chats_menuTopShadow")) {
        return 0;
      }
      return -65536;
    }
    return localInteger.intValue();
  }
  
  public static HashMap<String, Integer> getDefaultColors()
  {
    return defaultColors;
  }
  
  public static Drawable getRoundRectSelectorDrawable()
  {
    if (Build.VERSION.SDK_INT >= 21)
    {
      localObject1 = createRoundRectDrawable(AndroidUtilities.dp(3.0F), -1);
      localObject2 = StateSet.WILD_CARD;
      int i = getColor("dialogButtonSelector");
      return new RippleDrawable(new ColorStateList(new int[][] { localObject2 }, new int[] { i }), null, (Drawable)localObject1);
    }
    Object localObject1 = new StateListDrawable();
    Object localObject2 = createRoundRectDrawable(AndroidUtilities.dp(3.0F), getColor("dialogButtonSelector"));
    ((StateListDrawable)localObject1).addState(new int[] { 16842919 }, (Drawable)localObject2);
    localObject2 = createRoundRectDrawable(AndroidUtilities.dp(3.0F), getColor("dialogButtonSelector"));
    ((StateListDrawable)localObject1).addState(new int[] { 16842913 }, (Drawable)localObject2);
    ((StateListDrawable)localObject1).addState(StateSet.WILD_CARD, new ColorDrawable(0));
    return (Drawable)localObject1;
  }
  
  public static int getSelectedColor()
  {
    return selectedColor;
  }
  
  public static Drawable getSelectorDrawable(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      if (Build.VERSION.SDK_INT >= 21)
      {
        localObject1 = new ColorDrawable(-1);
        localObject2 = StateSet.WILD_CARD;
        i = getColor("listSelectorSDK21");
        return new RippleDrawable(new ColorStateList(new int[][] { localObject2 }, new int[] { i }), new ColorDrawable(getColor("windowBackgroundWhite")), (Drawable)localObject1);
      }
      int i = getColor("listSelectorSDK21");
      Object localObject1 = new StateListDrawable();
      Object localObject2 = new ColorDrawable(i);
      ((StateListDrawable)localObject1).addState(new int[] { 16842919 }, (Drawable)localObject2);
      localObject2 = new ColorDrawable(i);
      ((StateListDrawable)localObject1).addState(new int[] { 16842913 }, (Drawable)localObject2);
      ((StateListDrawable)localObject1).addState(StateSet.WILD_CARD, new ColorDrawable(getColor("windowBackgroundWhite")));
      return (Drawable)localObject1;
    }
    return createSelectorDrawable(getColor("listSelectorSDK21"), 2);
  }
  
  public static int getServiceMessageColor()
  {
    Integer localInteger = (Integer)currentColors.get("chat_serviceBackground");
    if (localInteger == null) {
      return serviceMessageColor;
    }
    return localInteger.intValue();
  }
  
  @SuppressLint({"PrivateApi"})
  private static Drawable getStateDrawable(Drawable paramDrawable, int paramInt)
  {
    if (StateListDrawable_getStateDrawableMethod == null) {}
    try
    {
      StateListDrawable_getStateDrawableMethod = StateListDrawable.class.getDeclaredMethod("getStateDrawable", new Class[] { Integer.TYPE });
      if (StateListDrawable_getStateDrawableMethod == null) {
        return null;
      }
      try
      {
        paramDrawable = (Drawable)StateListDrawable_getStateDrawableMethod.invoke(paramDrawable, new Object[] { Integer.valueOf(paramInt) });
        return paramDrawable;
      }
      catch (Exception paramDrawable)
      {
        return null;
      }
    }
    catch (Throwable localThrowable)
    {
      for (;;) {}
    }
  }
  
  /* Error */
  private static HashMap<String, Integer> getThemeFileValues(File paramFile, String paramString)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 13
    //   3: aconst_null
    //   4: astore 12
    //   6: new 1971	java/util/HashMap
    //   9: dup
    //   10: invokespecial 1972	java/util/HashMap:<init>	()V
    //   13: astore 14
    //   15: aload 13
    //   17: astore 11
    //   19: sipush 1024
    //   22: newarray <illegal type>
    //   24: astore 15
    //   26: iconst_0
    //   27: istore_2
    //   28: aload_1
    //   29: ifnull +12 -> 41
    //   32: aload 13
    //   34: astore 11
    //   36: aload_1
    //   37: invokestatic 3450	org/telegram/ui/ActionBar/Theme:getAssetFile	(Ljava/lang/String;)Ljava/io/File;
    //   40: astore_0
    //   41: aload 13
    //   43: astore 11
    //   45: new 3452	java/io/FileInputStream
    //   48: dup
    //   49: aload_0
    //   50: invokespecial 3455	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   53: astore_0
    //   54: iconst_0
    //   55: istore 4
    //   57: iconst_m1
    //   58: putstatic 2432	org/telegram/ui/ActionBar/Theme:themedWallpaperFileOffset	I
    //   61: iload_2
    //   62: istore 5
    //   64: aload_0
    //   65: aload 15
    //   67: invokevirtual 3459	java/io/FileInputStream:read	([B)I
    //   70: istore 9
    //   72: iload 9
    //   74: iconst_m1
    //   75: if_icmpeq +93 -> 168
    //   78: iconst_0
    //   79: istore 7
    //   81: iconst_0
    //   82: istore 6
    //   84: iload 5
    //   86: istore_2
    //   87: iload 4
    //   89: istore_3
    //   90: iload 6
    //   92: iload 9
    //   94: if_icmpge +68 -> 162
    //   97: iload_2
    //   98: istore_3
    //   99: iload 7
    //   101: istore 8
    //   103: aload 15
    //   105: iload 6
    //   107: baload
    //   108: bipush 10
    //   110: if_icmpne +271 -> 381
    //   113: iload 6
    //   115: iload 7
    //   117: isub
    //   118: iconst_1
    //   119: iadd
    //   120: istore 10
    //   122: new 1955	java/lang/String
    //   125: dup
    //   126: aload 15
    //   128: iload 7
    //   130: iload 10
    //   132: iconst_1
    //   133: isub
    //   134: ldc_w 3461
    //   137: invokespecial 3464	java/lang/String:<init>	([BIILjava/lang/String;)V
    //   140: astore 11
    //   142: aload 11
    //   144: ldc_w 3466
    //   147: invokevirtual 3469	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   150: ifeq +29 -> 179
    //   153: iload_2
    //   154: iload 10
    //   156: iadd
    //   157: putstatic 2432	org/telegram/ui/ActionBar/Theme:themedWallpaperFileOffset	I
    //   160: iconst_1
    //   161: istore_3
    //   162: iload 5
    //   164: iload_2
    //   165: if_icmpne +112 -> 277
    //   168: aload_0
    //   169: ifnull +7 -> 176
    //   172: aload_0
    //   173: invokevirtual 3470	java/io/FileInputStream:close	()V
    //   176: aload 14
    //   178: areturn
    //   179: aload 11
    //   181: bipush 61
    //   183: invokevirtual 3473	java/lang/String:indexOf	(I)I
    //   186: istore_3
    //   187: iload_3
    //   188: iconst_m1
    //   189: if_icmpeq +180 -> 369
    //   192: aload 11
    //   194: iconst_0
    //   195: iload_3
    //   196: invokevirtual 3410	java/lang/String:substring	(II)Ljava/lang/String;
    //   199: astore_1
    //   200: aload 11
    //   202: iload_3
    //   203: iconst_1
    //   204: iadd
    //   205: invokevirtual 3476	java/lang/String:substring	(I)Ljava/lang/String;
    //   208: astore 11
    //   210: aload 11
    //   212: invokevirtual 3477	java/lang/String:length	()I
    //   215: ifle +50 -> 265
    //   218: aload 11
    //   220: iconst_0
    //   221: invokevirtual 3481	java/lang/String:charAt	(I)C
    //   224: istore_3
    //   225: iload_3
    //   226: bipush 35
    //   228: if_icmpne +37 -> 265
    //   231: aload 11
    //   233: invokestatic 3486	android/graphics/Color:parseColor	(Ljava/lang/String;)I
    //   236: istore_3
    //   237: aload 14
    //   239: aload_1
    //   240: iload_3
    //   241: invokestatic 1982	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   244: invokevirtual 1986	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   247: pop
    //   248: goto +121 -> 369
    //   251: astore 12
    //   253: aload 11
    //   255: invokestatic 3491	org/telegram/messenger/Utilities:parseInt	(Ljava/lang/String;)Ljava/lang/Integer;
    //   258: invokevirtual 2479	java/lang/Integer:intValue	()I
    //   261: istore_3
    //   262: goto -25 -> 237
    //   265: aload 11
    //   267: invokestatic 3491	org/telegram/messenger/Utilities:parseInt	(Ljava/lang/String;)Ljava/lang/Integer;
    //   270: invokevirtual 2479	java/lang/Integer:intValue	()I
    //   273: istore_3
    //   274: goto -37 -> 237
    //   277: aload_0
    //   278: invokevirtual 3495	java/io/FileInputStream:getChannel	()Ljava/nio/channels/FileChannel;
    //   281: iload_2
    //   282: i2l
    //   283: invokevirtual 3501	java/nio/channels/FileChannel:position	(J)Ljava/nio/channels/FileChannel;
    //   286: pop
    //   287: iload_3
    //   288: istore 4
    //   290: iload_3
    //   291: ifeq -230 -> 61
    //   294: goto -126 -> 168
    //   297: astore_0
    //   298: aload_0
    //   299: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   302: aload 14
    //   304: areturn
    //   305: astore_1
    //   306: aload 12
    //   308: astore_0
    //   309: aload_0
    //   310: astore 11
    //   312: aload_1
    //   313: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   316: aload_0
    //   317: ifnull -141 -> 176
    //   320: aload_0
    //   321: invokevirtual 3470	java/io/FileInputStream:close	()V
    //   324: aload 14
    //   326: areturn
    //   327: astore_0
    //   328: aload_0
    //   329: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   332: aload 14
    //   334: areturn
    //   335: astore_0
    //   336: aload 11
    //   338: ifnull +8 -> 346
    //   341: aload 11
    //   343: invokevirtual 3470	java/io/FileInputStream:close	()V
    //   346: aload_0
    //   347: athrow
    //   348: astore_1
    //   349: aload_1
    //   350: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   353: goto -7 -> 346
    //   356: astore_1
    //   357: aload_0
    //   358: astore 11
    //   360: aload_1
    //   361: astore_0
    //   362: goto -26 -> 336
    //   365: astore_1
    //   366: goto -57 -> 309
    //   369: iload 7
    //   371: iload 10
    //   373: iadd
    //   374: istore 8
    //   376: iload_2
    //   377: iload 10
    //   379: iadd
    //   380: istore_3
    //   381: iload 6
    //   383: iconst_1
    //   384: iadd
    //   385: istore 6
    //   387: iload_3
    //   388: istore_2
    //   389: iload 8
    //   391: istore 7
    //   393: goto -306 -> 87
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	396	0	paramFile	File
    //   0	396	1	paramString	String
    //   27	362	2	i	int
    //   89	299	3	j	int
    //   55	234	4	k	int
    //   62	104	5	m	int
    //   82	304	6	n	int
    //   79	313	7	i1	int
    //   101	289	8	i2	int
    //   70	25	9	i3	int
    //   120	260	10	i4	int
    //   17	342	11	localObject1	Object
    //   4	1	12	localObject2	Object
    //   251	56	12	localException	Exception
    //   1	41	13	localObject3	Object
    //   13	320	14	localHashMap	HashMap
    //   24	103	15	arrayOfByte	byte[]
    // Exception table:
    //   from	to	target	type
    //   231	237	251	java/lang/Exception
    //   172	176	297	java/lang/Exception
    //   19	26	305	java/lang/Throwable
    //   36	41	305	java/lang/Throwable
    //   45	54	305	java/lang/Throwable
    //   320	324	327	java/lang/Exception
    //   19	26	335	finally
    //   36	41	335	finally
    //   45	54	335	finally
    //   312	316	335	finally
    //   341	346	348	java/lang/Exception
    //   57	61	356	finally
    //   64	72	356	finally
    //   122	160	356	finally
    //   179	187	356	finally
    //   192	225	356	finally
    //   231	237	356	finally
    //   237	248	356	finally
    //   253	262	356	finally
    //   265	274	356	finally
    //   277	287	356	finally
    //   57	61	365	java/lang/Throwable
    //   64	72	365	java/lang/Throwable
    //   122	160	365	java/lang/Throwable
    //   179	187	365	java/lang/Throwable
    //   192	225	365	java/lang/Throwable
    //   231	237	365	java/lang/Throwable
    //   237	248	365	java/lang/Throwable
    //   253	262	365	java/lang/Throwable
    //   265	274	365	java/lang/Throwable
    //   277	287	365	java/lang/Throwable
  }
  
  public static Drawable getThemedDrawable(Context paramContext, int paramInt, String paramString)
  {
    paramContext = paramContext.getResources().getDrawable(paramInt).mutate();
    paramContext.setColorFilter(new PorterDuffColorFilter(getColor(paramString), PorterDuff.Mode.MULTIPLY));
    return paramContext;
  }
  
  /* Error */
  public static Drawable getThemedWallpaper(boolean paramBoolean)
  {
    // Byte code:
    //   0: getstatic 2229	org/telegram/ui/ActionBar/Theme:currentColors	Ljava/util/HashMap;
    //   3: ldc_w 1200
    //   6: invokevirtual 2324	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   9: checkcast 1978	java/lang/Integer
    //   12: astore 6
    //   14: aload 6
    //   16: ifnull +20 -> 36
    //   19: new 3305	android/graphics/drawable/ColorDrawable
    //   22: dup
    //   23: aload 6
    //   25: invokevirtual 2479	java/lang/Integer:intValue	()I
    //   28: invokespecial 3306	android/graphics/drawable/ColorDrawable:<init>	(I)V
    //   31: astore 6
    //   33: aload 6
    //   35: areturn
    //   36: getstatic 2432	org/telegram/ui/ActionBar/Theme:themedWallpaperFileOffset	I
    //   39: ifle +261 -> 300
    //   42: getstatic 2239	org/telegram/ui/ActionBar/Theme:currentTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   45: getfield 2785	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
    //   48: ifnonnull +12 -> 60
    //   51: getstatic 2239	org/telegram/ui/ActionBar/Theme:currentTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   54: getfield 2252	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
    //   57: ifnull +243 -> 300
    //   60: aconst_null
    //   61: astore 9
    //   63: aconst_null
    //   64: astore 8
    //   66: aload 9
    //   68: astore 6
    //   70: getstatic 2239	org/telegram/ui/ActionBar/Theme:currentTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   73: getfield 2252	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
    //   76: ifnull +126 -> 202
    //   79: aload 9
    //   81: astore 6
    //   83: getstatic 2239	org/telegram/ui/ActionBar/Theme:currentTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   86: getfield 2252	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
    //   89: invokestatic 3450	org/telegram/ui/ActionBar/Theme:getAssetFile	(Ljava/lang/String;)Ljava/io/File;
    //   92: astore 7
    //   94: aload 9
    //   96: astore 6
    //   98: new 3452	java/io/FileInputStream
    //   101: dup
    //   102: aload 7
    //   104: invokespecial 3455	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   107: astore 7
    //   109: aload 7
    //   111: invokevirtual 3495	java/io/FileInputStream:getChannel	()Ljava/nio/channels/FileChannel;
    //   114: getstatic 2432	org/telegram/ui/ActionBar/Theme:themedWallpaperFileOffset	I
    //   117: i2l
    //   118: invokevirtual 3501	java/nio/channels/FileChannel:position	(J)Ljava/nio/channels/FileChannel;
    //   121: pop
    //   122: new 3507	android/graphics/BitmapFactory$Options
    //   125: dup
    //   126: invokespecial 3508	android/graphics/BitmapFactory$Options:<init>	()V
    //   129: astore 6
    //   131: iconst_1
    //   132: istore 4
    //   134: iconst_1
    //   135: istore_3
    //   136: iload_0
    //   137: ifeq +87 -> 224
    //   140: aload 6
    //   142: iconst_1
    //   143: putfield 3511	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
    //   146: aload 6
    //   148: getfield 3514	android/graphics/BitmapFactory$Options:outWidth	I
    //   151: i2f
    //   152: fstore_2
    //   153: aload 6
    //   155: getfield 3517	android/graphics/BitmapFactory$Options:outHeight	I
    //   158: i2f
    //   159: fstore_1
    //   160: ldc_w 3518
    //   163: invokestatic 3079	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   166: istore 5
    //   168: fload_2
    //   169: iload 5
    //   171: i2f
    //   172: fcmpl
    //   173: ifgt +14 -> 187
    //   176: iload_3
    //   177: istore 4
    //   179: fload_1
    //   180: iload 5
    //   182: i2f
    //   183: fcmpl
    //   184: ifle +40 -> 224
    //   187: iload_3
    //   188: iconst_2
    //   189: imul
    //   190: istore_3
    //   191: fload_2
    //   192: fconst_2
    //   193: fdiv
    //   194: fstore_2
    //   195: fload_1
    //   196: fconst_2
    //   197: fdiv
    //   198: fstore_1
    //   199: goto -31 -> 168
    //   202: aload 9
    //   204: astore 6
    //   206: new 2810	java/io/File
    //   209: dup
    //   210: getstatic 2239	org/telegram/ui/ActionBar/Theme:currentTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   213: getfield 2785	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
    //   216: invokespecial 2811	java/io/File:<init>	(Ljava/lang/String;)V
    //   219: astore 7
    //   221: goto -127 -> 94
    //   224: aload 6
    //   226: iconst_0
    //   227: putfield 3511	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
    //   230: aload 6
    //   232: iload 4
    //   234: putfield 3521	android/graphics/BitmapFactory$Options:inSampleSize	I
    //   237: aload 7
    //   239: aconst_null
    //   240: aload 6
    //   242: invokestatic 3527	android/graphics/BitmapFactory:decodeStream	(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
    //   245: astore 6
    //   247: aload 6
    //   249: ifnull +41 -> 290
    //   252: new 3146	android/graphics/drawable/BitmapDrawable
    //   255: dup
    //   256: aload 6
    //   258: invokespecial 3147	android/graphics/drawable/BitmapDrawable:<init>	(Landroid/graphics/Bitmap;)V
    //   261: astore 8
    //   263: aload 8
    //   265: astore 6
    //   267: aload 7
    //   269: ifnull -236 -> 33
    //   272: aload 7
    //   274: invokevirtual 3470	java/io/FileInputStream:close	()V
    //   277: aload 8
    //   279: areturn
    //   280: astore 6
    //   282: aload 6
    //   284: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   287: aload 8
    //   289: areturn
    //   290: aload 7
    //   292: ifnull +8 -> 300
    //   295: aload 7
    //   297: invokevirtual 3470	java/io/FileInputStream:close	()V
    //   300: aconst_null
    //   301: areturn
    //   302: astore 6
    //   304: aload 6
    //   306: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   309: goto -9 -> 300
    //   312: astore 6
    //   314: aload 8
    //   316: astore 7
    //   318: aload 6
    //   320: astore 8
    //   322: aload 7
    //   324: astore 6
    //   326: aload 8
    //   328: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   331: aload 7
    //   333: ifnull -33 -> 300
    //   336: aload 7
    //   338: invokevirtual 3470	java/io/FileInputStream:close	()V
    //   341: goto -41 -> 300
    //   344: astore 6
    //   346: aload 6
    //   348: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   351: goto -51 -> 300
    //   354: astore 7
    //   356: aload 6
    //   358: ifnull +8 -> 366
    //   361: aload 6
    //   363: invokevirtual 3470	java/io/FileInputStream:close	()V
    //   366: aload 7
    //   368: athrow
    //   369: astore 6
    //   371: aload 6
    //   373: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   376: goto -10 -> 366
    //   379: astore 8
    //   381: aload 7
    //   383: astore 6
    //   385: aload 8
    //   387: astore 7
    //   389: goto -33 -> 356
    //   392: astore 8
    //   394: goto -72 -> 322
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	397	0	paramBoolean	boolean
    //   159	40	1	f1	float
    //   152	43	2	f2	float
    //   135	56	3	i	int
    //   132	101	4	j	int
    //   166	15	5	k	int
    //   12	254	6	localObject1	Object
    //   280	3	6	localException1	Exception
    //   302	3	6	localException2	Exception
    //   312	7	6	localThrowable1	Throwable
    //   324	1	6	localObject2	Object
    //   344	18	6	localException3	Exception
    //   369	3	6	localException4	Exception
    //   383	1	6	localObject3	Object
    //   92	245	7	localObject4	Object
    //   354	28	7	localObject5	Object
    //   387	1	7	localObject6	Object
    //   64	263	8	localObject7	Object
    //   379	7	8	localObject8	Object
    //   392	1	8	localThrowable2	Throwable
    //   61	142	9	localObject9	Object
    // Exception table:
    //   from	to	target	type
    //   272	277	280	java/lang/Exception
    //   295	300	302	java/lang/Exception
    //   70	79	312	java/lang/Throwable
    //   83	94	312	java/lang/Throwable
    //   98	109	312	java/lang/Throwable
    //   206	221	312	java/lang/Throwable
    //   336	341	344	java/lang/Exception
    //   70	79	354	finally
    //   83	94	354	finally
    //   98	109	354	finally
    //   206	221	354	finally
    //   326	331	354	finally
    //   361	366	369	java/lang/Exception
    //   109	131	379	finally
    //   140	168	379	finally
    //   224	247	379	finally
    //   252	263	379	finally
    //   109	131	392	java/lang/Throwable
    //   140	168	392	java/lang/Throwable
    //   224	247	392	java/lang/Throwable
    //   252	263	392	java/lang/Throwable
  }
  
  public static boolean hasThemeKey(String paramString)
  {
    return currentColors.containsKey(paramString);
  }
  
  public static boolean hasWallpaperFromTheme()
  {
    return (currentColors.containsKey("chat_wallpaper")) || (themedWallpaperFileOffset > 0);
  }
  
  public static boolean isCurrentThemeNight()
  {
    return currentTheme == currentNightTheme;
  }
  
  public static boolean isCustomTheme()
  {
    return isCustomTheme;
  }
  
  public static void loadWallpaper()
  {
    if (wallpaper != null) {
      return;
    }
    Utilities.searchQueue.postRunnable(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: invokestatic 26	org/telegram/ui/ActionBar/Theme:access$900	()Ljava/lang/Object;
        //   3: astore 8
        //   5: aload 8
        //   7: monitorenter
        //   8: invokestatic 32	org/telegram/messenger/MessagesController:getGlobalMainSettings	()Landroid/content/SharedPreferences;
        //   11: ldc 34
        //   13: iconst_0
        //   14: invokeinterface 40 3 0
        //   19: ifne +42 -> 61
        //   22: invokestatic 44	org/telegram/ui/ActionBar/Theme:access$1000	()Ljava/util/HashMap;
        //   25: ldc 46
        //   27: invokevirtual 52	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
        //   30: checkcast 54	java/lang/Integer
        //   33: astore 4
        //   35: aload 4
        //   37: ifnull +157 -> 194
        //   40: new 56	android/graphics/drawable/ColorDrawable
        //   43: dup
        //   44: aload 4
        //   46: invokevirtual 60	java/lang/Integer:intValue	()I
        //   49: invokespecial 63	android/graphics/drawable/ColorDrawable:<init>	(I)V
        //   52: invokestatic 67	org/telegram/ui/ActionBar/Theme:access$1102	(Landroid/graphics/drawable/Drawable;)Landroid/graphics/drawable/Drawable;
        //   55: pop
        //   56: iconst_1
        //   57: invokestatic 71	org/telegram/ui/ActionBar/Theme:access$1202	(Z)Z
        //   60: pop
        //   61: invokestatic 75	org/telegram/ui/ActionBar/Theme:access$1100	()Landroid/graphics/drawable/Drawable;
        //   64: astore 4
        //   66: aload 4
        //   68: ifnonnull +104 -> 172
        //   71: iconst_0
        //   72: istore_2
        //   73: iload_2
        //   74: istore_1
        //   75: invokestatic 32	org/telegram/messenger/MessagesController:getGlobalMainSettings	()Landroid/content/SharedPreferences;
        //   78: astore 4
        //   80: iload_2
        //   81: istore_1
        //   82: aload 4
        //   84: ldc 77
        //   86: ldc 78
        //   88: invokeinterface 82 3 0
        //   93: istore_3
        //   94: iload_2
        //   95: istore_1
        //   96: aload 4
        //   98: ldc 84
        //   100: iconst_0
        //   101: invokeinterface 82 3 0
        //   106: istore_2
        //   107: iload_2
        //   108: istore_1
        //   109: iload_2
        //   110: ifne +35 -> 145
        //   113: iload_3
        //   114: ldc 78
        //   116: if_icmpne +321 -> 437
        //   119: iload_2
        //   120: istore_1
        //   121: getstatic 90	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
        //   124: invokevirtual 96	android/content/Context:getResources	()Landroid/content/res/Resources;
        //   127: ldc 97
        //   129: invokevirtual 103	android/content/res/Resources:getDrawable	(I)Landroid/graphics/drawable/Drawable;
        //   132: invokestatic 67	org/telegram/ui/ActionBar/Theme:access$1102	(Landroid/graphics/drawable/Drawable;)Landroid/graphics/drawable/Drawable;
        //   135: pop
        //   136: iload_2
        //   137: istore_1
        //   138: iconst_0
        //   139: invokestatic 71	org/telegram/ui/ActionBar/Theme:access$1202	(Z)Z
        //   142: pop
        //   143: iload_2
        //   144: istore_1
        //   145: invokestatic 75	org/telegram/ui/ActionBar/Theme:access$1100	()Landroid/graphics/drawable/Drawable;
        //   148: ifnonnull +24 -> 172
        //   151: iload_1
        //   152: istore_2
        //   153: iload_1
        //   154: ifne +6 -> 160
        //   157: ldc 104
        //   159: istore_2
        //   160: new 56	android/graphics/drawable/ColorDrawable
        //   163: dup
        //   164: iload_2
        //   165: invokespecial 63	android/graphics/drawable/ColorDrawable:<init>	(I)V
        //   168: invokestatic 67	org/telegram/ui/ActionBar/Theme:access$1102	(Landroid/graphics/drawable/Drawable;)Landroid/graphics/drawable/Drawable;
        //   171: pop
        //   172: invokestatic 75	org/telegram/ui/ActionBar/Theme:access$1100	()Landroid/graphics/drawable/Drawable;
        //   175: iconst_1
        //   176: invokestatic 108	org/telegram/ui/ActionBar/Theme:access$1600	(Landroid/graphics/drawable/Drawable;I)V
        //   179: new 13	org/telegram/ui/ActionBar/Theme$11$1
        //   182: dup
        //   183: aload_0
        //   184: invokespecial 111	org/telegram/ui/ActionBar/Theme$11$1:<init>	(Lorg/telegram/ui/ActionBar/Theme$11;)V
        //   187: invokestatic 117	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
        //   190: aload 8
        //   192: monitorexit
        //   193: return
        //   194: invokestatic 120	org/telegram/ui/ActionBar/Theme:access$1300	()I
        //   197: ifle -136 -> 61
        //   200: invokestatic 124	org/telegram/ui/ActionBar/Theme:access$1400	()Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
        //   203: getfield 130	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
        //   206: ifnonnull +16 -> 222
        //   209: invokestatic 124	org/telegram/ui/ActionBar/Theme:access$1400	()Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
        //   212: getfield 133	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
        //   215: astore 4
        //   217: aload 4
        //   219: ifnull -158 -> 61
        //   222: aconst_null
        //   223: astore 7
        //   225: aconst_null
        //   226: astore 6
        //   228: aload 7
        //   230: astore 4
        //   232: invokestatic 124	org/telegram/ui/ActionBar/Theme:access$1400	()Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
        //   235: getfield 133	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
        //   238: ifnull +110 -> 348
        //   241: aload 7
        //   243: astore 4
        //   245: invokestatic 124	org/telegram/ui/ActionBar/Theme:access$1400	()Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
        //   248: getfield 133	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
        //   251: invokestatic 137	org/telegram/ui/ActionBar/Theme:getAssetFile	(Ljava/lang/String;)Ljava/io/File;
        //   254: astore 5
        //   256: aload 7
        //   258: astore 4
        //   260: new 139	java/io/FileInputStream
        //   263: dup
        //   264: aload 5
        //   266: invokespecial 142	java/io/FileInputStream:<init>	(Ljava/io/File;)V
        //   269: astore 5
        //   271: aload 5
        //   273: invokevirtual 146	java/io/FileInputStream:getChannel	()Ljava/nio/channels/FileChannel;
        //   276: invokestatic 120	org/telegram/ui/ActionBar/Theme:access$1300	()I
        //   279: i2l
        //   280: invokevirtual 152	java/nio/channels/FileChannel:position	(J)Ljava/nio/channels/FileChannel;
        //   283: pop
        //   284: aload 5
        //   286: invokestatic 158	android/graphics/BitmapFactory:decodeStream	(Ljava/io/InputStream;)Landroid/graphics/Bitmap;
        //   289: astore 4
        //   291: aload 4
        //   293: ifnull +24 -> 317
        //   296: new 160	android/graphics/drawable/BitmapDrawable
        //   299: dup
        //   300: aload 4
        //   302: invokespecial 163	android/graphics/drawable/BitmapDrawable:<init>	(Landroid/graphics/Bitmap;)V
        //   305: invokestatic 67	org/telegram/ui/ActionBar/Theme:access$1102	(Landroid/graphics/drawable/Drawable;)Landroid/graphics/drawable/Drawable;
        //   308: invokestatic 166	org/telegram/ui/ActionBar/Theme:access$1502	(Landroid/graphics/drawable/Drawable;)Landroid/graphics/drawable/Drawable;
        //   311: pop
        //   312: iconst_1
        //   313: invokestatic 71	org/telegram/ui/ActionBar/Theme:access$1202	(Z)Z
        //   316: pop
        //   317: aload 5
        //   319: ifnull -258 -> 61
        //   322: aload 5
        //   324: invokevirtual 169	java/io/FileInputStream:close	()V
        //   327: goto -266 -> 61
        //   330: astore 4
        //   332: aload 4
        //   334: invokestatic 175	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   337: goto -276 -> 61
        //   340: astore 4
        //   342: aload 8
        //   344: monitorexit
        //   345: aload 4
        //   347: athrow
        //   348: aload 7
        //   350: astore 4
        //   352: new 177	java/io/File
        //   355: dup
        //   356: invokestatic 124	org/telegram/ui/ActionBar/Theme:access$1400	()Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
        //   359: getfield 130	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
        //   362: invokespecial 180	java/io/File:<init>	(Ljava/lang/String;)V
        //   365: astore 5
        //   367: goto -111 -> 256
        //   370: astore 4
        //   372: aload 6
        //   374: astore 5
        //   376: aload 4
        //   378: astore 6
        //   380: aload 5
        //   382: astore 4
        //   384: aload 6
        //   386: invokestatic 175	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   389: aload 5
        //   391: ifnull -330 -> 61
        //   394: aload 5
        //   396: invokevirtual 169	java/io/FileInputStream:close	()V
        //   399: goto -338 -> 61
        //   402: astore 4
        //   404: aload 4
        //   406: invokestatic 175	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   409: goto -348 -> 61
        //   412: astore 5
        //   414: aload 4
        //   416: ifnull +8 -> 424
        //   419: aload 4
        //   421: invokevirtual 169	java/io/FileInputStream:close	()V
        //   424: aload 5
        //   426: athrow
        //   427: astore 4
        //   429: aload 4
        //   431: invokestatic 175	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   434: goto -10 -> 424
        //   437: iload_2
        //   438: istore_1
        //   439: new 177	java/io/File
        //   442: dup
        //   443: invokestatic 184	org/telegram/messenger/ApplicationLoader:getFilesDirFixed	()Ljava/io/File;
        //   446: ldc -70
        //   448: invokespecial 189	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
        //   451: astore 4
        //   453: iload_2
        //   454: istore_1
        //   455: aload 4
        //   457: invokevirtual 193	java/io/File:exists	()Z
        //   460: ifeq +29 -> 489
        //   463: iload_2
        //   464: istore_1
        //   465: aload 4
        //   467: invokevirtual 197	java/io/File:getAbsolutePath	()Ljava/lang/String;
        //   470: invokestatic 203	android/graphics/drawable/Drawable:createFromPath	(Ljava/lang/String;)Landroid/graphics/drawable/Drawable;
        //   473: invokestatic 67	org/telegram/ui/ActionBar/Theme:access$1102	(Landroid/graphics/drawable/Drawable;)Landroid/graphics/drawable/Drawable;
        //   476: pop
        //   477: iload_2
        //   478: istore_1
        //   479: iconst_1
        //   480: invokestatic 71	org/telegram/ui/ActionBar/Theme:access$1202	(Z)Z
        //   483: pop
        //   484: iload_2
        //   485: istore_1
        //   486: goto -341 -> 145
        //   489: iload_2
        //   490: istore_1
        //   491: getstatic 90	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
        //   494: invokevirtual 96	android/content/Context:getResources	()Landroid/content/res/Resources;
        //   497: ldc 97
        //   499: invokevirtual 103	android/content/res/Resources:getDrawable	(I)Landroid/graphics/drawable/Drawable;
        //   502: invokestatic 67	org/telegram/ui/ActionBar/Theme:access$1102	(Landroid/graphics/drawable/Drawable;)Landroid/graphics/drawable/Drawable;
        //   505: pop
        //   506: iload_2
        //   507: istore_1
        //   508: iconst_0
        //   509: invokestatic 71	org/telegram/ui/ActionBar/Theme:access$1202	(Z)Z
        //   512: pop
        //   513: iload_2
        //   514: istore_1
        //   515: goto -370 -> 145
        //   518: astore 6
        //   520: aload 5
        //   522: astore 4
        //   524: aload 6
        //   526: astore 5
        //   528: goto -114 -> 414
        //   531: astore 6
        //   533: goto -153 -> 380
        //   536: astore 4
        //   538: goto -393 -> 145
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	541	0	this	11
        //   74	441	1	i	int
        //   72	442	2	j	int
        //   93	24	3	k	int
        //   33	268	4	localObject1	Object
        //   330	3	4	localException1	Exception
        //   340	6	4	localObject2	Object
        //   350	1	4	localObject3	Object
        //   370	7	4	localThrowable1	Throwable
        //   382	1	4	localObject4	Object
        //   402	18	4	localException2	Exception
        //   427	3	4	localException3	Exception
        //   451	72	4	localObject5	Object
        //   536	1	4	localThrowable2	Throwable
        //   254	141	5	localObject6	Object
        //   412	109	5	localObject7	Object
        //   526	1	5	localObject8	Object
        //   226	159	6	localThrowable3	Throwable
        //   518	7	6	localObject9	Object
        //   531	1	6	localThrowable4	Throwable
        //   223	126	7	localObject10	Object
        //   3	340	8	localObject11	Object
        // Exception table:
        //   from	to	target	type
        //   322	327	330	java/lang/Exception
        //   8	35	340	finally
        //   40	61	340	finally
        //   61	66	340	finally
        //   75	80	340	finally
        //   82	94	340	finally
        //   96	107	340	finally
        //   121	136	340	finally
        //   138	143	340	finally
        //   145	151	340	finally
        //   160	172	340	finally
        //   172	193	340	finally
        //   194	217	340	finally
        //   322	327	340	finally
        //   332	337	340	finally
        //   342	345	340	finally
        //   394	399	340	finally
        //   404	409	340	finally
        //   419	424	340	finally
        //   424	427	340	finally
        //   429	434	340	finally
        //   439	453	340	finally
        //   455	463	340	finally
        //   465	477	340	finally
        //   479	484	340	finally
        //   491	506	340	finally
        //   508	513	340	finally
        //   232	241	370	java/lang/Throwable
        //   245	256	370	java/lang/Throwable
        //   260	271	370	java/lang/Throwable
        //   352	367	370	java/lang/Throwable
        //   394	399	402	java/lang/Exception
        //   232	241	412	finally
        //   245	256	412	finally
        //   260	271	412	finally
        //   352	367	412	finally
        //   384	389	412	finally
        //   419	424	427	java/lang/Exception
        //   271	291	518	finally
        //   296	317	518	finally
        //   271	291	531	java/lang/Throwable
        //   296	317	531	java/lang/Throwable
        //   75	80	536	java/lang/Throwable
        //   82	94	536	java/lang/Throwable
        //   96	107	536	java/lang/Throwable
        //   121	136	536	java/lang/Throwable
        //   138	143	536	java/lang/Throwable
        //   439	453	536	java/lang/Throwable
        //   455	463	536	java/lang/Throwable
        //   465	477	536	java/lang/Throwable
        //   479	484	536	java/lang/Throwable
        //   491	506	536	java/lang/Throwable
        //   508	513	536	java/lang/Throwable
      }
    });
  }
  
  public static void reloadWallpaper()
  {
    wallpaper = null;
    themedWallpaper = null;
    loadWallpaper();
  }
  
  public static void saveAutoNightThemeConfig()
  {
    SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
    localEditor.putInt("selectedAutoNightType", selectedAutoNightType);
    localEditor.putBoolean("autoNightScheduleByLocation", autoNightScheduleByLocation);
    localEditor.putFloat("autoNightBrighnessThreshold", autoNightBrighnessThreshold);
    localEditor.putInt("autoNightDayStartTime", autoNightDayStartTime);
    localEditor.putInt("autoNightDayEndTime", autoNightDayEndTime);
    localEditor.putInt("autoNightSunriseTime", autoNightSunriseTime);
    localEditor.putString("autoNightCityName", autoNightCityName);
    localEditor.putInt("autoNightSunsetTime", autoNightSunsetTime);
    localEditor.putLong("autoNightLocationLatitude3", Double.doubleToRawLongBits(autoNightLocationLatitude));
    localEditor.putLong("autoNightLocationLongitude3", Double.doubleToRawLongBits(autoNightLocationLongitude));
    localEditor.putInt("autoNightLastSunCheckDay", autoNightLastSunCheckDay);
    if (currentNightTheme != null) {
      localEditor.putString("nighttheme", currentNightTheme.name);
    }
    for (;;)
    {
      localEditor.commit();
      return;
      localEditor.remove("nighttheme");
    }
  }
  
  /* Error */
  public static void saveCurrentTheme(String paramString, boolean paramBoolean)
  {
    // Byte code:
    //   0: new 3570	java/lang/StringBuilder
    //   3: dup
    //   4: invokespecial 3571	java/lang/StringBuilder:<init>	()V
    //   7: astore 6
    //   9: getstatic 2229	org/telegram/ui/ActionBar/Theme:currentColors	Ljava/util/HashMap;
    //   12: invokevirtual 3575	java/util/HashMap:entrySet	()Ljava/util/Set;
    //   15: invokeinterface 3581 1 0
    //   20: astore_2
    //   21: aload_2
    //   22: invokeinterface 3586 1 0
    //   27: ifeq +52 -> 79
    //   30: aload_2
    //   31: invokeinterface 3589 1 0
    //   36: checkcast 3591	java/util/Map$Entry
    //   39: astore_3
    //   40: aload 6
    //   42: aload_3
    //   43: invokeinterface 3594 1 0
    //   48: checkcast 1955	java/lang/String
    //   51: invokevirtual 3598	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   54: ldc_w 3600
    //   57: invokevirtual 3598	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   60: aload_3
    //   61: invokeinterface 3603 1 0
    //   66: invokevirtual 3606	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   69: ldc_w 3608
    //   72: invokevirtual 3598	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   75: pop
    //   76: goto -55 -> 21
    //   79: new 2810	java/io/File
    //   82: dup
    //   83: invokestatic 2823	org/telegram/messenger/ApplicationLoader:getFilesDirFixed	()Ljava/io/File;
    //   86: aload_0
    //   87: invokespecial 2826	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   90: astore 5
    //   92: aconst_null
    //   93: astore_2
    //   94: aconst_null
    //   95: astore 4
    //   97: new 3610	java/io/FileOutputStream
    //   100: dup
    //   101: aload 5
    //   103: invokespecial 3611	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   106: astore_3
    //   107: aload_3
    //   108: aload 6
    //   110: invokevirtual 3614	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   113: invokevirtual 3618	java/lang/String:getBytes	()[B
    //   116: invokevirtual 3622	java/io/FileOutputStream:write	([B)V
    //   119: getstatic 2437	org/telegram/ui/ActionBar/Theme:themedWallpaper	Landroid/graphics/drawable/Drawable;
    //   122: instanceof 3146
    //   125: ifeq +113 -> 238
    //   128: getstatic 2437	org/telegram/ui/ActionBar/Theme:themedWallpaper	Landroid/graphics/drawable/Drawable;
    //   131: checkcast 3146	android/graphics/drawable/BitmapDrawable
    //   134: invokevirtual 3626	android/graphics/drawable/BitmapDrawable:getBitmap	()Landroid/graphics/Bitmap;
    //   137: astore_2
    //   138: aload_2
    //   139: ifnull +82 -> 221
    //   142: aload_3
    //   143: iconst_4
    //   144: newarray <illegal type>
    //   146: dup
    //   147: iconst_0
    //   148: ldc_w 3627
    //   151: bastore
    //   152: dup
    //   153: iconst_1
    //   154: ldc_w 3628
    //   157: bastore
    //   158: dup
    //   159: iconst_2
    //   160: ldc_w 3629
    //   163: bastore
    //   164: dup
    //   165: iconst_3
    //   166: ldc_w 3630
    //   169: bastore
    //   170: invokevirtual 3622	java/io/FileOutputStream:write	([B)V
    //   173: aload_2
    //   174: getstatic 3636	android/graphics/Bitmap$CompressFormat:JPEG	Landroid/graphics/Bitmap$CompressFormat;
    //   177: bipush 87
    //   179: aload_3
    //   180: invokevirtual 3640	android/graphics/Bitmap:compress	(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z
    //   183: pop
    //   184: aload_3
    //   185: iconst_5
    //   186: newarray <illegal type>
    //   188: dup
    //   189: iconst_0
    //   190: ldc_w 3630
    //   193: bastore
    //   194: dup
    //   195: iconst_1
    //   196: ldc_w 3627
    //   199: bastore
    //   200: dup
    //   201: iconst_2
    //   202: ldc_w 3628
    //   205: bastore
    //   206: dup
    //   207: iconst_3
    //   208: ldc_w 3641
    //   211: bastore
    //   212: dup
    //   213: iconst_4
    //   214: ldc_w 3630
    //   217: bastore
    //   218: invokevirtual 3622	java/io/FileOutputStream:write	([B)V
    //   221: iload_1
    //   222: ifeq +16 -> 238
    //   225: getstatic 2437	org/telegram/ui/ActionBar/Theme:themedWallpaper	Landroid/graphics/drawable/Drawable;
    //   228: putstatic 2424	org/telegram/ui/ActionBar/Theme:wallpaper	Landroid/graphics/drawable/Drawable;
    //   231: getstatic 2424	org/telegram/ui/ActionBar/Theme:wallpaper	Landroid/graphics/drawable/Drawable;
    //   234: iconst_2
    //   235: invokestatic 2442	org/telegram/ui/ActionBar/Theme:calcBackgroundColor	(Landroid/graphics/drawable/Drawable;I)V
    //   238: getstatic 2227	org/telegram/ui/ActionBar/Theme:themesDict	Ljava/util/HashMap;
    //   241: aload_0
    //   242: invokevirtual 2324	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   245: checkcast 30	org/telegram/ui/ActionBar/Theme$ThemeInfo
    //   248: astore 4
    //   250: aload 4
    //   252: astore_2
    //   253: aload 4
    //   255: ifnonnull +59 -> 314
    //   258: new 30	org/telegram/ui/ActionBar/Theme$ThemeInfo
    //   261: dup
    //   262: invokespecial 2230	org/telegram/ui/ActionBar/Theme$ThemeInfo:<init>	()V
    //   265: astore_2
    //   266: aload_2
    //   267: aload 5
    //   269: invokevirtual 2834	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   272: putfield 2785	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
    //   275: aload_2
    //   276: aload_0
    //   277: putfield 2235	org/telegram/ui/ActionBar/Theme$ThemeInfo:name	Ljava/lang/String;
    //   280: getstatic 2223	org/telegram/ui/ActionBar/Theme:themes	Ljava/util/ArrayList;
    //   283: aload_2
    //   284: invokevirtual 2245	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   287: pop
    //   288: getstatic 2227	org/telegram/ui/ActionBar/Theme:themesDict	Ljava/util/HashMap;
    //   291: aload_2
    //   292: getfield 2235	org/telegram/ui/ActionBar/Theme$ThemeInfo:name	Ljava/lang/String;
    //   295: aload_2
    //   296: invokevirtual 1986	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   299: pop
    //   300: getstatic 2225	org/telegram/ui/ActionBar/Theme:otherThemes	Ljava/util/ArrayList;
    //   303: aload_2
    //   304: invokevirtual 2245	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   307: pop
    //   308: invokestatic 2393	org/telegram/ui/ActionBar/Theme:saveOtherThemes	()V
    //   311: invokestatic 2312	org/telegram/ui/ActionBar/Theme:sortThemes	()V
    //   314: aload_2
    //   315: putstatic 2239	org/telegram/ui/ActionBar/Theme:currentTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   318: getstatic 2239	org/telegram/ui/ActionBar/Theme:currentTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   321: getstatic 2254	org/telegram/ui/ActionBar/Theme:currentNightTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   324: if_acmpeq +9 -> 333
    //   327: getstatic 2239	org/telegram/ui/ActionBar/Theme:currentTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   330: putstatic 2241	org/telegram/ui/ActionBar/Theme:currentDayTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   333: invokestatic 2318	org/telegram/messenger/MessagesController:getGlobalMainSettings	()Landroid/content/SharedPreferences;
    //   336: invokeinterface 2397 1 0
    //   341: astore_0
    //   342: aload_0
    //   343: ldc_w 2320
    //   346: getstatic 2241	org/telegram/ui/ActionBar/Theme:currentDayTheme	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   349: getfield 2235	org/telegram/ui/ActionBar/Theme$ThemeInfo:name	Ljava/lang/String;
    //   352: invokeinterface 2789 3 0
    //   357: pop
    //   358: aload_0
    //   359: invokeinterface 2407 1 0
    //   364: pop
    //   365: aload_3
    //   366: ifnull +7 -> 373
    //   369: aload_3
    //   370: invokevirtual 3642	java/io/FileOutputStream:close	()V
    //   373: return
    //   374: astore_0
    //   375: aload_0
    //   376: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   379: return
    //   380: astore_3
    //   381: aload 4
    //   383: astore_0
    //   384: aload_0
    //   385: astore_2
    //   386: aload_3
    //   387: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   390: aload_0
    //   391: ifnull -18 -> 373
    //   394: aload_0
    //   395: invokevirtual 3642	java/io/FileOutputStream:close	()V
    //   398: return
    //   399: astore_0
    //   400: aload_0
    //   401: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   404: return
    //   405: astore_0
    //   406: aload_2
    //   407: ifnull +7 -> 414
    //   410: aload_2
    //   411: invokevirtual 3642	java/io/FileOutputStream:close	()V
    //   414: aload_0
    //   415: athrow
    //   416: astore_2
    //   417: aload_2
    //   418: invokestatic 2309	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   421: goto -7 -> 414
    //   424: astore_0
    //   425: aload_3
    //   426: astore_2
    //   427: goto -21 -> 406
    //   430: astore_2
    //   431: aload_3
    //   432: astore_0
    //   433: aload_2
    //   434: astore_3
    //   435: goto -51 -> 384
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	438	0	paramString	String
    //   0	438	1	paramBoolean	boolean
    //   20	391	2	localObject1	Object
    //   416	2	2	localException1	Exception
    //   426	1	2	localObject2	Object
    //   430	4	2	localException2	Exception
    //   39	331	3	localObject3	Object
    //   380	52	3	localException3	Exception
    //   434	1	3	localException4	Exception
    //   95	287	4	localThemeInfo	ThemeInfo
    //   90	178	5	localFile	File
    //   7	102	6	localStringBuilder	StringBuilder
    // Exception table:
    //   from	to	target	type
    //   369	373	374	java/lang/Exception
    //   97	107	380	java/lang/Exception
    //   394	398	399	java/lang/Exception
    //   97	107	405	finally
    //   386	390	405	finally
    //   410	414	416	java/lang/Exception
    //   107	138	424	finally
    //   142	221	424	finally
    //   225	238	424	finally
    //   238	250	424	finally
    //   258	314	424	finally
    //   314	333	424	finally
    //   333	365	424	finally
    //   107	138	430	java/lang/Exception
    //   142	221	430	java/lang/Exception
    //   225	238	430	java/lang/Exception
    //   238	250	430	java/lang/Exception
    //   258	314	430	java/lang/Exception
    //   314	333	430	java/lang/Exception
    //   333	365	430	java/lang/Exception
  }
  
  private static void saveOtherThemes()
  {
    SharedPreferences.Editor localEditor = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", 0).edit();
    JSONArray localJSONArray = new JSONArray();
    int i = 0;
    while (i < otherThemes.size())
    {
      JSONObject localJSONObject = ((ThemeInfo)otherThemes.get(i)).getSaveJson();
      if (localJSONObject != null) {
        localJSONArray.put(localJSONObject);
      }
      i += 1;
    }
    localEditor.putString("themes2", localJSONArray.toString());
    localEditor.commit();
  }
  
  public static void setColor(String paramString, int paramInt, boolean paramBoolean)
  {
    int i = paramInt;
    if (paramString.equals("chat_wallpaper")) {
      i = paramInt | 0xFF000000;
    }
    if (paramBoolean)
    {
      currentColors.remove(paramString);
      if ((!paramString.equals("chat_serviceBackground")) && (!paramString.equals("chat_serviceBackgroundSelected"))) {
        break label68;
      }
      applyChatServiceMessageColor();
    }
    label68:
    while (!paramString.equals("chat_wallpaper"))
    {
      return;
      currentColors.put(paramString, Integer.valueOf(i));
      break;
    }
    reloadWallpaper();
  }
  
  public static void setCombinedDrawableColor(Drawable paramDrawable, int paramInt, boolean paramBoolean)
  {
    if (!(paramDrawable instanceof CombinedDrawable)) {
      return;
    }
    if (paramBoolean) {}
    for (paramDrawable = ((CombinedDrawable)paramDrawable).getIcon();; paramDrawable = ((CombinedDrawable)paramDrawable).getBackground())
    {
      paramDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      return;
    }
  }
  
  public static void setCurrentNightTheme(ThemeInfo paramThemeInfo)
  {
    if (currentTheme == currentNightTheme) {}
    for (int i = 1;; i = 0)
    {
      currentNightTheme = paramThemeInfo;
      if (i != 0) {
        applyDayNightThemeMaybe(true);
      }
      return;
    }
  }
  
  public static void setDrawableColor(Drawable paramDrawable, int paramInt)
  {
    if (paramDrawable == null) {
      return;
    }
    if ((paramDrawable instanceof ShapeDrawable))
    {
      ((ShapeDrawable)paramDrawable).getPaint().setColor(paramInt);
      return;
    }
    paramDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
  }
  
  public static void setDrawableColorByKey(Drawable paramDrawable, String paramString)
  {
    if (paramString == null) {
      return;
    }
    setDrawableColor(paramDrawable, getColor(paramString));
  }
  
  public static void setEmojiDrawableColor(Drawable paramDrawable, int paramInt, boolean paramBoolean)
  {
    if ((!(paramDrawable instanceof StateListDrawable)) || (paramBoolean)) {}
    try
    {
      paramDrawable = getStateDrawable(paramDrawable, 0);
      if ((paramDrawable instanceof ShapeDrawable))
      {
        ((ShapeDrawable)paramDrawable).getPaint().setColor(paramInt);
        return;
      }
      paramDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      return;
    }
    catch (Throwable paramDrawable) {}
    paramDrawable = getStateDrawable(paramDrawable, 1);
    if ((paramDrawable instanceof ShapeDrawable))
    {
      ((ShapeDrawable)paramDrawable).getPaint().setColor(paramInt);
      return;
    }
    paramDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
    return;
  }
  
  public static void setSelectorDrawableColor(Drawable paramDrawable, int paramInt, boolean paramBoolean)
  {
    if ((!(paramDrawable instanceof StateListDrawable)) || (paramBoolean)) {}
    try
    {
      Drawable localDrawable = getStateDrawable(paramDrawable, 0);
      if ((localDrawable instanceof ShapeDrawable)) {
        ((ShapeDrawable)localDrawable).getPaint().setColor(paramInt);
      }
      for (;;)
      {
        paramDrawable = getStateDrawable(paramDrawable, 1);
        if (!(paramDrawable instanceof ShapeDrawable)) {
          break;
        }
        ((ShapeDrawable)paramDrawable).getPaint().setColor(paramInt);
        return;
        localDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      }
      paramDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      return;
    }
    catch (Throwable paramDrawable) {}
    paramDrawable = getStateDrawable(paramDrawable, 2);
    if ((paramDrawable instanceof ShapeDrawable))
    {
      ((ShapeDrawable)paramDrawable).getPaint().setColor(paramInt);
      return;
    }
    paramDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
    return;
    if ((Build.VERSION.SDK_INT >= 21) && ((paramDrawable instanceof RippleDrawable)))
    {
      paramDrawable = (RippleDrawable)paramDrawable;
      if (paramBoolean)
      {
        paramDrawable.setColor(new ColorStateList(new int[][] { StateSet.WILD_CARD }, new int[] { paramInt }));
        return;
      }
      if (paramDrawable.getNumberOfLayers() > 0)
      {
        paramDrawable = paramDrawable.getDrawable(0);
        if ((paramDrawable instanceof ShapeDrawable))
        {
          ((ShapeDrawable)paramDrawable).getPaint().setColor(paramInt);
          return;
        }
        paramDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      }
    }
    return;
  }
  
  public static void setThemeWallpaper(String paramString, Bitmap paramBitmap, File paramFile)
  {
    currentColors.remove("chat_wallpaper");
    MessagesController.getGlobalMainSettings().edit().remove("overrideThemeWallpaper").commit();
    if (paramBitmap != null)
    {
      themedWallpaper = new BitmapDrawable(paramBitmap);
      saveCurrentTheme(paramString, false);
      calcBackgroundColor(themedWallpaper, 0);
      applyChatServiceMessageColor();
      NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper, new Object[0]);
      return;
    }
    themedWallpaper = null;
    wallpaper = null;
    saveCurrentTheme(paramString, false);
    reloadWallpaper();
  }
  
  private static void sortThemes()
  {
    Collections.sort(themes, new Comparator()
    {
      public int compare(Theme.ThemeInfo paramAnonymousThemeInfo1, Theme.ThemeInfo paramAnonymousThemeInfo2)
      {
        if ((paramAnonymousThemeInfo1.pathToFile == null) && (paramAnonymousThemeInfo1.assetName == null)) {
          return -1;
        }
        if ((paramAnonymousThemeInfo2.pathToFile == null) && (paramAnonymousThemeInfo2.assetName == null)) {
          return 1;
        }
        return paramAnonymousThemeInfo1.name.compareTo(paramAnonymousThemeInfo2.name);
      }
    });
  }
  
  public static class ThemeInfo
  {
    public String assetName;
    public String name;
    public String pathToFile;
    
    public static ThemeInfo createWithJson(JSONObject paramJSONObject)
    {
      if (paramJSONObject == null) {
        return null;
      }
      try
      {
        ThemeInfo localThemeInfo = new ThemeInfo();
        localThemeInfo.name = paramJSONObject.getString("name");
        localThemeInfo.pathToFile = paramJSONObject.getString("path");
        return localThemeInfo;
      }
      catch (Exception paramJSONObject)
      {
        FileLog.e(paramJSONObject);
      }
      return null;
    }
    
    public static ThemeInfo createWithString(String paramString)
    {
      if (TextUtils.isEmpty(paramString)) {}
      do
      {
        return null;
        paramString = paramString.split("\\|");
      } while (paramString.length != 2);
      ThemeInfo localThemeInfo = new ThemeInfo();
      localThemeInfo.name = paramString[0];
      localThemeInfo.pathToFile = paramString[1];
      return localThemeInfo;
    }
    
    public String getName()
    {
      if ("Default".equals(this.name)) {
        return LocaleController.getString("Default", 2131493354);
      }
      if ("Blue".equals(this.name)) {
        return LocaleController.getString("ThemeBlue", 2131494483);
      }
      if ("Dark".equals(this.name)) {
        return LocaleController.getString("ThemeDark", 2131494484);
      }
      return this.name;
    }
    
    public JSONObject getSaveJson()
    {
      try
      {
        JSONObject localJSONObject = new JSONObject();
        localJSONObject.put("name", this.name);
        localJSONObject.put("path", this.pathToFile);
        return localJSONObject;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
      return null;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ActionBar/Theme.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */