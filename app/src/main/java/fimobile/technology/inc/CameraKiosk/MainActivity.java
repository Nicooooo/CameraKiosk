package fimobile.technology.inc.CameraKiosk;
/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fimobile.technology.inc.CameraKiosk.imageeditor.EditImageActivity;
import fimobile.technology.inc.CameraKiosk.photoeditor.PhotoEditor;
import fimobile.technology.inc.CameraKiosk.photoeditor.PhotoEditorView;


/**
 * This demo app saves the taken picture to a constant file.
 * $ adb pull /sdcard/Android/data/com.google.android.cameraview.demo/files/Pictures/picture.jpg
 */
public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        AspectRatioFragment.Listener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private CameraFragment CameraFragment;
    private EditSavePhotoFragment EditSavePhotoFragment;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final int REQUEST_STORAGE = 5;

    private View mToolsLayout, mValueLayout;
    private ImageButton save_photo;
    private ToggleButton camera_button;
    private ImageParameters mImageParameters;

    private static String newBmp = "bitmap";
    private PhotoEditorView mPhotoEditorView;
    private PhotoEditor mPhotoEditor;
    private String oldBmp = null;
    //    private USBMonitor.UsbControlBlock usbCtrlBlock;
    private UsbDevice usbDevice;
    private boolean isPrefname;
    private TextView countTextView;
    private ImageButton timer;
    private String capture;
    private int[] images = { R.drawable.timerthree_100_white, R.drawable.timerfive_100_whiteb,
            R.drawable.timer_100_whiteb };
    private int currentImage;
    Bitmap bmp; // your bitmap
    ByteArrayOutputStream bs;

    private int mCurrentFlash;

    private CameraView mCameraView;

    private Handler mBackgroundHandler;

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    private static final boolean USE_SURFACE_ENCODER = false;
    private Handler handler;
    private Runnable r;
    private boolean stopHandler = false;
    private Future longRunningTask;
    private Bitmap bmp2;
    Canvas comboImage;


//    private static final File mediaStorageDir = new File("mnt/usb_storage/USB_DISK0/udisk0/testFolder");
//private static final File mediaStorageDir = new File("mnt/storage/emulated/0/DCIM/USBCameraTest/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " oncreate ");
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_layout);
        mCameraView = findViewById(R.id.camera_view);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }

        save_photo = (ImageButton)findViewById(R.id.save_photo);
        save_photo.setOnClickListener(this);
        camera_button = (ToggleButton) findViewById(R.id.camera_button);
        countTextView = (TextView) findViewById(R.id.countdown);
        currentImage = 2;
        timer = (ImageButton) findViewById(R.id.timer);
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, " timer1 ");
                currentImage++;
                currentImage = currentImage % images.length;

                timer.setImageResource(images[currentImage]);
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        handler = new Handler();
        r = new Runnable() {

            @Override
            public void run() {
                stopHandler = true;
//                Toast.makeText(MainActivity.this, "user Is Idle from last 5 minutes",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "runnable");
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    Intent i = new Intent(MainActivity.this, VideoplayerActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(i);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        stopHandler = false;

        String[] perms = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

        int permsRequestCode = 200;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        {
            mCameraView.start();
            stopHandler();
            startHandler();
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(perms, permsRequestCode);
            }
        }
        hideSystemUI();
        save_photo.setEnabled(true);
        save_photo.setClickable(true);
    }


    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
        save_photo.setEnabled(true);
        save_photo.setClickable(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case 200:
                boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                break;
        }
    }


    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        if (mCameraView != null) {
//            Toast.makeText(this, ratio.toString(), Toast.LENGTH_SHORT).show();
            mCameraView.setAspectRatio(ratio);
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onPictureTaken onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) throws IOException {
//            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT).show();
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap bmpFrame = BitmapFactory.decodeResource(getResources(), R.drawable.cherryframe);

            if(bmp != null)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Log.d(TAG, "onPictureTaken onPictureTaken" + bmp);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File mediaStorageDir = new File(
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
//                        "Kiosk"
                        "mnt/usb_storage/USB_DISK0/udisk0/testFolder"
                );
//                File mediaStorageDir = new File(
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
//                        "Kiosk"
//                );
                String media = String.valueOf(mediaStorageDir);
                Log.d(TAG, "MEDIASTORAGE"+mediaStorageDir);

                if (!mediaStorageDir.exists()) {
                    new File(media).mkdir();
                }

                File file = new File(mediaStorageDir+"/"+timeStamp+".jpg");
                file.createNewFile();


                Bitmap bitmap = bmp;
                bitmap = RotateBitmap(bitmap, 270);
                Log.d(TAG, "FILEKO"+ bitmap.getWidth() + " comboImage " + bitmap.getHeight());
                bmpFrame = RotateBitmap2(bitmap, bmpFrame, 0);
                Bitmap bmp3 = combineImages(bitmap, bmpFrame);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp3.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();


//                Bitmap fbitmap = new Bitmap(comboImage);
                FileOutputStream fos = new FileOutputStream(file);
                Log.d(TAG, "FILEKO"+ file + " comboImage " + bmp3);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                String path = String.valueOf(file);
                Intent myIntent = new Intent(MainActivity.this, EditImageActivity.class);
                myIntent.putExtra("bitmap", path);
                MainActivity.this.startActivity(myIntent); //ran_edit_101018: removed (MainActivity.this) in MainActivity.this.startActivity(myIntent)
            }
        }

    };

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postScale(-1f, 1f, source.getWidth()/2, source.getHeight()/2);
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap RotateBitmap2(Bitmap source, Bitmap qwe, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postScale(1f, 1f, qwe.getWidth()/2, qwe.getHeight()/2);
        matrix.postRotate(0);

        return Bitmap.createScaledBitmap(qwe, source.getWidth(),source.getHeight(), false);
    }

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    Toast.makeText(getActivity(),
//                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
//                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

        public void show(String fragmentDialog) {
        }
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.save_photo){
            Log.d(TAG," save_photo " + currentImage);
//            if (mCameraView != null) {
//                mCameraView.takePicture();
//            }

            if (currentImage == 0 )
            {
                countTextView.setVisibility(View.VISIBLE);
                new CountDownTimer(3000, 1000)
                {
                    public void onTick(long millisUntilFinished) {
                        countTextView.setText("" + millisUntilFinished / 1000);
                    }
                    public void onFinish() {
                        countTextView.setVisibility(View.GONE);
//                    if (mCameraHandler.isOpened()) {
//                        mCameraHandler.captureStill();
//                        updateBitmap();
//                    }
//                        if(!mediaStorageDir.exists())
//                            Toast.makeText(MainActivity.this, "No usb storage, please insert usb.",Toast.LENGTH_SHORT).show();
                        mCameraView.takePicture();
                    }
                }.start();
            }
            else if (currentImage == 1)
            {
                countTextView.setVisibility(View.VISIBLE);
                new CountDownTimer(6000, 1000)
                {
                    public void onTick(long millisUntilFinished) {
                        countTextView.setText("" + millisUntilFinished / 1000);
                    }
                    public void onFinish() {
                        countTextView.setVisibility(View.GONE);
//                    if (mCameraHandler.isOpened()) {
//                        mCameraHandler.captureStill();
//                        updateBitmap();
//                    }
//                        if(!mediaStorageDir.exists())
//                            Toast.makeText(MainActivity.this, "No usb storage, please insert usb.",Toast.LENGTH_SHORT).show();
                        mCameraView.takePicture();
                    }
                }.start();
            }
            else if (currentImage == 2)
            {
//                if(!mediaStorageDir.exists())
//                    Toast.makeText(MainActivity.this, "No usb storage, please insert usb.",Toast.LENGTH_SHORT).show();
                mCameraView.takePicture();
//            if (mCameraHandler.isOpened()) {
//                mCameraHandler.captureStill();
//                updateBitmap();
//            }
            }
        }
    }


//    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
//            = new CompoundButton.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
//            switch (compoundButton.getId()) {
//                case R.id.camera_button:
//                    Log.d(TAG, " oncheckchange ");
////                    if (isChecked && !mCameraHandler.isOpened()) {
////                        CameraDialog.showDialog(MainActivity.this);
////                    } else {
////                        mCameraHandler.close();
//////                        setCameraButton(false);
////                    }
//                    break;
//            }
//        }
//    };

    @Override
    public void onUserInteraction()
    {
        super.onUserInteraction();
        Log.d(TAG,"onuserinteraction");
        if(!stopHandler){
            stopHandler();//stop first and then start
            startHandler();

        }

//        showAToast();
//        if (toast != null){
//            if(toast.getView().isShown()){
//                Log.d(TAG, " onUserInteraction " + "gumana ka");
//            }
//        }
//        else
//        {
//            Log.d(TAG, " onUserInteraction " + "gumana ka2");
//        }

    }

    public void stopHandler() {
        Log.d(TAG, "stopHandler");
        handler.removeCallbacks(r);
        handler.removeMessages(0);
    }
    public void startHandler() {
        Log.d(TAG, "startHandler");
        handler.postDelayed(r, 4*60*1000);
    }

    public void hideSystemUI(){
        Log.d(TAG," hideSystemUI ");
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public Bitmap combineImages(Bitmap picture, Bitmap frame) {
        Bitmap bmp2 = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
//        Matrix matrix = new Matrix();
//        matrix.postScale(1f, 1f, picture.getWidth()/2, picture.getHeight()/2);
//        matrix.postRotate(90);
        comboImage = new Canvas(bmp2);

        comboImage.drawBitmap(picture, 0f, 0f, null);
        comboImage.drawBitmap(frame,0f,0f, null);

        return bmp2;
    }
}
