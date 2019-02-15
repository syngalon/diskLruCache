package com.tpv.mantis.cache.vo;

import java.io.Serializable;

/**
 * Created by mantis on 17-12-6.
 */

public class TpvWifiInfo implements Serializable {
    private String bssid;
    private String ssid;
    private double longitude;
    private double latitude;
    private String lastConnTime;
    private int rssi;

    public TpvWifiInfo(String bssid, String ssid, double longitude, double latitude, String lastConnTime, int rssi) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.longitude = longitude;
        this.latitude = latitude;
        this.lastConnTime = lastConnTime;
        this.rssi = rssi;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLastConnTime() {
        return lastConnTime;
    }

    public void setLastConnTime(String lastConnTime) {
        this.lastConnTime = lastConnTime;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "TpvWifiInfo{" +
                "bssid='" + bssid + '\'' +
                ", ssid='" + ssid + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", lastConnTime=" + lastConnTime +
                ", rssi=" + rssi +
                '}';
    }
}
