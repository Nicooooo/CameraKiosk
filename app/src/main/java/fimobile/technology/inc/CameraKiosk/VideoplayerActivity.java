package fimobile.technology.inc.CameraKiosk;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;

public class VideoplayerActivity extends Activity implements MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {

    private static final String TAG = VideoplayerActivity.class.getSimpleName();
    MediaPlayer mPlayer;
    SurfaceView sv;
    public ArrayList <String> videoList = new ArrayList <String>();
    private SurfaceHolder holder;
    private int currentVideo = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

//        Uri uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "bata.3gp");
        videoList.add( "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "cherry1.mp4");
//        videoList.add( "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + "cherry2.mp4");
//        videoList.add("/sdcard/download/test9-3.m4v");
        Log.d(TAG,"dir " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
//        mPlayer = MediaPlayer.create(this, uri);
        sv = (SurfaceView) findViewById(R.id.surfaceViewFrame);
        holder = sv.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        currentVideo = 0;



        sv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mPlayer.stop();
//                mPlayer.release();
                finish();
            }
        });

//        sv = (SurfaceView) findViewById(R.id.surfaceViewFrame);
//        mPlayer.setOnCompletionListener(this);
//        SurfaceHolder holder = sv.getHolder();
//        holder.addCallback(new SurfaceHolder.Callback(){
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
//
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                mPlayer .setDisplay(holder);
//                mPlayer .start();
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                Log.d(TAG, " asd123 surfacedestroyed ");
//            }
//        });
//
//    }
//
//    @Override
//    protected void onPause(){
//        super.onPause();
//
//        if(null != mPlayer) mPlayer.release();
//        mPlayer = null;
//    }
//
//    @Override
//    public void onCompletion(MediaPlayer mp) {
//        Log.d(TAG," asd123 onCompletion ");
//        mPlayer.release();
//        mPlayer.start();
//    }


    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        currentVideo++;
        if (currentVideo > videoList.size() - 1) {
            currentVideo = 0;
        }
        Log.d(TAG,"currentVideo " + currentVideo);
        mPlayer.release();
        playVideo(videoList.get(currentVideo));

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        playVideo(videoList.get(0));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "play next video");

    }
    private void playVideo(String videoPath) {
        Log.d(TAG, "playvideo");
        try {
            Log.d(TAG, "playvideo try");
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(this);
            mPlayer.setDisplay(holder);
            mPlayer.setDataSource(videoPath);
            mPlayer.prepare();
            mPlayer.start();
            Log.d(TAG, " videoPath " + videoPath);
        } catch (IllegalArgumentException e) {
            Log.e("MEDIA_PLAYER", "" +e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("MEDIA_PLAYER", "" +e.getMessage());
        } catch (IOException e) {
            Log.e("MEDIA_PLAYER", "" +e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.stop();
        mPlayer.release();
        finish();

    }
}

