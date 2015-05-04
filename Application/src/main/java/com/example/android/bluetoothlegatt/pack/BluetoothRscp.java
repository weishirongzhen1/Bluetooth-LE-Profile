package com.example.android.bluetoothlegatt.pack;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.example.android.bluetoothlegatt.SampleGattAttributes;

import java.util.List;
import java.util.UUID;

/**
 * This class provide the public APIs to control the Bluetooth RSCP(Running Speed and Cadence Profile).
 * <p/>
 * <p>BluetoothRscp is a wrap object based on {@link android.bluetooth.BluetoothGatt}
 */
public final class BluetoothRscp implements BluetoothProfile {

    private static final String TAG = BluetoothRscp.class.getSimpleName();
    private static final boolean DBG = true;
    private static final boolean VDBG = false;

    public static final UUID RSC_SERVICE = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
    public static final UUID RSC_MEASUREMENT_CHARAC = UUID.fromString("00002a53-0000-1000-8000-00805f9b34fb");
    public static final UUID RSC_FEATURE_CHARAC = UUID.fromString("00002a54-0000-1000-8000-00805f9b34fb");
    public static final UUID RSC_SENSOR_LOCATION_CHARAC = UUID.fromString("00002a5d-0000-1000-8000-00805f9b34fb");
    public static final UUID RSC_CONTROL_POINT_CHARAC = UUID.fromString("00002a55-0000-1000-8000-00805f9b34fb");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final int RSC_SENSOR_CONTROL_POINT_OP_CODE = 0x10;
    private static final int INSTANTANEOUS_STRIDE_LENGTH_PRESENT_BITMASK = 0x01;
    private static final int TOTAL_DISTANCE_PRESENT_BITMASK = 0x01 << 1;
    private static final int WALKING_OR_RUNNING_STATUS_BITMASK = 0x01 << 2;
    private static final int INSTANTANEOUS_STRIDE_LENGTH_MEASUREMENT_SUPPORTED_BITMASK = 0x0001;
    private static final int TOTAL_DISTANCE_MEASUREMENT_SUPPORTED_BITMASK = 0x0001 << 1;
    private static final int WALKING_OR_RUNNING_STATUS_SUPPORTED_BITMASK = 0x0001 << 2;
    private static final int CALIBRATION_PROCEDURE_SUPPORTED = 0x0001 << 3;
    private static final int MULTIPLE_SENSOR_LOCATIONS_SUPPORTED = 0x0001 << 4;


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final String WALKING = "Walking";
    private static final String RUNNING = "Running";
    private static final String STANDING_STILL = "Standing still";

    private Context mContext;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothRscpCallback mBluetoothRscpCallback;
    private int mConnectionState = STATE_DISCONNECTED;

    private int mRSCFeature;
    private int mRSCSensorLocation;

    private static String[] sLocations = {"other", "Top of shoe", "In shoe", "Hip", "Front Wheel", "Left Crank",
            "Right Crank", "Left Pedal", "Right Pedal", "Front Hub", "Rear Dropout", "Chainstay",
            "Rear Wheel", "Rear Hub", "Chest"};

    /**
     * @param context
     * @param callback
     */
    public BluetoothRscp(Context context, BluetoothRscpCallback callback) {
        mContext = context;
        mBluetoothRscpCallback = callback;
    }

    public String getSensorLocation() {
        if (mRSCSensorLocation < sLocations.length) {
            return sLocations[mRSCSensorLocation];
        }
        return null;
    }

    private BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());



            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
            if (mBluetoothRscpCallback != null) {
                mBluetoothRscpCallback.onConnectionStateChange(status, newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setCharacteristicNotification(true);
                setCharacteristicIndication(true);

                // sth wrong here
                return;
            }
            disconnect();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                UUID uuid = characteristic.getUuid();

                if (uuid.equals(RSC_SENSOR_LOCATION_CHARAC)) {
                    mRSCSensorLocation = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    if (mBluetoothRscpCallback != null) {
                        mBluetoothRscpCallback.onSensorLocationChange(getSensorLocation());
                    }
                } else if (uuid.equals(RSC_FEATURE_CHARAC)) {
                    mRSCFeature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    parseRSCFeatureCharac(mRSCFeature);
                }

            }
        }

        private void parseRSCFeatureCharac(int feature) {

            boolean instantaneousStrideLengthMeasurementSupported;
            boolean totalDistanceMeasurementSupported;
            boolean walkingOrRunningStatusSupported;
            boolean calibrationProcedureSupported;
            boolean multipleSensorLocationSupported;

            instantaneousStrideLengthMeasurementSupported = ((feature & INSTANTANEOUS_STRIDE_LENGTH_MEASUREMENT_SUPPORTED_BITMASK) != 0);
            totalDistanceMeasurementSupported = ((feature & TOTAL_DISTANCE_MEASUREMENT_SUPPORTED_BITMASK) != 0);
            walkingOrRunningStatusSupported = ((feature & WALKING_OR_RUNNING_STATUS_SUPPORTED_BITMASK) != 0);
            calibrationProcedureSupported = ((feature & CALIBRATION_PROCEDURE_SUPPORTED) != 0);
            multipleSensorLocationSupported = ((feature & MULTIPLE_SENSOR_LOCATIONS_SUPPORTED) != 0);

            if (mBluetoothRscpCallback != null) {
                mBluetoothRscpCallback.onRSCFeatureChange(instantaneousStrideLengthMeasurementSupported,
                        totalDistanceMeasurementSupported, walkingOrRunningStatusSupported,
                        calibrationProcedureSupported, multipleSensorLocationSupported);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            if (uuid.equals(RSC_CONTROL_POINT_CHARAC)) {
                Log.i("mylog", "onCharacteristicWrite call");
                setCharacteristicNotification(true);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(RSC_MEASUREMENT_CHARAC)) {
                parseRSCMeasurementCharac(characteristic);
            } else if (characteristic.getUuid().equals(RSC_CONTROL_POINT_CHARAC)) {
                Log.i("mylog", "onCharacteristicChanged call");
                int requestOpCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
                int responseValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,1);
                int responseParameter = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,2);
                if (requestOpCode == RSC_SENSOR_CONTROL_POINT_OP_CODE) {
                    mBluetoothRscpCallback.onCumulativeValueSet();
                }

            }
        }


        private void parseRSCMeasurementCharac(BluetoothGattCharacteristic characteristic) {
            int offset = 0;
            int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
            int instantaneousSpeed;
            int instantaneousCadence;
            int instantaneousStrideLength = 0;
            int totalDistance = 0;
            boolean isInstantaneousStrideLengthPresent;
            boolean isTotalDistancePresent;
            String motion;

            isInstantaneousStrideLengthPresent = ((flags & INSTANTANEOUS_STRIDE_LENGTH_PRESENT_BITMASK) != 0);
            isTotalDistancePresent = ((flags & TOTAL_DISTANCE_PRESENT_BITMASK) != 0);

            if ((flags & WALKING_OR_RUNNING_STATUS_BITMASK) != 0) {
                motion = RUNNING;
            } else
                motion = WALKING;

            offset += 1;
            instantaneousSpeed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
            offset += 2;
            instantaneousCadence = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
            offset += 1;

            if (isInstantaneousStrideLengthPresent) {
                instantaneousStrideLength = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                offset += 2;
            }

            if (isTotalDistancePresent) {
                totalDistance = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
            }

            if (instantaneousSpeed == 0 && instantaneousCadence == 0) {
                motion = STANDING_STILL;
            }
            if (mBluetoothRscpCallback != null) {
                mBluetoothRscpCallback.onRSCMeasurementCharacChange(instantaneousSpeed, instantaneousCadence, instantaneousStrideLength,
                        totalDistance, isInstantaneousStrideLengthPresent, isTotalDistancePresent, motion);
            }

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            if (uuid.equals(CLIENT_CHARACTERISTIC_CONFIG)) {
                // todo
                //setCharacteristicNotification(true);
            }
        }

    };

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.readCharacteristic(characteristic);
    }
    public boolean connect(BluetoothDevice device, boolean autoConnect) {
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        if (mBluetoothDevice != null && device.equals(mBluetoothDevice) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else
                return false;
        }
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothGatt = device.connectGatt(mContext, autoConnect, mGattCallBack);
        mBluetoothDevice = device;
        return true;

    }

    public BluetoothGatt getBluetoothGatt () {
        if (mBluetoothGatt != null) {
            return mBluetoothGatt;
        } else
            return null;

    }


    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(boolean enabled) {
        BluetoothGattCharacteristic charac;
        if (mBluetoothGatt == null) {
            return;
        }
        BluetoothGattService rscService = mBluetoothGatt.getService(RSC_SERVICE);
        if (rscService == null) {
            return;
        }
        charac = rscService.getCharacteristic(RSC_MEASUREMENT_CHARAC);

        if (!mBluetoothGatt.setCharacteristicNotification(charac, enabled)) {
            return;
        }

        BluetoothGattDescriptor descriptor = charac.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public void setCharacteristicIndication(boolean enabled) {
        BluetoothGattCharacteristic charac;
        if (mBluetoothGatt == null) {
            return;
        }
        BluetoothGattService rscService = mBluetoothGatt.getService(RSC_SERVICE);
        if (rscService == null) {
            return;
        }
        charac = rscService.getCharacteristic(RSC_CONTROL_POINT_CHARAC);

        if (!mBluetoothGatt.setCharacteristicNotification(charac, enabled)) {
            return;
        }
        BluetoothGattDescriptor descriptor = charac.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        }
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public boolean close() {
        if (mBluetoothGatt == null) {
            return false;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mBluetoothDevice = null;
        return true;
    }

    public void setCumulativeValue(int cumulativeValue) {
        if (mBluetoothGatt == null) {
            return;
        }

        byte [] bytes = intToBytes(cumulativeValue);

        BluetoothGattService rscService = mBluetoothGatt.getService(RSC_SERVICE);
        if (rscService == null) {
            return;
        }
        BluetoothGattCharacteristic rscControlPointCharac = rscService.getCharacteristic(RSC_CONTROL_POINT_CHARAC);
        if (rscControlPointCharac == null) {
            return;
        }

        byte [] value = {0x01,bytes[0],bytes[1],bytes[2],bytes[3]};
        rscControlPointCharac.setValue(value);
        mBluetoothGatt.writeCharacteristic(rscControlPointCharac);
        Log.i("mylog", "set finish " + cumulativeValue);
    }

    public void setIndication() {
        setCharacteristicIndication(true);
    }

    public static byte[] intToBytes( int value )
    {
        byte[] src = new byte[4];
        src[3] =  (byte) ((value>>24) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }

    @Override
    public List<BluetoothDevice> getConnectedDevices() {
        return null;
    }

    @Override
    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        return null;
    }

    @Override
    public int getConnectionState(BluetoothDevice device) {
        return 0;
    }
}