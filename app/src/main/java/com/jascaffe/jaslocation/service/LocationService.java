package com.jascaffe.jaslocation.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.jascaffe.jaslocation.net.OkHttpClientManager;
import com.jascaffe.jaslocation.utils.AbAppConfig;
import com.jascaffe.jaslocation.utils.AbLogUtil;
import com.jascaffe.jaslocation.utils.AbSharedUtil;
import com.jascaffe.jaslocation.utils.SecurityUtil;
import com.squareup.okhttp.Request;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 定位service
 *
 * @author yqy
 */
public class LocationService extends Service {
    private String TAG = "LocationService";
    private LocationClient mLocClient;
    private BDLocation mLocation;
    private Timer mTimer;
    private TimerTask mTimerTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //startTimer();
            AbLogUtil.i(TAG, "开始定位....");
            // 初始化搜索模块，注册事件监听
            mLocClient = new LocationClient(getApplicationContext());
            //声明LocationClient类
            mLocClient.registerLocationListener(new MyLocationListenner());
            //注册监听函数
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);// 打开gps
            option.setCoorType("bd09ll"); // 设置坐标类型
            option.setScanSpan(AbAppConfig.LOCATION_TIME);
            //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

            option.setIsNeedAddress(true);
            //可选，设置是否需要地址信息，默认不需要

            option.setLocationNotify(true);
            //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

            option.setIsNeedLocationDescribe(true);
            //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

            option.setIsNeedLocationPoiList(true);
            //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

            option.setIgnoreKillProcess(false);
            //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

            mLocClient.setLocOption(option);
            mLocClient.start();
            startTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class MyLocationListenner extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            try {
                // map view 销毁后不在处理新接收的位置
                if (location == null)
                    return;
                mLocation = location;
                /**
                 * 保存当前的经纬度
                 */
                double latitude = mLocation.getLatitude();
                double longitude = mLocation.getLongitude();
                double altitude = mLocation.getAltitude();
                if ((latitude != 4.9E-324) && (longitude != 4.9E-324)) {
                    AbSharedUtil.putString(LocationService.this,
                            AbAppConfig.LATITUDE, mLocation.getLatitude() + "");
                    AbSharedUtil.putString(LocationService.this,
                            AbAppConfig.LONGITUDE,
                            mLocation.getLongitude() + "");
                    AbSharedUtil.putString(LocationService.this,
                            AbAppConfig.ALTITUDE, altitude + "");
                }
            } catch (Exception e) {
            }
        }
    }

    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    public void startTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                AbLogUtil.e(TAG, "TIMERRRRRRR");
                String engineerId = AbSharedUtil.getString(LocationService.this, AbAppConfig.USERNAME);
                String times = System.currentTimeMillis() + "";
                if (!TextUtils.isEmpty(engineerId)) {
                    String longitude = AbSharedUtil.getString(LocationService.this, AbAppConfig.LONGITUDE);
                    String latitude = AbSharedUtil.getString(LocationService.this, AbAppConfig.LATITUDE);
                    String altitude = AbSharedUtil.getString(LocationService.this, AbAppConfig.ALTITUDE);
                    String sign = SecurityUtil.md5(longitude + SecurityUtil.APP_SIGTURE + latitude);
                    if (!TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(latitude)) {
                        doUploadGPS(engineerId, longitude, latitude, altitude, sign, times);
                    }
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, AbAppConfig.TIMER_DURATION);
    }

    /**
     * 上传gps数据
     *
     * @param engineerId
     * @param longitude
     * @param latitude
     * @param altitude
     * @param sign
     * @param times
     */
    private void doUploadGPS(String engineerId, String longitude, String latitude, String altitude, String sign, String times) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("engineerId", engineerId);
        params.put("longitude", longitude);
        params.put("latitude", latitude);
        params.put("altitude", altitude);
        params.put("sign", sign);
        params.put("times", times);
        OkHttpClientManager.postAsyn(AbAppConfig.UPLOAD_GPS_URL, new OkHttpClientManager.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                AbLogUtil.i(TAG, "upload erro!");
            }

            @Override
            public void onResponse(String response) {
                AbLogUtil.e("TAG", "result--->" + response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.has("status")) {
                        int status = obj.getInt("status");
                        if (status == 0) {
                            AbLogUtil.i(TAG, "upload success!");
                            return;
                        }
                    }
                    AbLogUtil.i(TAG, "upload erro!");
                } catch (Exception e) {
                    AbLogUtil.i(TAG, "upload erro!");
                }
            }
        }, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AbLogUtil.i(TAG, "定位服务已关闭");
        if (mLocClient != null) {
            // 退出时销毁定位
            mLocClient.stop();
        }
        stopTimer();
        mLocClient = null;
    }
}
