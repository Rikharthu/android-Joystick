package com.uberv.joystick.bluetooth;

import java.util.HashMap;

public abstract class GattAttributesContract {

    private static HashMap<String, String> attributes = new HashMap();
    public static final String HM_10 = "0000ffe1-0000-1000-8000-00805f9b34fb";

    static {
        // Services
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "HM-10 Service");
        // Characteristics
        attributes.put(HM_10, "HM-10 Module");
    }

}
