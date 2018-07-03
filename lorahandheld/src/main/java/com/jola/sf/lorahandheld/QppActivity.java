package com.jola.sf.lorahandheld;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jola.sf.lorahandheld.lib.QppApi;
import com.jola.sf.lorahandheld.lib.iQppCallback;
import com.jola.sf.lorahandheld.util.HexBytesUtils;


public class QppActivity extends Activity {
    protected static final String TAG = QppActivity.class.getSimpleName();
    private BluetoothManager mBluetoothManager = null;
    private static BluetoothAdapter mBluetoothAdapter = null;
    public static BluetoothGatt mBluetoothGatt = null;

    public static final String EXTRAS_DEVICE_NAME = "deviceName";
    public static final String EXTRAS_DEVICE_ADDRESS = "deviceAddress";

    private boolean dataRecvFlag = false;
    private String deviceName;
    private String deviceAddress;

    /**
     * connection state
     */
    private boolean mConnected = false;
    /**
     * scan all Service ?
     */
    private boolean isInitialize = false;
    private static final int MAX_DATA_SIZE = 40;
    /// public
    private TextView textDeviceName;
    private TextView textDeviceAddress;
    private TextView textConnectionStatus;

    /// qpp start
    protected static String uuidQppService = "0000fee9-0000-1000-8000-00805f9b34fb";
    protected static String uuidQppCharWrite = "d44bc439-abfd-45a2-b575-925416129600";

    protected boolean qppConfirmFlag;

    /// receive data
    private TextView textQppNotify;
    private TextView textQppDataRate;

    private long qppSumDataReceived = 0;   /// summary of data received.
    private long qppRecvDataTime = 0;
    /// send
    private EditText editSend;
    private Button btnQppTextSend;

    /// repeat start
    private CheckBox checkboxRepeat;
    private boolean qppSendDataState = false;

    private TextView labelRepeatCounter;
    private TextView textRepeatCounter;

    private long qppRepeatCounter = 0;
    /// repeat end

    private byte qppDataSend[] = {0x00, '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 0x10, 0x11, 0x12};


    private Handler handlersend = new Handler();
    final Runnable runnableSend = new Runnable() {
        private void QppSendNextData() {
            //qppDataSend[0]++;

            byte qppDataSend1[] = editSend.getText().toString().getBytes();

            if (!QppApi.qppSendData(mBluetoothGatt, qppDataSend1)) {
                Log.e(TAG, "send data failed");
                return;
            }

            qppRepeatCounter++;
            setRepeatCounter(" " + qppRepeatCounter);
        }

        public void run() {
            QppSendNextData();
        }
    };

    private void showToastShort(String tipMsg){
        Toast.makeText(this,tipMsg,Toast.LENGTH_SHORT).show();
    }

    final Handler handlerQppDataRate = new Handler();
    final Runnable runnableQppDataRate = new Runnable() {
        public void run() {
            qppRecvDataTime++;
            textQppDataRate.setText(" " + qppSumDataReceived / qppRecvDataTime + " Bps");

            dataRecvFlag = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qpp);
        if (getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initView();
        initIntent();

        if (!initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }

//        设置监听，接受连接的蓝牙设备所传输回来的数据
        QppApi.setCallback(new iQppCallback() {

            @Override
            public void onQppReceiveData(BluetoothGatt mBluetoothGatt, String qppUUIDForNotifyChar, byte[] qppData) {
                if (!dataRecvFlag) {
                    handlerQppDataRate.postDelayed(runnableQppDataRate, 1000);
                    dataRecvFlag = true;
                }
                qppSumDataReceived = qppSumDataReceived + qppData.length;
                //setQppNotify((byte)qppData[0]+" "+(byte)qppData[1]);
//                显示连接设备所传送回来的数据
                setQppNotify(new String(qppData));
            }
        });

        initListener();


    }



    private void initListener() {
        /**
         * start to send qpp package OR RESET...
         */
        btnQppTextSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mConnected || !isInitialize) {
                    Toast.makeText(QppActivity.this, "Please connect device first and ensure your device support Qpp service!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (checkboxRepeat.isChecked()) {
                    if (!qppSendDataState) {
                        qppRepeatCounter = 0;
                        qppSendDataState = true;
                        btnQppTextSend.setText("Stop");
                        handlersend.post(runnableSend);
                    } else {
                        qppSendDataState = false;
                        btnQppTextSend.setText("Send");
                    }
                } else {
                    handlersend.post(runnableSend);
                }
            }

            private byte[] packageDataSend() {
                int data_sent_lenth = editSend.getText().length();
                String strInput;

                strInput = editSend.getText().toString();
                if ((data_sent_lenth % 2) == 1) {
                    strInput = "0" + strInput;
                }
                return HexBytesUtils.hexStrToByteArr(strInput);
            }
        });

        checkboxRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    labelRepeatCounter.setVisibility(View.VISIBLE);
                    textRepeatCounter.setVisibility(View.VISIBLE);
                } else {
                    labelRepeatCounter.setVisibility(View.INVISIBLE);
                    textRepeatCounter.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initIntent() {
//        deviceName = getIntent().getExtras().getString(EXTRAS_DEVICE_NAME);
//        deviceAddress = getIntent().getExtras().getString(EXTRAS_DEVICE_ADDRESS);

        deviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        textDeviceName.setText(deviceName);
        textDeviceAddress.setText(deviceAddress);
    }

    private void initView() {
        textDeviceName = (TextView) findViewById(R.id.text_device_name);
        textDeviceAddress = (TextView) findViewById(R.id.text_device_address);
        textConnectionStatus = (TextView) findViewById(R.id.text_connection_state);
        editSend = (EditText) findViewById(R.id.edit_send);
        editSend.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DATA_SIZE)});
        btnQppTextSend = (Button) findViewById(R.id.btn_qpp_text_send);
        checkboxRepeat = (CheckBox) findViewById(R.id.cb_repeat);
        labelRepeatCounter = (TextView) findViewById(R.id.label_repeat_counter);
        textRepeatCounter = (TextView) findViewById(R.id.text_repeat_counter);
        textQppNotify = (TextView) findViewById(R.id.text_qpp_notify);
        textQppDataRate = (TextView) findViewById(R.id.text_qpp_data_rate);
    }


    /**
     * 获得初始化蓝牙管理对象
     */
    private boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }



    private void clearHandler(Handler handler, Runnable runner) {
        if (handler != null) {
            handler.removeCallbacks(runner);
            handler = null;
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange : " + status + "  newState : " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();
                mConnected = true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                mConnected = false;
                clearHandler(handlerQppDataRate, runnableQppDataRate);
                clearHandler(handlersend, runnableSend);
                dataRecvFlag = false;
                if (qppSendDataState) {
                    setBtnSendState("Send");
                    qppSendDataState = false;
                }
                close();
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (QppApi.qppEnable(mBluetoothGatt, uuidQppService, uuidQppCharWrite)) {
                isInitialize = true;
                setConnectState(R.string.qpp_support);
            } else {
                isInitialize = false;
                setConnectState(R.string.qpp_not_support);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            QppApi.updateValueForNotification(gatt, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //super.onDescriptorWrite(gatt, descriptor, status);
            Log.w(TAG, "onDescriptorWrite");
            QppApi.setQppNextNotify(gatt, true);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS && qppSendDataState) {
            /*This is a workaround,20140819,xiesc: it paused with unknown reason on android 4.4.3
			 */
//                if (handlersend != null && runnableSend != null)
                if (handlersend != null)
                    handlersend.postDelayed(runnableSend, 60);
                //handlersend.post(runnableSend);
            } else {
                Log.e(TAG, "Send failed!!!!");
            }
        }
    };

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w("Qn Dbg", "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        //setting the autoConnect parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

        Log.d(TAG, "Trying to create a new connection. Gatt: " + mBluetoothGatt);
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("Qn Dbg", "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private void setConnectState(final int stat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textConnectionStatus.setText(stat);
            }
        });
    }

    private void setQppNotify(final String errStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textQppNotify.setText(errStr);
            }
        });
    }

    private void setRepeatCounter(final String errStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textRepeatCounter.setText(errStr);
            }
        });
    }

    private void setBtnSendState(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnQppTextSend.setText(str);
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.qpp, menu);

        if (mConnected) {
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
                connect(deviceAddress);
                return true;
            case R.id.menu_disconnect:
                disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mConnected) {
            textConnectionStatus.setText(R.string.qpp_not_support);
            invalidateOptionsMenu();
            connect(deviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearHandler(handlerQppDataRate, runnableQppDataRate);
        clearHandler(handlersend, runnableSend);
        close();
    }
}
