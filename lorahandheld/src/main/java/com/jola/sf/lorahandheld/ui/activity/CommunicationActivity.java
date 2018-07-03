package com.jola.sf.lorahandheld.ui.activity;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jola.sf.lorahandheld.R;
import com.jola.sf.lorahandheld.entity.CommondSendResponseEntity;
import com.jola.sf.lorahandheld.entity.MainNodeEntity;
import com.jola.sf.lorahandheld.entity.Protocol188Entity;
import com.jola.sf.lorahandheld.lib.QppApi;
import com.jola.sf.lorahandheld.lib.iQppCallback;
import com.jola.sf.lorahandheld.util.HexBytesUtils;
import com.jola.sf.lorahandheld.widget.PopCommandGridView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CommunicationActivity extends Activity implements OnClickListener {

    private static final int Request_Bar_Code = 100;
    private static final int Request_Bar_Code_First = 101;
    private static final int Request_Bar_Code_Second = 102;
    private static final int Request_Bar_Code_Third = 103;
    public static final int Result_Bar_Code = 201;
    protected static final String TAG = CommunicationActivity.class.getSimpleName();
    private BluetoothManager mBluetoothManager = null;
    private static BluetoothAdapter mBluetoothAdapter = null;
    public static BluetoothGatt mBluetoothGatt = null;

    public static final String EXTRAS_DEVICE_NAME = "deviceName";
    public static final String EXTRAS_DEVICE_ADDRESS = "deviceAddress";

    //    private String deviceName;
    private String deviceAddress;

    /**
     * connection state
     */
    private boolean mConnected = false;
    /**
     * scan all Service ?
     */
    private boolean isInitialize = false;
    //    private static final int MAX_DATA_SIZE = 40;
    /// public
    private TextView textDeviceName;
    private TextView textDeviceAddress;
    private TextView textConnectionStatus;

    /// qpp start
    protected static String uuidQppService = "0000fee9-0000-1000-8000-00805f9b34fb";
    protected static String uuidQppCharWrite = "d44bc439-abfd-45a2-b575-925416129600";

    /// send
    private EditText editSend;
    private Button btnQppTextSend;

    private boolean qppSendDataState = false;

    private List<CommondSendResponseEntity> mDataList = new ArrayList<>();

    private List<String> mAddrHistList = new ArrayList<>();
    private List<String> mSecondList = new ArrayList<>();
    private List<String> mThirdList = new ArrayList<>();

    private int mEquipTypePos = 0;

    private ListView mCommandsListView;
    private CommandsListViewAdapter mCommandListAdapter;

    private byte[] mSendDataByte;
    private int frameNo = 1;

    private PopCommandGridView popCommandView;

    private AlertDialog alertDialog;

    private String[] dataSourceArr;


    private TextView mMainNodeTitleTv;
    private LinearLayout mMainNodeContentLL;

    private EditText mMainNodeAddrEt;
    private EditText mMainNodeSetAddrEt;

    private EditText mMainChannelRouteEt;
    private EditText mMainNetRouteET;
    private EditText mMainRxEt;
    private EditText mMainTxEt;

    private EditText mMainSoftwareEt;
    private EditText mMainHardwareEt;

    /**
     * 在命令界面选择的命令
     */
    String commandStr;
    private String preReceiveStr = "";


    private Handler handlersend = new Handler();
    final Runnable runnableSend = new Runnable() {
        private void QppSendNextData() {

            String hexStr = editSend.getText().toString();
            if (hexStr.length() == 0) {
                Toast.makeText(CommunicationActivity.this, getString(R.string.tip_no_msg), Toast.LENGTH_SHORT).show();
                return;
            }
//            mSendDataByte = HexBytesUtils.hexStr2Bytes(hexStr);
            mSendDataByte = HexBytesUtils.hexStrToByteArr(hexStr);
            if (!QppApi.qppSendData(mBluetoothGatt, mSendDataByte)) {
                Toast.makeText(CommunicationActivity.this, "当前命令：" + getString(R.string.send_fail), Toast.LENGTH_SHORT).show();
//                Log.e(TAG, "send data failed");
                return;
            } else {
                Toast.makeText(CommunicationActivity.this, "当前命令：" + getString(R.string.send_success), Toast.LENGTH_SHORT).show();

                Log.e("jola", "send:" + hexStr);

            }
            addSendDataItem(hexStr);
            frameNo++;
        }

        public void run() {
            QppSendNextData();
        }
    };


    final Runnable setAddRunnable = new Runnable() {
        private void setAddress() {

        }

        public void run() {
            setAddress();
        }
    };

    final Runnable sendCommandRunnable = new Runnable() {
        private void QppSendNextData() {
            if (!mConnected || !isInitialize) {
                Toast.makeText(CommunicationActivity.this, R.string.tip_conn_status_err, Toast.LENGTH_LONG).show();
                return;
            }
            if (null == mSendDataByte || mSendDataByte.length == 0) {
                Toast.makeText(CommunicationActivity.this, getString(R.string.tip_no_msg), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!QppApi.qppSendData(mBluetoothGatt, mSendDataByte)) {
                Toast.makeText(CommunicationActivity.this, commandStr + ":" + getString(R.string.send_fail), Toast.LENGTH_SHORT).show();
//                Log.e(TAG, "send data failed");
                return;
            } else {
                Log.e("jola", "send:" + HexBytesUtils.byteArrToHexStr(mSendDataByte));
                if (null != popCommandView) {
                    popCommandView.dismiss();
                }
                Toast.makeText(CommunicationActivity.this, commandStr + ":" + getString(R.string.send_success), Toast.LENGTH_SHORT).show();
            }
            String hexStr = HexBytesUtils.byteArrToHexStr(mSendDataByte);
            addSendDataItem(hexStr);
            setConmmandStrEt(hexStr);
            frameNo++;
        }

        public void run() {
            QppSendNextData();
        }
    };


    private void setConmmandStrEt(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editSend.setText(str);
            }
        });
    }


    private void addSendDataItem(String hexStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hexStr.length() / 2; i++) {
            sb.append(hexStr.substring(2 * i, 2 * i + 2));
            sb.append(" ");
        }
        CommondSendResponseEntity entity = new CommondSendResponseEntity();
        entity.setCommandStr(sb.toString());
        mDataList.add(entity);
        if (null != mCommandListAdapter) {
            mCommandListAdapter.notifyDataSetChanged();
            mCommandsListView.setSelection(mDataList.size() - 1);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initView();
        initIntent();
        if (!initialize()) {
            Toast.makeText(CommunicationActivity.this, "初始化蓝牙模块失败，无法使用！，", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
//        设置监听，接受连接的蓝牙设备所传输回来的数据
        setQppCallback();

        initListener();
    }

    /**
     * 80 毫秒延迟
     */
    private int offsetTime = 80;

    private Long lastReceiverTime;
    /**
     * 负责延迟处理的handler
     */
    private Handler handlerReceive = new Handler();
    Runnable receiveRunnable = new Runnable() {
        @Override
        public void run() {

//            Log.e("jola", "deal    time:" + System.currentTimeMillis());
//            Log.e("jola", "last    time:" + lastReceiverTime);
            long curOffsetTime = System.currentTimeMillis() - lastReceiverTime;
//            Log.e("jola", "curOffsetTime:" + curOffsetTime);
            if (curOffsetTime < offsetTime) {
//                Log.e("jola", "System.currentTimeMillis() - lastReceiverTime < 80 不处理");
                return;
            }
//            处理接受到的消息
            dealwithCallback(preReceiveStr);
//            Log.e("jola", "System.currentTimeMillis() - lastReceiverTime >= 80 要处理");
        }
    };

    private void setQppCallback() {
        QppApi.setCallback(new iQppCallback() {
            @Override
            public void onQppReceiveData(BluetoothGatt mBluetoothGatt, String qppUUIDForNotifyChar, byte[] qppData) {
                lastReceiverTime = System.currentTimeMillis();
//                Log.e("jola", "receive time:" + lastReceiverTime);
//                延迟处理机制：接收到消息后，延迟80毫秒，若80毫秒内无信息接收到、则立即处理；若80毫秒内再次接收到信息，再次延迟80毫秒再处理
                String hexStr = HexBytesUtils.byteArrToHexStr(qppData);
//                Log.e("jola", "receive hex :" + hexStr);
                preReceiveStr = preReceiveStr + hexStr;
                handlerReceive.postDelayed(receiveRunnable, offsetTime);

            }
        });

    }

    private void dealwithCallback(String hexStr) {
        if (null == hexStr || hexStr.length() == 0) {
            return;
        }
        Log.e("Jola", "deal receive:" + hexStr);
        byte[] qppData = HexBytesUtils.hexStrToByteArr(hexStr);
        setQppNotify(hexStr);


//                干线节点查询响应：				  23 ：读地址应答
//                68 13 03 01 FF FF FF FF FF FF     01 00 00 80     23      01 00 00 80 53 24 7B
//                68 13 23 01 FF FF FF FF FF FF     08 00 00 80     23      08 00 00 80 C5 FE 23
//                0-2 2-4                                   14*2 = 28 - 30
//                Log.e("Jola", " qppData[14] ="+ qppData[14]);
//                Log.e("Jola", " 0x23="+  0x23);
        if (qppData.length > 15 && qppData[14] == 0x23) {
            final String readAddr = hexStr.substring(26, 28) + hexStr.substring(24, 26) + hexStr.substring(22, 24) + hexStr.substring(20, 22);
//            Log.e("Jola", " readAddr=" + readAddr);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMainNodeAddrEt.setText(readAddr);
                    mMainNodeSetAddrEt.setText(readAddr);
                }
            });

//                    读通信参数应答： 0x33
//                    68 13 03 01 	57 04	 FF FF FF FF	 01 00 00 80 	33 0A 57 04 6B 07 98 6B
//                                                                                      Rx       Tx
//                    Log.e("Jola", " qppData[14] ="+ qppData[14]);
//                    Log.e("Jola", " 0x33="+  0x33);
        } else if (qppData.length > 20 && qppData[14] == 0x33) {

            final String readAddr = hexStr.substring(26, 28) + hexStr.substring(24, 26) + hexStr.substring(22, 24) + hexStr.substring(20, 22);

//                    信道号 qppData[15] 0A - > 10
            final String channelNo = (qppData[15] & 0xff) + "";
//                    网络号 57 04 -> 0457 > 1111
            final String netNo = Integer.parseInt(hexStr.substring(10, 12) + hexStr.substring(8, 10), 16) + "";
//                    Rx  qppData[18] 6B - > 107
            final String rx = (qppData[18] & 0xff) + "";

//                    Tx  qppData[21] 6B - > 107
            final String tx = (qppData[21] & 0xff) + "";

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMainNodeAddrEt.setText(readAddr);
                    mMainNodeSetAddrEt.setText(readAddr);
                    mMainChannelRouteEt.setText(channelNo);
                    mMainNetRouteET.setText(netNo);
                    mMainRxEt.setText(rx);
                    mMainTxEt.setText(tx);
                }
            });
//                    查询版本信息的响应
//                    68 1A 03 01 57 04 FF FF FF FF 01 00 00 80  51  18 05 05 15 01     56 32 2E 30 30   6E 11 1E 6F
//                                                      软件版本呢：18 05 05 15 01
//                                                      硬件版本 56 32 2E 30 30   V 2 . 0 0
        } else if (qppData.length > 0x1A && qppData[14] == 0x51) {
            final String softwareV = hexStr.substring(30, 32) + "-" + hexStr.substring(32, 34) + "-" + hexStr.substring(34, 36) + "-" + hexStr.substring(36, 38) + "-" + hexStr.substring(38, 40);
//                    String hardwareV = char(Integer.parseInt(hexStr.substring(36,38)))+"";
            final String hardwareV =
                    (char) (Integer.parseInt(hexStr.substring(40, 42), 16)) + ""
                            + (char) (Integer.parseInt(hexStr.substring(42, 44), 16)) + ""
                            + (char) (Integer.parseInt(hexStr.substring(44, 46), 16)) + ""
                            + (char) (Integer.parseInt(hexStr.substring(46, 48), 16)) + ""
                            + (char) (Integer.parseInt(hexStr.substring(48, 50), 16)) + "";
//                    Log.e("Jola", " hardwareV="+  hardwareV);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMainSoftwareEt.setText(softwareV);
                    mMainHardwareEt.setText(hardwareV);
                }
            });
//                } else if (qppData.length > 13 && qppData[10] == (byte) 0x81 && qppData[11] == (byte) 0x0A) {
//                子节点查询表地址 响应    68 10    69 00 01 07 17 20 00     83 03   0A 81    01 32 16 54 52
        } else if (qppData.length > 13 && qppData[11] == (byte) 0x0A && qppData[12] == (byte) 0x81) {
//                Log.e("Jola", " qppData[11] =:" + qppData[11] + " ; (byte) 0x0A = " + (byte) 0x0A);
//                Log.e("Jola", " qppData[12] =:" + qppData[12] + " ;(byte) 0x81 = " + (byte) 0x81);
//            Log.e("Jola", " address:" + hexStr.substring(16, 18) + hexStr.substring(14, 16) + hexStr.substring(12, 14) + hexStr.substring(10, 12) + hexStr.substring(8, 10) + hexStr.substring(6, 8) + hexStr.substring(4, 6));
//                如果是读表地址的响应帧，则需要多帧进行处理：形成历史帧地址记录
//                    mAddrHistList.add(HexBytesUtils.byteArrToHexStr(qppData).substring(4, 18));
            addItemStrToList(mAddrHistList, hexStr.substring(16, 18) + hexStr.substring(14, 16) + hexStr.substring(12, 14) + hexStr.substring(10, 12) + hexStr.substring(8, 10) + hexStr.substring(6, 8) + hexStr.substring(4, 6));
        }
//        isNeedTwice = false;
        preReceiveStr = "";

    }


    public void addItemStrToList(List<String> list, String addItem) {
        boolean canAdd = true;
        for (int i = 0; i < list.size(); i++) {
            if (addItem.equals(list.get(i))) {
                canAdd = false;
                break;
            }
        }
        if (canAdd) {
            list.add(addItem);
        }
    }

    /**
     * 按字节返序
     *
     * @param originalStr 12345678
     * @return 78 56 34 12
     */
    private String reverseStringByByte(String originalStr) {
        if (null == originalStr || originalStr.length() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int len = originalStr.length() / 2;
        for (int i = len; i > 0; i--) {
//            len = 4
//            6-8 4-6 2-4 0-2
            sb.append(originalStr.substring(i * 2 - 2, i * 2));
        }
        return sb.toString();
    }

    private String formatFrameNo(int frameNo) {
        String hexString = Integer.toHexString(frameNo);
        if (hexString.length() < 2) {
            return "0" + hexString;
        } else if (hexString.length() == 2) {
            return hexString;
        } else {
            return hexString.substring(0, 2);
        }
    }

    private void initIntent() {
        deviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        textDeviceName.setText(getIntent().getStringExtra(EXTRAS_DEVICE_NAME));
        textDeviceAddress.setText(deviceAddress);
    }

    private void initView() {
        textDeviceName = (TextView) findViewById(R.id.text_device_name);
        textDeviceAddress = (TextView) findViewById(R.id.text_device_address);
        textConnectionStatus = (TextView) findViewById(R.id.text_connection_state);
        editSend = (EditText) findViewById(R.id.edit_send);
//        editSend.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DATA_SIZE)});
        btnQppTextSend = (Button) findViewById(R.id.btn_qpp_text_send);
        mCommandsListView = findViewById(R.id.command_send_res_lv);
        mCommandListAdapter = new CommandsListViewAdapter();
        mCommandsListView.setAdapter(mCommandListAdapter);

        mMainNodeTitleTv = findViewById(R.id.table_main_node_tv);
        mMainNodeTitleTv.setOnClickListener(this);
        mMainNodeContentLL = findViewById(R.id.main_node_content_ll);
        mMainNodeContentLL.setVisibility(View.GONE);

        mMainNodeAddrEt = findViewById(R.id.main_node_addr_ev);
        mMainNodeSetAddrEt = findViewById(R.id.main_node_addr_set_ev);

        mMainChannelRouteEt = findViewById(R.id.channel_route_ev);
        mMainNetRouteET = findViewById(R.id.net_route_ev);
        mMainRxEt = findViewById(R.id.rx_ev);
        mMainTxEt = findViewById(R.id.tx_ev);


        mMainSoftwareEt = findViewById(R.id.software_version_ev);
        mMainHardwareEt = findViewById(R.id.hardware_version_ev);

        findViewById(R.id.btn_query_main_addr).setOnClickListener(this);
        findViewById(R.id.btn_set_main_addr).setOnClickListener(this);
        findViewById(R.id.btn_query_channel).setOnClickListener(this);
        findViewById(R.id.btn_query_version).setOnClickListener(this);
        findViewById(R.id.btn_set_channel).setOnClickListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result_code = "";
        String address = "";
        if (resultCode == Result_Bar_Code) {
//            99    2117    0802063 00004558    4
//            00    2117            00004558

            result_code = data.getStringExtra("result_code");
//            if (result_code.contains("no.")){
//                int index = result_code.lastIndexOf("no.");
//                address = result_code.substring(index+3, index + 15);
//            }

            if (result_code.length() >= 22) {
                address = "00" + result_code.substring(2, 6) + result_code.substring(13, 21);
            }

        }

        if (address.length() == 0) {
//            showToastShort("没有表地址信息（no. 后12位是表地址）！");
            showToastShort("没有表地址信息（条码长度不是表条码格式）！");
            return;
        }

        if (requestCode == Request_Bar_Code_First) {
            if (null != popCommandView) {
                popCommandView.setEtContentByIndex(address, 1);
            }
        } else if (requestCode == Request_Bar_Code_Second) {
            if (null != popCommandView) {
                popCommandView.setEtContentByIndex(address, 2);
            }
        }
        if (requestCode == Request_Bar_Code_Third) {
            if (null != popCommandView) {
                popCommandView.setEtContentByIndex(address, 3);
            }
        }
    }

    @Override
    public void onClick(View v) {
//        isNeedTwice = false;
        MainNodeEntity mainNodeEntity = new MainNodeEntity();
        String originalAddr = "";
        String setAddr = "";
        String hexStr = "";
        switch (v.getId()) {
            case R.id.table_main_node_tv:
                if (mMainNodeContentLL.getVisibility() == View.GONE) {
                    mMainNodeContentLL.setVisibility(View.VISIBLE);
                    mMainNodeTitleTv.setBackground(getResources().getDrawable(R.drawable.radius_border_shape));
                } else {
                    mMainNodeContentLL.setVisibility(View.GONE);
                    mMainNodeTitleTv.setBackground(getResources().getDrawable(R.drawable.radius_border_shape_gray));
                }
                break;
            case R.id.btn_query_main_addr:
//                680F0301FFFFFFFFFFFFFFFFFFFF22C569
//                isNeedTwice = true;
                String commandHexStr = "680F0301FFFFFFFFFFFFFFFFFFFF22C569";
                editSend.setText(commandHexStr);
//                mSendDataByte = HexBytesUtils.hexStr2Bytes(commandHexStr);
                mSendDataByte = HexBytesUtils.hexStrToByteArr(commandHexStr);
//                handlersend.post(runnableSend);
                handlersend.post(sendCommandRunnable);
                commandStr = "查询节点地址";
                break;
            case R.id.btn_set_main_addr:
//                设置干线节点 地址
//                68 13 03 01 FF FF 01 00 00 80 FF FF FF FF 20 03 00 00 90 C4 02
                originalAddr = mMainNodeAddrEt.getText().toString();
                setAddr = mMainNodeSetAddrEt.getText().toString();
                if (originalAddr.length() != 8 || setAddr.length() != 8) {
                    showToastShort("原地址和设置地址的地址长度必须为8位！");
                    return;
                }
                if (originalAddr.equals(setAddr)) {
                    showToastShort("设置地址不能和原地址相同！");
                    return;
                }
                mainNodeEntity.setMacHead("68");
                mainNodeEntity.setLen("13");
                mainNodeEntity.setControlCode("03");
                mainNodeEntity.setFrameNo(formatFrameNo(frameNo));
//                mainNodeEntity.setFrameNo("01");
                mainNodeEntity.setNetworkNo("FFFF");
                mainNodeEntity.setTargetAddr(reverseStringByByte(originalAddr));
                mainNodeEntity.setSourceAddr("FFFFFFFF");
                mainNodeEntity.setMacLoad("20" + reverseStringByByte(setAddr));

                hexStr = mainNodeEntity.rebuildMac2HexStr();

                mSendDataByte = HexBytesUtils.hexStrToByteArr(hexStr);
                handlersend.post(sendCommandRunnable);
//                handlersend.postDelayed(sendCommandRunnable, 300);
                editSend.setText(hexStr);
//                handlersend.post(sendCommandRunnable);
//                handlersend.postDelayed(sendCommandRunnable,500);
                commandStr = "设置节点地址";
                break;
            case R.id.btn_query_channel:
//                isNeedTwice = true;
                originalAddr = mMainNodeAddrEt.getText().toString();
                if (originalAddr.length() != 8) {
                    showToastShort("节点地址长度必须为8位！");
                    return;
                }
//                查询干线节点 信道号 网络号 Rx Tx 信息
//                68 0F 03 01 FF FF 01 00 00 80 FF FF FF FF 32 8C 0C
                mainNodeEntity.setMacHead("68");
                mainNodeEntity.setLen("0F");
                mainNodeEntity.setControlCode("03");
                mainNodeEntity.setFrameNo(formatFrameNo(frameNo));
//                mainNodeEntity.setFrameNo("01");
                mainNodeEntity.setNetworkNo("FFFF");
                mainNodeEntity.setTargetAddr(reverseStringByByte(originalAddr));
                mainNodeEntity.setSourceAddr("FFFFFFFF");
                mainNodeEntity.setMacLoad("32");

                hexStr = mainNodeEntity.rebuildMac2HexStr();
                editSend.setText(hexStr);

                mSendDataByte = HexBytesUtils.hexStrToByteArr(hexStr);
                handlersend.post(sendCommandRunnable);

                commandStr = "查询干线节点信息";
//                handlersend.post(runnableSend);
                break;
            case R.id.btn_query_version:
//                isNeedTwice = true;
                originalAddr = mMainNodeAddrEt.getText().toString();
                if (originalAddr.length() != 8) {
                    showToastShort("节点地址长度必须为8位！");
                    return;
                }
//                版本查询
//                68 0F 03 01 FF FF 01 00 00 80 FF FF FF FF 50 98 4C
                mainNodeEntity.setMacHead("68");
                mainNodeEntity.setLen("0F");
                mainNodeEntity.setControlCode("03");
                mainNodeEntity.setFrameNo(formatFrameNo(frameNo));
//                mainNodeEntity.setFrameNo("01");
                mainNodeEntity.setNetworkNo("FFFF");
                mainNodeEntity.setTargetAddr(reverseStringByByte(originalAddr));
                mainNodeEntity.setSourceAddr("FFFFFFFF");
                mainNodeEntity.setMacLoad("50");

                hexStr = mainNodeEntity.rebuildMac2HexStr();
                editSend.setText(hexStr);
//                handlersend.post(runnableSend);
                mSendDataByte = HexBytesUtils.hexStrToByteArr(hexStr);
                handlersend.post(sendCommandRunnable);
                commandStr = "查询干线节点版本";
                break;
            case R.id.btn_set_channel:
//                68 12 03 01 FF FF 01 00 00 80 FF FF FF FF 30      0B  58 04       4A 55
//                设置信道号
//                信道号 ：11 --> 0B
//                网络号 ：1112 --> 458--》 58 04
                originalAddr = mMainNodeAddrEt.getText().toString();
                String channelStr = mMainChannelRouteEt.getText().toString();
                String netRouteStr = mMainNetRouteET.getText().toString();
                if (originalAddr.length() != 8) {
                    showToastShort("节点地址长度必须为8位！");
                    return;
                }
                if (channelStr.length() == 0 || netRouteStr.length() == 0) {
                    showToastShort("信道号 、 网络号不能为空");
                    return;
                }
                String channelStrHex = Integer.toHexString(Integer.parseInt(channelStr));

                String netRouteStrHex = Integer.toHexString(Integer.parseInt(netRouteStr));

                mainNodeEntity.setMacHead("68");
                mainNodeEntity.setLen("12");
                mainNodeEntity.setControlCode("03");
//                mainNodeEntity.setFrameNo(formatFrameNo(frameNo));
                mainNodeEntity.setFrameNo("01");
                mainNodeEntity.setNetworkNo("FFFF");
                mainNodeEntity.setTargetAddr(reverseStringByByte(originalAddr));
                mainNodeEntity.setSourceAddr("FFFFFFFF");
                mainNodeEntity.setMacLoad("30" + prefix0ToLen(channelStrHex, 2) + reverseStringByByte(prefix0ToLen(netRouteStrHex, 4)));

                hexStr = mainNodeEntity.rebuildMac2HexStr();
                editSend.setText(hexStr);

                mSendDataByte = HexBytesUtils.hexStrToByteArr(hexStr);
                handlersend.post(sendCommandRunnable);


//                handlersend.post(runnableSend);
                break;
        }
    }

    /**
     * 补0操作
     */
    public String prefix0ToLen(String originalStr, int needLen) {
        int length = originalStr.length();
        if (length >= needLen) {
            return originalStr;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < needLen - length; i++) {
                sb.append("0");
            }
            sb.append(originalStr);
            return sb.toString();
        }
    }


    private void showToastShort(String tipMsg) {
        Toast.makeText(this, tipMsg, Toast.LENGTH_SHORT).show();
    }

    private void showToastLong(String tipMsg) {
        Toast.makeText(this, tipMsg, Toast.LENGTH_LONG).show();
    }


    private void initListener() {

        (findViewById(R.id.build_command_btn)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != popCommandView && popCommandView.isShowing()) {
                    popCommandView.dismiss();
                }
//                isNeedTwice = false;
                showPopCommandGridView();
            }
        });

        /**
         * start to send qpp package OR RESET...
         */
        btnQppTextSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mConnected || !isInitialize) {
                    Toast.makeText(CommunicationActivity.this, R.string.tip_conn_status_err, Toast.LENGTH_LONG).show();
                    return;
                }
//                isNeedTwice = false;
                handlersend.post(runnableSend);
            }
        });

        ((Spinner) findViewById(R.id.equip_type_sp)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEquipTypePos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((Spinner) findViewById(R.id.mode_controller_sp)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                setModeCommand(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ((Spinner) findViewById(R.id.rate_controller_sp)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                setRateCommand(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    /**
     * 设置抄表频率
     *
     * @param position
     */
    private void setRateCommand(int position) {
//        isNeedTwice = false;
        //                发送指令
        Protocol188Entity entity = new Protocol188Entity();
        switch (mEquipTypePos) {
//          水表
            case 0:
                entity.setEquipType("10");
                break;
//          气表
            case 1:
                entity.setEquipType("30");
                break;
            default:
                Toast.makeText(CommunicationActivity.this, "请在“仪表类型”中选择 水表 或 气表 再操作", Toast.LENGTH_SHORT).show();
                return;
        }
        entity.setAddress("AEAEAEAEAEAEAE");
        entity.setControlCode("04");
        entity.setDataLen("05");
        entity.setDataFlag("52E1");
//        entity.setFrameNo("01");
        entity.setFrameNo(formatFrameNo(frameNo));
        String dataStr = "";
//                数据域
        switch (position) {
            case 1:
//                dataStr = "005C901D";
                dataStr = "6013";
                break;
            case 2:
//                dataStr = "4095621D";
                dataStr = "4213";
                break;
            case 3:
//                dataStr = "C0AF681D";
                dataStr = "4613";
                break;
            case 4:
//                dataStr = "40CA6E1D";
                dataStr = "4A13";
                break;
            case 5:
//                dataStr = "C0E4741D";
                dataStr = "4E13";
                break;
            case 6:
//                dataStr = "40FF7A1D";
                dataStr = "5213";
                break;
            case 7:
//                dataStr = "C019811D";
                dataStr = "5613";
                break;
            case 8:
//                dataStr = "4034871D";
                dataStr = "5A13";
                break;
            case 9:
//                dataStr = "80418A1D";
                dataStr = "5C13";
                break;
            case 10:
//                dataStr = "409E9F1D";
                dataStr = "6A13";
                break;
            case 11:
//                dataStr = "C0B8A51D";
                dataStr = "6E13";
                break;
            case 12:
//                dataStr = "40D3AB1D";
                dataStr = "7213";
                break;
            case 13:
//                dataStr = "C0EDB11D";
                dataStr = "7613";
                break;
            case 14:
//                dataStr = "4008B81D";
                dataStr = "7A13";
                break;
            case 15:
//                dataStr = "C022BE1D";
                dataStr = "7E13";
                break;
            case 16:
//                dataStr = "20F45A1D";
                dataStr = "3D13";
                break;
            case 17:
//                dataStr = "0053531D";
                dataStr = "3813";
                break;
            case 18:
//                dataStr = "E0B14B1D";
                dataStr = "3313";
                break;
        }
        entity.setDataStr(dataStr);
        mSendDataByte = entity.rebuildHex2ByteArr();

        if (!mConnected || !isInitialize) {
            Toast.makeText(CommunicationActivity.this, R.string.tip_conn_status_err, Toast.LENGTH_LONG).show();
            return;
        }
        commandStr = "设置抄表频率";
        handlersend.post(sendCommandRunnable);
    }

    /**
     * 设置操控器模式
     *
     * @param position
     */
    private void setModeCommand(int position) {
//        isNeedTwice = false;
        //                发送指令
        Protocol188Entity entity = new Protocol188Entity();
        switch (mEquipTypePos) {
//                    水表
            case 0:
                entity.setEquipType("10");
                break;
//                        气表
            case 1:
                entity.setEquipType("30");
                break;
            default:
                Toast.makeText(CommunicationActivity.this, "请在“仪表类型”中选择 水表 或 气表 再操作", Toast.LENGTH_SHORT).show();
                return;
        }
        entity.setAddress("AEAEAEAEAEAEAE");
        entity.setControlCode("04");
        entity.setDataLen("04");
        entity.setDataFlag("51E1");
//        entity.setFrameNo("01");
        entity.setFrameNo(formatFrameNo(frameNo));
        String dataStr = "";
//                数据域
        switch (position) {
            case 1:
                dataStr = "00";
                break;
            case 2:
                dataStr = "01";
                break;
            case 3:
                dataStr = "02";
                break;
        }
        entity.setDataStr(dataStr);

        mSendDataByte = entity.rebuildHex2ByteArr();

        if (!mConnected || !isInitialize) {
            Toast.makeText(CommunicationActivity.this, R.string.tip_conn_status_err, Toast.LENGTH_LONG).show();
            return;
        }
        commandStr = "设置操控器模式";
        handlersend.post(sendCommandRunnable);
    }


    private void showPopCommandGridView() {
//        是否为干线节点
        boolean isMain = false;
        if (mEquipTypePos == 2) {
            isMain = true;
        }
        popCommandView = new PopCommandGridView(CommunicationActivity.this, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doClickPos(position);
            }
        }, new OnClickListener() {
            @Override
            public void onClick(View v) {
                doClickListen(v);
            }
        }, isMain);
        popCommandView.showAtLocation(CommunicationActivity.this.findViewById(R.id.command_ll_communication), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        dataSourceArr = popCommandView.getStringArray();
    }

    private void doClickListen(View v) {
        switch (v.getId()) {
            case R.id.first_scan_iv:
                startActivityForResult(new Intent(CommunicationActivity.this, SimpleScannerActivity.class), Request_Bar_Code_First);
                break;
            case R.id.second_scan_iv:
                startActivityForResult(new Intent(CommunicationActivity.this, SimpleScannerActivity.class), Request_Bar_Code_Second);
                break;
            case R.id.third_scan_iv:
                startActivityForResult(new Intent(CommunicationActivity.this, SimpleScannerActivity.class), Request_Bar_Code_Third);
                break;
            case R.id.first_pulldown_iv:
//                展示历史水表的对话框列表
//                if (null != popCommandView){
//                    commandStr = popCommandView.getTitleByIndex(1);
//                }
                showListDialog(mAddrHistList.toArray(new String[0]), 1);
                break;
            case R.id.second_pulldown_iv:
//                if (null != popCommandView){
//                    commandStr = popCommandView.getTitleByIndex(2);
//                }
                showListDialog(mSecondList.toArray(new String[0]), 2);
                break;
            case R.id.third_pulldown_iv:
//                if (null != popCommandView){
//                    commandStr = popCommandView.getTitleByIndex(3);
//                }
                showListDialog(mThirdList.toArray(new String[0]), 3);
                break;
            case R.id.close_popup_iv:
                if (null != popCommandView) {
                    popCommandView.dismiss();
                }
                break;
            case R.id.cancel_btn:
                if (null != popCommandView) {
                    popCommandView.hindeOrShowInputLl(true);
                }
                break;
            case R.id.confirm_btn:
                confirmBuildCommand(v);
                break;
        }
    }

    /**
     * 选择命令后，输入相关参数，构建命令帧，发送给操控器
     */
    private void confirmBuildCommand(View view) {
        if (!popCommandView.getIsAllSelected()) {
            Toast.makeText(this, "请填写或选择相关内容后，再确认！", Toast.LENGTH_SHORT).show();
            return;
        }
//        isNeedTwice = false;
        Protocol188Entity entity = new Protocol188Entity();
        switch (mEquipTypePos) {
            case 0:
                entity.setEquipType("10");
                break;
            case 1:
                entity.setEquipType("30");
                break;
            case 2:
//                 在此处理干线节点的命令

                return;
        }
        String dataStr = "";
        entity.setFrameNo(formatFrameNo(frameNo));
        if (popCommandView != null) {
            addItemStrToList(mAddrHistList, popCommandView.getEtContentByIndex(1));
        }
        switch (commandStr) {
            case "写表地址":
//                输入：旧地址、新地址
//                secondHandle(commandStr,"表旧地址","新地址");
//                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setAddress((popCommandView.getEtContentByIndex(1)));
                entity.setControlCode("15");
                entity.setDataLen("0A");
//                entity.setDataFlag("18A0");
                entity.setDataFlag("18A0");
                dataStr = reverseStringByByte(popCommandView.getEtContentByIndex(2));

                break;
            case "读计量数据":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress((popCommandView.getEtContentByIndex(1)));
                entity.setControlCode("01");
                entity.setDataLen("03");
                entity.setDataFlag("1F90");
                dataStr = "";
//                isNeedTwice = true;
                break;
            case "读历史累积量":
//                输入：表地址 、上1-18个月
//                secondHandle(commandStr,"表地址","历史月（1-18）");
                entity.setAddress((popCommandView.getEtContentByIndex(1)));
                entity.setControlCode("01");
                entity.setDataLen("03");
                entity.setDataFlag(reverseStringByByte(popCommandView.getEtContentByIndex(2)));
                dataStr = "";
//                isNeedTwice = true;
                break;
            case "读历史冻结量":
//                输入：表地址、上1-31日累计
//                secondHandle(commandStr,"表地址","历史日（1-31）");
                entity.setAddress((popCommandView.getEtContentByIndex(1)));
                entity.setControlCode("01");
                entity.setDataLen("03");
                entity.setDataFlag(reverseStringByByte(popCommandView.getEtContentByIndex(2)));
                dataStr = "";
//                isNeedTwice = true;
                break;
            case "写机电同步数据":
//                输入：表地址、XXXXXX.XX（当前累计流量）2C
//                secondHandle(commandStr,"表地址","当前累计流量(8位bcd)");
                entity.setAddress((popCommandView.getEtContentByIndex(1)));
                entity.setControlCode("16");
                entity.setDataLen("08");
                entity.setDataFlag("16A0");
//                机电同步数据 格式xxxxxx.xx
                String inputNum = popCommandView.getEtContentByIndex(2);
                String floatStr = Float.parseFloat(inputNum) + "";
                if (floatStr.length() > 9) {
                    showToastLong("当前累计流量格式xxxxxx.xx 6位整数，2位小数");
                }
                int indexDian = floatStr.lastIndexOf(".");
                String zhengshu = prefix0ToLen(floatStr.substring(0, indexDian), 6);
                String xiaoshu = prefix0ToLen(floatStr.substring(indexDian + 1, floatStr.length()), 2);
//                dataStr = reverseStringByByte(zhengshu + xiaoshu + "2C");
                dataStr = reverseStringByByte(zhengshu + xiaoshu) + "2C";
                break;
            case "校时":
//                输入：表地址、YYYYMMDDhhmmss
//                secondHandle(commandStr,"表地址","手机时间");
//                secondHandle(commandStr,"表地址");//直接将手机时间设置进去
                entity.setAddress((popCommandView.getEtContentByIndex(1)));
                entity.setControlCode("04");
                entity.setDataLen("0A");
                entity.setDataFlag("15A0");
//                YYYYMMDDhhmmss
                dataStr = getCurTimeFormat();
                break;
            case "开阀":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress((popCommandView.getEtContentByIndex(1)));
                entity.setControlCode("04");
                entity.setDataLen("04");
                entity.setDataFlag("17A0");
                dataStr = "55";
                break;
            case "关阀":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress((popCommandView.getEtContentByIndex(1)));
                entity.setControlCode("04");
                entity.setDataLen("04");
                entity.setDataFlag("17A0");
                dataStr = "99";
                break;
            case "出厂启用":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("04");
                entity.setDataLen("03");
                entity.setDataFlag("19A0");
                dataStr = "";
                break;
            case "设置厂内":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("04");
                entity.setDataLen("03");
                entity.setDataFlag("18A0");
                dataStr = "";
                break;
            case "读EEPROM":
//                输入：表地址、addr(2字节)、len（1字节）
//                secondHandle(commandStr"表地址", "ADDR地址(四位数字)", "LEN长度(<=64)");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("06");
                entity.setDataFlag("10A0");
                String address = popCommandView.getEtContentByIndex(2);
                if (null == address || address.length() != 4) {
                    showToastLong("错误：请输入四位数字的ADDR地址，再操作");
                    return;
                }
                String len = popCommandView.getEtContentByIndex(3);
                if (null == len) {
                    showToastLong("错误：LEN长度数值范围0-64，请重新输入后再操作");
                    return;
                }
//                dataStr = reverseStringByByte(address) + prefix0ToLen(Integer.parseInt(len,16)+"",2);
                dataStr = reverseStringByByte(address) + prefix0ToLen(Integer.toHexString(Integer.parseInt(len)), 2);
                break;
            case "读电池电压":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("03");
                entity.setDataFlag("11A0");
                dataStr = "";
                break;
            case "清表数据":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("03");
                entity.setDataFlag("13A0");
                dataStr = "";
                break;
            case "读软件版本号":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("03");
                entity.setDataFlag("1AA0");
                dataStr = "";
                break;
            case "读表运行数据":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("03");
                entity.setDataFlag("1BA0");
                dataStr = "";
//                isNeedTwice = true;
                break;
            case "读Boot版本号":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("03");
                entity.setDataFlag("1CA0");
                dataStr = "";
                break;
            case "设置无线信道":
//                输入：表地址、无线频率（14个频率）
//                secondHandle(commandStr,"表地址","设置无线信道");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("04");
                entity.setDataFlag("1DA0");
                dataStr = popCommandView.getEtContentByIndex(2);
                break;
            case "读硬件版本号":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("03");
                entity.setDataFlag("1EA0");
                dataStr = "";
//                isNeedTwice = true;
                break;
            case "设置内条码":
//                输入：表地址、内条码（16位数字）
//                secondHandle(commandStr,"表地址","内条码（16数字）");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("0B");
                entity.setDataFlag("1FA0");
                dataStr = popCommandView.getEtContentByIndex(2);
                break;
            case "读内条码":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("03");
                entity.setDataFlag("1FA0");
                dataStr = "0000";
                break;
            case "生产专用1":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("0A");
                entity.setDataFlag("20A0");
                dataStr = getCurTimeFormat();
//                isNeedTwice = true;
                break;
            case "生产专用2":
//                输入：旧表地址、新地址
//                secondHandle(commandStr,"表地址","新地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("0A");
                entity.setDataFlag("21A0");
                dataStr = reverseStringByByte(popCommandView.getEtContentByIndex(2));
//                isNeedTwice = true;
                break;
            case "软件复位":
//                输入：表地址
//                secondHandle(commandStr,"表地址");
                entity.setAddress(popCommandView.getEtContentByIndex(1));
                entity.setControlCode("33");
                entity.setDataLen("03");
                entity.setDataFlag("22A0");
                dataStr = "";
                break;
            default:
                Toast.makeText(this, commandStr + ":暂无相关命令操作", Toast.LENGTH_SHORT).show();
                return;
        }
        entity.setDataStr(dataStr);
        mSendDataByte = entity.rebuildHex2ByteArr();
        handlersend.post(sendCommandRunnable);
        hideSoftInputLayout(view);
    }

    private void hideSoftInputLayout(View view) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


    }

    private String getCurTimeFormat() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
    }


    public void showListDialog(final String[] selectArr, final int spannerIndex) {
        if (null == selectArr || selectArr.length == 0) {
            Toast.makeText(this, "无可选内容", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = commandStr;
        if (null != popCommandView) {
            title = "请选择" + popCommandView.getTitleByIndex(spannerIndex);
        }
        final AlertDialog.Builder listDialog =
                new AlertDialog.Builder(this);
        listDialog.setTitle(title);
        listDialog.setItems(selectArr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (null != popCommandView) {
                    Toast.makeText(CommunicationActivity.this, selectArr[which], Toast.LENGTH_SHORT).show();
                    switch (commandStr) {
                        case "读历史累积量":
                            if (spannerIndex == 1) {
                                popCommandView.setEtContentByIndex((selectArr[which]), spannerIndex);
                            } else {
                                popCommandView.setEtContentByIndex(getValueByMonth(selectArr[which]), spannerIndex);
                            }

                            break;
                        case "读历史冻结量":
                            if (spannerIndex == 1) {
                                popCommandView.setEtContentByIndex((selectArr[which]), spannerIndex);
                            } else {
                                popCommandView.setEtContentByIndex(getValueByDay(selectArr[which]), spannerIndex);
                            }
                            break;
                        case "设置无线信道":
                            if (spannerIndex == 1) {
                                popCommandView.setEtContentByIndex((selectArr[which]), spannerIndex);
                            } else {
                                popCommandView.setEtContentByIndex(getRateHexStrByPos(which), spannerIndex);
                            }

                            break;
                        default:
                            popCommandView.setEtContentByIndex(selectArr[which], spannerIndex);
                    }
                }
            }
        });
        listDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (null != alertDialog) {
                    alertDialog.dismiss();
                }
            }
        });
        alertDialog = listDialog.show();
    }


    /**
     * gridview 命令选择后响应
     *
     * @param position
     */
    private void doClickPos(int position) {
//        isNeedTwice = false;
        if (null == dataSourceArr || dataSourceArr.length == 0) {
            dataSourceArr = popCommandView.getStringArray();
        }
        //准备数据下拉框的数据源
        mSecondList.clear();
        mThirdList.clear();
//        根据点击的内容，结合文档解析，生成命令帧，发送给操控器
//        选择命令后处理情况：
//                1:可直接生成命令帧
//                2:需要输入相关内容才能生成命令帧
//                3:需要选择相关参数才能生成命令帧
        commandStr = dataSourceArr[position];
//        Toast.makeText(this, commandStr, Toast.LENGTH_SHORT).show();

//        如果选择的是干线节点，直接生成命令
        if (mEquipTypePos == 2) {
            String hexStr = "";
            switch (commandStr) {
                case "广播搜索地址":
                    hexStr = "680F0301FFFFFFFFFFFFFFFFFFFF22C569";
                    break;
                case "读通信参数":
                    hexStr = "680F0301FFFF50450500FFFFFFFF32175F";
                    break;
            }
            mSendDataByte = HexBytesUtils.hexStrToByteArr(hexStr);
            handlersend.post(sendCommandRunnable);
            return;
        }
        switch (commandStr) {
            case "读表地址":
                Protocol188Entity entity = new Protocol188Entity();
                switch (mEquipTypePos) {
                    case 0:
                        entity.setEquipType("10");
                        break;
                    case 1:
                        entity.setEquipType("30");
                        break;
                }
                entity.setAddress("AAAAAAAAAAAAAA");
                entity.setControlCode("03");
                entity.setDataLen("03");
                entity.setDataFlag("0A81");
                entity.setFrameNo(formatFrameNo(frameNo));
                entity.setDataStr("");
                mSendDataByte = entity.rebuildHex2ByteArr();
                handlersend.post(sendCommandRunnable);
                if (null != popCommandView) {
                    popCommandView.dismiss();
                }
                break;
            case "写表地址":
//                输入：旧地址、新地址
                secondHandle(commandStr, "表旧地址", "新地址");
                break;
            case "读计量数据":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "读历史累积量":
//                输入：表地址 、上1-18个月
                secondHandle(commandStr, "表地址", "历史月（1-18）");
                for (int i = 1; i < 19; i++) {
                    mSecondList.add(i + "月");
                }
                break;
            case "读历史冻结量":
//                输入：表地址、上1-31日累计
                secondHandle(commandStr, "表地址", "历史日（1-31）");
                for (int i = 1; i < 32; i++) {
                    mSecondList.add(i + "日");
                }
                break;
            case "写机电同步数据":
//                输入：表地址、XXXXXX.XX（当前累计流量）2C
                secondHandle(commandStr, "表地址", "当前累计流量(.xx)");
                break;
            case "校时":
//                输入：表地址、YYYYMMDDhhmmss
//                secondHandle(commandStr,"表地址","手机时间");
                secondHandle(commandStr, "表地址");//直接将手机时间设置进去
                break;
            case "开阀":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "关阀":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "出厂启用":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "设置厂内":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "读EEPROM":
//                输入：表地址、addr(2字节)、len（1字节）
                secondHandle(commandStr, "表地址", "ADDR地址(四位数字)", "LEN长度(<=64)");
                break;
            case "读电池电压":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "清表数据":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "读软件版本号":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "读表运行数据":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "读Boot版本号":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "设置无线信道":
//                输入：表地址、无线频率（14个频率）
                secondHandle(commandStr, "表地址", "设置无线信道");
//              配置secondList数据源
//                String[] stringArray = getResources().getStringArray(R.array.rate_wrieless_arr);
                mSecondList.addAll(Arrays.asList(getResources().getStringArray(R.array.rate_wrieless_arr)));
                break;
            case "读硬件版本号":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "设置内条码":
//                输入：表地址、内条码（16位数字）
                secondHandle(commandStr, "表地址", "内条码（16数字）");
                break;
            case "读内条码":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            case "生产专用1":
//                输入：表地址
//                secondHandle(commandStr,"表地址","手机时间");
                secondHandle(commandStr, "表地址");
                break;
            case "生产专用2":
//                输入：旧表地址、新地址
                secondHandle(commandStr, "表地址", "新地址");
                break;
            case "软件复位":
//                输入：表地址
                secondHandle(commandStr, "表地址");
                break;
            default:
                Toast.makeText(this, commandStr + ":暂无相关命令操作", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 弹出输入框页面，确认信息后，组装命令然后发送出去
     *
     * @param args 边长参数
     */
    private void secondHandle(String commandStr, String... args) {
        if (null != popCommandView) {
            popCommandView.hindeOrShowInputLl(false);
            popCommandView.setInputTitle(commandStr);
            popCommandView.setTitles(args);
        }
    }


    /**
     * 1月--D120
     *
     * @return
     */
    public String getValueByMonth(String month) {
//        int monthNum = Integer.parseInt(month.substring(0, month.length() - 1));
//        int starNum = 53535;//D120 - 1
//        int resultNum = monthNum + starNum;
//        String resultStr =   Integer.toHexString(resultNum);
        return (Integer.toHexString(53535 + Integer.parseInt(month.substring(0, month.length() - 1)))).toUpperCase();
    }

    /**
     * 1日--D134 53556
     *
     * @return
     */
    public String getValueByDay(String day) {
//        int monthNum = Integer.parseInt(month.substring(0, month.length() - 1));
//        int starNum = 53535;//D120 - 1
//        int resultNum = monthNum + starNum;
//        String resultStr =   Integer.toHexString(resultNum);
        return (Integer.toHexString(53555 + Integer.parseInt(day.substring(0, day.length() - 1)))).toUpperCase();
    }


//    begin CommandsListViewAdapter

    private class CommandsListViewAdapter extends BaseAdapter {

        LayoutInflater mInflater = LayoutInflater.from(CommunicationActivity.this);

        @Override
        public int getCount() {
            return mDataList == null ? 0 : mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.item_command_lv, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mTimeTv = convertView.findViewById(R.id.pre_fix_tv);
                viewHolder.mCommandTv = convertView.findViewById(R.id.command_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            CommondSendResponseEntity entity = mDataList.get(position);

//            发送帧
            if (!entity.getIsRes()) {
                viewHolder.mTimeTv.setText("发送 (" + entity.getTimeStamp() + ")");
                viewHolder.mCommandTv.setText(entity.getCommandStr());
//            响应帧
            } else {
                viewHolder.mTimeTv.setText("响应 (" + entity.getTimeStamp() + ")");
                viewHolder.mCommandTv.setText(entity.getCommandStr() + "\n" + entity.getResolveStr());
            }


            return convertView;
        }

        class ViewHolder {
            TextView mTimeTv;
            TextView mCommandTv;
        }

    }


//    end CommandsListViewAdapter


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
                clearHandler(handlersend, runnableSend);
                clearHandler(handlersend, sendCommandRunnable);
//                if (qppSendDataState) {
//                    setBtnSendState("Send");
//                    qppSendDataState = false;
//                }
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
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//            /*This is a workaround,20140819,xiesc: it paused with unknown reason on android 4.4.3
//             */
                if (handlersend != null)
                    handlersend.postDelayed(runnableSend, 60);
                //handlersend.post(runnableSend);
            } else {
//                Log.e(TAG, "Send failed!!!!");
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

    /**
     * 更新接收到的消息
     *
     * @param errStr
     */
    private void setQppNotify(final String errStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addReceiveDataItem(errStr);
            }
        });
    }

    private void addReceiveDataItem(String hexStr) {
        StringBuilder sb = new StringBuilder();
        StringBuilder resolveSb = new StringBuilder();
        int hexLen = hexStr.length();
        for (int i = 0; i < hexLen / 2; i++) {
            sb.append(hexStr.substring(2 * i, 2 * i + 2));
            sb.append(" ");
        }
        CommondSendResponseEntity entity = new CommondSendResponseEntity();
        entity.setISRes(true);
        entity.setCommandStr(sb.toString());

//                              681303015704FFFFFFFF01000080330A57046B07986B
        //                    68 13 03 01 	57 04	 FF FF FF FF	 01 00 00 80 	33 0A 57 04 6B 07 98 6B
//                                                                                                    Rx Tx

        String signal = "\n信号强度-接受:" + Integer.parseInt(hexStr.substring(hexLen - 4, hexLen - 2), 16) + "、发送:" + Integer.parseInt(hexStr.substring(hexLen - 2, hexLen), 16);


//        在此解析命令意义，并显示： 188协议最小长度 32 【68开始，16结束（有时不以16结尾）】 依据“数据标识解析”
//        if (hexStr.length() >= 32 && hexStr.substring(0,2).equals("68") && hexStr.substring(hexStr.length() - 3,hexStr.length() - 1).equals("16")){
        if (hexStr.length() >= 32 && hexStr.substring(0, 2).equals("68")) {
            String controlCode = (hexStr.substring(18, 20));
            String dataLen = (hexStr.substring(20, 22));
            String dataFlag = hexStr.substring(22, 26);
            switch (dataFlag) {
//                响应读表地址
                case "0A81":
                    resolveSb.append("响应读表地址: \n[");
                    String meterAddr = reverseStringByByte(hexStr.substring(4, 18));
                    resolveSb.append("表地址：" + meterAddr);
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                 响应写表地址   响应设置厂内扩展命令
                case "18A0":
                    String meterAddrWrite = reverseStringByByte(hexStr.substring(4, 18));
                    if (controlCode.equals("95")) {
                        resolveSb.append("响应写表地址:  \n[");
                        resolveSb.append("成功 ; 新地址：" + meterAddrWrite);
                    } else if (controlCode.equals("D5")) {
                        resolveSb.append("响应写表地址: \n[");
                        resolveSb.append("失败 ;原地址：" + meterAddrWrite);
                    } else if (controlCode.equals("84")) {
                        resolveSb.append("响应设置厂内扩展命令:  \n[");
                        resolveSb.append("成功;");
                    } else if (controlCode.equals("C4")) {
                        resolveSb.append("响应设置厂内扩展命令:  \n[");
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                    响应读计量数据
//                68 10 69 00 01 07 17 20 00 81 16 1F 90 02 00 00 00 00 2C 00 00 00 00 2C 59 54 16 19 08 17 20 13 01 EF 16 52 4F
                case "1F90":
                    resolveSb.append("响应读计量数据:  \n[");
                    if (hexStr.length() < 66) {
                        resolveSb.append("长度异常; ");
                        return;
                    }
                    String curTotalFlow = reverseStringByByte(hexStr.substring(28, 36));
                    String curDayFlow = reverseStringByByte(hexStr.substring(38, 46));
                    String curTime = reverseStringByByte(hexStr.substring(48, 62));
                    String meterStatus = reverseStringByByte(hexStr.substring(62, 66));
                    if (controlCode.equals("81")) {
                        resolveSb.append("成功; ");
                        resolveSb.append("; 当前累计流量：" + Integer.parseInt(curTotalFlow.substring(0, 6)) + "." + Integer.parseInt(curTotalFlow.substring(6, 8)));
                        resolveSb.append("; 结算日累积量：" + Integer.parseInt(curDayFlow.substring(0, 6)) + "." + Integer.parseInt(curDayFlow.substring(6, 8)));
                        resolveSb.append("; 实时时间：");
                        resolveSb.append("; " + curTime.substring(0, 4) + "-" + curTime.substring(4, 6) + "-" + curTime.substring(6, 8) + " " + curTime.substring(8, 10) + ":" + curTime.substring(10, 12) + ":" + curTime.substring(12, 14));
                        resolveSb.append("; 表状态：" + meterStatus.substring(0, 2) + " " + meterStatus.substring(2, 4));
                    } else if (controlCode.equals("C1")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应读历史月结算日累积量
                case "20D1":
                case "21D1":
                case "22D1":
                case "23D1":
                case "24D1":
                case "25D1":
                case "26D1":
                case "27D1":
                case "28D1":
                case "29D1":
                case "2AD1":
                case "2BD1":
                case "2CD1":
                case "2DD1":
                case "2ED1":
                case "2FD1":
                case "30D1":
                case "31D1":
                    resolveSb.append("响应读历史月结算日累积量:  \n[");
                    String totalFow = reverseStringByByte(hexStr.substring(28, 36));
                    int offset = Integer.parseInt(dataFlag.substring(0, 2), 16) - 0x20 + 1;
                    if (controlCode.equals("81")) {
                        resolveSb.append("成功;");
                        resolveSb.append("; 上" + offset + "月结算日累积量:" + Integer.parseInt(totalFow.substring(0, 6)) + "." + Integer.parseInt(totalFow.substring(6, 8)));
                    } else if (controlCode.equals("C1")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应读历史日冻结累计流量
                case "34D1":
                case "35D1":
                case "36D1":
                case "37D1":
                case "38D1":
                case "39D1":
                case "3AD1":
                case "3BD1":
                case "3CD1":
                case "3DD1":
                case "3ED1":
                case "3FD1":
                case "40D1":
                case "41D1":
                case "42D1":
                case "43D1":
                case "44D1":
                case "45D1":
                case "46D1":
                case "47D1":
                case "48D1":
                case "49D1":
                case "4AD1":
                case "4BD1":
                case "4CD1":
                case "4DD1":
                case "4ED1":
                case "4FD1":
                case "50D1":
                case "51D1":
                case "52D1":
                    resolveSb.append("响应读历史日冻结累计流量:  \n[");
                    String totalFowFreeze = reverseStringByByte(hexStr.substring(28, 36));
                    int offsetDay = Integer.parseInt(dataFlag.substring(0, 2), 16) - 0x34 + 1;
                    if (controlCode.equals("81")) {
                        resolveSb.append("成功;");
                        resolveSb.append("; 上" + offsetDay + "日冻结累积流量:" + Integer.parseInt(totalFowFreeze.substring(0, 6)) + "." + Integer.parseInt(totalFowFreeze.substring(6, 8)));
                    } else if (controlCode.equals("C1")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                   响应写机电同步数据
                case "16A0":
                    resolveSb.append("响应写机电同步数据: \n[");
                    String meterStatusJd = reverseStringByByte(hexStr.substring(30, 34));
                    if (controlCode.equals("96")) {
                        resolveSb.append("成功;");
                        resolveSb.append("表状态;" + meterStatusJd.substring(0, 2) + " " + meterStatusJd.substring(2, 4));
                    } else if (controlCode.equals("D6")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                    响应校时
                case "15A0":
                    resolveSb.append("响应校时:  \n[");
                    if (controlCode.equals("84")) {
                        resolveSb.append("成功;");
                    } else if (controlCode.equals("C4")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                    响应开阀
                case "17A0":
//                    String resData = hexStr.substring(28, 30);
                    resolveSb.append("响应" + commandStr + ":  \n[");
                    if (controlCode.equals("84")) {
                        resolveSb.append("成功;");
                    } else if (controlCode.equals("C4")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                    响应出厂启用
                case "19A0":
                    resolveSb.append(" 响应出厂启用:  \n[");
                    if (controlCode.equals("84")) {
                        resolveSb.append("成功;");
                    } else if (controlCode.equals("C4")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应读EEPROM
                case "10A0":
                    int dataOffset = (Integer.parseInt(dataLen, 16) - 3) * 2;
                    String data = hexStr.substring(28, 28 + dataOffset);
                    resolveSb.append(" 响应读EEPROM:  \n[");
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                        resolveSb.append("EEPROM:" + data);
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应读电池电压
                case "11A0":
//                    int bateryVoll = Integer.parseInt(reverseStringByByte(hexStr.substring(30,34)));
                    int bateryVoll = Integer.parseInt(reverseStringByByte(hexStr.substring(28, 32)), 16);
                    resolveSb.append(" 响应读电池电压:  \n[");
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                        resolveSb.append("电池电压:" + bateryVoll + " (10mV)");
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应读软件版本号
                case "1AA0":
//                    String softwareV = hexStr.substring(28, 30) + "-" + hexStr.substring(30, 32) + "-" + hexStr.substring(32, 34) + "-" + hexStr.substring(34, 36) + "-" + hexStr.substring(36, 38);
                    String softwareV = hexStr.substring(34, 36) + "-" + hexStr.substring(32, 34) + "-" + hexStr.substring(30, 32) + "-" + hexStr.substring(28, 30) + "-" + hexStr.substring(36, 38);
                    resolveSb.append(" 响应读软件版本号: \n[");
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                        resolveSb.append("软件版本号:" + softwareV);
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append("失败; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应读表运行数据
                case "1BA0":
                    resolveSb.append(" 响应读表运行数据:  \n[");
                    if (controlCode.equals("B3")) {
                        if (hexStr.length() < 72) {
                            resolveSb.append(" 数据长度异常 ; ");
                            return;
                        }
                        String dataContentStr = hexStr.substring(28, 72);
                        resolveSb.append("成功;");
                        resolveSb.append("; 单边累计流量:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(0, 8)), 16));
                        resolveSb.append("; 开阀次数:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(8, 12)), 16));
                        resolveSb.append("; 关阀次数:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(12, 16)), 16));
                        resolveSb.append("; 强磁次数:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(16, 20)), 16));
                        resolveSb.append("; 广播命令次数:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(20, 24)), 16));
                        resolveSb.append("; 广播点名次数:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(24, 28)), 16));
                        resolveSb.append("; 点抄次数:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(28, 32)), 16));
                        resolveSb.append("; 电池电压:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(32, 36)), 16) + "(10mV)");
                        resolveSb.append("; 表运行时间:" + Long.parseLong(reverseStringByByte(dataContentStr.substring(36, 44)), 16) + "(秒)");
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append(" 失败 ; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应读Boot版本号
                case "1CA0":
//                    String bootV = hexStr.substring(30, 38);
                    String bootV = hexStr.substring(28, 36);
                    resolveSb.append(" 响应读Boot版本号:  \n[");
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                        resolveSb.append(" Boot版本号:" + bootV.substring(6, 8) + "-" + bootV.substring(4, 6) + "-" + bootV.substring(2, 4) + "-" + bootV.substring(0, 2));
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append(" 失败 ; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应设置无线信道
                case "1DA0":
                    resolveSb.append(" 响应设置无线信道:  \n[");
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append(" 失败 ; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应读硬件版本号
                case "1EA0":
                    String hardwareV =
                            (char) (Integer.parseInt(hexStr.substring(36, 38), 16)) + ""
                                    + (char) (Integer.parseInt(hexStr.substring(34, 36), 16)) + ""
                                    + (char) (Integer.parseInt(hexStr.substring(32, 34), 16)) + ""
                                    + (char) (Integer.parseInt(hexStr.substring(30, 32), 16)) + ""
                                    + (char) (Integer.parseInt(hexStr.substring(28, 30), 16)) + "";

                    resolveSb.append(" 响应读硬件版本号:  \n[");
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                        resolveSb.append("硬件版本号;" + hardwareV);
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append(" 失败 ; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应设置内条码
                case "1FA0":
                    resolveSb.append(" 响应设置内条码:  \n[");
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append(" 失败 ; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应生产专用1
                case "20A0":
                    resolveSb.append(" 响应生产专用1:  \n[");
                    if (hexStr.length() < 70) {
                        resolveSb.append("数据长度异常！;");
                        return;
                    }
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                        String dataContentStr = hexStr.substring(28, 70);
//                        String timeStr = reverseStringByByte(dataContentStr.substring(16,30));
                        resolveSb.append("成功;");
                        resolveSb.append("; 当前累计流量:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(0, 6))) + "." + Integer.parseInt(reverseStringByByte(dataContentStr.substring(6, 8))));
                        resolveSb.append("; 结算日累积量:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(10, 16))) + "." + Integer.parseInt(reverseStringByByte(dataContentStr.substring(16, 18))));
                        resolveSb.append("; 实时时间:" + dataContentStr.substring(20, 24) + "-" + dataContentStr.substring(24, 26) + "-" + dataContentStr.substring(26, 28) + " " + dataContentStr.substring(28, 30) + ":" + dataContentStr.substring(30, 32) + ":" + dataContentStr.substring(32, 34));
                        resolveSb.append("; 表状态:" + dataContentStr.substring(34, 36) + " " + dataContentStr.substring(36, 38));
                        resolveSb.append("; 电池电压:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(38, 42)), 16) + " (10mV)");
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append(" 失败 ; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应生产专用2
                case "21A0":
                    resolveSb.append(" 响应生产专用2:  \n[");
                    if (hexStr.length() <= 79) {
                        resolveSb.append("数据长度异常！;");
                        return;
                    }
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                        String dataContentStr = hexStr.substring(28, 80);
//                        String timeStr = reverseStringByByte(dataContentStr.substring(16,30));
                        String softwareV2 = (dataContentStr.substring(dataContentStr.length() - 11, dataContentStr.length() - 1));
                        resolveSb.append("成功;");

                        resolveSb.append("; 当前累计流量:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(0, 6))) + "." + Integer.parseInt(reverseStringByByte(dataContentStr.substring(6, 8))));
                        resolveSb.append("; 结算日累积量:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(10, 16))) + "." + Integer.parseInt(reverseStringByByte(dataContentStr.substring(16, 18))));
                        resolveSb.append("; 实时时间:" + dataContentStr.substring(20, 24) + "-" + dataContentStr.substring(24, 26) + "-" + dataContentStr.substring(26, 28) + " " + dataContentStr.substring(28, 30) + ":" + dataContentStr.substring(30, 32) + ":" + dataContentStr.substring(32, 34));
                        resolveSb.append("; 表状态:" + dataContentStr.substring(34, 36) + " " + dataContentStr.substring(36, 38));
                        resolveSb.append("; 电池电压:" + Integer.parseInt(reverseStringByByte(dataContentStr.substring(38, 42)), 16) + " (10mV)");

//                        resolveSb.append("; 当前累计流量:"+Integer.parseInt(reverseStringByByte(dataContentStr.substring(0,6))+"."+Integer.parseInt(reverseStringByByte(dataContentStr.substring(6,8)))));
//                        resolveSb.append("; 结算日累积量:"+Integer.parseInt(reverseStringByByte(dataContentStr.substring(8,14))+"."+Integer.parseInt(reverseStringByByte(dataContentStr.substring(14,16)))));
//                        resolveSb.append("; 实时时间:"+timeStr.substring(0,4)+"-"+timeStr.substring(4,6)+"-"+timeStr.substring(6,8)+" "+timeStr.substring(8,10)+":"+timeStr.substring(10,12)+":"+timeStr.substring(12,14));
//                        resolveSb.append("; 表状态:"+dataContentStr.substring(30,32)+" "+dataContentStr.substring(32,34));
//                        resolveSb.append("; 电池电压:"+Integer.parseInt(reverseStringByByte(dataContentStr.substring(34,38)),16)+" (10mV)");
                        resolveSb.append("; 软件版本号:" + softwareV2.substring(8, 10) + "-" + softwareV2.substring(6, 8) + "-" + softwareV2.substring(4, 6) + "-" + softwareV2.substring(2, 4) + "-" + softwareV2.substring(0, 2));
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append(" 失败 ; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
//                响应软件复位
                case "22A0":
                    resolveSb.append(" 响应软件复位:  \n[");
                    if (controlCode.equals("B3")) {
                        resolveSb.append("成功;");
                    } else if (controlCode.equals("F3")) {
                        resolveSb.append(" 失败 ; ");
                    }
                    resolveSb.append("]");
                    resolveSb.append(signal);
                    break;
            }
        }

        entity.setResolveStr(resolveSb.toString());
        mDataList.add(entity);
        if (null != mCommandListAdapter) {
            mCommandListAdapter.notifyDataSetChanged();
            mCommandsListView.setSelection(mDataList.size() - 1);
        }
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
        clearHandler(handlersend, runnableSend);
        clearHandler(handlersend, sendCommandRunnable);
        close();
    }


    /**
     * @return 无线频率--hex
     */
    public String getRateHexStrByPos(int pos) {
//        pos++;
//        String dataStr = pos+"";
        return prefix0ToLen(Integer.toHexString(pos), 2);
//        switch (pos) {
//            case 1:
////                dataStr = "005C901D";
//                dataStr = "6013";
//                break;
//            case 2:
////                dataStr = "4095621D";
//                dataStr = "4213";
//                break;
//            case 3:
////                dataStr = "C0AF681D";
//                dataStr = "4613";
//                break;
//            case 4:
////                dataStr = "40CA6E1D";
//                dataStr = "4A13";
//                break;
//            case 5:
////                dataStr = "C0E4741D";
//                dataStr = "4E13";
//                break;
//            case 6:
////                dataStr = "40FF7A1D";
//                dataStr = "5213";
//                break;
//            case 7:
////                dataStr = "C019811D";
//                dataStr = "5613";
//                break;
//            case 8:
////                dataStr = "4034871D";
//                dataStr = "5A13";
//                break;
//            case 9:
////                dataStr = "80418A1D";
//                dataStr = "5C13";
//                break;
//            case 10:
////                dataStr = "409E9F1D";
//                dataStr = "6A13";
//                break;
//            case 11:
////                dataStr = "C0B8A51D";
//                dataStr = "6E13";
//                break;
//            case 12:
////                dataStr = "40D3AB1D";
//                dataStr = "7213";
//                break;
//            case 13:
////                dataStr = "C0EDB11D";
//                dataStr = "7613";
//                break;
//            case 14:
////                dataStr = "4008B81D";
//                dataStr = "7A13";
//                break;
//            case 15:
////                dataStr = "C022BE1D";
//                dataStr = "7E13";
//                break;
//        }
//        return dataStr;
    }


}
