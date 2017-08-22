package com.jascaffe.jaslocation.utils;

/**
 * 常量类
 */
public class AbAppConfig {
    /**
     * app 定位刷新时间
     */
    public static final int LOCATION_TIME = 1000;
    public static final long TIMER_DURATION = 1000 * 3;
    public static final long TIMER_DURATION2 = 1000;
    /**
     * 默认 SharePreferences文件名.
     */
    public static String SHARED_PATH = "jaslocation_share";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ALTITUDE = "ALTITUDE";
    public static final String USERNAME = "USERNAME";
    public static final String NAME = "NAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String SERVICE_URL = "http://taotao.s1.natapp.cc";

    //登录
    public static final String LOGIN_URL = SERVICE_URL + "/gps/engineer/interface/valcode.action";
    //上传gps数据
    public static final String UPLOAD_GPS_URL = SERVICE_URL + "/gps/locations/upload.action";
}
