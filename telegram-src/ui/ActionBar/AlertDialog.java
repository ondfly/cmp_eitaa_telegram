package org.telegram.ui.ActionBar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LineProgressView;
import org.telegram.ui.Components.RadialProgressView;

public class AlertDialog
  extends Dialog
  implements Drawable.Callback
{
  private Rect backgroundPaddings = new Rect();
  private FrameLayout buttonsLayout;
  private ScrollView contentScrollView;
  private int currentProgress;
  private View customView;
  private int customViewOffset = 20;
  private int[] itemIcons;
  private CharSequence[] items;
  private int lastScreenWidth;
  private LineProgressView lineProgressView;
  private TextView lineProgressViewPercent;
  private CharSequence message;
  private TextView messageTextView;
  private DialogInterface.OnClickListener negativeButtonListener;
  private CharSequence negativeButtonText;
  private DialogInterface.OnClickListener neutralButtonListener;
  private CharSequence neutralButtonText;
  private DialogInterface.OnClickListener onBackButtonListener;
  private DialogInterface.OnClickListener onClickListener;
  private DialogInterface.OnDismissListener onDismissListener;
  private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;
  private DialogInterface.OnClickListener positiveButtonListener;
  private CharSequence positiveButtonText;
  private FrameLayout progressViewContainer;
  private int progressViewStyle;
  private TextView progressViewTextView;
  private LinearLayout scrollContainer;
  private BitmapDrawable[] shadow = new BitmapDrawable[2];
  private AnimatorSet[] shadowAnimation = new AnimatorSet[2];
  private Drawable shadowDrawable;
  private boolean[] shadowVisibility = new boolean[2];
  private CharSequence subtitle;
  private TextView subtitleTextView;
  private CharSequence title;
  private TextView titleTextView;
  private int topBackgroundColor;
  private Drawable topDrawable;
  private ImageView topImageView;
  private int topResId;
  
  public AlertDialog(Context paramContext, int paramInt)
  {
    super(paramContext, 2131558424);
    this.shadowDrawable = paramContext.getResources().getDrawable(2131165610).mutate();
    this.shadowDrawable.setColorFilter(new PorterDuffColorFilter(getThemeColor("dialogBackground"), PorterDuff.Mode.MULTIPLY));
    this.shadowDrawable.getPadding(this.backgroundPaddings);
    this.progressViewStyle = paramInt;
  }
  
  private boolean canTextInput(View paramView)
  {
    if (paramView.onCheckIsTextEditor()) {
      return true;
    }
    if (!(paramView instanceof ViewGroup)) {
      return false;
    }
    paramView = (ViewGroup)paramView;
    int i = paramView.getChildCount();
    while (i > 0)
    {
      int j = i - 1;
      i = j;
      if (canTextInput(paramView.getChildAt(j))) {
        return true;
      }
    }
    return false;
  }
  
  private void runShadowAnimation(final int paramInt, boolean paramBoolean)
  {
    AnimatorSet localAnimatorSet;
    BitmapDrawable localBitmapDrawable;
    if (((paramBoolean) && (this.shadowVisibility[paramInt] == 0)) || ((!paramBoolean) && (this.shadowVisibility[paramInt] != 0)))
    {
      this.shadowVisibility[paramInt] = paramBoolean;
      if (this.shadowAnimation[paramInt] != null) {
        this.shadowAnimation[paramInt].cancel();
      }
      this.shadowAnimation[paramInt] = new AnimatorSet();
      if (this.shadow[paramInt] != null)
      {
        localAnimatorSet = this.shadowAnimation[paramInt];
        localBitmapDrawable = this.shadow[paramInt];
        if (!paramBoolean) {
          break label165;
        }
      }
    }
    for (int i = 255;; i = 0)
    {
      localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofInt(localBitmapDrawable, "alpha", new int[] { i }) });
      this.shadowAnimation[paramInt].setDuration(150L);
      this.shadowAnimation[paramInt].addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if ((AlertDialog.this.shadowAnimation[paramInt] != null) && (AlertDialog.this.shadowAnimation[paramInt].equals(paramAnonymousAnimator))) {
            AlertDialog.this.shadowAnimation[paramInt] = null;
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((AlertDialog.this.shadowAnimation[paramInt] != null) && (AlertDialog.this.shadowAnimation[paramInt].equals(paramAnonymousAnimator))) {
            AlertDialog.this.shadowAnimation[paramInt] = null;
          }
        }
      });
      try
      {
        this.shadowAnimation[paramInt].start();
        return;
      }
      catch (Exception localException)
      {
        label165:
        FileLog.e(localException);
      }
    }
  }
  
  private void updateLineProgressTextView()
  {
    this.lineProgressViewPercent.setText(String.format("%d%%", new Object[] { Integer.valueOf(this.currentProgress) }));
  }
  
  public void dismiss()
  {
    super.dismiss();
  }
  
  public View getButton(int paramInt)
  {
    return this.buttonsLayout.findViewWithTag(Integer.valueOf(paramInt));
  }
  
  protected int getThemeColor(String paramString)
  {
    return Theme.getColor(paramString);
  }
  
  public void invalidateDrawable(Drawable paramDrawable)
  {
    this.contentScrollView.invalidate();
    this.scrollContainer.invalidate();
  }
  
  public void onBackPressed()
  {
    super.onBackPressed();
    if (this.onBackButtonListener != null) {
      this.onBackButtonListener.onClick(this, -2);
    }
  }
  
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    paramBundle = new LinearLayout(getContext())
    {
      private boolean inLayout;
      
      public boolean hasOverlappingRendering()
      {
        return false;
      }
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
        if (AlertDialog.this.contentScrollView != null)
        {
          if (AlertDialog.this.onScrollChangedListener == null)
          {
            AlertDialog.access$1402(AlertDialog.this, new ViewTreeObserver.OnScrollChangedListener()
            {
              public void onScrollChanged()
              {
                boolean bool2 = false;
                AlertDialog localAlertDialog = AlertDialog.this;
                if ((AlertDialog.this.titleTextView != null) && (AlertDialog.this.contentScrollView.getScrollY() > AlertDialog.this.scrollContainer.getTop())) {}
                for (boolean bool1 = true;; bool1 = false)
                {
                  localAlertDialog.runShadowAnimation(0, bool1);
                  localAlertDialog = AlertDialog.this;
                  bool1 = bool2;
                  if (AlertDialog.this.buttonsLayout != null)
                  {
                    bool1 = bool2;
                    if (AlertDialog.this.contentScrollView.getScrollY() + AlertDialog.this.contentScrollView.getHeight() < AlertDialog.this.scrollContainer.getBottom()) {
                      bool1 = true;
                    }
                  }
                  localAlertDialog.runShadowAnimation(1, bool1);
                  AlertDialog.this.contentScrollView.invalidate();
                  return;
                }
              }
            });
            AlertDialog.this.contentScrollView.getViewTreeObserver().addOnScrollChangedListener(AlertDialog.this.onScrollChangedListener);
          }
          AlertDialog.this.onScrollChangedListener.onScrollChanged();
        }
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        this.inLayout = true;
        int k = View.MeasureSpec.getSize(paramAnonymousInt1);
        int j = View.MeasureSpec.getSize(paramAnonymousInt2) - getPaddingTop() - getPaddingBottom();
        int i = j;
        int i1 = k - getPaddingLeft() - getPaddingRight();
        int n = View.MeasureSpec.makeMeasureSpec(i1 - AndroidUtilities.dp(48.0F), 1073741824);
        int m = View.MeasureSpec.makeMeasureSpec(i1, 1073741824);
        paramAnonymousInt1 = i;
        LinearLayout.LayoutParams localLayoutParams;
        if (AlertDialog.this.buttonsLayout != null)
        {
          int i2 = AlertDialog.this.buttonsLayout.getChildCount();
          paramAnonymousInt1 = 0;
          while (paramAnonymousInt1 < i2)
          {
            ((TextView)AlertDialog.this.buttonsLayout.getChildAt(paramAnonymousInt1)).setMaxWidth(AndroidUtilities.dp((i1 - AndroidUtilities.dp(24.0F)) / 2));
            paramAnonymousInt1 += 1;
          }
          AlertDialog.this.buttonsLayout.measure(m, paramAnonymousInt2);
          localLayoutParams = (LinearLayout.LayoutParams)AlertDialog.this.buttonsLayout.getLayoutParams();
          paramAnonymousInt1 = i - (AlertDialog.this.buttonsLayout.getMeasuredHeight() + localLayoutParams.bottomMargin + localLayoutParams.topMargin);
        }
        i = paramAnonymousInt1;
        if (AlertDialog.this.titleTextView != null)
        {
          AlertDialog.this.titleTextView.measure(n, paramAnonymousInt2);
          localLayoutParams = (LinearLayout.LayoutParams)AlertDialog.this.titleTextView.getLayoutParams();
          i = paramAnonymousInt1 - (AlertDialog.this.titleTextView.getMeasuredHeight() + localLayoutParams.bottomMargin + localLayoutParams.topMargin);
        }
        paramAnonymousInt1 = i;
        if (AlertDialog.this.subtitleTextView != null)
        {
          AlertDialog.this.subtitleTextView.measure(n, paramAnonymousInt2);
          localLayoutParams = (LinearLayout.LayoutParams)AlertDialog.this.subtitleTextView.getLayoutParams();
          paramAnonymousInt1 = i - (AlertDialog.this.subtitleTextView.getMeasuredHeight() + localLayoutParams.bottomMargin + localLayoutParams.topMargin);
        }
        paramAnonymousInt2 = paramAnonymousInt1;
        if (AlertDialog.this.topImageView != null)
        {
          AlertDialog.this.topImageView.measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(132.0F), 1073741824));
          paramAnonymousInt2 = paramAnonymousInt1 - (AlertDialog.this.topImageView.getMeasuredHeight() - AndroidUtilities.dp(8.0F));
        }
        if (AlertDialog.this.progressViewStyle == 0)
        {
          localLayoutParams = (LinearLayout.LayoutParams)AlertDialog.this.contentScrollView.getLayoutParams();
          if (AlertDialog.this.customView != null) {
            if ((AlertDialog.this.titleTextView == null) && (AlertDialog.this.messageTextView.getVisibility() == 8) && (AlertDialog.this.items == null))
            {
              paramAnonymousInt1 = AndroidUtilities.dp(16.0F);
              localLayoutParams.topMargin = paramAnonymousInt1;
              if (AlertDialog.this.buttonsLayout != null) {
                break label586;
              }
              paramAnonymousInt1 = AndroidUtilities.dp(8.0F);
              label477:
              localLayoutParams.bottomMargin = paramAnonymousInt1;
            }
          }
          label586:
          do
          {
            paramAnonymousInt1 = paramAnonymousInt2 - (localLayoutParams.bottomMargin + localLayoutParams.topMargin);
            AlertDialog.this.contentScrollView.measure(m, View.MeasureSpec.makeMeasureSpec(paramAnonymousInt1, Integer.MIN_VALUE));
            paramAnonymousInt2 = paramAnonymousInt1 - AlertDialog.this.contentScrollView.getMeasuredHeight();
            setMeasuredDimension(k, j - paramAnonymousInt2 + getPaddingTop() + getPaddingBottom());
            this.inLayout = false;
            if (AlertDialog.this.lastScreenWidth != AndroidUtilities.displaySize.x) {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  AlertDialog.access$1202(AlertDialog.this, AndroidUtilities.displaySize.x);
                  int j = AndroidUtilities.displaySize.x;
                  int k = AndroidUtilities.dp(56.0F);
                  int i;
                  if (AndroidUtilities.isTablet()) {
                    if (AndroidUtilities.isSmallTablet()) {
                      i = AndroidUtilities.dp(446.0F);
                    }
                  }
                  for (;;)
                  {
                    Window localWindow = AlertDialog.this.getWindow();
                    WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
                    localLayoutParams.copyFrom(localWindow.getAttributes());
                    localLayoutParams.width = (Math.min(i, j - k) + AlertDialog.this.backgroundPaddings.left + AlertDialog.this.backgroundPaddings.right);
                    localWindow.setAttributes(localLayoutParams);
                    return;
                    i = AndroidUtilities.dp(496.0F);
                    continue;
                    i = AndroidUtilities.dp(356.0F);
                  }
                }
              });
            }
            return;
            paramAnonymousInt1 = 0;
            break;
            paramAnonymousInt1 = 0;
            break label477;
            if (AlertDialog.this.items != null)
            {
              if ((AlertDialog.this.titleTextView == null) && (AlertDialog.this.messageTextView.getVisibility() == 8)) {}
              for (paramAnonymousInt1 = AndroidUtilities.dp(8.0F);; paramAnonymousInt1 = 0)
              {
                localLayoutParams.topMargin = paramAnonymousInt1;
                localLayoutParams.bottomMargin = AndroidUtilities.dp(8.0F);
                break;
              }
            }
          } while (AlertDialog.this.messageTextView.getVisibility() != 0);
          if (AlertDialog.this.titleTextView == null) {}
          for (paramAnonymousInt1 = AndroidUtilities.dp(19.0F);; paramAnonymousInt1 = 0)
          {
            localLayoutParams.topMargin = paramAnonymousInt1;
            localLayoutParams.bottomMargin = AndroidUtilities.dp(20.0F);
            break;
          }
        }
        if (AlertDialog.this.progressViewContainer != null)
        {
          AlertDialog.this.progressViewContainer.measure(n, View.MeasureSpec.makeMeasureSpec(paramAnonymousInt2, Integer.MIN_VALUE));
          localLayoutParams = (LinearLayout.LayoutParams)AlertDialog.this.progressViewContainer.getLayoutParams();
          paramAnonymousInt1 = paramAnonymousInt2 - (AlertDialog.this.progressViewContainer.getMeasuredHeight() + localLayoutParams.bottomMargin + localLayoutParams.topMargin);
        }
        for (;;)
        {
          paramAnonymousInt2 = paramAnonymousInt1;
          if (AlertDialog.this.lineProgressView == null) {
            break;
          }
          AlertDialog.this.lineProgressView.measure(n, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(4.0F), 1073741824));
          localLayoutParams = (LinearLayout.LayoutParams)AlertDialog.this.lineProgressView.getLayoutParams();
          paramAnonymousInt1 -= AlertDialog.this.lineProgressView.getMeasuredHeight() + localLayoutParams.bottomMargin + localLayoutParams.topMargin;
          AlertDialog.this.lineProgressViewPercent.measure(n, View.MeasureSpec.makeMeasureSpec(paramAnonymousInt1, Integer.MIN_VALUE));
          localLayoutParams = (LinearLayout.LayoutParams)AlertDialog.this.lineProgressViewPercent.getLayoutParams();
          paramAnonymousInt2 = paramAnonymousInt1 - (AlertDialog.this.lineProgressViewPercent.getMeasuredHeight() + localLayoutParams.bottomMargin + localLayoutParams.topMargin);
          break;
          paramAnonymousInt1 = paramAnonymousInt2;
          if (AlertDialog.this.messageTextView != null)
          {
            AlertDialog.this.messageTextView.measure(n, View.MeasureSpec.makeMeasureSpec(paramAnonymousInt2, Integer.MIN_VALUE));
            paramAnonymousInt1 = paramAnonymousInt2;
            if (AlertDialog.this.messageTextView.getVisibility() != 8)
            {
              localLayoutParams = (LinearLayout.LayoutParams)AlertDialog.this.messageTextView.getLayoutParams();
              paramAnonymousInt1 = paramAnonymousInt2 - (AlertDialog.this.messageTextView.getMeasuredHeight() + localLayoutParams.bottomMargin + localLayoutParams.topMargin);
            }
          }
        }
      }
      
      public void requestLayout()
      {
        if (this.inLayout) {
          return;
        }
        super.requestLayout();
      }
    };
    paramBundle.setOrientation(1);
    paramBundle.setBackgroundDrawable(this.shadowDrawable);
    boolean bool;
    label77:
    label124:
    Object localObject1;
    int i;
    label203:
    label311:
    label335:
    label344:
    label441:
    label464:
    label475:
    label750:
    label798:
    Object localObject2;
    if (Build.VERSION.SDK_INT >= 21)
    {
      bool = true;
      paramBundle.setFitsSystemWindows(bool);
      setContentView(paramBundle);
      if ((this.positiveButtonText == null) && (this.negativeButtonText == null) && (this.neutralButtonText == null)) {
        break label1039;
      }
      j = 1;
      if ((this.topResId != 0) || (this.topDrawable != null))
      {
        this.topImageView = new ImageView(getContext());
        if (this.topDrawable == null) {
          break label1045;
        }
        this.topImageView.setImageDrawable(this.topDrawable);
        this.topImageView.setScaleType(ImageView.ScaleType.CENTER);
        this.topImageView.setBackgroundDrawable(getContext().getResources().getDrawable(2131165611));
        this.topImageView.getBackground().setColorFilter(new PorterDuffColorFilter(this.topBackgroundColor, PorterDuff.Mode.MULTIPLY));
        this.topImageView.setPadding(0, 0, 0, 0);
        localObject1 = this.topImageView;
        if (!LocaleController.isRTL) {
          break label1059;
        }
        i = 5;
        paramBundle.addView((View)localObject1, LayoutHelper.createLinear(-1, 132, i | 0x30, -8, -8, 0, 0));
      }
      if (this.title != null)
      {
        this.titleTextView = new TextView(getContext());
        this.titleTextView.setText(this.title);
        this.titleTextView.setTextColor(getThemeColor("dialogTextBlack"));
        this.titleTextView.setTextSize(1, 20.0F);
        this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        localObject1 = this.titleTextView;
        if (!LocaleController.isRTL) {
          break label1064;
        }
        i = 5;
        ((TextView)localObject1).setGravity(i | 0x30);
        localObject1 = this.titleTextView;
        if (!LocaleController.isRTL) {
          break label1069;
        }
        k = 5;
        if (this.subtitle == null) {
          break label1075;
        }
        i = 2;
        paramBundle.addView((View)localObject1, LayoutHelper.createLinear(-2, -2, k | 0x30, 24, 19, 24, i));
      }
      if (this.subtitle != null)
      {
        this.subtitleTextView = new TextView(getContext());
        this.subtitleTextView.setText(this.subtitle);
        this.subtitleTextView.setTextColor(getThemeColor("dialogIcon"));
        this.subtitleTextView.setTextSize(1, 14.0F);
        localObject1 = this.subtitleTextView;
        if (!LocaleController.isRTL) {
          break label1094;
        }
        i = 5;
        ((TextView)localObject1).setGravity(i | 0x30);
        localObject1 = this.subtitleTextView;
        if (!LocaleController.isRTL) {
          break label1099;
        }
        i = 5;
        if (this.items == null) {
          break label1104;
        }
        k = 14;
        paramBundle.addView((View)localObject1, LayoutHelper.createLinear(-2, -2, i | 0x30, 24, 0, 24, k));
      }
      if (this.progressViewStyle == 0)
      {
        this.shadow[0] = ((BitmapDrawable)getContext().getResources().getDrawable(2131165342).mutate());
        this.shadow[1] = ((BitmapDrawable)getContext().getResources().getDrawable(2131165343).mutate());
        this.shadow[0].setAlpha(0);
        this.shadow[1].setAlpha(0);
        this.shadow[0].setCallback(this);
        this.shadow[1].setCallback(this);
        this.contentScrollView = new ScrollView(getContext())
        {
          protected boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
          {
            boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
            if (AlertDialog.this.shadow[0].getPaint().getAlpha() != 0)
            {
              AlertDialog.this.shadow[0].setBounds(0, getScrollY(), getMeasuredWidth(), getScrollY() + AndroidUtilities.dp(3.0F));
              AlertDialog.this.shadow[0].draw(paramAnonymousCanvas);
            }
            if (AlertDialog.this.shadow[1].getPaint().getAlpha() != 0)
            {
              AlertDialog.this.shadow[1].setBounds(0, getScrollY() + getMeasuredHeight() - AndroidUtilities.dp(3.0F), getMeasuredWidth(), getScrollY() + getMeasuredHeight());
              AlertDialog.this.shadow[1].draw(paramAnonymousCanvas);
            }
            return bool;
          }
        };
        this.contentScrollView.setVerticalScrollBarEnabled(false);
        AndroidUtilities.setScrollViewEdgeEffectColor(this.contentScrollView, getThemeColor("dialogScrollGlow"));
        paramBundle.addView(this.contentScrollView, LayoutHelper.createLinear(-1, -2, 0.0F, 0.0F, 0.0F, 0.0F));
        this.scrollContainer = new LinearLayout(getContext());
        this.scrollContainer.setOrientation(1);
        this.contentScrollView.addView(this.scrollContainer, new FrameLayout.LayoutParams(-1, -2));
      }
      this.messageTextView = new TextView(getContext());
      this.messageTextView.setTextColor(getThemeColor("dialogTextBlack"));
      this.messageTextView.setTextSize(1, 16.0F);
      localObject1 = this.messageTextView;
      if (!LocaleController.isRTL) {
        break label1111;
      }
      i = 5;
      ((TextView)localObject1).setGravity(i | 0x30);
      if (this.progressViewStyle != 1) {
        break label1144;
      }
      this.progressViewContainer = new FrameLayout(getContext());
      localObject1 = this.progressViewContainer;
      if (this.title != null) {
        break label1116;
      }
      i = 24;
      paramBundle.addView((View)localObject1, LayoutHelper.createLinear(-1, 44, 51, 23, i, 23, 24));
      localObject1 = new RadialProgressView(getContext());
      ((RadialProgressView)localObject1).setProgressColor(getThemeColor("dialogProgressCircle"));
      localObject2 = this.progressViewContainer;
      if (!LocaleController.isRTL) {
        break label1121;
      }
      i = 5;
      label858:
      ((FrameLayout)localObject2).addView((View)localObject1, LayoutHelper.createFrame(44, 44, i | 0x30));
      this.messageTextView.setLines(1);
      this.messageTextView.setSingleLine(true);
      this.messageTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject1 = this.progressViewContainer;
      localObject2 = this.messageTextView;
      if (!LocaleController.isRTL) {
        break label1126;
      }
      i = 5;
      label922:
      if (!LocaleController.isRTL) {
        break label1131;
      }
      k = 0;
      label931:
      float f = k;
      if (!LocaleController.isRTL) {
        break label1138;
      }
      k = 62;
      label945:
      ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(-2, -2.0F, i | 0x10, f, 0.0F, k, 0.0F));
      if (TextUtils.isEmpty(this.message)) {
        break label1504;
      }
      this.messageTextView.setText(this.message);
      this.messageTextView.setVisibility(0);
    }
    for (;;)
    {
      label999:
      if (this.items != null)
      {
        i = 0;
        for (;;)
        {
          if (i < this.items.length)
          {
            if (this.items[i] == null)
            {
              i += 1;
              continue;
              bool = false;
              break;
              label1039:
              j = 0;
              break label77;
              label1045:
              this.topImageView.setImageResource(this.topResId);
              break label124;
              label1059:
              i = 3;
              break label203;
              label1064:
              i = 3;
              break label311;
              label1069:
              k = 3;
              break label335;
              label1075:
              if (this.items != null)
              {
                i = 14;
                break label344;
              }
              i = 10;
              break label344;
              label1094:
              i = 3;
              break label441;
              label1099:
              i = 3;
              break label464;
              label1104:
              k = 10;
              break label475;
              label1111:
              i = 3;
              break label750;
              label1116:
              i = 0;
              break label798;
              label1121:
              i = 3;
              break label858;
              label1126:
              i = 3;
              break label922;
              label1131:
              k = 62;
              break label931;
              label1138:
              k = 0;
              break label945;
              label1144:
              if (this.progressViewStyle == 2)
              {
                localObject1 = this.messageTextView;
                if (LocaleController.isRTL)
                {
                  i = 5;
                  label1166:
                  if (this.title != null) {
                    break label1409;
                  }
                  k = 19;
                  label1177:
                  paramBundle.addView((View)localObject1, LayoutHelper.createLinear(-2, -2, i | 0x30, 24, k, 24, 20));
                  this.lineProgressView = new LineProgressView(getContext());
                  this.lineProgressView.setProgress(this.currentProgress / 100.0F, false);
                  this.lineProgressView.setProgressColor(getThemeColor("dialogLineProgress"));
                  this.lineProgressView.setBackColor(getThemeColor("dialogLineProgressBackground"));
                  paramBundle.addView(this.lineProgressView, LayoutHelper.createLinear(-1, 4, 19, 24, 0, 24, 0));
                  this.lineProgressViewPercent = new TextView(getContext());
                  this.lineProgressViewPercent.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                  localObject1 = this.lineProgressViewPercent;
                  if (!LocaleController.isRTL) {
                    break label1415;
                  }
                  i = 5;
                  label1325:
                  ((TextView)localObject1).setGravity(i | 0x30);
                  this.lineProgressViewPercent.setTextColor(getThemeColor("dialogTextGray2"));
                  this.lineProgressViewPercent.setTextSize(1, 14.0F);
                  localObject1 = this.lineProgressViewPercent;
                  if (!LocaleController.isRTL) {
                    break label1420;
                  }
                }
                label1409:
                label1415:
                label1420:
                for (i = 5;; i = 3)
                {
                  paramBundle.addView((View)localObject1, LayoutHelper.createLinear(-2, -2, i | 0x30, 23, 4, 23, 24));
                  updateLineProgressTextView();
                  break;
                  i = 3;
                  break label1166;
                  k = 0;
                  break label1177;
                  i = 3;
                  break label1325;
                }
              }
              localObject1 = this.scrollContainer;
              localObject2 = this.messageTextView;
              if (LocaleController.isRTL)
              {
                i = 5;
                label1445:
                if ((this.customView == null) && (this.items == null)) {
                  break label1498;
                }
              }
              label1498:
              for (k = this.customViewOffset;; k = 0)
              {
                ((LinearLayout)localObject1).addView((View)localObject2, LayoutHelper.createLinear(-2, -2, i | 0x30, 24, 0, 24, k));
                break;
                i = 3;
                break label1445;
              }
              label1504:
              this.messageTextView.setVisibility(8);
              break label999;
            }
            localObject1 = new AlertDialogCell(getContext());
            localObject2 = this.items[i];
            if (this.itemIcons != null) {}
            for (k = this.itemIcons[i];; k = 0)
            {
              ((AlertDialogCell)localObject1).setTextAndIcon((CharSequence)localObject2, k);
              this.scrollContainer.addView((View)localObject1, LayoutHelper.createLinear(-1, 48));
              ((AlertDialogCell)localObject1).setTag(Integer.valueOf(i));
              ((AlertDialogCell)localObject1).setOnClickListener(new View.OnClickListener()
              {
                public void onClick(View paramAnonymousView)
                {
                  if (AlertDialog.this.onClickListener != null) {
                    AlertDialog.this.onClickListener.onClick(AlertDialog.this, ((Integer)paramAnonymousView.getTag()).intValue());
                  }
                  AlertDialog.this.dismiss();
                }
              });
              break;
            }
          }
        }
      }
    }
    if (this.customView != null)
    {
      if (this.customView.getParent() != null) {
        ((ViewGroup)this.customView.getParent()).removeView(this.customView);
      }
      this.scrollContainer.addView(this.customView, LayoutHelper.createLinear(-1, -2));
    }
    if (j != 0)
    {
      this.buttonsLayout = new FrameLayout(getContext())
      {
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          paramAnonymousInt4 = getChildCount();
          Object localObject = null;
          int i = paramAnonymousInt3 - paramAnonymousInt1;
          paramAnonymousInt1 = 0;
          if (paramAnonymousInt1 < paramAnonymousInt4)
          {
            View localView = getChildAt(paramAnonymousInt1);
            if (((Integer)localView.getTag()).intValue() == -1)
            {
              localObject = localView;
              if (LocaleController.isRTL) {
                localView.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + localView.getMeasuredWidth(), getPaddingTop() + localView.getMeasuredHeight());
              }
            }
            for (;;)
            {
              paramAnonymousInt1 += 1;
              break;
              localView.layout(i - getPaddingRight() - localView.getMeasuredWidth(), getPaddingTop(), i - getPaddingRight() + localView.getMeasuredWidth(), getPaddingTop() + localView.getMeasuredHeight());
              continue;
              if (((Integer)localView.getTag()).intValue() == -2)
              {
                if (LocaleController.isRTL)
                {
                  paramAnonymousInt3 = getPaddingLeft();
                  paramAnonymousInt2 = paramAnonymousInt3;
                  if (localObject != null) {
                    paramAnonymousInt2 = paramAnonymousInt3 + (((View)localObject).getMeasuredWidth() + AndroidUtilities.dp(8.0F));
                  }
                  localView.layout(paramAnonymousInt2, getPaddingTop(), localView.getMeasuredWidth() + paramAnonymousInt2, getPaddingTop() + localView.getMeasuredHeight());
                }
                else
                {
                  paramAnonymousInt3 = i - getPaddingRight() - localView.getMeasuredWidth();
                  paramAnonymousInt2 = paramAnonymousInt3;
                  if (localObject != null) {
                    paramAnonymousInt2 = paramAnonymousInt3 - (((View)localObject).getMeasuredWidth() + AndroidUtilities.dp(8.0F));
                  }
                  localView.layout(paramAnonymousInt2, getPaddingTop(), localView.getMeasuredWidth() + paramAnonymousInt2, getPaddingTop() + localView.getMeasuredHeight());
                }
              }
              else if (LocaleController.isRTL) {
                localView.layout(i - getPaddingRight() - localView.getMeasuredWidth(), getPaddingTop(), i - getPaddingRight() + localView.getMeasuredWidth(), getPaddingTop() + localView.getMeasuredHeight());
              } else {
                localView.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + localView.getMeasuredWidth(), getPaddingTop() + localView.getMeasuredHeight());
              }
            }
          }
        }
      };
      this.buttonsLayout.setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F));
      paramBundle.addView(this.buttonsLayout, LayoutHelper.createLinear(-1, 52));
      if (this.positiveButtonText != null)
      {
        paramBundle = new TextView(getContext())
        {
          public void setEnabled(boolean paramAnonymousBoolean)
          {
            super.setEnabled(paramAnonymousBoolean);
            if (paramAnonymousBoolean) {}
            for (float f = 1.0F;; f = 0.5F)
            {
              setAlpha(f);
              return;
            }
          }
        };
        paramBundle.setMinWidth(AndroidUtilities.dp(64.0F));
        paramBundle.setTag(Integer.valueOf(-1));
        paramBundle.setTextSize(1, 14.0F);
        paramBundle.setTextColor(getThemeColor("dialogButton"));
        paramBundle.setGravity(17);
        paramBundle.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        paramBundle.setText(this.positiveButtonText.toString().toUpperCase());
        paramBundle.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable());
        paramBundle.setPadding(AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(10.0F), 0);
        this.buttonsLayout.addView(paramBundle, LayoutHelper.createFrame(-2, 36, 53));
        paramBundle.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (AlertDialog.this.positiveButtonListener != null) {
              AlertDialog.this.positiveButtonListener.onClick(AlertDialog.this, -1);
            }
            AlertDialog.this.dismiss();
          }
        });
      }
      if (this.negativeButtonText != null)
      {
        paramBundle = new TextView(getContext())
        {
          public void setEnabled(boolean paramAnonymousBoolean)
          {
            super.setEnabled(paramAnonymousBoolean);
            if (paramAnonymousBoolean) {}
            for (float f = 1.0F;; f = 0.5F)
            {
              setAlpha(f);
              return;
            }
          }
        };
        paramBundle.setMinWidth(AndroidUtilities.dp(64.0F));
        paramBundle.setTag(Integer.valueOf(-2));
        paramBundle.setTextSize(1, 14.0F);
        paramBundle.setTextColor(getThemeColor("dialogButton"));
        paramBundle.setGravity(17);
        paramBundle.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        paramBundle.setText(this.negativeButtonText.toString().toUpperCase());
        paramBundle.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable());
        paramBundle.setPadding(AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(10.0F), 0);
        this.buttonsLayout.addView(paramBundle, LayoutHelper.createFrame(-2, 36, 53));
        paramBundle.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (AlertDialog.this.negativeButtonListener != null) {
              AlertDialog.this.negativeButtonListener.onClick(AlertDialog.this, -2);
            }
            AlertDialog.this.cancel();
          }
        });
      }
      if (this.neutralButtonText != null)
      {
        paramBundle = new TextView(getContext())
        {
          public void setEnabled(boolean paramAnonymousBoolean)
          {
            super.setEnabled(paramAnonymousBoolean);
            if (paramAnonymousBoolean) {}
            for (float f = 1.0F;; f = 0.5F)
            {
              setAlpha(f);
              return;
            }
          }
        };
        paramBundle.setMinWidth(AndroidUtilities.dp(64.0F));
        paramBundle.setTag(Integer.valueOf(-3));
        paramBundle.setTextSize(1, 14.0F);
        paramBundle.setTextColor(getThemeColor("dialogButton"));
        paramBundle.setGravity(17);
        paramBundle.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        paramBundle.setText(this.neutralButtonText.toString().toUpperCase());
        paramBundle.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable());
        paramBundle.setPadding(AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(10.0F), 0);
        this.buttonsLayout.addView(paramBundle, LayoutHelper.createFrame(-2, 36, 51));
        paramBundle.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (AlertDialog.this.neutralButtonListener != null) {
              AlertDialog.this.neutralButtonListener.onClick(AlertDialog.this, -2);
            }
            AlertDialog.this.dismiss();
          }
        });
      }
    }
    this.lastScreenWidth = AndroidUtilities.displaySize.x;
    int j = AndroidUtilities.displaySize.x;
    int k = AndroidUtilities.dp(48.0F);
    if (AndroidUtilities.isTablet()) {
      if (AndroidUtilities.isSmallTablet()) {
        i = AndroidUtilities.dp(446.0F);
      }
    }
    for (;;)
    {
      paramBundle = getWindow();
      localObject1 = new WindowManager.LayoutParams();
      ((WindowManager.LayoutParams)localObject1).copyFrom(paramBundle.getAttributes());
      ((WindowManager.LayoutParams)localObject1).dimAmount = 0.6F;
      ((WindowManager.LayoutParams)localObject1).width = (Math.min(i, j - k) + this.backgroundPaddings.left + this.backgroundPaddings.right);
      ((WindowManager.LayoutParams)localObject1).flags |= 0x2;
      if ((this.customView == null) || (!canTextInput(this.customView))) {
        ((WindowManager.LayoutParams)localObject1).flags |= 0x20000;
      }
      paramBundle.setAttributes((WindowManager.LayoutParams)localObject1);
      return;
      i = AndroidUtilities.dp(496.0F);
      continue;
      i = AndroidUtilities.dp(356.0F);
    }
  }
  
  public void scheduleDrawable(Drawable paramDrawable, Runnable paramRunnable, long paramLong)
  {
    if (this.contentScrollView != null) {
      this.contentScrollView.postDelayed(paramRunnable, paramLong);
    }
  }
  
  public void setButton(int paramInt, CharSequence paramCharSequence, DialogInterface.OnClickListener paramOnClickListener)
  {
    switch (paramInt)
    {
    default: 
      return;
    case -3: 
      this.neutralButtonText = paramCharSequence;
      this.neutralButtonListener = paramOnClickListener;
      return;
    case -2: 
      this.negativeButtonText = paramCharSequence;
      this.negativeButtonListener = paramOnClickListener;
      return;
    }
    this.positiveButtonText = paramCharSequence;
    this.positiveButtonListener = paramOnClickListener;
  }
  
  public void setCanceledOnTouchOutside(boolean paramBoolean)
  {
    super.setCanceledOnTouchOutside(paramBoolean);
  }
  
  public void setMessage(CharSequence paramCharSequence)
  {
    this.message = paramCharSequence;
    if (this.messageTextView != null)
    {
      if (!TextUtils.isEmpty(this.message))
      {
        this.messageTextView.setText(this.message);
        this.messageTextView.setVisibility(0);
      }
    }
    else {
      return;
    }
    this.messageTextView.setVisibility(8);
  }
  
  public void setProgress(int paramInt)
  {
    this.currentProgress = paramInt;
    if (this.lineProgressView != null)
    {
      this.lineProgressView.setProgress(paramInt / 100.0F, true);
      updateLineProgressTextView();
    }
  }
  
  public void setProgressStyle(int paramInt)
  {
    this.progressViewStyle = paramInt;
  }
  
  public void unscheduleDrawable(Drawable paramDrawable, Runnable paramRunnable)
  {
    if (this.contentScrollView != null) {
      this.contentScrollView.removeCallbacks(paramRunnable);
    }
  }
  
  public static class AlertDialogCell
    extends FrameLayout
  {
    private ImageView imageView;
    private TextView textView;
    
    public AlertDialogCell(Context paramContext)
    {
      super();
      setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor("dialogButtonSelector"), 2));
      setPadding(AndroidUtilities.dp(23.0F), 0, AndroidUtilities.dp(23.0F), 0);
      this.imageView = new ImageView(paramContext);
      this.imageView.setScaleType(ImageView.ScaleType.CENTER);
      this.imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("dialogIcon"), PorterDuff.Mode.MULTIPLY));
      ImageView localImageView = this.imageView;
      if (LocaleController.isRTL) {}
      for (int i = 5;; i = 3)
      {
        addView(localImageView, LayoutHelper.createFrame(24, 24, i | 0x10));
        this.textView = new TextView(paramContext);
        this.textView.setLines(1);
        this.textView.setSingleLine(true);
        this.textView.setGravity(1);
        this.textView.setEllipsize(TextUtils.TruncateAt.END);
        this.textView.setTextColor(Theme.getColor("dialogTextBlack"));
        this.textView.setTextSize(1, 16.0F);
        paramContext = this.textView;
        i = j;
        if (LocaleController.isRTL) {
          i = 5;
        }
        addView(paramContext, LayoutHelper.createFrame(-2, -2, i | 0x10));
        return;
      }
    }
    
    protected void onMeasure(int paramInt1, int paramInt2)
    {
      super.onMeasure(paramInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0F), 1073741824));
    }
    
    public void setGravity(int paramInt)
    {
      this.textView.setGravity(paramInt);
    }
    
    public void setTextAndIcon(CharSequence paramCharSequence, int paramInt)
    {
      this.textView.setText(paramCharSequence);
      if (paramInt != 0)
      {
        this.imageView.setImageResource(paramInt);
        this.imageView.setVisibility(0);
        paramCharSequence = this.textView;
        if (LocaleController.isRTL)
        {
          paramInt = 0;
          if (!LocaleController.isRTL) {
            break label71;
          }
        }
        label71:
        for (int i = AndroidUtilities.dp(56.0F);; i = 0)
        {
          paramCharSequence.setPadding(paramInt, 0, i, 0);
          return;
          paramInt = AndroidUtilities.dp(56.0F);
          break;
        }
      }
      this.imageView.setVisibility(4);
      this.textView.setPadding(0, 0, 0, 0);
    }
    
    public void setTextColor(int paramInt)
    {
      this.textView.setTextColor(paramInt);
    }
  }
  
  public static class Builder
  {
    private AlertDialog alertDialog;
    
    public Builder(Context paramContext)
    {
      this.alertDialog = new AlertDialog(paramContext, 0);
    }
    
    public Builder(Context paramContext, int paramInt)
    {
      this.alertDialog = new AlertDialog(paramContext, paramInt);
    }
    
    protected Builder(AlertDialog paramAlertDialog)
    {
      this.alertDialog = paramAlertDialog;
    }
    
    public AlertDialog create()
    {
      return this.alertDialog;
    }
    
    public Context getContext()
    {
      return this.alertDialog.getContext();
    }
    
    public Builder setCustomViewOffset(int paramInt)
    {
      AlertDialog.access$3402(this.alertDialog, paramInt);
      return this;
    }
    
    public Builder setItems(CharSequence[] paramArrayOfCharSequence, DialogInterface.OnClickListener paramOnClickListener)
    {
      AlertDialog.access$802(this.alertDialog, paramArrayOfCharSequence);
      AlertDialog.access$1802(this.alertDialog, paramOnClickListener);
      return this;
    }
    
    public Builder setItems(CharSequence[] paramArrayOfCharSequence, int[] paramArrayOfInt, DialogInterface.OnClickListener paramOnClickListener)
    {
      AlertDialog.access$802(this.alertDialog, paramArrayOfCharSequence);
      AlertDialog.access$2302(this.alertDialog, paramArrayOfInt);
      AlertDialog.access$1802(this.alertDialog, paramOnClickListener);
      return this;
    }
    
    public Builder setMessage(CharSequence paramCharSequence)
    {
      AlertDialog.access$2902(this.alertDialog, paramCharSequence);
      return this;
    }
    
    public Builder setNegativeButton(CharSequence paramCharSequence, DialogInterface.OnClickListener paramOnClickListener)
    {
      AlertDialog.access$3102(this.alertDialog, paramCharSequence);
      AlertDialog.access$2002(this.alertDialog, paramOnClickListener);
      return this;
    }
    
    public Builder setNeutralButton(CharSequence paramCharSequence, DialogInterface.OnClickListener paramOnClickListener)
    {
      AlertDialog.access$3202(this.alertDialog, paramCharSequence);
      AlertDialog.access$2102(this.alertDialog, paramOnClickListener);
      return this;
    }
    
    public Builder setOnBackButtonListener(DialogInterface.OnClickListener paramOnClickListener)
    {
      AlertDialog.access$3302(this.alertDialog, paramOnClickListener);
      return this;
    }
    
    public Builder setOnDismissListener(DialogInterface.OnDismissListener paramOnDismissListener)
    {
      this.alertDialog.setOnDismissListener(paramOnDismissListener);
      return this;
    }
    
    public Builder setPositiveButton(CharSequence paramCharSequence, DialogInterface.OnClickListener paramOnClickListener)
    {
      AlertDialog.access$3002(this.alertDialog, paramCharSequence);
      AlertDialog.access$1902(this.alertDialog, paramOnClickListener);
      return this;
    }
    
    public Builder setSubtitle(CharSequence paramCharSequence)
    {
      AlertDialog.access$2502(this.alertDialog, paramCharSequence);
      return this;
    }
    
    public Builder setTitle(CharSequence paramCharSequence)
    {
      AlertDialog.access$2402(this.alertDialog, paramCharSequence);
      return this;
    }
    
    public Builder setTopImage(int paramInt1, int paramInt2)
    {
      AlertDialog.access$2602(this.alertDialog, paramInt1);
      AlertDialog.access$2702(this.alertDialog, paramInt2);
      return this;
    }
    
    public Builder setTopImage(Drawable paramDrawable, int paramInt)
    {
      AlertDialog.access$2802(this.alertDialog, paramDrawable);
      AlertDialog.access$2702(this.alertDialog, paramInt);
      return this;
    }
    
    public Builder setView(View paramView)
    {
      AlertDialog.access$602(this.alertDialog, paramView);
      return this;
    }
    
    public AlertDialog show()
    {
      this.alertDialog.show();
      return this.alertDialog;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ActionBar/AlertDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */