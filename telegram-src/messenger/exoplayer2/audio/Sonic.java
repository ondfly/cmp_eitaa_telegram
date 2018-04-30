package org.telegram.messenger.exoplayer2.audio;

import java.nio.ShortBuffer;
import java.util.Arrays;
import org.telegram.messenger.exoplayer2.util.Assertions;

final class Sonic
{
  private static final int AMDF_FREQUENCY = 4000;
  private static final int MAXIMUM_PITCH = 400;
  private static final int MINIMUM_PITCH = 65;
  private final short[] downSampleBuffer;
  private short[] inputBuffer;
  private int inputBufferSize;
  private final int inputSampleRateHz;
  private int maxDiff;
  private final int maxPeriod;
  private final int maxRequired;
  private int minDiff;
  private final int minPeriod;
  private int newRatePosition;
  private final int numChannels;
  private int numInputSamples;
  private int numOutputSamples;
  private int numPitchSamples;
  private int oldRatePosition;
  private short[] outputBuffer;
  private int outputBufferSize;
  private final float pitch;
  private short[] pitchBuffer;
  private int pitchBufferSize;
  private int prevMinDiff;
  private int prevPeriod;
  private final float rate;
  private int remainingInputToCopy;
  private final float speed;
  
  public Sonic(int paramInt1, int paramInt2, float paramFloat1, float paramFloat2, int paramInt3)
  {
    this.inputSampleRateHz = paramInt1;
    this.numChannels = paramInt2;
    this.minPeriod = (paramInt1 / 400);
    this.maxPeriod = (paramInt1 / 65);
    this.maxRequired = (this.maxPeriod * 2);
    this.downSampleBuffer = new short[this.maxRequired];
    this.inputBufferSize = this.maxRequired;
    this.inputBuffer = new short[this.maxRequired * paramInt2];
    this.outputBufferSize = this.maxRequired;
    this.outputBuffer = new short[this.maxRequired * paramInt2];
    this.pitchBufferSize = this.maxRequired;
    this.pitchBuffer = new short[this.maxRequired * paramInt2];
    this.oldRatePosition = 0;
    this.newRatePosition = 0;
    this.prevPeriod = 0;
    this.speed = paramFloat1;
    this.pitch = paramFloat2;
    this.rate = (paramInt1 / paramInt3);
  }
  
