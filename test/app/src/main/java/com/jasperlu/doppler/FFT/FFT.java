package com.jasperlu.doppler.FFT;

public class FFT extends FourierTransform {
  private float[] coslookup;
  
  private int[] reverse;
  
  private float[] sinlookup;
  
  public FFT(int paramInt, float paramFloat) {
    super(paramInt, paramFloat);
    if ((paramInt - 1 & paramInt) == 0) {
      buildReverseTable();
      buildTrigTables();
      return;
    } 
    throw new IllegalArgumentException("FFT: timeSize must be a power of two.");
  }
  
  private void bitReverseComplex() {
    float[] arrayOfFloat1 = new float[this.real.length];
    float[] arrayOfFloat2 = new float[this.imag.length];
    for (int i = 0; i < this.real.length; i++) {
      arrayOfFloat1[i] = this.real[this.reverse[i]];
      arrayOfFloat2[i] = this.imag[this.reverse[i]];
    } 
    this.real = arrayOfFloat1;
    this.imag = arrayOfFloat2;
  }
  
  private void bitReverseSamples(float[] paramArrayOffloat, int paramInt) {
    for (int i = 0; i < this.timeSize; i++) {
      this.real[i] = paramArrayOffloat[this.reverse[i] + paramInt];
      this.imag[i] = 0.0F;
    } 
  }
  
  private void buildReverseTable() {
    int k = this.timeSize;
    int[] arrayOfInt = new int[k];
    this.reverse = arrayOfInt;
    arrayOfInt[0] = 0;
    int j = 1;
    for (int i = k / 2; j < k; i >>= 1) {
      for (int m = 0; m < j; m++) {
        arrayOfInt = this.reverse;
        arrayOfInt[m + j] = arrayOfInt[m] + i;
      } 
      j <<= 1;
    } 
  }
  
  private void buildTrigTables() {
    int j = this.timeSize;
    this.sinlookup = new float[j];
    this.coslookup = new float[j];
    for (int i = 0; i < j; i++) {
      this.sinlookup[i] = (float)Math.sin((-3.1415927F / i));
      this.coslookup[i] = (float)Math.cos((-3.1415927F / i));
    } 
  }
  
  private float cos(int paramInt) {
    return this.coslookup[paramInt];
  }
  
  private void fft() {
    int i;
    for (i = 1; i < this.real.length; i *= 2) {
      float f3 = cos(i);
      float f4 = sin(i);
      float f1 = 1.0F;
      float f2 = 0.0F;
      int j = 0;
      while (j < i) {
        int k;
        for (k = j; k < this.real.length; k += i * 2) {
          int m = k + i;
          float f5 = this.real[m] * f1 - this.imag[m] * f2;
          float f6 = this.imag[m] * f1 + this.real[m] * f2;
          this.real[m] = this.real[k] - f5;
          this.imag[m] = this.imag[k] - f6;
          float[] arrayOfFloat = this.real;
          arrayOfFloat[k] = arrayOfFloat[k] + f5;
          arrayOfFloat = this.imag;
          arrayOfFloat[k] = arrayOfFloat[k] + f6;
        } 
        float f = f1 * f3 - f2 * f4;
        f2 = f1 * f4 + f2 * f3;
        j++;
        f1 = f;
      } 
    } 
  }
  
  private float sin(int paramInt) {
    return this.sinlookup[paramInt];
  }
  
  protected void allocateArrays() {
    this.spectrum = new float[this.timeSize / 2 + 1];
    this.real = new float[this.timeSize];
    this.imag = new float[this.timeSize];
  }
  
  public void forward(float[] paramArrayOffloat) {
    if (paramArrayOffloat.length != this.timeSize)
      return; 
    bitReverseSamples(paramArrayOffloat, 0);
    fft();
    fillSpectrum();
  }
  
  public void forward(float[] paramArrayOffloat, int paramInt) {
    if (paramArrayOffloat.length - paramInt < this.timeSize)
      return; 
    bitReverseSamples(paramArrayOffloat, paramInt);
    fft();
    fillSpectrum();
  }
  
  public void forward(float[] paramArrayOffloat1, float[] paramArrayOffloat2) {
    if (paramArrayOffloat1.length == this.timeSize) {
      if (paramArrayOffloat2.length != this.timeSize)
        return; 
      setComplex(paramArrayOffloat1, paramArrayOffloat2);
      bitReverseComplex();
      fft();
      fillSpectrum();
      return;
    } 
  }
  
  public void inverse(float[] paramArrayOffloat) {
    if (paramArrayOffloat.length > this.real.length)
      return; 
    int i;
    for (i = 0; i < this.timeSize; i++) {
      float[] arrayOfFloat = this.imag;
      arrayOfFloat[i] = arrayOfFloat[i] * -1.0F;
    } 
    bitReverseComplex();
    fft();
    for (i = 0; i < paramArrayOffloat.length; i++)
      paramArrayOffloat[i] = this.real[i] / this.real.length; 
  }
  
  public void scaleBand(int paramInt, float paramFloat) {
    if (paramFloat < 0.0F)
      return; 
    float[] arrayOfFloat = this.real;
    arrayOfFloat[paramInt] = arrayOfFloat[paramInt] * paramFloat;
    arrayOfFloat = this.imag;
    arrayOfFloat[paramInt] = arrayOfFloat[paramInt] * paramFloat;
    arrayOfFloat = this.spectrum;
    arrayOfFloat[paramInt] = arrayOfFloat[paramInt] * paramFloat;
    if (paramInt != 0 && paramInt != this.timeSize / 2) {
      this.real[this.timeSize - paramInt] = this.real[paramInt];
      this.imag[this.timeSize - paramInt] = -this.imag[paramInt];
    } 
  }
  
  public void setBand(int paramInt, float paramFloat) {
    if (paramFloat < 0.0F)
      return; 
    if (this.real[paramInt] == 0.0F && this.imag[paramInt] == 0.0F) {
      this.real[paramInt] = paramFloat;
      this.spectrum[paramInt] = paramFloat;
    } else {
      float[] arrayOfFloat = this.real;
      arrayOfFloat[paramInt] = arrayOfFloat[paramInt] / this.spectrum[paramInt];
      arrayOfFloat = this.imag;
      arrayOfFloat[paramInt] = arrayOfFloat[paramInt] / this.spectrum[paramInt];
      this.spectrum[paramInt] = paramFloat;
      arrayOfFloat = this.real;
      arrayOfFloat[paramInt] = arrayOfFloat[paramInt] * this.spectrum[paramInt];
      arrayOfFloat = this.imag;
      arrayOfFloat[paramInt] = arrayOfFloat[paramInt] * this.spectrum[paramInt];
    } 
    if (paramInt != 0 && paramInt != this.timeSize / 2) {
      this.real[this.timeSize - paramInt] = this.real[paramInt];
      this.imag[this.timeSize - paramInt] = -this.imag[paramInt];
    } 
  }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\jasperlu\doppler\FFT\FFT.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */