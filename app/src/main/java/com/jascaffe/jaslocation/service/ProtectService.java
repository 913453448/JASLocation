package com.jascaffe.jaslocation.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.jascaffe.jaslocation.utils.AbAppConfig;
import com.jascaffe.jaslocation.utils.AbAppUtil;

/**
 * Created by yasin on 2017/8/22.
 */

public class ProtectService extends Service {
    public static boolean sPower = true, isRunning;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (sPower) {
                        SystemClock.sleep(AbAppConfig.TIMER_DURATION2);
                        if (!AbAppUtil.isServiceRunning(ProtectService.this, LocationService.class.getName())) {
                            startService(new Intent(ProtectService.this, LocationService.class));
                        }
                    }
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}