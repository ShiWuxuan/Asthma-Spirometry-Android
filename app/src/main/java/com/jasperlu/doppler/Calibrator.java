package com.jasperlu.doppler;

public class Calibrator {
  private final double DOWN_AMOUNT = 0.9D;
  
  private final int DOWN_THRESHOLD = 0;
  
  private final int ITERATION_CYCLES = 20;
  
  private final double MAX_RATIO = 0.95D;
  
  private final double MIN_RATIO = 1.0E-4D;
  
  private final double UP_AMOUNT = 1.1D;
  
  private final int UP_THRESHOLD = 5;
  
  private double directionChanges = 0.0D;
  
  private int iteration = 0;
  
  private double previousDiff = 0.0D;
  
  private int previousDirection = 0;
  
  public double calibrate(double paramDouble, int paramInt1, int paramInt2) {
    paramInt1 = (int)Math.signum((paramInt1 - paramInt2));
    if (this.previousDirection != paramInt1) {
      this.directionChanges++;
      this.previousDirection = paramInt1;
    } 
    paramInt1 = this.iteration + 1;
    this.iteration = paramInt1;
    paramInt1 %= 20;
    this.iteration = paramInt1;
    double d = paramDouble;
    if (paramInt1 == 0) {
      d = paramDouble;
      if (this.directionChanges >= 5.0D)
        d = paramDouble * 1.1D; 
      paramDouble = d;
      if (this.directionChanges == 0.0D)
        paramDouble = d * 0.9D; 
      d = Math.max(1.0E-4D, Math.min(0.95D, paramDouble));
      this.directionChanges = 0.0D;
    } 
    return d;
  }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\jasperlu\doppler\Calibrator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */