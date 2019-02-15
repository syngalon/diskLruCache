package com.tpv.mantis.cache;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import com.tpv.mantis.cache.location.LocationService;
import com.tpv.mantis.cache.vo.TpvWifiInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WifiConnChangeReceiver extends BroadcastReceiver {

    private final static String TAG = "WifiConnChangeReceiver";

    public final static String WIFI_CONNECTED_ACTION = "com.tpv.mantis.wifi_connected_acion";

    public final static String CONNECTED_WIFI_INFO = "connected_wifi_info";

    private WifiAdmin mWifiAdmin;

    private SimpleDateFormat mSDF;

    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "3G网络数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mWifiAdmin = new WifiAdmin(context);
        mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLING);
            Log.e(TAG, "wifiState:" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
            }
        }
        // 监听wifi的连接状态即是否连上了一个有效无线路由
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                sendWifiInfo(context, (NetworkInfo) parcelableExtra);

            }
        }
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager mConnMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取联网状态的NetworkInfo对象
            assert mConnMgr != null;
            NetworkInfo info = mConnMgr.getNetworkInfo(mConnMgr.getActiveNetwork());
            //intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI
                            || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Log.i(TAG, getConnectionType(info.getType()) + "连上");
                    }
                } else {
                    Log.i(TAG, getConnectionType(info.getType()) + "断开");
                }
            }
        }
    }

    private void  sendWifiInfo(Context context, NetworkInfo parcelableExtra) {
        // 获取联网状态的NetWorkInfo对象
        NetworkInfo networkInfo = parcelableExtra;
        //获取的State对象则代表着连接成功与否等状态
        NetworkInfo.State state = networkInfo.getState();
        //判断网络是否已经连接
        boolean isConnected = state == NetworkInfo.State.CONNECTED;
        Log.e(TAG, "isConnected:" + isConnected);
        if (isConnected) {
            Intent connIntent = new Intent();
            connIntent.setAction(WIFI_CONNECTED_ACTION);
            connIntent.setClass(context, MainActivity.class);
            TpvWifiInfo tpvWifiInfo = new TpvWifiInfo(mWifiAdmin.getBSSID(), mWifiAdmin.getSSID(), 0, 0, mSDF.format(new Date()), mWifiAdmin.getRssi());
            connIntent.putExtra(CONNECTED_WIFI_INFO, tpvWifiInfo);
            context.startActivity(connIntent);
        } else {

        }
    }

}