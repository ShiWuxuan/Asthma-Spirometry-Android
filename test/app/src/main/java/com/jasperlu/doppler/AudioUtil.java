package com.jasperlu.doppler;

import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AudioUtil {
  private static final String TAG = "AudioUtil";
  
  private static int audioChannel;
  
  private static int audioFormat;
  
  private static int audioRate;
  
  private static int audioSource = 1;
  
  private static int bufferSize;
  
  private static AudioUtil mInstance;
  
  private String basePath;
  
  private boolean firstTag;
  
  private String inFileName;
  
  private boolean isRecording = false;
  
  private short[] nataArrayData;
  
  private byte[] noteArray;
  
  private OutputStream os;
  
  private String outFileName;
  
  private File pcmFile;
  
  private AudioRecord recorder;
  
  private int timeTag;
  
  private File wavFile;
  
  static {
    audioRate = 44100;
    audioChannel = 16;
    audioFormat = 2;
    bufferSize = AudioRecord.getMinBufferSize(44100, 16, 2);
  }
  
  private AudioUtil() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(Environment.getExternalStorageDirectory().getPath());
    stringBuilder.append("/CollectedData/WAV文件");
    String str = stringBuilder.toString();
    this.basePath = str;
    this.firstTag = true;
    this.timeTag = 0;
    isFolderExists(str);
    this.recorder = new AudioRecord(audioSource, audioRate, audioChannel, audioFormat, bufferSize);
  }
  
  private AudioUtil(AudioRecord paramAudioRecord, int paramInt) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(Environment.getExternalStorageDirectory().getPath());
    stringBuilder.append("/CollectedData/WAV文件");
    String str = stringBuilder.toString();
    this.basePath = str;
    this.firstTag = true;
    this.timeTag = 0;
    this.timeTag = findTag(str);
    this.recorder = paramAudioRecord;
    bufferSize = paramInt;
  }
  
  private void WriteWaveFileHeader(FileOutputStream paramFileOutputStream, long paramLong1, long paramLong2, long paramLong3, int paramInt, long paramLong4) throws IOException {
    paramFileOutputStream.write(new byte[] { 
          82, 73, 70, 70, (byte)(int)(paramLong2 & 0xFFL), (byte)(int)(paramLong2 >> 8L & 0xFFL), (byte)(int)(paramLong2 >> 16L & 0xFFL), (byte)(int)(paramLong2 >> 24L & 0xFFL), 87, 65, 
          86, 69, 102, 109, 116, 32, 16, 0, 0, 0, 
          1, 0, (byte)paramInt, 0, (byte)(int)(paramLong3 & 0xFFL), (byte)(int)(paramLong3 >> 8L & 0xFFL), (byte)(int)(paramLong3 >> 16L & 0xFFL), (byte)(int)(paramLong3 >> 24L & 0xFFL), (byte)(int)(paramLong4 & 0xFFL), (byte)(int)(paramLong4 >> 8L & 0xFFL), 
          (byte)(int)(paramLong4 >> 16L & 0xFFL), (byte)(int)(paramLong4 >> 24L & 0xFFL), 2, 0, 16, 0, 100, 97, 116, 97, 
          (byte)(int)(paramLong1 & 0xFFL), (byte)(int)(paramLong1 >> 8L & 0xFFL), (byte)(int)(paramLong1 >> 16L & 0xFFL), (byte)(int)(0xFFL & paramLong1 >> 24L) }, 0, 44);
  }
  
  private int findTag(String paramString) {
    File file = new File(paramString);
    if (!file.exists()) {
      file.mkdirs();
      return 0;
    } 
    String[] arrayOfString = file.list();
    int k = arrayOfString.length;
    if (k == 0)
      return 0; 
    int j = -1;
    int i = 0;
    while (i < k) {
      char[] arrayOfChar = arrayOfString[i].toCharArray();
      int m = 0;
      int n = 0;
      while (n < arrayOfChar.length) {
        int i1 = m;
        if (arrayOfChar[n] >= '0') {
          i1 = m;
          if (arrayOfChar[n] <= '9')
            i1 = m * 10 + arrayOfChar[n] - 48; 
        } 
        n++;
        m = i1;
      } 
      n = j;
      if (m > j)
        n = m; 
      i++;
      j = n;
    } 
    return j;
  }

  public static AudioUtil getInstance(AudioRecord paramAudioRecord, int paramInt) {
    if (mInstance == null){
      mInstance = new AudioUtil(paramAudioRecord,paramInt);
    }

    // Byte code:
    //   0: ldc com/jasperlu/doppler/AudioUtil
    //   2: monitorenter
    //   3: getstatic com/jasperlu/doppler/AudioUtil.mInstance : Lcom/jasperlu/doppler/AudioUtil;
    //   6: ifnonnull -> 21
    //   9: new com/jasperlu/doppler/AudioUtil
    //   12: dup
    //   13: aload_0
    //   14: iload_1
    //   15: invokespecial <init> : (Landroid/media/AudioRecord;I)V
    //   18: putstatic com/jasperlu/doppler/AudioUtil.mInstance : Lcom/jasperlu/doppler/AudioUtil;
    //   21: getstatic com/jasperlu/doppler/AudioUtil.mInstance : Lcom/jasperlu/doppler/AudioUtil;
    //   24: astore_0
    //   25: ldc com/jasperlu/doppler/AudioUtil
    //   27: monitorexit
    //   28: aload_0
    //   29: areturn
    //   30: astore_0
    //   31: ldc com/jasperlu/doppler/AudioUtil
    //   33: monitorexit
    //   34: aload_0
    //   35: athrow
    // Exception table:
    //   from	to	target	type
    //   3	21	30	finally
    //   21	25	30	finally
    return mInstance;
  }
  
  public static AudioUtil getNewInstance() {
    // Byte code:
    //   0: ldc com/jasperlu/doppler/AudioUtil
    //   2: monitorenter
    //   3: getstatic com/jasperlu/doppler/AudioUtil.mInstance : Lcom/jasperlu/doppler/AudioUtil;
    //   6: ifnonnull -> 19
    //   9: new com/jasperlu/doppler/AudioUtil
    //   12: dup
    //   13: invokespecial <init> : ()V
    //   16: putstatic com/jasperlu/doppler/AudioUtil.mInstance : Lcom/jasperlu/doppler/AudioUtil;
    //   19: getstatic com/jasperlu/doppler/AudioUtil.mInstance : Lcom/jasperlu/doppler/AudioUtil;
    //   22: astore_0
    //   23: ldc com/jasperlu/doppler/AudioUtil
    //   25: monitorexit
    //   26: aload_0
    //   27: areturn
    //   28: astore_0
    //   29: ldc com/jasperlu/doppler/AudioUtil
    //   31: monitorexit
    //   32: aload_0
    //   33: athrow
    // Exception table:
    //   from	to	target	type
    //   3	19	28	finally
    //   19	23	28	finally
    return null;
  }

  
  public void convertWaveFile() {
    int i = audioRate;
    long l1 = i;
    long l2 = (i * 16 * 1 / 8);
    byte[] arrayOfByte = new byte[bufferSize];
    try {
      FileInputStream fileInputStream = new FileInputStream(this.inFileName);
      FileOutputStream fileOutputStream = new FileOutputStream(this.outFileName);
      long l = fileInputStream.getChannel().size();
      try {
        WriteWaveFileHeader(fileOutputStream, l, l + 36L, l1, 1, l2);
        while (fileInputStream.read(arrayOfByte) != -1)
          fileOutputStream.write(arrayOfByte); 
        fileInputStream.close();
        fileOutputStream.close();
        this.pcmFile.delete();
        return;
      } catch (FileNotFoundException e1) {
      } catch (IOException e2){
        e2.printStackTrace();
      }
    }  catch (IOException iOException) {
      iOException.printStackTrace();
      return;
    }
  }
  
  public void createFile() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(this.basePath);
    stringBuilder.append("/getWav");
    stringBuilder.append(this.timeTag);
    stringBuilder.append(".pcm");
    this.pcmFile = new File(stringBuilder.toString());
    stringBuilder = new StringBuilder();
    stringBuilder.append(this.basePath);
    stringBuilder.append("/getWav");
    stringBuilder.append(this.timeTag);
    stringBuilder.append(".wav");
    this.wavFile = new File(stringBuilder.toString());
    if (this.pcmFile.exists())
      this.pcmFile.delete(); 
    if (this.wavFile.exists())
      this.wavFile.delete(); 
    try {
      this.pcmFile.getParentFile().mkdirs();

      boolean result = this.pcmFile.createNewFile();
      if(result){
        Log.d(TAG, "createFile: success");
      }
      this.wavFile.getParentFile().mkdirs();
      this.wavFile.createNewFile();
      stringBuilder = new StringBuilder();
      stringBuilder.append(this.basePath);
      stringBuilder.append("/getWav");
      stringBuilder.append(this.timeTag);
      stringBuilder.append(".pcm");
      this.inFileName = stringBuilder.toString();
      stringBuilder = new StringBuilder();
      stringBuilder.append(this.basePath);
      stringBuilder.append("/getWav");
      stringBuilder.append(this.timeTag);
      stringBuilder.append(".wav");
      this.outFileName = stringBuilder.toString();
      return;
    } catch (IOException iOException) {
      iOException.printStackTrace();
      return;
    } 
  }
  
  boolean isFolderExists(String paramString) {
    File file = new File(paramString);
    return !file.exists() ? (file.mkdirs()) : true;
  }
  
  public void recordData() {
    (new Thread(new Runnable() {
          public void run() {
            try {
              AudioUtil.this.writeData();
              return;
            } catch (IOException iOException) {
              iOException.printStackTrace();
              return;
            } 
          }
        })).start();
  }
  
  public void startRecord() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("startRecord: basePath ");
    stringBuilder.append(this.basePath);
    Log.d("AudioUtil", stringBuilder.toString());
    this.isRecording = true;
    this.firstTag = true;
    this.timeTag++;
    createFile();
  }
  
  public void stopRecord() throws IOException {
    this.isRecording = false;
    writeData(this.nataArrayData, 0);
    convertWaveFile();
  }
  
  public void writeData() throws IOException {
    this.noteArray = new byte[bufferSize];
    try {
      this.os = new BufferedOutputStream(new FileOutputStream(this.pcmFile));
    } catch (IOException iOException) {
      iOException.printStackTrace();
    } 
    while (this.isRecording) {
      int j = this.recorder.read(this.noteArray, 0, bufferSize);
      for (int i = 0; i < bufferSize; i++) {
        byte[] arrayOfByte = this.noteArray;
        arrayOfByte[i] = (byte)(arrayOfByte[i] >> 2);
      } 
      if (j > 0)
        try {
          this.os.write(this.noteArray);
        } catch (IOException iOException) {
          iOException.printStackTrace();
          throw iOException;
        }  
    } 
    OutputStream outputStream = this.os;
    if (outputStream != null)
      try {
        outputStream.close();
        return;
      } catch (IOException iOException) {
        iOException.printStackTrace();
      }  
  }
  
  public void writeData(short[] paramArrayOfshort, int paramInt) throws IOException {
    OutputStream outputStream = this.os;
    if (outputStream != null && !this.isRecording)
      try {
        outputStream.close();
      } catch (IOException iOException) {
        iOException.printStackTrace();
      }  
    if (this.firstTag) {
      this.nataArrayData = new short[bufferSize];
      try {
        if (pcmFile!=null){
          Log.d(TAG, "writeData: fileIsNotNull");
        }
        this.os = new BufferedOutputStream(new FileOutputStream(this.pcmFile));
      } catch (IOException iOException) {
        iOException.printStackTrace();
      } 
      this.firstTag = false;
    } 
    this.nataArrayData = paramArrayOfshort;
    byte[] arrayOfByte = new byte[bufferSize * 2];
    ByteBuffer.wrap(arrayOfByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(this.nataArrayData);
    if (paramInt * 2 > 0) {
      try {
        Log.d(TAG, "writeData: "+ Arrays.toString(arrayOfByte));
        if (this.os==null){
          Log.d(TAG, "writeData: osIsNull");
        }
        this.os.write(arrayOfByte);
      } catch (IOException iOException) {
        iOException.printStackTrace();
        throw iOException;
      }
    }
  }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\jasperlu\doppler\AudioUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */