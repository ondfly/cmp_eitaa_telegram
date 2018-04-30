package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.IdenticonDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.URLSpanReplacement;

public class IdenticonActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private AnimatorSet animatorSet;
  private int chat_id;
  private TextView codeTextView;
  private FrameLayout container;
  private boolean emojiSelected;
  private String emojiText;
  private TextView emojiTextView;
  private AnimatorSet hintAnimatorSet;
  private LinearLayout linearLayout;
  private LinearLayout linearLayout1;
  private TextView textView;
  private int textWidth;
  
  public IdenticonActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  private void fixLayout()
  {
    this.fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
    {
      public boolean onPreDraw()
      {
        if (IdenticonActivity.this.fragmentView == null) {
          return true;
        }
        IdenticonActivity.this.fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
        int i = ((WindowManager)ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
        if ((i == 3) || (i == 1)) {
          IdenticonActivity.this.linearLayout.setOrientation(0);
        }
        for (;;)
        {
          IdenticonActivity.this.fragmentView.setPadding(IdenticonActivity.this.fragmentView.getPaddingLeft(), 0, IdenticonActivity.this.fragmentView.getPaddingRight(), IdenticonActivity.this.fragmentView.getPaddingBottom());
          return true;
          IdenticonActivity.this.linearLayout.setOrientation(1);
        }
      }
    });
  }
  
  private void updateEmojiButton(boolean paramBoolean)
  {
    float f3 = 0.0F;
    float f2 = 0.0F;
    if (this.animatorSet != null)
    {
      this.animatorSet.cancel();
      this.animatorSet = null;
    }
    Object localObject2;
    float f1;
    if (paramBoolean)
    {
      this.animatorSet = new AnimatorSet();
      localObject1 = this.animatorSet;
      localObject2 = this.emojiTextView;
      if (this.emojiSelected)
      {
        f1 = 1.0F;
        localObject2 = ObjectAnimator.ofFloat(localObject2, "alpha", new float[] { f1 });
        Object localObject3 = this.codeTextView;
        if (!this.emojiSelected) {
          break label344;
        }
        f1 = 0.0F;
        label91:
        localObject3 = ObjectAnimator.ofFloat(localObject3, "alpha", new float[] { f1 });
        Object localObject4 = this.emojiTextView;
        if (!this.emojiSelected) {
          break label349;
        }
        f1 = 1.0F;
        label122:
        localObject4 = ObjectAnimator.ofFloat(localObject4, "scaleX", new float[] { f1 });
        Object localObject5 = this.emojiTextView;
        if (!this.emojiSelected) {
          break label354;
        }
        f1 = 1.0F;
        label153:
        localObject5 = ObjectAnimator.ofFloat(localObject5, "scaleY", new float[] { f1 });
        Object localObject6 = this.codeTextView;
        if (!this.emojiSelected) {
          break label359;
        }
        f1 = 0.0F;
        label184:
        localObject6 = ObjectAnimator.ofFloat(localObject6, "scaleX", new float[] { f1 });
        TextView localTextView = this.codeTextView;
        if (!this.emojiSelected) {
          break label364;
        }
        f1 = f2;
        label215:
        ((AnimatorSet)localObject1).playTogether(new Animator[] { localObject2, localObject3, localObject4, localObject5, localObject6, ObjectAnimator.ofFloat(localTextView, "scaleY", new float[] { f1 }) });
        this.animatorSet.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            if (paramAnonymousAnimator.equals(IdenticonActivity.this.animatorSet)) {
              IdenticonActivity.access$402(IdenticonActivity.this, null);
            }
          }
        });
        this.animatorSet.setInterpolator(new DecelerateInterpolator());
        this.animatorSet.setDuration(150L);
        this.animatorSet.start();
        localObject2 = this.emojiTextView;
        if (this.emojiSelected) {
          break label529;
        }
      }
    }
    label344:
    label349:
    label354:
    label359:
    label364:
    label384:
    label405:
    label426:
    label447:
    label468:
    label504:
    label509:
    label514:
    label519:
    label524:
    label529:
    for (Object localObject1 = "chat_emojiPanelIcon";; localObject1 = "chat_emojiPanelIconSelected")
    {
      ((TextView)localObject2).setTag(localObject1);
      return;
      f1 = 0.0F;
      break;
      f1 = 1.0F;
      break label91;
      f1 = 0.0F;
      break label122;
      f1 = 0.0F;
      break label153;
      f1 = 1.0F;
      break label184;
      f1 = 1.0F;
      break label215;
      localObject1 = this.emojiTextView;
      if (this.emojiSelected)
      {
        f1 = 1.0F;
        ((TextView)localObject1).setAlpha(f1);
        localObject1 = this.codeTextView;
        if (!this.emojiSelected) {
          break label504;
        }
        f1 = 0.0F;
        ((TextView)localObject1).setAlpha(f1);
        localObject1 = this.emojiTextView;
        if (!this.emojiSelected) {
          break label509;
        }
        f1 = 1.0F;
        ((TextView)localObject1).setScaleX(f1);
        localObject1 = this.emojiTextView;
        if (!this.emojiSelected) {
          break label514;
        }
        f1 = 1.0F;
        ((TextView)localObject1).setScaleY(f1);
        localObject1 = this.codeTextView;
        if (!this.emojiSelected) {
          break label519;
        }
        f1 = 0.0F;
        ((TextView)localObject1).setScaleX(f1);
        localObject1 = this.codeTextView;
        if (!this.emojiSelected) {
          break label524;
        }
      }
      for (f1 = f3;; f1 = 1.0F)
      {
        ((TextView)localObject1).setScaleY(f1);
        break;
        f1 = 0.0F;
        break label384;
        f1 = 1.0F;
        break label405;
        f1 = 0.0F;
        break label426;
        f1 = 0.0F;
        break label447;
        f1 = 1.0F;
        break label468;
      }
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("EncryptionKey", 2131493438));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          IdenticonActivity.this.finishFragment();
        }
      }
    });
    this.fragmentView = new FrameLayout(paramContext);
    Object localObject1 = (FrameLayout)this.fragmentView;
    this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.fragmentView.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    });
    this.linearLayout = new LinearLayout(paramContext);
    this.linearLayout.setOrientation(1);
    this.linearLayout.setWeightSum(100.0F);
    ((FrameLayout)localObject1).addView(this.linearLayout, LayoutHelper.createFrame(-1, -1.0F));
    Object localObject2 = new FrameLayout(paramContext);
    ((FrameLayout)localObject2).setPadding(AndroidUtilities.dp(20.0F), AndroidUtilities.dp(20.0F), AndroidUtilities.dp(20.0F), AndroidUtilities.dp(20.0F));
    this.linearLayout.addView((View)localObject2, LayoutHelper.createLinear(-1, -1, 50.0F));
    localObject1 = new ImageView(paramContext);
    ((ImageView)localObject1).setScaleType(ImageView.ScaleType.FIT_XY);
    ((FrameLayout)localObject2).addView((View)localObject1, LayoutHelper.createFrame(-1, -1.0F));
    this.container = new FrameLayout(paramContext)
    {
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
        if (IdenticonActivity.this.codeTextView != null)
        {
          paramAnonymousInt1 = IdenticonActivity.this.codeTextView.getLeft() + IdenticonActivity.this.codeTextView.getMeasuredWidth() / 2 - IdenticonActivity.this.emojiTextView.getMeasuredWidth() / 2;
          paramAnonymousInt2 = (IdenticonActivity.this.codeTextView.getMeasuredHeight() - IdenticonActivity.this.emojiTextView.getMeasuredHeight()) / 2 + IdenticonActivity.this.linearLayout1.getTop() - AndroidUtilities.dp(16.0F);
          IdenticonActivity.this.emojiTextView.layout(paramAnonymousInt1, paramAnonymousInt2, IdenticonActivity.this.emojiTextView.getMeasuredWidth() + paramAnonymousInt1, IdenticonActivity.this.emojiTextView.getMeasuredHeight() + paramAnonymousInt2);
        }
      }
    };
    this.container.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
    this.linearLayout.addView(this.container, LayoutHelper.createLinear(-1, -1, 50.0F));
    this.linearLayout1 = new LinearLayout(paramContext);
    this.linearLayout1.setOrientation(1);
    this.linearLayout1.setPadding(AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(10.0F), 0);
    this.container.addView(this.linearLayout1, LayoutHelper.createFrame(-2, -2, 17));
    this.codeTextView = new TextView(paramContext);
    this.codeTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText4"));
    this.codeTextView.setGravity(17);
    this.codeTextView.setTypeface(Typeface.MONOSPACE);
    this.codeTextView.setTextSize(1, 16.0F);
    this.linearLayout1.addView(this.codeTextView, LayoutHelper.createLinear(-2, -2, 1));
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText4"));
    this.textView.setLinkTextColor(Theme.getColor("windowBackgroundWhiteLinkText"));
    this.textView.setTextSize(1, 16.0F);
    this.textView.setLinksClickable(true);
    this.textView.setClickable(true);
    this.textView.setGravity(17);
    this.textView.setMovementMethod(new LinkMovementMethodMy(null));
    this.linearLayout1.addView(this.textView, LayoutHelper.createFrame(-2, -2, 1));
    this.emojiTextView = new TextView(paramContext);
    this.emojiTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText4"));
    this.emojiTextView.setGravity(17);
    this.emojiTextView.setTextSize(1, 32.0F);
    this.container.addView(this.emojiTextView, LayoutHelper.createFrame(-2, -2.0F));
    paramContext = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(this.chat_id));
    if (paramContext != null)
    {
      localObject2 = new IdenticonDrawable();
      ((ImageView)localObject1).setImageDrawable((Drawable)localObject2);
      ((IdenticonDrawable)localObject2).setEncryptedChat(paramContext);
      localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramContext.user_id));
      localObject2 = new SpannableStringBuilder();
      StringBuilder localStringBuilder = new StringBuilder();
      if (paramContext.key_hash.length > 16)
      {
        String str = Utilities.bytesToHex(paramContext.key_hash);
        i = 0;
        if (i < 32)
        {
          if (i != 0)
          {
            if (i % 8 != 0) {
              break label732;
            }
            ((SpannableStringBuilder)localObject2).append('\n');
          }
          for (;;)
          {
            ((SpannableStringBuilder)localObject2).append(str.substring(i * 2, i * 2 + 2));
            ((SpannableStringBuilder)localObject2).append(' ');
            i += 1;
            break;
            label732:
            if (i % 4 == 0) {
              ((SpannableStringBuilder)localObject2).append(' ');
            }
          }
        }
        ((SpannableStringBuilder)localObject2).append("\n");
        i = 0;
        while (i < 5)
        {
          int j = paramContext.key_hash[(i * 4 + 16)];
          int k = paramContext.key_hash[(i * 4 + 16 + 1)];
          int m = paramContext.key_hash[(i * 4 + 16 + 2)];
          int n = paramContext.key_hash[(i * 4 + 16 + 3)];
          if (i != 0) {
            localStringBuilder.append(" ");
          }
          localStringBuilder.append(org.telegram.messenger.EmojiData.emojiSecret[(((j & 0x7F) << 24 | (k & 0xFF) << 16 | (m & 0xFF) << 8 | n & 0xFF) % org.telegram.messenger.EmojiData.emojiSecret.length)]);
          i += 1;
        }
        this.emojiText = localStringBuilder.toString();
      }
      this.codeTextView.setText(((SpannableStringBuilder)localObject2).toString());
      ((SpannableStringBuilder)localObject2).clear();
      ((SpannableStringBuilder)localObject2).append(AndroidUtilities.replaceTags(LocaleController.formatString("EncryptionKeyDescription", 2131493439, new Object[] { ((TLRPC.User)localObject1).first_name, ((TLRPC.User)localObject1).first_name })));
      int i = ((SpannableStringBuilder)localObject2).toString().indexOf("telegram.org");
      if (i != -1) {
        ((SpannableStringBuilder)localObject2).setSpan(new URLSpanReplacement(LocaleController.getString("EncryptionKeyLink", 2131493440)), i, "telegram.org".length() + i, 33);
      }
      this.textView.setText((CharSequence)localObject2);
    }
    updateEmojiButton(false);
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if ((paramInt1 == NotificationCenter.emojiDidLoaded) && (this.emojiTextView != null)) {
      this.emojiTextView.invalidate();
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    return new ThemeDescription[] { new ThemeDescription(this.container, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(this.textView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.codeTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.textView, ThemeDescription.FLAG_LINKCOLOR, null, null, null, null, "windowBackgroundWhiteLinkText") };
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    fixLayout();
  }
  
  public boolean onFragmentCreate()
  {
    this.chat_id = getArguments().getInt("chat_id");
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
  }
  
  public void onResume()
  {
    super.onResume();
    fixLayout();
  }
  
  protected void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean1) && (!paramBoolean2) && (this.emojiText != null)) {
      this.emojiTextView.setText(Emoji.replaceEmoji(this.emojiText, this.emojiTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(32.0F), false));
    }
  }
  
  private static class LinkMovementMethodMy
    extends LinkMovementMethod
  {
    public boolean onTouchEvent(TextView paramTextView, Spannable paramSpannable, MotionEvent paramMotionEvent)
    {
      try
      {
        boolean bool = super.onTouchEvent(paramTextView, paramSpannable, paramMotionEvent);
        return bool;
      }
      catch (Exception paramTextView)
      {
        FileLog.e(paramTextView);
      }
      return false;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/IdenticonActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */