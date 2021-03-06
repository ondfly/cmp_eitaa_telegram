package ir.eitaa.ui;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.BuildVars;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.tgnet.ConnectionsManager;
import ir.eitaa.ui.ActionBar.Theme;

public class IntroActivity
  extends Activity
{
  private ViewGroup bottomPages;
  private int[] icons;
  private boolean justCreated = false;
  private int lastPage = 0;
  private int[] messages;
  private boolean startPressed = false;
  private int[] titles;
  private ImageView topImage1;
  private ImageView topImage2;
  private ViewPager viewPager;
  
  protected void onCreate(Bundle paramBundle)
  {
    setTheme(2131296259);
    super.onCreate(paramBundle);
    Theme.loadRecources(this);
    requestWindowFeature(1);
    Object localObject;
    if (AndroidUtilities.isTablet())
    {
      setContentView(2130903043);
      paramBundle = findViewById(2131492885);
      localObject = (BitmapDrawable)getResources().getDrawable(2130837591);
      ((BitmapDrawable)localObject).setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
      paramBundle.setBackgroundDrawable((Drawable)localObject);
      if (!LocaleController.isRTL) {
        break label497;
      }
      this.icons = new int[] { 2130837786, 2130837785, 2130837784, 2130837783, 2130837782, 2130837781, 2130837780 };
      this.titles = new int[] { 2131166077, 2131166075, 2131166073, 2131166071, 2131166069, 2131166067, 2131166065 };
    }
    for (this.messages = new int[] { 2131166076, 2131166074, 2131166072, 2131166070, 2131166068, 2131166066, 2131166064 };; this.messages = new int[] { 2131166064, 2131166066, 2131166068, 2131166070, 2131166072, 2131166074, 2131166076 })
    {
      this.viewPager = ((ViewPager)findViewById(2131492882));
      paramBundle = (TextView)findViewById(2131492883);
      paramBundle.setText(LocaleController.getString("StartMessaging", 2131166311).toUpperCase());
      if (Build.VERSION.SDK_INT >= 21)
      {
        localObject = new StateListAnimator();
        ObjectAnimator localObjectAnimator = ObjectAnimator.ofFloat(paramBundle, "translationZ", new float[] { AndroidUtilities.dp(2.0F), AndroidUtilities.dp(4.0F) }).setDuration(200L);
        ((StateListAnimator)localObject).addState(new int[] { 16842919 }, localObjectAnimator);
        localObjectAnimator = ObjectAnimator.ofFloat(paramBundle, "translationZ", new float[] { AndroidUtilities.dp(4.0F), AndroidUtilities.dp(2.0F) }).setDuration(200L);
        ((StateListAnimator)localObject).addState(new int[0], localObjectAnimator);
        paramBundle.setStateListAnimator((StateListAnimator)localObject);
      }
      this.topImage1 = ((ImageView)findViewById(2131492880));
      this.topImage2 = ((ImageView)findViewById(2131492881));
      this.bottomPages = ((ViewGroup)findViewById(2131492884));
      this.topImage2.setVisibility(8);
      this.viewPager.setAdapter(new IntroAdapter(null));
      this.viewPager.setPageMargin(0);
      this.viewPager.setOffscreenPageLimit(1);
      this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
      {
        public void onPageScrollStateChanged(int paramAnonymousInt)
        {
          final ImageView localImageView2;
          if (((paramAnonymousInt == 0) || (paramAnonymousInt == 2)) && (IntroActivity.this.lastPage != IntroActivity.this.viewPager.getCurrentItem()))
          {
            IntroActivity.access$102(IntroActivity.this, IntroActivity.this.viewPager.getCurrentItem());
            if (IntroActivity.this.topImage1.getVisibility() != 0) {
              break label170;
            }
            localImageView2 = IntroActivity.this.topImage1;
          }
          for (final ImageView localImageView1 = IntroActivity.this.topImage2;; localImageView1 = IntroActivity.this.topImage1)
          {
            localImageView1.bringToFront();
            localImageView1.setImageResource(IntroActivity.this.icons[IntroActivity.this.lastPage]);
            localImageView1.clearAnimation();
            localImageView2.clearAnimation();
            Animation localAnimation1 = AnimationUtils.loadAnimation(IntroActivity.this, 2130968577);
            localAnimation1.setAnimationListener(new Animation.AnimationListener()
            {
              public void onAnimationEnd(Animation paramAnonymous2Animation)
              {
                localImageView2.setVisibility(8);
              }
              
              public void onAnimationRepeat(Animation paramAnonymous2Animation) {}
              
              public void onAnimationStart(Animation paramAnonymous2Animation) {}
            });
            Animation localAnimation2 = AnimationUtils.loadAnimation(IntroActivity.this, 2130968576);
            localAnimation2.setAnimationListener(new Animation.AnimationListener()
            {
              public void onAnimationEnd(Animation paramAnonymous2Animation) {}
              
              public void onAnimationRepeat(Animation paramAnonymous2Animation) {}
              
              public void onAnimationStart(Animation paramAnonymous2Animation)
              {
                localImageView1.setVisibility(0);
              }
            });
            localImageView2.startAnimation(localAnimation1);
            localImageView1.startAnimation(localAnimation2);
            return;
            label170:
            localImageView2 = IntroActivity.this.topImage2;
          }
        }
        
        public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2) {}
        
        public void onPageSelected(int paramAnonymousInt) {}
      });
      paramBundle.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (IntroActivity.this.startPressed) {
            return;
          }
          IntroActivity.access$602(IntroActivity.this, true);
          paramAnonymousView = new Intent(IntroActivity.this, LaunchActivity.class);
          paramAnonymousView.putExtra("fromIntro", true);
          IntroActivity.this.startActivity(paramAnonymousView);
          IntroActivity.this.finish();
        }
      });
      if (BuildVars.DEBUG_VERSION) {
        paramBundle.setOnLongClickListener(new View.OnLongClickListener()
        {
          public boolean onLongClick(View paramAnonymousView)
          {
            ConnectionsManager.getInstance().switchBackend();
            return true;
          }
        });
      }
      this.justCreated = true;
      return;
      setRequestedOrientation(1);
      setContentView(2130903042);
      break;
      label497:
      this.icons = new int[] { 2130837780, 2130837781, 2130837782, 2130837783, 2130837784, 2130837785, 2130837786 };
      this.titles = new int[] { 2131166065, 2131166067, 2131166069, 2131166071, 2131166073, 2131166075, 2131166077 };
    }
  }
  
  protected void onPause()
  {
    super.onPause();
    AndroidUtilities.unregisterUpdates();
  }
  
  protected void onResume()
  {
    super.onResume();
    if (this.justCreated)
    {
      if (!LocaleController.isRTL) {
        break label46;
      }
      this.viewPager.setCurrentItem(6);
    }
    for (this.lastPage = 6;; this.lastPage = 0)
    {
      this.justCreated = false;
      AndroidUtilities.checkForCrashes(this);
      AndroidUtilities.checkForUpdates(this);
      return;
      label46:
      this.viewPager.setCurrentItem(0);
    }
  }
  
  private class IntroAdapter
    extends PagerAdapter
  {
    private IntroAdapter() {}
    
    public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject)
    {
      paramViewGroup.removeView((View)paramObject);
    }
    
    public int getCount()
    {
      return 7;
    }
    
    public Object instantiateItem(ViewGroup paramViewGroup, int paramInt)
    {
      View localView = View.inflate(paramViewGroup.getContext(), 2130903044, null);
      TextView localTextView1 = (TextView)localView.findViewById(2131492886);
      TextView localTextView2 = (TextView)localView.findViewById(2131492887);
      paramViewGroup.addView(localView, 0);
      localTextView1.setText(IntroActivity.this.getString(IntroActivity.this.titles[paramInt]));
      localTextView2.setText(AndroidUtilities.replaceTags(IntroActivity.this.getString(IntroActivity.this.messages[paramInt])));
      return localView;
    }
    
    public boolean isViewFromObject(View paramView, Object paramObject)
    {
      return paramView.equals(paramObject);
    }
    
    public void restoreState(Parcelable paramParcelable, ClassLoader paramClassLoader) {}
    
    public Parcelable saveState()
    {
      return null;
    }
    
    public void setPrimaryItem(ViewGroup paramViewGroup, int paramInt, Object paramObject)
    {
      super.setPrimaryItem(paramViewGroup, paramInt, paramObject);
      int j = IntroActivity.this.bottomPages.getChildCount();
      int i = 0;
      if (i < j)
      {
        paramViewGroup = IntroActivity.this.bottomPages.getChildAt(i);
        if (i == paramInt) {
          paramViewGroup.setBackgroundColor(-13851168);
        }
        for (;;)
        {
          i += 1;
          break;
          paramViewGroup.setBackgroundColor(-4473925);
        }
      }
    }
    
    public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver)
    {
      if (paramDataSetObserver != null) {
        super.unregisterDataSetObserver(paramDataSetObserver);
      }
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/IntroActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */