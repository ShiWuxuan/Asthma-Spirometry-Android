package com.example.test;

import android.app.Activity;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;

public class PlaySound extends Activity {
  private final int duration = 10;
  
  private final double freqOfTone = 20000.0D;
  
  private final byte[] generatedSnd = new byte[882000];
  
  Handler handler = new Handler();
  
  private final int numSamples = 441000;
  
  private final double[] sample = new double[441000];
  
  private final int sampleRate = 44100;
  
  void genTone() {
    int i;
    for (i = 0; i < 441000; i++) {
      double[] arrayOfDouble1 = this.sample;
      double d = i;
      Double.isNaN(d);
      arrayOfDouble1[i] = Math.sin(d * 6.283185307179586D / 2.205D);
    } 
    int j = 0;
    double[] arrayOfDouble = this.sample;
    int k = arrayOfDouble.length;
    for (i = 0; i < k; i++) {
      short s = (short)(int)(32767.0D * arrayOfDouble[i]);
      byte[] arrayOfByte = this.generatedSnd;
      int m = j + 1;
      arrayOfByte[j] = (byte)(s & 0xFF);
      j = m + 1;
      arrayOfByte[m] = (byte)((0xFF00 & s) >>> 8);
    } 
  }
  
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
  }
  
  protected void onResume() {
    super.onResume();
    (new Thread(new Runnable() {
          public void run() {
            PlaySound.this.genTone();
            PlaySound.this.handler.post(new Runnable() {
                  public void run() {
                    PlaySound.this.playSound();
                  }
                });
          }
        })).start();
  }
  
  void playSound() {
    AudioTrack audioTrack = new AudioTrack(3, 44100, 4, 2, this.generatedSnd.length, 0);
    byte[] arrayOfByte = this.generatedSnd;
    audioTrack.write(arrayOfByte, 0, arrayOfByte.length);
    audioTrack.play();
  }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\example\mydoppler\PlaySound.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */