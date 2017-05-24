package com.uberv.joystick.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Wrapper for bluetooth devices
 */
public class BleDevice {
    private BluetoothDevice mDevice;
    private int mRSSI;

    public BleDevice(BluetoothDevice device) {
        mDevice = device;
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public String getName() {
        return mDevice.getName();
    }

    public int getRSSI() {
        return mRSSI;
    }

    public void setRSSI(int RSSI) {
        mRSSI = RSSI;
    }
}
