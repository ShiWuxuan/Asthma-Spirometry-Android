package com.example.test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jasperlu.doppler.Doppler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import uk.me.berndporr.iirj.Butterworth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private String CollectedDataPath;

    private int bins_highNum = 1865;

    private int bins_highNum_paint = 1865;

    private int bins_lowNum = 1850;

    private int bins_lowNum_paint = 1850;

    private Button btStart;

    private BufferedWriter bw = null;

    private double[] chart2Data = new double[200];

    private int chart2Len = 200;

    private Chronometer chronometer;

    private int currentIndex = 0;

    private final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

    private final XYMultipleSeriesDataset dataset2 = new XYMultipleSeriesDataset();

    private XYSeries div10;

    private XYSeriesRenderer div10Renderer;

    private Doppler doppler;

    File file = null;

    RandomAccessFile file_l = null;

    RandomAccessFile file_2 = null;

    private boolean firstChart2 = true;

    private FileOutputStream fos = null;

    private boolean isFirstRead = true;

    private XYSeries line2;

    private XYSeriesRenderer line2Renderer;

    private GraphicalView mChart;

    private GraphicalView mChart2;

    private XYSeriesRenderer mRenderer;

    private XYSeriesRenderer mRenderer2;

    private XYSeries mSeries;

    private XYSeries mSeries2;

    private OutputStreamWriter osw = null;

    private TextView promptTextView;

    private final XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

    private final XYMultipleSeriesRenderer renderer2 = new XYMultipleSeriesRenderer();

    private int tag = 0;

    private String timetag;

    private String timeFormat = "HH:mm:ss:SSS";

    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> mPermissionList = new ArrayList<>();

    // private ImageView welcomeImg = null;
    private static final int PERMISSION_REQUEST = 1;

    public MainActivity() {
        String stringBuilder;
        stringBuilder = Environment.getExternalStorageDirectory().getPath() +
                "/CollectedData/BINS文件/";
        this.CollectedDataPath = stringBuilder;
    }

    private double ESD_result(int paramInt1, int paramInt2, double[] paramArrayOfdouble) {
        double d = 0.0D;
        while (paramInt1 <= paramInt2) {
            d += paramArrayOfdouble[paramInt1] * paramArrayOfdouble[paramInt1];
            paramInt1++;
        }
        return d;
    }

    private int findTag(String paramString) {
        File file = new File(paramString);
        if (!file.exists()) {
            boolean isCreate = file.mkdirs();
            if(!isCreate){
                Log.d(TAG, "findTag: 文件夹未生成");
            }
            return 0;
        }
        //返回一个字符串数组，命名该抽象路径名表示的目录中的文件和目录。
        String[] arrayOfString = file.list();
//        if(arrayOfString==null)
//        {
//            Log.d(TAG, "findTag: arrayIsNull");
//        }
        Log.d(TAG, "findTag: "+ Arrays.toString(arrayOfString));
        return arrayOfString.length;
        /**
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
        */
    }

    private void renderGraph() {
        this.mSeries = new XYSeries("Vols/FreqBin");
        this.div10 = new XYSeries("div10");
        this.mSeries.add(3.0D, 4.0D);
        this.mRenderer = new XYSeriesRenderer();
        XYSeriesRenderer xYSeriesRenderer = new XYSeriesRenderer();
        this.div10Renderer = xYSeriesRenderer;
        //TODO
        xYSeriesRenderer.setColor(Color.RED);
        this.dataset.addSeries(this.mSeries);
        this.dataset.addSeries(this.div10);
        this.renderer.addSeriesRenderer((SimpleSeriesRenderer)this.mRenderer);
        this.renderer.addSeriesRenderer((SimpleSeriesRenderer)this.div10Renderer);
        this.renderer.setPanEnabled(true);
        this.renderer.setZoomEnabled(true);
        this.renderer.setZoomEnabled(true);
        this.renderer.setLabelsTextSize(28.0F);
        this.mRenderer.setDisplayChartValues(true);
        this.mRenderer.setPointStyle(PointStyle.CIRCLE);
        this.mRenderer.setPointStrokeWidth(6.0F);
        this.mRenderer.setChartValuesTextSize(28.0F);
        this.mRenderer.setDisplayChartValuesDistance(10);
        this.mChart = ChartFactory.getLineChartView((Context)this, this.dataset, this.renderer);

        ((LinearLayout)findViewById(R.id.chart)).addView((View)this.mChart);
    }

    private void renderGraph2() {
        this.mSeries2 = new XYSeries("Energy");
        this.line2 = new XYSeries("line2");
        this.mSeries2.add(3.0D, 4.0D);
        this.mRenderer2 = new XYSeriesRenderer();
        XYSeriesRenderer xYSeriesRenderer = new XYSeriesRenderer();
        this.line2Renderer = xYSeriesRenderer;
        xYSeriesRenderer.setColor(Color.RED);
        this.dataset2.addSeries(this.mSeries2);
        this.dataset2.addSeries(this.line2);
        this.renderer2.addSeriesRenderer((SimpleSeriesRenderer)this.mRenderer2);
        this.renderer2.addSeriesRenderer((SimpleSeriesRenderer)this.line2Renderer);
        this.renderer2.setPanEnabled(true);
        this.renderer2.setZoomEnabled(true);
        this.renderer2.setZoomEnabled(true);
        this.renderer2.setLabelsTextSize(28.0F);
        this.mChart2 = ChartFactory.getLineChartView((Context)this, this.dataset2, this.renderer2);
        ((LinearLayout)findViewById(R.id.chart2)).addView((View)this.mChart2);
    }

    boolean isFolderExists(String paramString) {
        File file = new File(paramString);
        return !file.exists() ? file.mkdir() : true;
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        checkPermission();
        this.promptTextView = (TextView)findViewById(R.id.promptText);
        this.tag = findTag(this.CollectedDataPath);
        //保持屏幕常亮
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.doppler = new Doppler();
        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        this.chronometer = chronometer;
        chronometer.setFormat("录音用时：%s");
        Button button = (Button)findViewById(R.id.btStart);
        this.btStart = button;
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View param1View) {
                MainActivity.this.btStart.setSelected(!MainActivity.this.btStart.isSelected());
                if (MainActivity.this.btStart.isSelected()) {
                    MainActivity.this.btStart.setText("结束录音");
                    MainActivity.this.btStart.setBackgroundColor(Color.rgb(240,240,240));
                    /**
                    MainActivity.access$108(MainActivity.this);
                     */
//                    MainActivity.access$202(MainActivity.this, true);
                    MainActivity.this.isFirstRead = true;

                    MainActivity.this.chronometer.setBase(SystemClock.elapsedRealtime());
                    MainActivity.this.chronometer.start();
                    MainActivity.this.doppler.start();
                    return;
                }
                MainActivity.this.btStart.setText("开始录音");
                MainActivity.this.btStart.setBackgroundColor(Color.rgb(164,233,167));
                MainActivity.this.chronometer.stop();
                MainActivity.this.doppler.pause();
            }
        });
        renderGraph();
        renderGraph2();
        startGraph();
    }

    public void startGraph() {
        this.doppler.setOnReadCallback(new Doppler.OnReadCallback() {
            public void onBandwidthRead(int param1Int1, int param1Int2) {}

            /**
            //写入时间戳
            public void writeTimeStamp(){
                try{
                    MainActivity mainActivity1 = MainActivity.this;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(MainActivity.this.CollectedDataPath);
                    stringBuilder.append("/TEST_BINS");
                    stringBuilder.append(MainActivity.this.tag);
                    stringBuilder.append(".csv");
                    mainActivity1.file_2 = new RandomAccessFile(stringBuilder.toString(), "rw");
                    long l = MainActivity.this.file_2.length();
                    MainActivity.this.file_2.seek(l);
                    String format = "YYYY-MM-dd hh:mm:ss:SSS";
                    MainActivity.this.file_2.writeBytes(LocalDateTime.now(ZoneOffset.of("+8")).format(DateTimeFormatter.ofPattern(format)));
                    MainActivity.this.file_2.writeBytes("\n");
                    MainActivity.this.file_2.close();

                    Log.d(TAG, "writeTimeStamp: 写入时间戳成功");
                }catch (Exception exception) {
                    exception.printStackTrace();
                    Log.d(TAG, "writeTimeStamp: 写入时间戳失败");
                }
            }*/

            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onBinsRead(double[] param1ArrayOfdouble) {
                MainActivity.this.mSeries.clear();
                MainActivity.this.div10.clear();
                double d1 = param1ArrayOfdouble[929];

/**                MainActivity.this.doppler;
 *
 */
                double d2 = Doppler.maxVolRatio;
                int i;
                for (i = MainActivity.this.bins_lowNum_paint; i < MainActivity.this.bins_highNum_paint; i++) {
                    MainActivity.this.mSeries.add(i, param1ArrayOfdouble[i]);
                    MainActivity.this.div10.add(i, d1 * d2);
                }
                MainActivity.this.mChart.repaint();
                if (MainActivity.this.isFirstRead) {
                    MainActivity mainActivity1 = MainActivity.this;
                    String str = MainActivity.this.CollectedDataPath;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("/TEST_BINS");
                    stringBuilder.append(MainActivity.this.tag);
                    stringBuilder.append(".csv");
                    Log.i(TAG, "onBinsRead: fileName"+stringBuilder);
                    mainActivity1.file = new File(str, stringBuilder.toString());
                    Log.d(TAG, "onBinsRead: canWrite"+file.canWrite());
                    Log.d(TAG, "onBinsRead: canRead"+file.canRead());
                    Log.d(TAG, "onBinsRead: exists"+file.exists());
                    try {
                        MainActivity.this.file.createNewFile();
//                        MainActivity.access$1102(MainActivity.this, new FileOutputStream(MainActivity.this.file));
                        MainActivity.this.fos = new FileOutputStream(MainActivity.this.file);
//                        MainActivity.access$1202(MainActivity.this, new OutputStreamWriter(MainActivity.this.fos));
                        MainActivity.this.osw = new OutputStreamWriter(MainActivity.this.fos);
//                        MainActivity.access$1302(MainActivity.this, new BufferedWriter(MainActivity.this.osw));
                        MainActivity.this.bw = new BufferedWriter(MainActivity.this.osw);
                        boolean bool = true;
                        i = MainActivity.this.bins_lowNum;
                        while (i <= MainActivity.this.bins_highNum) {
                            StringBuilder stringBuilder1 = new StringBuilder();
                            boolean bool1 = bool;
                            if (bool) {
                                stringBuilder1.append("Time Tag");
                                stringBuilder1.append(",");
                                bool1 = false;
                            }
                            stringBuilder1.append(i);
                            stringBuilder1.append(",");
                            MainActivity.this.bw.write(stringBuilder1.toString());
                            MainActivity.this.bw.flush();
                            i++;
                            bool = bool1;
                        }
                        MainActivity.this.bw.write("\n");
                        MainActivity.this.bw.flush();
//                        MainActivity.access$1602(MainActivity.this, System.currentTimeMillis());

                        for (i = MainActivity.this.bins_lowNum; i <= MainActivity.this.bins_highNum; i++) {
                            Log.d("MainActivity", "onBinsRead: in for");
                            StringBuilder stringBuilder1 = new StringBuilder();
                            stringBuilder1.append(MainActivity.this.timetag);
                            stringBuilder1.append(",");
                            stringBuilder1.append(param1ArrayOfdouble[i]);
                            MainActivity.this.bw.write(stringBuilder1.toString());
                            MainActivity.this.bw.flush();
                        }
                        MainActivity.this.bw.write("\n");
                        MainActivity.this.bw.flush();
//                        MainActivity.access$202(MainActivity.this, false);
                        MainActivity.this.isFirstRead = false;
                        Log.d("MainActivity", "onBinsRead: file created");
                        if (MainActivity.this.fos != null)
                            try {
                                MainActivity.this.fos.close();
//                                MainActivity.access$1102(MainActivity.this, (FileOutputStream)null);
                                MainActivity.this.fos=null;
                            } catch (IOException iOException) {
                                iOException.printStackTrace();
                            }
                        if (MainActivity.this.osw != null)
                            try {
                                MainActivity.this.osw.close();
//                                MainActivity.access$1202(MainActivity.this, (OutputStreamWriter)null);
                                MainActivity.this.osw = null;
                            } catch (IOException iOException) {
                                iOException.printStackTrace();
                            }
                        if (MainActivity.this.bw != null)
                            try {
                                MainActivity.this.bw.close();
//                                MainActivity.access$1302(MainActivity.this, (BufferedWriter)null);
                                MainActivity.this.bw=null;
                            } catch (IOException iOException) {
                                iOException.printStackTrace();
                            }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        if (MainActivity.this.fos != null)
                            try {
                                MainActivity.this.fos.close();
//                                MainActivity.access$1102(MainActivity.this, (FileOutputStream)null);
                                MainActivity.this.fos=null;
                            } catch (IOException iOException) {
                                iOException.printStackTrace();
                            }
                        if (MainActivity.this.osw != null)
                            try {
                                MainActivity.this.osw.close();
//                                MainActivity.access$1202(MainActivity.this, (OutputStreamWriter)null);
                                MainActivity.this.osw = null;
                            } catch (IOException iOException) {
                                iOException.printStackTrace();
                            }
                        if (MainActivity.this.bw != null) {
                            try {
                                MainActivity.this.bw.close();
//                                MainActivity.access$1302(MainActivity.this, (BufferedWriter)null);
                                MainActivity.this.bw = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } finally {}
                } else {
                    try {
                        MainActivity mainActivity1 = MainActivity.this;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(MainActivity.this.CollectedDataPath);
                        stringBuilder.append("/TEST_BINS");
                        stringBuilder.append(MainActivity.this.tag);
                        stringBuilder.append(".csv");
                        mainActivity1.file_l = new RandomAccessFile(stringBuilder.toString(), "rw");
                        long l = MainActivity.this.file_l.length();
                        MainActivity.this.file_l.seek(l);
//                        MainActivity.access$1602(MainActivity.this, System.currentTimeMillis());
                        //todo 已修改
                        // MainActivity.this.timetag = System.currentTimeMillis();
                        MainActivity.this.timetag = LocalDateTime.now(ZoneOffset.of("+8")).format(DateTimeFormatter.ofPattern(timeFormat));
                        MainActivity.this.file_l.writeBytes(String.valueOf(MainActivity.this.timetag));
                        for (i = MainActivity.this.bins_lowNum; i <= MainActivity.this.bins_highNum; i++) {
                            MainActivity.this.file_l.writeBytes(",");
                            MainActivity.this.file_l.writeBytes(String.valueOf(param1ArrayOfdouble[i]));
                        }
                        MainActivity.this.file_l.writeBytes("\n");
                        MainActivity.this.file_l.close();
                        Log.d("MainActivity", "onBinsRead: file write finished");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Log.d("MainActivity", "onBinsRead: TEST_BIN.csv write error");
                    }
                }
                MainActivity.this.mSeries2.clear();
                d1 = MainActivity.this.ESD_result(1856, 1860, param1ArrayOfdouble) / 2048.0D;
                if (MainActivity.this.firstChart2) {
                    MainActivity.this.line2.clear();
                    for (i = 0; i < MainActivity.this.chart2Len; i++) {
                        MainActivity.this.chart2Data[i] = 0.0D;
                        MainActivity.this.line2.add(i, 0.0D);
                    }
//                    MainActivity.access$1902(MainActivity.this, false);
                    MainActivity.this.firstChart2 = false;
                }
//                Log.i(TAG, "onBinsRead: 是否为0："+MainActivity.this.chart2Len);
                MainActivity.this.chart2Data[MainActivity.this.currentIndex] = d1;
                for (i = 0; i < MainActivity.this.chart2Len; i++)
                    MainActivity.this.mSeries2.add(i, MainActivity.this.chart2Data[(MainActivity.this.currentIndex + i + 1) % MainActivity.this.chart2Len]);
                MainActivity mainActivity = MainActivity.this;
//                MainActivity.access$2302(mainActivity, (mainActivity.currentIndex + 1) % MainActivity.this.chart2Len);
                mainActivity.currentIndex = (mainActivity.currentIndex + 1) % MainActivity.this.chart2Len;
                MainActivity.this.mChart2.repaint();
            }
        });

        this.doppler.setOnGestureListener(new Doppler.OnGestureListener() {
            public void onDoubleTap() {
                MainActivity.this.promptTextView.setBackgroundColor(Color.GRAY);
                MainActivity.this.promptTextView.setText("doubleTap");
            }

            public void onNothing() {
                MainActivity.this.promptTextView.setBackgroundColor(Color.WHITE);
                MainActivity.this.promptTextView.setText("nothing");
            }

            public void onPull() {
                MainActivity.this.promptTextView.setBackgroundColor(Color.BLUE);
                MainActivity.this.promptTextView.setText("pull");
            }

            public void onPush() {
                MainActivity.this.promptTextView.setBackgroundColor(Color.RED);
                MainActivity.this.promptTextView.setText("push");
            }

            public void onTap() {
                MainActivity.this.promptTextView.setBackgroundColor(Color.YELLOW);
                MainActivity.this.promptTextView.setText("tap");
            }
        });
    }


    /**
     * 巴特沃斯带通滤波
     * @param data 原始音频数据
     * @param lowCut 最小截止频率
     * @param highCut 最大截止频率
     * @param fs 音频采样率
     * @param order 滤波阶数
     * @return 滤波结果
     */
    public double[] butter_bandpass_filter(Double[] data, double lowCut, double highCut, int fs, int order){
        Butterworth butterworth = new Butterworth();
        double widthFrequency=highCut-lowCut;
        double centerFrequency=(highCut+lowCut)/2;
        butterworth.bandPass(order,fs,centerFrequency,widthFrequency);
        double[] list = new double[data.length];
        int in=0;
        for(double v : data){
            double f=butterworth.filter(v);
            list[in]=f;
            in++;
        }
        return list;
    }


    private void checkPermission() {
        mPermissionList.clear();

        //判断哪些权限未授予
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        /**
         * 判断是否为空
         */
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了

        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }

    /**
     * 响应授权
     * 这里不管用户是否拒绝，都进入首页，不再重复申请权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
}


/* Location:              D:\Program file\AndroidTool\dex2jar-2.0\classes-dex2jar.jar!\com\example\mydoppler\MainActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */