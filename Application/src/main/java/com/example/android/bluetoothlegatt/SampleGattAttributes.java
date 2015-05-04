/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String BODY_SENSOR_LOCATION = "00002a38-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String RSC_MEASUREMENT_CHARAC = "00002a53-0000-1000-8000-00805f9b34fb";
    public static String RSC_FEATURE_CHARAC = "00002a54-0000-1000-8000-00805f9b34fb";
    public static String RSC_SENSOR_LOCATION_CHARAC = "00002a5d-0000-1000-8000-00805f9b34fb";
    public static String RSC_CONTROL_POINT_CHARAC = "00002a55-0000-1000-8000-00805f9b34fb";

//    private static final UUID RSCP_SERVICE = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
//    private static final UUID RSC_MEASUREMENT_CHARAC = UUID.fromString("00002A53-0000-1000-8000-00805f9b34fb");
//    private static final UUID RSC_FEATURE_CHARAC = UUID.fromString("00002A54-0000-1000-8000-00805f9b34fb");
//    private static final UUID RSC_SENSOR_LOCATION_CHARAC = UUID.fromString("00002A5D-0000-1000-8000-00805f9b34fb");
//    private static final UUID RSC_CONTROL_POINT_CHARAC = UUID.fromString("00002A55-0000-1000-8000-00805f9b34fb");

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed And Cadence Service");





        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(BODY_SENSOR_LOCATION, "Body Sensor Location");
        attributes.put(RSC_MEASUREMENT_CHARAC, "RSC Measurement");
        attributes.put(RSC_FEATURE_CHARAC, "RSC Feature");
        attributes.put(RSC_CONTROL_POINT_CHARAC, "RSC Control point");
        attributes.put(RSC_SENSOR_LOCATION_CHARAC, "RSC Sensor location");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a38-0000-1000-8000-00805f9b34fb" , "Body Sensor Location");
        attributes.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart control point");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
