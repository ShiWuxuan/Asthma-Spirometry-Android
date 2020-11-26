package com.example.test;

import com.jasperlu.doppler.Doppler;

public class TheDoppler {
  private static Doppler doppler;
  
  public static Doppler getDoppler() {
    if (doppler == null)
      doppler = new Doppler(); 
    return doppler;
  }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\example\mydoppler\TheDoppler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */