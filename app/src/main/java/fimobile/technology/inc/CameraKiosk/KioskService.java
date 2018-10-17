package fimobile.technology.inc.CameraKiosk;

/**
 * Created by CTI-OBD-RA on 2/12/2018.
 */

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import fimobile.technology.inc.CameraKiosk.utils.PrefUtils;

public class KioskService extends Service {

    private static final long INTERVAL = TimeUnit.SECONDS.toMillis(5); // periodic interval to check in seconds -> 2 seconds
    private static final String TAG = KioskService.class.getSimpleName();

    private Thread t = null;
    private Context ctx = null;
    private boolean running = false;

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service 'KioskService'");
        running =false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service 'KioskService'");
        running = true;
        ctx = this;

        // start a thread that periodically checks if your app is in the foreground
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    Log.d(TAG," Starting service handleKioskMode");
//                    handleKioskMode();
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Thread interrupted: 'KioskService'");
                    }
                }while(running);
                stopSelf();
            }
        });

        t.start();
        return Service.START_NOT_STICKY;
    }

    private void handleKioskMode() {
        // is Kiosk Mode active?
        Log.d(TAG,"isKioskModeActive: Force stop handleKioskMode");
        if(!PrefUtils.isKioskModeActive(ctx)) {
            Log.d(TAG,"isKioskModeActive: Force stopadadadadadad");
/*onUserInteraction*/
                if(isInBackground()) {
                    Log.d(TAG,"isKioskModeActive: Force stop!!!!!!!!!!!!");
                    if(!CameraSettingPreferences.forceStop()){
                        Log.d(TAG,"isKioskModeActive: Force stop");
                    // is App in background?
//                        videoPlayer(); // restore!
                    }
                }
        }
    }

    private boolean isInBackground() {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return (!ctx.getApplicationContext().getPackageName().equals(componentInfo.getPackageName()));
    }

    private void restoreApp() {
        // Restart activity
        Log.d(TAG, "restoreApp");
        Intent i = new Intent(ctx, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    private void videoPlayer() {
        // Restart activity
        Log.d(TAG, "restoreApp");
        Intent i = new Intent(ctx, VideoplayerActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
