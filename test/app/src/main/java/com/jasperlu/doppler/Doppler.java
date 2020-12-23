package com.jasperlu.doppler;

import android.media.AudioRecord;
import android.os.Handler;
import android.util.Log;
import com.jasperlu.doppler.FFT.FFT;
import java.io.IOException;
import java.util.Arrays;

public class Doppler {
  public static final int DEFAULT_SAMPLE_RATE = 44100;
  
  private static final int LEFT_BANDWIDTH = 0;
  
  public static final int MAX_FREQ = 21000;
  
  private static final double MAX_VOL_RATIO_DEFAULT = 0.05D;
  
  public static final int MIN_FREQ = 19000;
  
  public static final float PRELIM_FREQ = 20000.0F;
  
  public static final int PRELIM_FREQ_INDEX = 20000;
  
  public static final int RELEVANT_FREQ_WINDOW = 33;
  
  private static final int RIGHT_BANDWIDTH = 1;
  
  private static final double SECOND_PEAK_RATIO = 0.3D;
  
  private static final float SMOOTHING_TIME_CONSTANT = 0.5F;
  
  private static final String TAG = "Doppler";
  
  public static double maxVolRatio = 0.05D;
  
  private int SAMPLE_RATE = 44100;
  
  private short[] buffer;
  
  int bufferReadResult;
  
  private int bufferSize = 2048;
  
  private boolean calibrate = true;
  
  Calibrator calibrator;
  
  private int cyclesLeftToRead = -1;
  
  private final int cyclesToRead = 5;
  
  private int cyclesToRefresh;
  
  private int directionChanges;
  
  private int directionSame;
  
  FFT fft;
  
  private float[] fftRealArray;
  
  private int freqIndex;
  
  private float frequency;
  
  private FrequencyPlayer frequencyPlayer;
  
  private OnGestureListener gestureListener;
  
  private boolean isGestureListenerAttached = false;
  
  private boolean isReadCallbackOn = false;
  
  private Handler mHandler;
  
  private AudioRecord microphone;
  
  private float[] oldFreqs;
  
  private int previousDirection = 0;
  
  private OnReadCallback readCallback;
  
  private boolean repeat;
  
  private long time;
  
  private AudioUtil util = null;
  
  private boolean wait_ges = false;
  
  public Doppler() {
    this.bufferSize = AudioRecord.getMinBufferSize(44100, 16, 2);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Doppler Construct: buffersize: ");
    stringBuilder.append(this.bufferSize);
    Log.d("Doppler", stringBuilder.toString());
    this.buffer = new short[this.bufferSize];
    this.frequency = 20000.0F;
    this.freqIndex = 20000;
    this.frequencyPlayer = new FrequencyPlayer(20000.0D);
    AudioRecord audioRecord = new AudioRecord(1, 44100, 16, 2, this.bufferSize);
    this.microphone = audioRecord;
    this.util = AudioUtil.getInstance(audioRecord, this.bufferSize);
    this.mHandler = new Handler();
    this.calibrator = new Calibrator();
  }
  
  private void setFrequency(float paramFloat) {
    this.frequency = paramFloat;
    this.freqIndex = this.fft.freqToIndex(paramFloat);
  }
  
  public void callGestureCallback(int paramInt1, int paramInt2) {
    int i = this.cyclesToRefresh;
    if (i > 0) {
      this.cyclesToRefresh = i - 1;
      return;
    } 
    if (paramInt1 > 4 || paramInt2 > 4) {
      paramInt1 = (int)Math.signum((paramInt1 - paramInt2));
      if (paramInt1 != 0 && paramInt1 != this.previousDirection) {
        Log.d("Doppler", "callGestureCallback: Gesture find! ");
        if (this.directionChanges == 2) {
          this.cyclesLeftToRead = 10;
        } else {
          this.cyclesLeftToRead = 5;
        } 
        this.previousDirection = paramInt1;
        this.directionChanges++;
        this.wait_ges = true;
      } 
    } 
    if (this.wait_ges) {
      this.cyclesLeftToRead--;
    } else {
      this.cyclesLeftToRead = -1;
    } 
    if (this.cyclesLeftToRead == 0) {
      paramInt1 = this.directionChanges;
      if (paramInt1 == 1) {
        if (this.previousDirection == -1) {
          this.gestureListener.onPush();
        } else {
          Log.d("Doppler", "callGestureCallback: onPull Find! ");
          this.gestureListener.onPull();
        } 
      } else if (paramInt1 == 2) {
        this.gestureListener.onTap();
      } else {
        this.gestureListener.onDoubleTap();
      } 
      this.previousDirection = 0;
      this.directionChanges = 0;
      this.cyclesToRefresh = 5;
      this.cyclesLeftToRead = -1;
      this.wait_ges = false;
      return;
    } 
    this.gestureListener.onNothing();
  }
  
