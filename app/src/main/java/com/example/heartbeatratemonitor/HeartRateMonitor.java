package com.example.heartbeatratemonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.heartbeatratemonitor.ui.main.History;
import com.google.android.material.snackbar.Snackbar;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
public class HeartRateMonitor extends Activity {

    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);

    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static TextView text = null;

    private static WakeLock wakeLock = null;

    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];

    private static ProgressBar progressBar;
    private static TextView progressText;
    public static  int imgAvg;


    public enum TYPE {
        GREEN, RED
    }

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }
    private static final int WAKE_LOCK_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
    int historyId;
    static final Handler handler = new Handler();
    static boolean alreadyExecuted;
    static boolean detected = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        SurfaceView preview = findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        image = findViewById(R.id.image);
        text = findViewById(R.id.text);
        alreadyExecuted = false;
       int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }

        int permissionWakeLock = ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK);
        if (permissionWakeLock != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, WAKE_LOCK_REQUEST_CODE);
        }else{
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen:");
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,  @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == WAKE_LOCK_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onResume() {
        super.onResume();
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, WAKE_LOCK_REQUEST_CODE);
        }else{
            wakeLock.acquire(10*60*1000L /*10 minutes*/);
            camera = Camera.open();
            startTime = System.currentTimeMillis();
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        wakeLock.release();

        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
        handler.removeCallbacksAndMessages(null);
    }

    private static final PreviewCallback previewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();
//            if (!processing.compareAndSet(false, true)) return;
            int width = size.width;
            int height = size.height;
            imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
            //if (!processing.compareAndSet(false, true)) return;
            // Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }
            if(imgAvg > 200) {
                if (!alreadyExecuted){
                    progressText.setText(R.string.detecting);
                }
                int averageArrayAvg = 0;
                int averageArrayCnt = 0;
                for (int k : averageArray) {
                    if (k > 0) {
                        averageArrayAvg += k;
                        averageArrayCnt++;
                    }
                }

                int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
                TYPE newType = currentType;
                if (imgAvg < rollingAverage) {
                    newType = TYPE.RED;
                    if (newType != currentType) {
                        beats++;
                        // Log.d(TAG, "BEAT!! beats="+beats);
                    }
                } else if (imgAvg > rollingAverage) {
                    newType = TYPE.GREEN;
                }

                if (averageIndex == averageArraySize) averageIndex = 0;
                averageArray[averageIndex] = imgAvg;
                averageIndex++;

                // Transitioned from one state to another to the same
                if (newType != currentType) {
                    currentType = newType;
                    image.postInvalidate();
                }
                long endTime = System.currentTimeMillis();
                double totalTimeInSecs = (endTime - startTime) / 1000d;
                if (totalTimeInSecs >= 10) {
                    double bps = (beats / totalTimeInSecs);
                    int dpm = (int) (bps * 60d);
                    if (dpm < 30 || dpm > 180) {
                        startTime = System.currentTimeMillis();
                        beats = 0;
                        processing.set(false);
                        return;
                    }

                    //pause handler
                    if (!alreadyExecuted){
                        runProgressBar();
                    }

                    if (beatsIndex == beatsArraySize) beatsIndex = 0;
                    beatsArray[beatsIndex] = dpm;
                    beatsIndex++;

                    int beatsArrayAvg = 0;
                    int beatsArrayCnt = 0;
                    for (int j : beatsArray) {
                        if (j > 0) {
                            beatsArrayAvg += j;
                            beatsArrayCnt++;
                        }
                    }
                    int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                    text.setText(MessageFormat.format("{0} bpm", String.valueOf(beatsAvg)));
                    startTime = System.currentTimeMillis();
                    beats = 0;
                }
                processing.set(false);
            }else{
                if (!alreadyExecuted){
                    progressText.setText(R.string.progressText);
                }
            }

        }
    };

    private static final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @SuppressLint("LongLogTag")
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }
        @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStop() {

        super.onStop();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String date = dateTimeFormatter.format(now);

        String heartbeatValue = (String) text.getText();
        String full_history = date+" -> "+heartbeatValue;

            History.history.add(" ");
            historyId = History.history.size() -1;
            History.history.set(historyId, full_history);
            History.arrayAdapter.notifyDataSetChanged();

            //Creating Object of SharePreference to store data in the phone
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.heartBeatRate", Context.MODE_PRIVATE);
            HashSet<String> set = new HashSet<>(History.history);
            sharedPreferences.edit().putStringSet("history", set).apply();
    }
    public static void runProgressBar(){
        alreadyExecuted = true;
        int[] i = {0};
           handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                   if (i[0] <101){
                       progressText.setText(MessageFormat.format(" {0} % processing...", i[0]));
                       progressBar.setProgress(i[0]);
                       i[0]++;
                       if(imgAvg<200){
                           progressText.setText("Place back your finger");
                           alreadyExecuted = false;
                           text.setText("00 bpm");
                           return;
                       }
                       handler.postDelayed(this, 200);
                   }else{
                       handler.removeCallbacks(this);
                       progressText.setText(MessageFormat.format(" {0} % completed", i[0]-1));
                       progressText.setTextColor(Color.parseColor("#11801E"));
                       camera.stopPreview();
                   }
               }
           }, 200);
    }
}