  private void adjustRate(float paramFloat, int paramInt)
  {
    if (this.numOutputSamples == paramInt) {
      return;
    }
    int j = (int)(this.inputSampleRateHz / paramFloat);
    int i = this.inputSampleRateHz;
    while ((j > 16384) || (i > 16384))
    {
      j /= 2;
      i /= 2;
    }
    moveNewSamplesToPitchBuffer(paramInt);
    paramInt = 0;
    if (paramInt < this.numPitchSamples - 1)
    {
      while ((this.oldRatePosition + 1) * j > this.newRatePosition * i)
      {
        enlargeOutputBufferIfNeeded(1);
        int k = 0;
        while (k < this.numChannels)
        {
          this.outputBuffer[(this.numOutputSamples * this.numChannels + k)] = interpolate(this.pitchBuffer, this.numChannels * paramInt + k, i, j);
          k += 1;
        }
        this.newRatePosition += 1;
        this.numOutputSamples += 1;
      }
      this.oldRatePosition += 1;
      if (this.oldRatePosition == i)
      {
        this.oldRatePosition = 0;
        if (this.newRatePosition != j) {
          break label225;
        }
      }
      label225:
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkState(bool);
        this.newRatePosition = 0;
        paramInt += 1;
        break;
      }
    }
    removePitchSamples(this.numPitchSamples - 1);
  }
  
  private void changeSpeed(float paramFloat)
  {
    if (this.numInputSamples < this.maxRequired) {
      return;
    }
    int k = this.numInputSamples;
    int j = 0;
    int i;
    if (this.remainingInputToCopy > 0) {
      i = j + copyInputToOutput(j);
    }
    for (;;)
    {
      j = i;
      if (this.maxRequired + i <= k) {
        break;
      }
      removeProcessedInputSamples(i);
      return;
      i = findPitchPeriod(this.inputBuffer, j, true);
      if (paramFloat > 1.0D) {
        i = j + (skipPitchPeriod(this.inputBuffer, j, paramFloat, i) + i);
      } else {
        i = j + insertPitchPeriod(this.inputBuffer, j, paramFloat, i);
      }
    }
  }
  
  private int copyInputToOutput(int paramInt)
  {
    int i = Math.min(this.maxRequired, this.remainingInputToCopy);
    copyToOutput(this.inputBuffer, paramInt, i);
    this.remainingInputToCopy -= i;
    return i;
  }
  
  private void copyToOutput(short[] paramArrayOfShort, int paramInt1, int paramInt2)
  {
    enlargeOutputBufferIfNeeded(paramInt2);
    System.arraycopy(paramArrayOfShort, this.numChannels * paramInt1, this.outputBuffer, this.numOutputSamples * this.numChannels, this.numChannels * paramInt2);
    this.numOutputSamples += paramInt2;
  }
  
  private void downSampleInput(short[] paramArrayOfShort, int paramInt1, int paramInt2)
  {
    int k = this.maxRequired / paramInt2;
    int m = this.numChannels * paramInt2;
    int n = this.numChannels;
    paramInt2 = 0;
    while (paramInt2 < k)
    {
      int j = 0;
      int i = 0;
      while (i < m)
      {
        j += paramArrayOfShort[(paramInt2 * m + paramInt1 * n + i)];
        i += 1;
      }
      i = j / m;
      this.downSampleBuffer[paramInt2] = ((short)i);
      paramInt2 += 1;
    }
  }
  
  private void enlargeInputBufferIfNeeded(int paramInt)
  {
    if (this.numInputSamples + paramInt > this.inputBufferSize)
    {
      this.inputBufferSize += this.inputBufferSize / 2 + paramInt;
      this.inputBuffer = Arrays.copyOf(this.inputBuffer, this.inputBufferSize * this.numChannels);
    }
  }
  
  private void enlargeOutputBufferIfNeeded(int paramInt)
  {
    if (this.numOutputSamples + paramInt > this.outputBufferSize)
    {
      this.outputBufferSize += this.outputBufferSize / 2 + paramInt;
      this.outputBuffer = Arrays.copyOf(this.outputBuffer, this.outputBufferSize * this.numChannels);
    }
  }
  
  private int findPitchPeriod(short[] paramArrayOfShort, int paramInt, boolean paramBoolean)
  {
    int j;
    int i;
    if (this.inputSampleRateHz > 4000)
    {
      j = this.inputSampleRateHz / 4000;
      if ((this.numChannels != 1) || (j != 1)) {
        break label93;
      }
      i = findPitchPeriodInRange(paramArrayOfShort, paramInt, this.minPeriod, this.maxPeriod);
      label50:
      if (!previousPeriodBetter(this.minDiff, this.maxDiff, paramBoolean)) {
        break label247;
      }
    }
    label93:
    label247:
    for (paramInt = this.prevPeriod;; paramInt = i)
    {
      this.prevMinDiff = this.minDiff;
      this.prevPeriod = i;
      return paramInt;
      j = 1;
      break;
      downSampleInput(paramArrayOfShort, paramInt, j);
      int k = findPitchPeriodInRange(this.downSampleBuffer, 0, this.minPeriod / j, this.maxPeriod / j);
      i = k;
      if (j == 1) {
        break label50;
      }
      i = k * j;
      k = i - j * 4;
      int m = i + j * 4;
      i = k;
      if (k < this.minPeriod) {
        i = this.minPeriod;
      }
      j = m;
      if (m > this.maxPeriod) {
        j = this.maxPeriod;
      }
      if (this.numChannels == 1)
      {
        i = findPitchPeriodInRange(paramArrayOfShort, paramInt, i, j);
        break label50;
      }
      downSampleInput(paramArrayOfShort, paramInt, 1);
      i = findPitchPeriodInRange(this.downSampleBuffer, 0, i, j);
      break label50;
    }
  }
  
  private int findPitchPeriodInRange(short[] paramArrayOfShort, int paramInt1, int paramInt2, int paramInt3)
  {
    int m = 0;
    int i = 255;
    int k = 1;
    int j = 0;
    int i3 = paramInt1 * this.numChannels;
    paramInt1 = paramInt2;
    while (paramInt1 <= paramInt3)
    {
      paramInt2 = 0;
      int n = 0;
      while (n < paramInt1)
      {
        paramInt2 += Math.abs(paramArrayOfShort[(i3 + n)] - paramArrayOfShort[(i3 + paramInt1 + n)]);
        n += 1;
      }
      int i1 = m;
      n = k;
      if (paramInt2 * m < k * paramInt1)
      {
        n = paramInt2;
        i1 = paramInt1;
      }
      k = j;
      int i2 = i;
      if (paramInt2 * i > j * paramInt1)
      {
        i2 = paramInt1;
        k = paramInt2;
      }
      paramInt1 += 1;
      m = i1;
      j = k;
      k = n;
      i = i2;
    }
    this.minDiff = (k / m);
    this.maxDiff = (j / i);
    return m;
  }
  
  private int insertPitchPeriod(short[] paramArrayOfShort, int paramInt1, float paramFloat, int paramInt2)
  {
    int i;
    if (paramFloat < 0.5F) {
      i = (int)(paramInt2 * paramFloat / (1.0F - paramFloat));
    }
    for (;;)
    {
      enlargeOutputBufferIfNeeded(paramInt2 + i);
      System.arraycopy(paramArrayOfShort, this.numChannels * paramInt1, this.outputBuffer, this.numOutputSamples * this.numChannels, this.numChannels * paramInt2);
      overlapAdd(i, this.numChannels, this.outputBuffer, this.numOutputSamples + paramInt2, paramArrayOfShort, paramInt1 + paramInt2, paramArrayOfShort, paramInt1);
      this.numOutputSamples += paramInt2 + i;
      return i;
      i = paramInt2;
      this.remainingInputToCopy = ((int)(paramInt2 * (2.0F * paramFloat - 1.0F) / (1.0F - paramFloat)));
    }
  }
  
  private short interpolate(short[] paramArrayOfShort, int paramInt1, int paramInt2, int paramInt3)
  {
    int i = paramArrayOfShort[paramInt1];
    paramInt1 = paramArrayOfShort[(this.numChannels + paramInt1)];
    int m = this.newRatePosition;
    int j = this.oldRatePosition;
    int k = (this.oldRatePosition + 1) * paramInt3;
    paramInt2 = k - m * paramInt2;
    paramInt3 = k - j * paramInt3;
    return (short)((paramInt2 * i + (paramInt3 - paramInt2) * paramInt1) / paramInt3);
  }
  
  private void moveNewSamplesToPitchBuffer(int paramInt)
  {
    int i = this.numOutputSamples - paramInt;
    if (this.numPitchSamples + i > this.pitchBufferSize)
    {
      this.pitchBufferSize += this.pitchBufferSize / 2 + i;
      this.pitchBuffer = Arrays.copyOf(this.pitchBuffer, this.pitchBufferSize * this.numChannels);
    }
    System.arraycopy(this.outputBuffer, this.numChannels * paramInt, this.pitchBuffer, this.numPitchSamples * this.numChannels, this.numChannels * i);
    this.numOutputSamples = paramInt;
    this.numPitchSamples += i;
  }
  
  private static void overlapAdd(int paramInt1, int paramInt2, short[] paramArrayOfShort1, int paramInt3, short[] paramArrayOfShort2, int paramInt4, short[] paramArrayOfShort3, int paramInt5)
  {
    int i = 0;
    while (i < paramInt2)
    {
      int n = paramInt3 * paramInt2 + i;
      int k = paramInt5 * paramInt2 + i;
      int m = paramInt4 * paramInt2 + i;
      int j = 0;
      while (j < paramInt1)
      {
        paramArrayOfShort1[n] = ((short)((paramArrayOfShort2[m] * (paramInt1 - j) + paramArrayOfShort3[k] * j) / paramInt1));
        n += paramInt2;
        m += paramInt2;
        k += paramInt2;
        j += 1;
      }
      i += 1;
    }
  }
  
  private boolean previousPeriodBetter(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    if ((paramInt1 == 0) || (this.prevPeriod == 0)) {}
    do
    {
      return false;
      if (!paramBoolean) {
        break;
      }
    } while ((paramInt2 > paramInt1 * 3) || (paramInt1 * 2 <= this.prevMinDiff * 3));
    while (paramInt1 > this.prevMinDiff) {
      return true;
    }
    return false;
  }
  
  private void processStreamInput()
  {
    int i = this.numOutputSamples;
    float f1 = this.speed / this.pitch;
    float f2 = this.rate * this.pitch;
    if ((f1 > 1.00001D) || (f1 < 0.99999D)) {
      changeSpeed(f1);
    }
    for (;;)
    {
      if (f2 != 1.0F) {
        adjustRate(f2, i);
      }
      return;
      copyToOutput(this.inputBuffer, 0, this.numInputSamples);
      this.numInputSamples = 0;
    }
  }
  
  private void removePitchSamples(int paramInt)
  {
    if (paramInt == 0) {
      return;
    }
    System.arraycopy(this.pitchBuffer, this.numChannels * paramInt, this.pitchBuffer, 0, (this.numPitchSamples - paramInt) * this.numChannels);
    this.numPitchSamples -= paramInt;
  }
  
  private void removeProcessedInputSamples(int paramInt)
  {
    int i = this.numInputSamples - paramInt;
    System.arraycopy(this.inputBuffer, this.numChannels * paramInt, this.inputBuffer, 0, this.numChannels * i);
    this.numInputSamples = i;
  }
  
  private int skipPitchPeriod(short[] paramArrayOfShort, int paramInt1, float paramFloat, int paramInt2)
  {
    int i;
    if (paramFloat >= 2.0F) {
      i = (int)(paramInt2 / (paramFloat - 1.0F));
    }
    for (;;)
    {
      enlargeOutputBufferIfNeeded(i);
      overlapAdd(i, this.numChannels, this.outputBuffer, this.numOutputSamples, paramArrayOfShort, paramInt1, paramArrayOfShort, paramInt1 + paramInt2);
      this.numOutputSamples += i;
      return i;
      i = paramInt2;
      this.remainingInputToCopy = ((int)(paramInt2 * (2.0F - paramFloat) / (paramFloat - 1.0F)));
    }
  }
  
  public void getOutput(ShortBuffer paramShortBuffer)
  {
    int i = Math.min(paramShortBuffer.remaining() / this.numChannels, this.numOutputSamples);
    paramShortBuffer.put(this.outputBuffer, 0, this.numChannels * i);
    this.numOutputSamples -= i;
    System.arraycopy(this.outputBuffer, this.numChannels * i, this.outputBuffer, 0, this.numOutputSamples * this.numChannels);
  }
  
  public int getSamplesAvailable()
  {
    return this.numOutputSamples;
  }
  
  public void queueEndOfStream()
  {
    int j = this.numInputSamples;
    float f1 = this.speed / this.pitch;
    float f2 = this.rate;
    float f3 = this.pitch;
    int k = this.numOutputSamples + (int)((j / f1 + this.numPitchSamples) / (f2 * f3) + 0.5F);
    enlargeInputBufferIfNeeded(this.maxRequired * 2 + j);
    int i = 0;
    while (i < this.maxRequired * 2 * this.numChannels)
    {
      this.inputBuffer[(this.numChannels * j + i)] = 0;
      i += 1;
    }
    this.numInputSamples += this.maxRequired * 2;
    processStreamInput();
    if (this.numOutputSamples > k) {
      this.numOutputSamples = k;
    }
    this.numInputSamples = 0;
    this.remainingInputToCopy = 0;
    this.numPitchSamples = 0;
  }
  
  public void queueInput(ShortBuffer paramShortBuffer)
  {
    int i = paramShortBuffer.remaining() / this.numChannels;
    int j = this.numChannels;
    enlargeInputBufferIfNeeded(i);
    paramShortBuffer.get(this.inputBuffer, this.numInputSamples * this.numChannels, j * i * 2 / 2);
    this.numInputSamples += i;
    processStreamInput();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/audio/Sonic.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */