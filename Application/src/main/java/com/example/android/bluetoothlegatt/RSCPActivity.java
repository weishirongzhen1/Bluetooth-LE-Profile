package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.bluetoothlegatt.pack.BluetoothRscp;
import com.example.android.bluetoothlegatt.pack.RscpService;

public class RSCPActivity extends Activity {

    private static final String TAG = RSCPActivity.class.getSimpleName();
    private RscpService mRscpService;
    private boolean mConnected = false;
    private String mDeviceAddress;

    private TextView mConnectState;
    private TextView mMotion;
    private TextView mSpeed;
    private TextView mCadence;
    private TextView mStrideLength;
    private TextView mTotalDistance;
    private TextView mSLMS;
    private TextView mTDMS;
    private TextView mWORS;
    private TextView mCS;
    private TextView mMSLS;
    private TextView mSensorLocation;
    private Button mBtnReadFeature;
    private Button mBtnReadSensorLocation;
    private Button mEnableNotify;

    private EditText mCumulativeValue;
    private Button mSetCumulativeValueBtn;
    private Button mStartSensorCalibrationBtn;
    private Button mUpdateSensorLocationBtn;
    private Button mRequestSupportedSensorLocationBtn;

    private ProgressBar mFeatureProgressBar;
    private ProgressBar mLocationProgressBar;

    private boolean mNotifyFlag = true;




    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRscpService = ((RscpService.LocalBinder) service).getService();
            if (!mRscpService.initialize()) {
                Log.i(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            mRscpService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRscpService = null;
        }
    };

    BroadcastReceiver mRSCPUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (RscpService.ACTION_RSC_CONNECTED.equals(action)) {
                mConnected = true;
                mConnectState.setText("Connected.");

                invalidateOptionsMenu();
            } else if (RscpService.ACTION_RSC_DISCONNECTED.equals(action)) {
                mConnected = false;
                mConnectState.setText("Disconnected.");
                clearUI();
                invalidateOptionsMenu();
            } else if (RscpService.ACTION_RSC_MEASUREMENT_DATA_AVAILABLE.equals(action)) {

                showData(intent.getStringExtra(RscpService.RSC_WALKING_OR_RUNNING_STATE),
                        intent.getIntExtra(RscpService.RSC_SPEED_DATA, -1),
                        intent.getIntExtra(RscpService.RSC_CADENCE_DATA, -1),
                        intent.getIntExtra(RscpService.RSC_STRIDE_LENGTH_DATA, -1),
                        intent.getIntExtra(RscpService.RSC_TOTAL_DISTANCE_DATA, -1));

            } else if (RscpService.ACTION_RSC_FEATURE_DATA_AVAILABLE.equals(action)) {
                mSLMS.setText(String.valueOf(intent.getBooleanExtra(RscpService.RSC_FEATURE_STRIDE_LENGTH_SUPPORTED, false)));
                mTDMS.setText(String.valueOf(intent.getBooleanExtra(RscpService.RSC_FEATURE_TOTAL_DISTANCE_SUPPORTED, false)));
                mWORS.setText(String.valueOf(intent.getBooleanExtra(RscpService.RSC_FEATURE_WALKING_OR_RUNNING_STATUS, false)));
                mCS.setText(String.valueOf(intent.getBooleanExtra(RscpService.RSC_FEATURE_CALIBRATION_SUPPORTED, false)));
                mMSLS.setText(String.valueOf(intent.getBooleanExtra(RscpService.RSC_FEATURE_MULTIPLE_SENSOR_SUPPORTED, false)));
                mFeatureProgressBar.setVisibility(View.INVISIBLE);
            } else if (RscpService.ACTION_RSC_SENSOR_LOCATION_DATA_AVAILABLE.equals(action)) {
                mSensorLocation.setText(intent.getStringExtra(RscpService.RSC_SENSOR_LOCATION_DATA));
                mLocationProgressBar.setVisibility(View.INVISIBLE);

            } else if (RscpService.ACTION_RSC_CUMULATIVE_VALUE_SET.equals(action)) {
                mCumulativeValue.setText("success");

            } else if (RscpService.ACTION_RSC_START_SENSOR_CALIBRATION.equals(action)) {

            } else if (RscpService.ACTION_RSC_UPDATE_SENSOR_LOCATION.equals(action)) {

            } else if (RscpService.ACTION_RSC_REQUEST_SUPPORTED_SENSOR_LOCATION.equals(action)) {

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rscp);
        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS);

        mConnectState = (TextView) findViewById(R.id.connected_state);
        mMotion = (TextView) findViewById(R.id.motion);
        mSpeed = (TextView) findViewById(R.id.speed);
        mCadence = (TextView) findViewById(R.id.cadence);
        mStrideLength = (TextView) findViewById(R.id.stride_length);
        mTotalDistance = (TextView) findViewById(R.id.total_distance);

        mSLMS = (TextView) findViewById(R.id.stride_length_supported);
        mTDMS = (TextView) findViewById(R.id.total_distance_supported);
        mWORS = (TextView) findViewById(R.id.walking_or_running_status);
        mCS = (TextView) findViewById(R.id.calibration_supported);
        mMSLS = (TextView) findViewById(R.id.multi_sensor_supported);

        mSensorLocation = (TextView) findViewById(R.id.sensor_location);

        mEnableNotify = (Button) findViewById(R.id.enable_notify);

        mBtnReadFeature = (Button) findViewById(R.id.btn_read_feature);
        mBtnReadSensorLocation = (Button) findViewById(R.id.btn_read_sensor_location);

        mCumulativeValue = (EditText) findViewById(R.id.cumulative_value);
        mSetCumulativeValueBtn = (Button) findViewById(R.id.btn_set_cumulative_value);
        mStartSensorCalibrationBtn = (Button) findViewById(R.id.btn_start_sensor_calibration);
        mUpdateSensorLocationBtn = (Button) findViewById(R.id.btn_update_sensor_location);
        mRequestSupportedSensorLocationBtn = (Button) findViewById(R.id.btn_request_supported_sensor_location);

        mFeatureProgressBar = (ProgressBar) findViewById(R.id.progressbar_feature);
        mLocationProgressBar = (ProgressBar) findViewById(R.id.progressbar_location);
        mFeatureProgressBar.setIndeterminate(true);
        mLocationProgressBar.setIndeterminate(true);

        mFeatureProgressBar.setVisibility(View.INVISIBLE);
        mLocationProgressBar.setVisibility(View.INVISIBLE);

        mSetCumulativeValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRscpService.setCumulativeValue(Integer.parseInt(mCumulativeValue.getText().toString()));
            }
        });

        mBtnReadFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BluetoothGattService rscpService = mRscpService.getBluetoothGatt().getService(BluetoothRscp.RSC_SERVICE);
//                if (rscpService != null) {
//                    mFeatureProgressBar.setVisibility(View.VISIBLE);
//
//                    BluetoothGattCharacteristic rscFeatureCharac = rscpService.getCharacteristic(BluetoothRscp.RSC_FEATURE_CHARAC);
//                    mRscpService.readCharacteristic(rscFeatureCharac);
//                }

            }
        });

        mBtnReadSensorLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BluetoothGattService rscpService = mRscpService.getBluetoothGatt().getService(BluetoothRscp.RSC_SERVICE);
//                if (rscpService != null) {
//                    mLocationProgressBar.setVisibility(View.VISIBLE);
//                    BluetoothGattCharacteristic rscSensorLocationCharac = rscpService.getCharacteristic(BluetoothRscp.RSC_SENSOR_LOCATION_CHARAC);
//                    mRscpService.readCharacteristic(rscSensorLocationCharac);
//                }
            }
        });

//        mStartSensorCalibrationBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mRscpService.setIndicaion();
//            }
//        });

        mEnableNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNotifyFlag == false) {
                    mRscpService.setCharacteristicNotification(mNotifyFlag);
                    clearUI();
                    mNotifyFlag = true;
                } else {
                    mRscpService.setCharacteristicNotification(mNotifyFlag);
                    mNotifyFlag = false;
                }
            }
        });

        Intent rscpServiceIntent = new Intent(RSCPActivity.this, RscpService.class);
        bindService(rscpServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mRSCPUpdateReceiver, initializeIntentFilter());
        if (mRscpService != null) {
            mRscpService.connect(mDeviceAddress);
//            mRscpService.setCharacteristicNotification(BluetoothRscp.RSC_MEASUREMENT_CHARAC, true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRscpService.close();
        unbindService(mServiceConnection);
        unregisterReceiver(mRSCPUpdateReceiver);
    }

    private void showData(String motion, int speed, int cadence, int stride_length, int total_distance) {
        mMotion.setText(motion);
        mSpeed.setText(String.valueOf(speed));
        mCadence.setText(String.valueOf(cadence));
        mStrideLength.setText(String.valueOf(stride_length));
        mTotalDistance.setText(String.valueOf(total_distance));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rsc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_get_location) {
            if (mConnected == true) {
                mSensorLocation.setText(mRscpService.getSensorLocation());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter initializeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RscpService.ACTION_RSC_CONNECTED);
        intentFilter.addAction(RscpService.ACTION_RSC_DISCONNECTED);
        intentFilter.addAction(RscpService.ACTION_RSC_MEASUREMENT_DATA_AVAILABLE);
        intentFilter.addAction(RscpService.ACTION_RSC_FEATURE_DATA_AVAILABLE);
        intentFilter.addAction(RscpService.ACTION_RSC_SENSOR_LOCATION_DATA_AVAILABLE);
        intentFilter.addAction(RscpService.ACTION_RSC_CUMULATIVE_VALUE_SET);
        intentFilter.addAction(RscpService.ACTION_RSC_START_SENSOR_CALIBRATION);
        intentFilter.addAction(RscpService.ACTION_RSC_UPDATE_SENSOR_LOCATION);
        intentFilter.addAction(RscpService.ACTION_RSC_REQUEST_SUPPORTED_SENSOR_LOCATION);

        return intentFilter;
    }

    public void clearUI() {

        mMotion.setText("null");
        mSpeed.setText("null");
        mCadence.setText("null");
        mStrideLength.setText("null");
        mTotalDistance.setText("null");
        mSLMS.setText("null");
        mTDMS.setText("null");
        mWORS.setText("null");
        mCS.setText("null");
        mMSLS.setText("null");
        mSensorLocation.setText("null");

    }

}