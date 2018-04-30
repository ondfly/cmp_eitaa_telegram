package org.telegram.messenger.exoplayer2.text.subrip;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.messenger.exoplayer2.text.Cue;
import org.telegram.messenger.exoplayer2.text.SimpleSubtitleDecoder;
import org.telegram.messenger.exoplayer2.util.LongArray;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;

public final class SubripDecoder
  extends SimpleSubtitleDecoder
{
  private static final String SUBRIP_TIMECODE = "(?:(\\d+):)?(\\d+):(\\d+),(\\d+)";
  private static final Pattern SUBRIP_TIMING_LINE = Pattern.compile("\\s*((?:(\\d+):)?(\\d+):(\\d+),(\\d+))\\s*-->\\s*((?:(\\d+):)?(\\d+):(\\d+),(\\d+))?\\s*");
  private static final String TAG = "SubripDecoder";
  private final StringBuilder textBuilder = new StringBuilder();
  
  public SubripDecoder()
  {
    super("SubripDecoder");
  }
  
  private static long parseTimecode(Matcher paramMatcher, int paramInt)
  {
    return (Long.parseLong(paramMatcher.group(paramInt + 1)) * 60L * 60L * 1000L + Long.parseLong(paramMatcher.group(paramInt + 2)) * 60L * 1000L + Long.parseLong(paramMatcher.group(paramInt + 3)) * 1000L + Long.parseLong(paramMatcher.group(paramInt + 4))) * 1000L;
  }
  
  protected SubripSubtitle decode(byte[] paramArrayOfByte, int paramInt, boolean paramBoolean)
  {
    ArrayList localArrayList = new ArrayList();
    LongArray localLongArray = new LongArray();
    paramArrayOfByte = new ParsableByteArray(paramArrayOfByte, paramInt);
    for (;;)
    {
      String str = paramArrayOfByte.readLine();
      if ((str == null) || (str.length() != 0))
      {
        try
        {
          Integer.parseInt(str);
          paramInt = 0;
          str = paramArrayOfByte.readLine();
          if (str != null) {
            break label135;
          }
          Log.w("SubripDecoder", "Unexpected end");
          paramArrayOfByte = new Cue[localArrayList.size()];
          localArrayList.toArray(paramArrayOfByte);
          return new SubripSubtitle(paramArrayOfByte, localLongArray.toArray());
        }
        catch (NumberFormatException localNumberFormatException)
        {
          Log.w("SubripDecoder", "Skipping invalid index: " + str);
        }
        continue;
        label135:
        Matcher localMatcher = SUBRIP_TIMING_LINE.matcher(str);
        if (localMatcher.matches())
        {
          localLongArray.add(parseTimecode(localMatcher, 1));
          if (!TextUtils.isEmpty(localMatcher.group(6)))
          {
            paramInt = 1;
            localLongArray.add(parseTimecode(localMatcher, 6));
          }
          this.textBuilder.setLength(0);
          for (;;)
          {
            str = paramArrayOfByte.readLine();
            if (TextUtils.isEmpty(str)) {
              break;
            }
            if (this.textBuilder.length() > 0) {
              this.textBuilder.append("<br>");
            }
            this.textBuilder.append(str.trim());
          }
        }
        Log.w("SubripDecoder", "Skipping invalid timing: " + str);
        continue;
        localArrayList.add(new Cue(Html.fromHtml(this.textBuilder.toString())));
        if (paramInt != 0) {
          localArrayList.add(null);
        }
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/subrip/SubripDecoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */