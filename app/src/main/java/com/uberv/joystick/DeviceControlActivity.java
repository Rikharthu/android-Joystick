package com.uberv.joystick;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.uberv.joystick.bluetooth.BluetoothLeService;
import com.uberv.joystick.bluetooth.GattAttributesContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.uberv.joystick.bluetooth.BluetoothLeService.ACTION_DATA_AVAILABLE;
import static com.uberv.joystick.bluetooth.BluetoothLeService.ACTION_DATA_WRITE;
import static com.uberv.joystick.bluetooth.BluetoothLeService.ACTION_GATT_CONNECTED;
import static com.uberv.joystick.bluetooth.BluetoothLeService.ACTION_GATT_DISCONNECTED;
import static com.uberv.joystick.bluetooth.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED;
import static com.uberv.joystick.bluetooth.GattAttributesContract.HM_10;

public class DeviceControlActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String LOG_TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String KEY_NAME = "KEY_NAME";
    public static final String KEY_UUID = "KEY_UUID";

    @BindView(R.id.device_address_tv)
    TextView mDeviceAddressTv;
    @BindView(R.id.device_connection_state_tv)
    TextView mConnectionStateTv;
    @BindView(R.id.hm10_uuid_tv)
    TextView mHM10UUIDTv;
    @BindView(R.id.send_btn)
    Button mSendBtn;
    @BindView(R.id.data_et)
    EditText mDataEt;

    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mIsConnected;
    // desirec characteristic
    private BluetoothGattCharacteristic mHM10Module;
    private BluetoothLeService mBluetoothLeService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(LOG_TAG, "Unable to initialize bluetooth");
                Toast.makeText(mBluetoothLeService, "Unable to initialize bluetooth", Toast.LENGTH_SHORT).show();

                finish();
            }
            // Connect to the device upon successful start-up initialization
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Setup UI
        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDeviceAddressTv.setText(mDeviceAddress);
        mConnectionStateTv.setText("Disconnected");

        mSendBtn.setOnClickListener(this);

        Toast.makeText(this, String.format("Connecting to %s at %s", mDeviceName, mDeviceAddress), Toast.LENGTH_SHORT).show();

        // Start/connect to our connection service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(LOG_TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.control_device, menu);

        if (mIsConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        return intentFilter;
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionStateTv.setText(resourceId);
            }
        });
    }

    private void setupDataTransmission(BluetoothGattCharacteristic characteristic) {
        mHM10Module = characteristic;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHM10UUIDTv.setText(mHM10Module.getUuid().toString());
                mBluetoothLeService.setCharacteristicNotification(mHM10Module, true);
                mSendBtn.setEnabled(true);
            }
        });
    }

    /**
     * Handles various events fired by the service<hr/>
     * <ul>
     * <li>{@link com.uberv.joystick.bluetooth.BluetoothLeService#ACTION_GATT_CONNECTED} - connected to a GATT server</li>
     * <li>{@link com.uberv.joystick.bluetooth.BluetoothLeService#ACTION_GATT_DISCONNECTED} - disconnected from a GATT server</li>
     * <li>{@link com.uberv.joystick.bluetooth.BluetoothLeService#ACTION_GATT_SERVICES_DISCOVERED} - discovered GATT services on the server</li>
     * <li>{@link com.uberv.joystick.bluetooth.BluetoothLeService#ACTION_DATA_AVAILABLE} - received data from the device. This can be
     * a result of a read or notification operations</li>
     * </ul>
     */
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED == action) {
                mIsConnected = true;
                invalidateOptionsMenu();
                updateConnectionState(R.string.connected);
            } else if (ACTION_GATT_DISCONNECTED == action) {
                mIsConnected = false;
                invalidateOptionsMenu();
                updateConnectionState(R.string.disconnected);
                // TODO
                // clearUI();
            } else if (ACTION_GATT_SERVICES_DISCOVERED == action) {
                Log.d(LOG_TAG, "services discovered");
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (ACTION_DATA_AVAILABLE == action) {
                Log.d(LOG_TAG, "data available");
                // read characteristic value
                onDataAvailable();
            } else if (ACTION_DATA_WRITE == action) {
                Log.d(LOG_TAG, "write data");
            }
        }
    };

    private void onDataAvailable() {
        final byte[] rxBytes = mHM10Module.getValue();
        String message = new String(rxBytes);
        Log.d(LOG_TAG, "received: " + message);
    }

    private void displayGattServices(List<BluetoothGattService> services) {
        if (services == null) return;

        UUID UUID_HM_10 = UUID.fromString(HM_10);

        String uuid;
        String unknownServiceString = "Unknown service";
        String unknownCharacteristicString = "Unknown characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();

        mGattCharacteristics = new ArrayList<>();

        // Loop through available GATT Services;
        for (BluetoothGattService service : services) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = service.getUuid().toString();
            currentServiceData.put(KEY_NAME, GattAttributesContract.lookup(uuid, unknownServiceString));
            currentServiceData.put(KEY_UUID, uuid);

            // get characteristics for the current service
            List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
            // Loop through characteristics on a given service
            for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                characteristics.add(characteristic);

                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = characteristic.getUuid().toString();
                currentCharaData.put(
                        KEY_NAME, GattAttributesContract.lookup(uuid, unknownCharacteristicString));
                currentCharaData.put(KEY_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                //Check if it is "HM_10"
                if (uuid.equals(HM_10)) {
                    Log.d(LOG_TAG, "HM-10 Module found!");
                    setupDataTransmission(service.getCharacteristic(UUID_HM_10));
                }
            }
            mGattCharacteristics.add(characteristics);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    @Override
    public void onClick(View v) {
        if (mHM10Module != null) {
            String text = mDataEt.getText().toString();
            mHM10Module.setValue(text.getBytes());
            mBluetoothLeService.writeCharacteristic(mHM10Module);
        }
    }
}