  public void callReadCallback(int paramInt1, int paramInt2) {
    double[] arrayOfDouble = new double[this.fft.specSize()];
    for (int i = 0; i < this.fft.specSize(); i++)
      arrayOfDouble[i] = this.fft.getBand(i); 
    this.readCallback.onBandwidthRead(paramInt1, paramInt2);
    this.readCallback.onBinsRead(arrayOfDouble);
  }
  
  public int[] getBandwidth() {
    double d2;
    double d3;
    Doppler doppler = this;
    readAndFFT();
    int n = doppler.freqIndex;
    double d1 = doppler.fft.getBand(n);
    int i = 0;
    //TODO
    int j;
    while (true) {
      j = i + 1;
      d2 = doppler.fft.getBand(n - j);
      Double.isNaN(d2);
      Double.isNaN(d1);
      if (d2 / d1 > maxVolRatio) {
        i = j;
        if (j >= 33)
          break; 
        continue;
      } 
      break;
    } 
    int m = 0;
    int k = j;
    i = n;
    do {
      doppler = this;
      d2 = doppler.fft.getBand(i - ++k);
      Double.isNaN(d2);
      Double.isNaN(d1);
      d3 = d2 / d1;
      if (d3 <= 0.3D)
        continue; 
      m = 1;
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(d2);
      stringBuilder.append("");
      Log.d("Volume", stringBuilder.toString());
      stringBuilder = new StringBuilder();
      stringBuilder.append(d1);
      stringBuilder.append("");
      Log.d("Primary Vol", stringBuilder.toString());
      stringBuilder = new StringBuilder();
      stringBuilder.append(i - k);
      stringBuilder.append("");
      Log.d("Tone is ", stringBuilder.toString());
    } while ((m != 1 || d3 >= maxVolRatio) && k < 33);
    if (m != 1)
      k = j; 
    j = 0;
    do {
      d2 = doppler.fft.getBand(i + ++j);
      Double.isNaN(d2);
      Double.isNaN(d1);
    } while (d2 / d1 > maxVolRatio && j < 33);
    n = 0;
    int i1 = j;
    m = i;
    while (true) {
      i = i1 + 1;
      d2 = this.fft.getBand(m + i);
      Double.isNaN(d2);
      Double.isNaN(d1);
      d3 = d2 / d1;
      if (d3 > 0.3D) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(d2);
        stringBuilder.append("");
        Log.d("Volume", stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append(d1);
        stringBuilder.append("");
        Log.d("Primary Vol", stringBuilder.toString());
        n = 1;
      } 
      if ((n == 1 && d3 < maxVolRatio) || i >= 33) {
        if (n == 1)
          j = i; 
        return new int[] { k, j };
      } 
      i1 = i;
    } 
  }
  
  int getHigherP2(int paramInt) {
    paramInt = --paramInt | paramInt >> 1;
    paramInt |= paramInt >> 2;
    paramInt |= paramInt >> 8;
    return (paramInt | paramInt >> 16) + 1;
  }
  
