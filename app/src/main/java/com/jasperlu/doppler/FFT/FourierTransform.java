package com.jasperlu.doppler.FFT;

public abstract class FourierTransform {
  protected static final int LINAVG = 1;
  
  protected static final int LOGAVG = 2;
  
  protected static final int NOAVG = 3;
  
  protected static final float TWO_PI = 6.2831855F;
  
  protected float[] averages;
  
  protected int avgPerOctave;
  
  protected float bandWidth;
  
  protected float[] imag;
  
  protected int octaves;
  
  protected float[] real;
  
  protected int sampleRate;
  
  protected float[] spectrum;
  
  protected int timeSize;
  
  protected int whichAverage;
  
  FourierTransform(int paramInt, float paramFloat) {
    this.timeSize = paramInt;
    int i = (int)paramFloat;
    this.sampleRate = i;
    this.bandWidth = 2.0F / paramInt * i / 2.0F;
    noAverages();
    allocateArrays();
  }
  
  protected abstract void allocateArrays();
  
  public int avgSize() {
    return this.averages.length;
  }
  
  public float calcAvg(float paramFloat1, float paramFloat2) {
    int j = freqToIndex(paramFloat1);
    int k = freqToIndex(paramFloat2);
    paramFloat1 = 0.0F;
    for (int i = j; i <= k; i++)
      paramFloat1 += this.spectrum[i]; 
    return paramFloat1 / (k - j + 1);
  }
  
  protected void fillSpectrum() {
    int i = 0;
    while (true) {
      float[] arrayOfFloat = this.spectrum;
      if (i < arrayOfFloat.length) {
        float[] arrayOfFloat1 = this.real;
        float f1 = arrayOfFloat1[i];
        float f2 = arrayOfFloat1[i];
        arrayOfFloat1 = this.imag;
        arrayOfFloat[i] = (float)Math.sqrt((f1 * f2 + arrayOfFloat1[i] * arrayOfFloat1[i]));
        i++;
        continue;
      } 
      i = this.whichAverage;
      if (i == 1) {
        int j = arrayOfFloat.length / this.averages.length;
        for (i = 0; i < this.averages.length; i++) {
          float f = 0.0F;
          int k = 0;
          while (k < j) {
            int m = i * j + k;
            arrayOfFloat = this.spectrum;
            if (m < arrayOfFloat.length) {
              f += arrayOfFloat[m];
              k++;
            } 
          } 
          f /= (k + 1);
          this.averages[i] = f;
        } 
        return;
      } 
      if (i == 2) {
        i = 0;
        while (true) {
          int j = this.octaves;
          if (i < j) {
            float f1;
            if (i == 0) {
              f1 = 0.0F;
            } else {
              f1 = (this.sampleRate / 2) / (float)Math.pow(2.0D, (j - i));
            } 
            float f2 = ((this.sampleRate / 2) / (float)Math.pow(2.0D, (this.octaves - i - 1)) - f1) / this.avgPerOctave;
            j = 0;
            while (true) {
              int k = this.avgPerOctave;
              if (j < k) {
                this.averages[k * i + j] = calcAvg(f1, f1 + f2);
                f1 += f2;
                j++;
                continue;
              } 
              i++;
            } 
          } 
          break;
        } 
      } 
      return;
    } 
  }
  
  public abstract void forward(float[] paramArrayOffloat);
  
  public void forward(float[] paramArrayOffloat, int paramInt) {
    int i = paramArrayOffloat.length;
    int j = this.timeSize;
    if (i - paramInt < j)
      return; 
    float[] arrayOfFloat = new float[j];
    System.arraycopy(paramArrayOffloat, paramInt, arrayOfFloat, 0, arrayOfFloat.length);
    forward(arrayOfFloat);
  }
  
  public int freqToIndex(float paramFloat) {
    if (paramFloat < getBandWidth() / 2.0F)
      return 0; 
    if (paramFloat > (this.sampleRate / 2) - getBandWidth() / 2.0F)
      return this.spectrum.length - 1; 
    paramFloat /= this.sampleRate;
    return Math.round(this.timeSize * paramFloat);
  }
  
  public float getAverageBandWidth(int paramInt) {
    int i = this.whichAverage;
    if (i == 1)
      return (this.spectrum.length / this.averages.length) * getBandWidth(); 
    if (i == 2) {
      float f;
      paramInt /= this.avgPerOctave;
      if (paramInt == 0) {
        f = 0.0F;
      } else {
        f = (this.sampleRate / 2) / (float)Math.pow(2.0D, (this.octaves - paramInt));
      } 
      return ((this.sampleRate / 2) / (float)Math.pow(2.0D, (this.octaves - paramInt - 1)) - f) / this.avgPerOctave;
    } 
    return 0.0F;
  }
  
  public float getAverageCenterFrequency(int paramInt) {
    int i = this.whichAverage;
    if (i == 1) {
      i = this.spectrum.length / this.averages.length;
      return indexToFreq(paramInt * i + i / 2);
    } 
    if (i == 2) {
      float f1;
      i = this.avgPerOctave;
      int j = paramInt / i;
      if (j == 0) {
        f1 = 0.0F;
      } else {
        f1 = (this.sampleRate / 2) / (float)Math.pow(2.0D, (this.octaves - j));
      } 
      float f2 = ((this.sampleRate / 2) / (float)Math.pow(2.0D, (this.octaves - j - 1)) - f1) / this.avgPerOctave;
      float f3 = (paramInt % i);
      return f2 / 2.0F + f3 * f2 + f1;
    } 
    return 0.0F;
  }
  
  public float getAvg(int paramInt) {
    float[] arrayOfFloat = this.averages;
    return (arrayOfFloat.length > 0) ? arrayOfFloat[paramInt] : 0.0F;
  }
  
  public float getBand(int paramInt) {
    int i = paramInt;
    if (paramInt < 0)
      i = 0; 
    float[] arrayOfFloat = this.spectrum;
    paramInt = i;
    if (i > arrayOfFloat.length - 1)
      paramInt = arrayOfFloat.length - 1; 
    return this.spectrum[paramInt];
  }
  
  public float getBandWidth() {
    return this.bandWidth;
  }
  
  public float getFreq(float paramFloat) {
    return getBand(freqToIndex(paramFloat));
  }
  
  public float[] getSpectrumImaginary() {
    return this.imag;
  }
  
  public float[] getSpectrumReal() {
    return this.real;
  }
  
  public float indexToFreq(int paramInt) {
    float f = getBandWidth();
    return (paramInt == 0) ? (0.25F * f) : ((paramInt == this.spectrum.length - 1) ? ((this.sampleRate / 2) - f / 2.0F + 0.25F * f) : (paramInt * f));
  }
  
  public abstract void inverse(float[] paramArrayOffloat);
  
  public void inverse(float[] paramArrayOffloat1, float[] paramArrayOffloat2, float[] paramArrayOffloat3) {
    setComplex(paramArrayOffloat1, paramArrayOffloat2);
    inverse(paramArrayOffloat3);
  }
  
  public void linAverages(int paramInt) {
    if (paramInt > this.spectrum.length / 2)
      return; 
    this.averages = new float[paramInt];
    this.whichAverage = 1;
  }
  
  public void logAverages(int paramInt1, int paramInt2) {
    float f = this.sampleRate / 2.0F;
    this.octaves = 1;
    while (true) {
      float f1 = f / 2.0F;
      f = f1;
      if (f1 > paramInt1) {
        this.octaves++;
        continue;
      } 
      this.avgPerOctave = paramInt2;
      this.averages = new float[this.octaves * paramInt2];
      this.whichAverage = 2;
      return;
    } 
  }
  
  public void noAverages() {
    this.averages = new float[0];
    this.whichAverage = 3;
  }
  
  public abstract void scaleBand(int paramInt, float paramFloat);
  
  public void scaleFreq(float paramFloat1, float paramFloat2) {
    scaleBand(freqToIndex(paramFloat1), paramFloat2);
  }
  
  public abstract void setBand(int paramInt, float paramFloat);
  
  protected void setComplex(float[] paramArrayOffloat1, float[] paramArrayOffloat2) {
    if (this.real.length != paramArrayOffloat1.length && this.imag.length != paramArrayOffloat2.length)
      return; 
    System.arraycopy(paramArrayOffloat1, 0, this.real, 0, paramArrayOffloat1.length);
    System.arraycopy(paramArrayOffloat2, 0, this.imag, 0, paramArrayOffloat2.length);
  }
  
  public void setFreq(float paramFloat1, float paramFloat2) {
    setBand(freqToIndex(paramFloat1), paramFloat2);
  }
  
  public int specSize() {
    return this.spectrum.length;
  }
  
  public int timeSize() {
    return this.timeSize;
  }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\jasperlu\doppler\FFT\FourierTransform.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */