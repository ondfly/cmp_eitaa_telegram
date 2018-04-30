package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.os.Build.VERSION;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import java.security.SecureRandom;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;

public class FireworksEffect
{
  final float angleDiff = 1.0471976F;
  private ArrayList<Particle> freeParticles = new ArrayList();
  private long lastAnimationTime;
  private Paint particlePaint = new Paint(1);
  private ArrayList<Particle> particles = new ArrayList();
  
  public FireworksEffect()
  {
    this.particlePaint.setStrokeWidth(AndroidUtilities.dp(1.5F));
    this.particlePaint.setColor(Theme.getColor("actionBarDefaultTitle") & 0xFFE6E6E6);
    this.particlePaint.setStrokeCap(Paint.Cap.ROUND);
    this.particlePaint.setStyle(Paint.Style.STROKE);
    int i = 0;
    while (i < 20)
    {
      this.freeParticles.add(new Particle(null));
      i += 1;
    }
  }
  
  private void updateParticles(long paramLong)
  {
    int j = this.particles.size();
    int i = 0;
    if (i < j)
    {
      Particle localParticle = (Particle)this.particles.get(i);
      if (localParticle.currentTime >= localParticle.lifeTime)
      {
        if (this.freeParticles.size() < 40) {
          this.freeParticles.add(localParticle);
        }
        this.particles.remove(i);
        i -= 1;
        j -= 1;
      }
      for (;;)
      {
        i += 1;
        break;
        localParticle.alpha = (1.0F - AndroidUtilities.decelerateInterpolator.getInterpolation(localParticle.currentTime / localParticle.lifeTime));
        localParticle.x += localParticle.vx * localParticle.velocity * (float)paramLong / 500.0F;
        localParticle.y += localParticle.vy * localParticle.velocity * (float)paramLong / 500.0F;
        localParticle.vy += (float)paramLong / 100.0F;
        localParticle.currentTime += (float)paramLong;
      }
    }
  }
  
  public void onDraw(View paramView, Canvas paramCanvas)
  {
    if ((paramView == null) || (paramCanvas == null)) {
      return;
    }
    int j = this.particles.size();
    int i = 0;
    while (i < j)
    {
      ((Particle)this.particles.get(i)).draw(paramCanvas);
      i += 1;
    }
    if ((Utilities.random.nextBoolean()) && (this.particles.size() + 8 < 150))
    {
      float f1;
      float f2;
      float f3;
      float f4;
      float f5;
      label177:
      label180:
      float f6;
      float f7;
      if (Build.VERSION.SDK_INT >= 21)
      {
        i = AndroidUtilities.statusBarHeight;
        f1 = Utilities.random.nextFloat();
        f2 = paramView.getMeasuredWidth();
        f3 = i;
        f4 = Utilities.random.nextFloat();
        f5 = paramView.getMeasuredHeight() - AndroidUtilities.dp(20.0F) - i;
        switch (Utilities.random.nextInt(4))
        {
        default: 
          i = 59784;
          j = 0;
          if (j >= 8) {
            break label431;
          }
          int k = Utilities.random.nextInt(270) - 225;
          f6 = (float)Math.cos(0.017453292519943295D * k);
          f7 = (float)Math.sin(0.017453292519943295D * k);
          if (!this.freeParticles.isEmpty())
          {
            paramCanvas = (Particle)this.freeParticles.get(0);
            this.freeParticles.remove(0);
          }
          break;
        }
      }
      for (;;)
      {
        paramCanvas.x = (f1 * f2);
        paramCanvas.y = (f3 + f4 * f5);
        paramCanvas.vx = (1.5F * f6);
        paramCanvas.vy = f7;
        paramCanvas.color = i;
        paramCanvas.alpha = 1.0F;
        paramCanvas.currentTime = 0.0F;
        paramCanvas.scale = Math.max(1.0F, Utilities.random.nextFloat() * 1.5F);
        paramCanvas.type = 0;
        paramCanvas.lifeTime = (Utilities.random.nextInt(1000) + 1000);
        paramCanvas.velocity = (20.0F + Utilities.random.nextFloat() * 4.0F);
        this.particles.add(paramCanvas);
        j += 1;
        break label180;
        i = 0;
        break;
        i = -13357350;
        break label177;
        i = -843755;
        break label177;
        i = -207021;
        break label177;
        i = -15088582;
        break label177;
        paramCanvas = new Particle(null);
      }
    }
    label431:
    long l = System.currentTimeMillis();
    updateParticles(Math.min(17L, l - this.lastAnimationTime));
    this.lastAnimationTime = l;
    paramView.invalidate();
  }
  
  private class Particle
  {
    float alpha;
    int color;
    float currentTime;
    float lifeTime;
    float scale;
    int type;
    float velocity;
    float vx;
    float vy;
    float x;
    float y;
    
    private Particle() {}
    
    public void draw(Canvas paramCanvas)
    {
      switch (this.type)
      {
      default: 
        return;
      }
      FireworksEffect.this.particlePaint.setColor(this.color);
      FireworksEffect.this.particlePaint.setStrokeWidth(AndroidUtilities.dp(1.5F) * this.scale);
      FireworksEffect.this.particlePaint.setAlpha((int)(255.0F * this.alpha));
      paramCanvas.drawPoint(this.x, this.y, FireworksEffect.this.particlePaint);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/FireworksEffect.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */