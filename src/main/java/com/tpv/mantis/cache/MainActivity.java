package com.tpv.mantis.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.tpv.mantis.cache.disklrucache.DiskLruCache;
import com.tpv.mantis.cache.location.LocationService;
import com.tpv.mantis.cache.vo.TpvWifiInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "CACHE_MainActivity";

    private TextView mInfoView;
    private final static String CACHE_DIR = "wifiInfoCache";
    private String key = "";
    private TpvWifiInfo mTpvWifiInfo;
    private SimpleDateFormat mSDF;
    DiskLruCache mDiskLruCache = null;
    private ExecutorService mExecutor;
    private LocationService mLocationService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mLocationService = LocationService.newInstance(this);
        mInfoView = findViewById(R.id.info);


    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mDiskLruCache = DiskLruCache.open(getDiskCacheDir(this, CACHE_DIR), getAppVersion(this), 1, 1 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String action = getIntent().getAction();
        Log.d(TAG, "action: " + action);

        if (action.equals(WifiConnChangeReceiver.WIFI_CONNECTED_ACTION)) {
            mTpvWifiInfo = (TpvWifiInfo) getIntent().getSerializableExtra(WifiConnChangeReceiver.CONNECTED_WIFI_INFO);
            mTpvWifiInfo.setLongitude(mLocationService.longitude);
            mTpvWifiInfo.setLatitude(mLocationService.latitude);
            key = mTpvWifiInfo.getBssid().replace(":", "").toLowerCase();
        } else if (action.equals(LocationService.LOCATION_CHANGE_ACTION)) {
            key = getIntent().getStringExtra(LocationService.WIFI_BSSID);
            mTpvWifiInfo = readTpvWifiInfo(key);
            if (mTpvWifiInfo != null) {
                mTpvWifiInfo.setLongitude(getIntent().getDoubleExtra(LocationService.NEW_LONGITUDE, 0));
                mTpvWifiInfo.setLatitude(getIntent().getDoubleExtra(LocationService.NEW_LATITUDE, 0));
            }
        }

        if (mTpvWifiInfo != null) {
            Log.d(TAG, "mTpvWifiInfo: " + mTpvWifiInfo);


            mExecutor = Executors.newSingleThreadExecutor();
            mExecutor.execute(commitRunnable);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TpvWifiInfo tpvWifiInfo = readTpvWifiInfo(key);
            Log.d(TAG, "read tpvWifiInfo: " + tpvWifiInfo);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mDiskLruCache != null) {
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
        try {
            if (mDiskLruCache != null) {
                mDiskLruCache.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        //如果sd卡存在并且没有被移除
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private TpvWifiInfo readTpvWifiInfo(String key) {
        TpvWifiInfo tpvWifiInfo = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                InputStream is = snapshot.getInputStream(0);
                ObjectInputStream ois = new ObjectInputStream(is);
                tpvWifiInfo = (TpvWifiInfo) ois.readObject();
                Log.d(TAG, "read object success!");
            }
        } catch (IOException e) {
            Log.d(TAG, "read object failed!");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tpvWifiInfo;
    }

    private boolean cacheTpvWifiInfo(TpvWifiInfo tpvWifiInfo, OutputStream obj) {
        try {
            ObjectOutputStream objOS = new ObjectOutputStream(obj);
            objOS.writeObject(tpvWifiInfo);
            objOS.flush();
            objOS.close();
            Log.d(TAG, "write object success!");
            return true;
        } catch (IOException e) {
            Log.d(TAG, "write object failed");
            e.printStackTrace();
        }
        return false;
    }

    private Runnable commitRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);

                if (editor != null) {
                    OutputStream out = editor.newOutputStream(0);
                    if (cacheTpvWifiInfo(mTpvWifiInfo, out)) {
                        editor.commit();
                    } else {
                        editor.abort();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

}