  public void optimizeFrequency(int paramInt1, int paramInt2) {
    readAndFFT();
    paramInt1 = this.fft.freqToIndex(paramInt1);
    int i = this.fft.freqToIndex(paramInt2);
    int j;
    for (paramInt2 = this.freqIndex; paramInt1 <= i; paramInt2 = j) {
      j = paramInt2;
      if (this.fft.getBand(paramInt1) > this.fft.getBand(paramInt2))
        j = paramInt1; 
      paramInt1++;
    } 
    setFrequency(this.fft.indexToFreq(paramInt2));
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("optimizeFrequency: bandwidth：");
    stringBuilder.append(this.fft.indexToFreq(2) - this.fft.indexToFreq(1));
    Log.d("Doppler", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    stringBuilder.append(this.fft.indexToFreq(paramInt2));
    stringBuilder.append("");
    Log.d("NEW PRIMARY IND", stringBuilder.toString());
  }
  
  public boolean pause() {
    try {
      this.microphone.stop();
      this.frequencyPlayer.pause();
      this.repeat = false;
      this.util.stopRecord();
      return true;
    } catch (Exception exception) {
      exception.printStackTrace();
      return false;
    } 
  }
  
  public void readAndFFT() {
    if (this.fft.specSize() != 0 && this.oldFreqs == null)
      this.oldFreqs = new float[this.fft.specSize()]; 
    int i;
    for (i = 0; i < this.fft.specSize(); i++)
      this.oldFreqs[i] = this.fft.getBand(i); 
    i = this.microphone.read(this.buffer, 0, this.bufferSize);
    Log.d(TAG, "readAndFFT_1: "+i);
    Log.d(TAG, "readAndFFT_2: "+ Arrays.toString(this.buffer));
    this.bufferReadResult = i;
    try {
      this.util.writeData(this.buffer, i);
    } catch (IOException iOException) {
      iOException.printStackTrace();
    } 
    for (i = 0; i < this.bufferReadResult; i++)
      this.fftRealArray[i] = this.buffer[i] / 32767.0F; 
    i = 0;
    while (true) {
      int j = this.bufferReadResult;
      if (i < j / 2) {
        double d1 = i;
        Double.isNaN(d1);
        double d2 = (j / 2);
        Double.isNaN(d2);
        float f = (float)(Math.cos(d1 * Math.PI / d2) * 0.5D + 0.5D);
        if (i > this.bufferReadResult / 2)
          f = 0.0F; 
        float[] arrayOfFloat = this.fftRealArray;
        j = this.bufferReadResult;
        int k = j / 2 + i;
        arrayOfFloat[k] = arrayOfFloat[k] * f;
        j = j / 2 - i;
        arrayOfFloat[j] = arrayOfFloat[j] * f;
        i++;
        continue;
      } 
      this.fft.forward(this.fftRealArray);
      smoothOutFreqs();
      return;
    } 
  }
  
  public void readMic() {
    int[] arrayOfInt = getBandwidth();
    int i = arrayOfInt[0];
    int j = arrayOfInt[1];
    if (System.currentTimeMillis() - this.time > 5000L)
      this.calibrate = true; 
    if (this.isReadCallbackOn)
      callReadCallback(i, j); 
    if (this.isGestureListenerAttached)
      callGestureCallback(i, j); 
    if (this.calibrate) {
      maxVolRatio = this.calibrator.calibrate(maxVolRatio, i, j);
      this.calibrate = false;
      this.time = System.currentTimeMillis();
    } 
    if (this.repeat)
      this.mHandler.post(new Runnable() {
            public void run() {
              Doppler.this.readMic();
            }
          }); 
  }
  
  public void removeGestureListener() {
    this.gestureListener = null;
    this.isGestureListenerAttached = false;
  }
  
  public void removeReadCallback() {
    this.readCallback = null;
    this.isReadCallbackOn = false;
  }
  
  public boolean setCalibrate(boolean paramBoolean) {
    this.calibrate = paramBoolean;
    return paramBoolean;
  }
  
  public void setOnGestureListener(OnGestureListener paramOnGestureListener) {
    this.gestureListener = paramOnGestureListener;
    this.isGestureListenerAttached = true;
  }
  
  public void setOnReadCallback(OnReadCallback paramOnReadCallback) {
    this.readCallback = paramOnReadCallback;
    this.isReadCallbackOn = true;
  }
  
  public void smoothOutFreqs() {
    for (int i = 0; i < this.fft.specSize(); i++) {
      float f1 = this.fft.getBand(i);
      float f2 = this.oldFreqs[i];
      this.fft.setBand(i, f1 * 0.5F + f2 * 0.5F);
    } 
  }
  
  public boolean start() {
    this.frequencyPlayer.play();
    try {
      this.microphone.startRecording();
      this.util.startRecord();
      this.repeat = true;
      (new Handler()).post(new Runnable() {
            public void run() {
              Doppler.this.optimizeFrequency(19000, 21000);
              Doppler.this.readMic();
            }
          });
      if (true) {
        int i = getHigherP2(this.microphone.read(this.buffer, 0, this.bufferSize));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("start: bufferReadResult: ");
        stringBuilder.append(i);
        Log.d("Doppler", stringBuilder.toString());
        this.fftRealArray = new float[getHigherP2(i)];
        this.fft = new FFT(getHigherP2(i), this.SAMPLE_RATE);
      } 
      return true;
    } catch (Exception exception) {
      exception.printStackTrace();
      Log.d("DOPPLER", "start recording error");
      return false;
    } 
  }
  
  public static interface OnGestureListener {
    void onDoubleTap();
    
    void onNothing();
    
    void onPull();
    
    void onPush();
    
    void onTap();
  }
  
  public static interface OnReadCallback {
    void onBandwidthRead(int param1Int1, int param1Int2);
    
    void onBinsRead(double[] param1ArrayOfdouble);
  }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\jasperlu\doppler\Doppler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */