package com.jasperlu.doppler;

import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

public class FrequencyPlayer {
  private static int MILLIS_PER_SECOND = 1000;
  
  private AudioTrack audioTrack;
  
  private final int duration = 5000;
  
  private double freqOfTone;
  
  private byte[] generatedSound;
  
  Handler handler;
  
  private int numSamples;
  
  private double[] sample;
  
  private final int sampleRate = 44100;
  
  FrequencyPlayer(double paramDouble) {
    int i = this.numSamples;
    this.sample = new double[i];
    this.freqOfTone = 10000.0D;
    this.generatedSound = new byte[i * 2];
    this.handler = new Handler();
    i = 220500000 / MILLIS_PER_SECOND;
    this.numSamples = i;
    this.generatedSound = new byte[i * 2];
    this.sample = new double[i];
    this.audioTrack = new AudioTrack(3, 44100, 4, 2, this.generatedSound.length, 0);
    setFrequency(paramDouble);
  }
  
  public void changeFrequency(double paramDouble) {
    setFrequency(paramDouble);
    play();
  }
  
  void genTone() {
    int i;
    for (i = 0; i < this.numSamples; i++) {
      double[] arrayOfDouble1 = this.sample;
      double d = i;
      Double.isNaN(d);
      arrayOfDouble1[i] = Math.sin(d * 6.283185307179586D / 44100.0D / this.freqOfTone);
    } 
    int j = 0;
    double[] arrayOfDouble = this.sample;
    int k = arrayOfDouble.length;
    for (i = 0; i < k; i++) {
      short s = (short)(int)(32767.0D * arrayOfDouble[i]);
      byte[] arrayOfByte = this.generatedSound;
      int m = j + 1;
      arrayOfByte[j] = (byte)(s & 0xFF);
      j = m + 1;
      arrayOfByte[m] = (byte)((0xFF00 & s) >>> 8);
    } 
  }
  
  public void pause() {
    this.audioTrack.pause();
  }
  
  public void play() {
    this.audioTrack.play();
  }
  
  public void setFrequency(double paramDouble) {
    this.freqOfTone = paramDouble;
    genTone();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("");
    AudioTrack audioTrack = this.audioTrack;
    byte[] arrayOfByte = this.generatedSound;
    stringBuilder.append(audioTrack.write(arrayOfByte, 0, arrayOfByte.length));
    Log.d("FreqPlayer", stringBuilder.toString());
    this.audioTrack.setLoopPoints(0, this.generatedSound.length / 4, -1);
  }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\jasperlu\doppler\FrequencyPlayer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */