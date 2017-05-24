package com.uberv.joystick.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

public abstract class Utils {

    public static boolean isBluetoothEnabled(BluetoothAdapter adapter) {
        if (adapter == null || !adapter.isEnabled()) {
            return false;
        } else {
            return true;
        }
    }
}
