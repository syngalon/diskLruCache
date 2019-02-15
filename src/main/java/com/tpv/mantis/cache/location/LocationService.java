package com.tpv.mantis.cache.location;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.tpv.mantis.cache.MainActivity;
import com.tpv.mantis.cache.WifiAdmin;

/**
 * Created by mantis on 17-12-8.
 */

public class LocationService implements LocationListener {

    private final static String TAG = "TpvLocationService";

    public final static String LOCATION_CHANGE_ACTION = "com.tpv.mantis.location_change_acion";

    public final static String NEW_LONGITUDE = "new_longitude";
    public final static String NEW_LATITUDE = "new_latitude";
    public final static String WIFI_BSSID = "wifi_bssid";

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    //The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    private final static boolean forceNetwork = false;

    private static LocationService instance = null;


    private LocationManager locationManager;
    public Location location;
    public double longitude;
    public double latitude;
    public boolean isGPSEnabled;
    public boolean isNetworkEnabled;
    public boolean locationServiceAvailable;
    private Context mContext;
    private  WifiAdmin mWifiAdmin;


    /**
     * Singleton implementation
     * @return
     */
    public static LocationService newInstance(Context context)     {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    private LocationService(Context context)     {
        mContext = context;
        initLocationService(context);
        Log.d(TAG, "LocationService created");
    }



    /**
     * Sets up location service after permissions is granted
     */
    @TargetApi(23)
    private void initLocationService(Context context) {


        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try   {
            this.longitude = 0.0;
            this.latitude = 0.0;
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (forceNetwork) isGPSEnabled = false;

            if (!isNetworkEnabled && !isGPSEnabled)    {
                // cannot get location
                this.locationServiceAvailable = false;
            }
            //else
            {
                this.locationServiceAvailable = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null)   {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        updateCoordinates(location);
                    }
                } else if (isGPSEnabled)  {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)  {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        updateCoordinates(location);
                    }
                }
            }
        } catch (Exception ex)  {
            Log.d(TAG, "Error creating location service: " + ex.getMessage() );

        }
    }

    private void updateCoordinates(Location location) {
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        Log.d(TAG, "longitude: " + this.longitude + ", latitude: " + this.latitude);
        Intent locIntent = new Intent(LOCATION_CHANGE_ACTION);
        locIntent.putExtra(NEW_LONGITUDE, this.longitude);
        locIntent.putExtra(NEW_LATITUDE, this.latitude);
        locIntent.putExtra(WIFI_BSSID, mWifiAdmin.getBSSID());
        locIntent.setClass(mContext, MainActivity.class);
        mContext.startActivity(locIntent);
    }


    @Override
    public void onLocationChanged(Location location) {
        updateCoordinates(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, "onStatusChanged: " + s + ", i=" + i);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "onProviderEnabled: " + s);

    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "onProviderDisabled: " + s);
    }
}
