package com.jascaffe.jaslocation.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.jascaffe.jaslocation.R;

/**
 * Created by yasin on 2017/8/22.
 */

public class CancelService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(250, builder.build());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(1000);
                    stopForeground(true);
                    NotificationManager manager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.cancel(250);
                    stopSelf();
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}