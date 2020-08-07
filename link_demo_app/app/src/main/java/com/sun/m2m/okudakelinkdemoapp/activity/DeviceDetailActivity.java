/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.sun.m2m.okudakelinkdemoapp.R;
import com.sun.m2m.okudakelinkdemoapp.common.AppSettings;
import com.sun.m2m.okudakelinkdemoapp.common.Common;
import com.sun.m2m.okudakelinkdemoapp.common.SpinnerValues;
import com.sun.m2m.okudakelinkdemoapp.ifttt.IFTTTManager;
import com.sun.m2m.okudakelinkdemoapp.okudake.AccelerometerValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.BatteryValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.BeaconData;
import com.sun.m2m.okudakelinkdemoapp.okudake.DevicePreferenceValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.IlluminometerValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.LedValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.MagnetometerValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.OkudakeManager;
import com.sun.m2m.okudakelinkdemoapp.okudake.OkudakeManagerListener;
import com.sun.m2m.okudakelinkdemoapp.okudake.ThermohygrometerValue;
import com.sun.m2m.okudakelinkdemoapp.view.ListViewSpinnerListener;
import com.sun.m2m.okudakelinkdemoapp.view.SensorListAdapter;
import com.sun.m2m.okudakelinkdemoapp.view.SensorValue;
import com.sun.m2m.okudakelinkdemoapp.view.SettingListAdapter;
import com.sun.m2m.okudakelinkdemoapp.view.SettingValue;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.sun.m2m.okudakelinkdemoapp.common.SpinnerValues.ReadDataWay;

/**
 * デバイス情報詳細画面
 */
public class DeviceDetailActivity extends AppCompatActivity {
    /** ビーコン送信間隔のListViewでの行インデックス */
    private static final int PREFERENCE_LIST_BEACON_TRANSMITTING_INDEX = 0;
    /** ビーコン更新間隔のListViewでの行インデックス */
    private static final int PREFERENCE_LIST_BEACON_UPDATE_INDEX = 1;
    /** データ取得方法のListViewでの行インデックス */
    private static final int PREFERENCE_LIST_READ_DATA_WAY_INDEX = 2;
    /** データ取得間隔のListViewでの行インデックス */
    private static final int PREFERENCE_LIST_READ_DATA_UPDATE_INDEX = 3;
    /** 送信電力のListViewでの行インデックス */
    private static final int PREFERENCE_LIST_TX_POWER_LEVEL_INDEX = 4;
    /** 自動切断のListViewでの行インデックス */
    private static final int PREFERENCE_LIST_IDLE_TIMEOUT_INDEX = 5;

    /** 温湿度のListViewでの行インデックス */
    private static final int SENSOR_LIST_THERMOHYGROMETER_INDEX = 0;
    /** 照度のListViewでの行インデックス */
    private static final int SENSOR_LIST_ILLUMINOMETER_INDEX = 1;
    /** 加速度のListViewでの行インデックス */
    private static final int SENSOR_LIST_ACCELEROMETER_INDEX = 2;
    /** 磁気のListViewでの行インデックス */
    private static final int SENSOR_LIST_MAGNETOMETER_INDEX = 3;
    /** 給電状態のListViewでの行インデックス */
    private static final int SENSOR_LIST_BATTERY_INDEX = 4;
    /** LEDのListViewでの行インデックス */
    private static final int SENSOR_LIST_LED_INDEX = 5;

    /** IFTTT Value1のListViewでの行インデックス */
    private static final int IFTTT_LIST_VALUE1_INDEX = 0;
    /** IFTTT Value2のListViewでの行インデックス */
    private static final int IFTTT_LIST_VALUE2_INDEX = 1;
    /** IFTTT Value3のListViewでの行インデックス */
    private static final int IFTTT_LIST_VALUE3_INDEX = 2;

    /** ビーコン送信間隔のデフォルト値 */
    private static final int BEACON_TRANSMITTING_INTERVAL_DEF = 1600;
    /** ビーコン更新間隔のデフォルト値 */
    private static final int BEACON_UPDATE_INTERVAL_DEF = 5;
    /** データ取得方法のデフォルト値 */
    private static final ReadDataWay READ_DATA_WAY_DEF = ReadDataWay.READ;
    /** データ取得間隔のデフォルト値 */
    private static final int READ_DATA_INTERVAL_DEF = 5;
    /** 送信電力のデフォルト値 */
    private static final int TX_POWER_LEVEL_DEF = 0;
    /** 自動切断のデフォルト値 */
    private static final int IDLE_TIMEOUT_DEF = 30;

    /** 温湿度センサー更新間隔のデフォルト値 */
    private static final int NOTIFICATION_THERMOHYGROMETER_PERIOD_DEF = 5000;
    /** 照度センサー更新間隔のデフォルト値 */
    private static final int NOTIFICATION_ILLUMINOMETER_PERIOD_DEF = 5000;
    /** 加速度センサー更新間隔のデフォルト値 */
    private static final int NOTIFICATION_ACCELEROMETER_PERIOD_DEF = 5000;

    /** GATT接続のSwitch */
    private Switch connectBleSwitch;
    /** 接続中／未接続のTextView */
    private TextView connectStatusTextView;
    /** ビーコン情報のTextView */
    private TextView beaconInfoTextView;
    /** 設定情報のListView */
    private ListView devicePreferenceListView;
    /** センサー情報のListView */
    private ExpandableListView sensorInfoListView;
    /** IFTTT送信のSwitch */
    private Switch iftttEnableSwitch;
    /** IFTTT設定のListView */
    private ListView iftttValuesListView;

    /** OkudakeManager */
    private OkudakeManager okudakeManager;
    /** シリアル番号 */
    private String serialNumber;
    /** BLEデバイス */
    private BluetoothDevice device;
    /** IFTTTManager */
    private IFTTTManager iftttManager;
    /** 設定情報ListViewのAdapter */
    private SettingListAdapter devicePreferenceListAdapter;
    /** センサー情報ListViewのAdapter */
    private SensorListAdapter sensorListAdapter;
    /** IFTTT設定ListViewのAdapter */
    private SettingListAdapter iftttSettingListAdapter;

    /** データ取得方法 */
    private ReadDataWay readDataWaySetting;
    /** データ取得間隔 */
    private int readDataIntervalSetting;
    /** 前回データ取得時刻 */
    private long readDataTime;

    /**
     * onCreate
     * @param savedInstanceState SavedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        connectBleSwitch = findViewById(R.id.connect_ble_switch);
        connectStatusTextView = findViewById(R.id.connect_status_textview);
        beaconInfoTextView = findViewById(R.id.beacon_info_textview);
        devicePreferenceListView = findViewById(R.id.device_preference_listview);
        sensorInfoListView = findViewById(R.id.sensor_info_listview);
        iftttEnableSwitch = findViewById(R.id.ifttt_enable_switch);
        iftttValuesListView = findViewById(R.id.ifttt_values_listview);

        okudakeManager =
                OkudakeManager.getInstance(getApplicationContext(), okudakeManagerListener);
        serialNumber = Common.selectedOkudakeDevice.serialNumber;
        device = Common.selectedOkudakeDevice.device;
        if (device.getName() != null) actionBar.setTitle(device.getName());
        else actionBar.setTitle("UNKNOWN");
        setBeaconData(Common.selectedOkudakeDevice);

        AppSettings.loadSettings(getApplicationContext());
        iftttManager = new IFTTTManager(
                getApplicationContext(),
                AppSettings.iftttKey,
                AppSettings.iftttEventName
        );
        iftttManager.setPostingInterval(AppSettings.iftttUploadInterval);
        iftttManager.setValues("", "", "");

        readDataWaySetting = READ_DATA_WAY_DEF;
        readDataIntervalSetting = READ_DATA_INTERVAL_DEF;
        readDataTime = 0;

        connectBleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    connectToOkudakeSensor();
                } else {
                    disconnectFromOkudakeSensor();
                }
            }
        });

        sensorInfoListView.setOnGroupExpandListener(
                new ExpandableListView.OnGroupExpandListener() {
                    @Override
                    public void onGroupExpand(int i) {
                        setListViewHeight(sensorInfoListView);
                    }
                }
        );

        sensorInfoListView.setOnGroupCollapseListener(
                new ExpandableListView.OnGroupCollapseListener() {
                    @Override
                    public void onGroupCollapse(int i) {
                        setListViewHeight(sensorInfoListView);
                    }
                }
        );

        iftttEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (okudakeManager.isConnected()) {
                        if (AppSettings.iftttKey.equals("")
                                || AppSettings.iftttEventName.equals("")) {
                            iftttEnableSwitch.setChecked(false);
                            showToast("Please enter the IFTTT settings.");
                        } else {
                            iftttManager.startPosting();
                        }
                    } else {
                        iftttEnableSwitch.setChecked(false);
                        showToast("Please connect to okudake sensor.");
                    }
                } else {
                    iftttManager.stopPosting();
                }
            }
        });
    }

    /**
     * onResume
     * Activity表示時のコールバック
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (!okudakeManager.isScanning()) {
            okudakeManager.startScanning();
            beaconInfoTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * onSupportNavigateUp
     * ActionBarの矢印ボタンで前画面に戻る
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return super.onSupportNavigateUp();
    }

    /**
     * onPause
     * Activity非表示時のコールバック
     */
    @Override
    protected void onPause() {
        exitActivity();

        super.onPause();
    }

    /**
     * onDestroy
     * Activity破棄時のコールバック
     */
    @Override
    protected void onDestroy() {
        exitActivity();

        super.onDestroy();
    }

    /**
     * Activity終了時の処理
     * GATT接続、BLEスキャン、IFTTTアップロードを停止する
     */
    private void exitActivity() {
        readDataWaySetting = ReadDataWay.NONE;
        readDataHandler.removeCallbacks(readDataRunnable);

        if (okudakeManager.isConnected()) {
            okudakeManager.disconnect();
        }

        if (okudakeManager.isScanning()) {
            okudakeManager.stopScanning();
        }

        if (iftttManager.isPosting()) {
            iftttManager.stopPosting();
        }
    }

    /**
     * おくだけセンサーに接続する
     */
    private void connectToOkudakeSensor() {
        if (!okudakeManager.isConnected()) {
            okudakeManager.stopScanning();
            if (!okudakeManager.connect(device)) {
                showToast("cannot connect to okudake sensor.");
                connectBleSwitch.setChecked(false);
            }
        }
    }

    /**
     * おくだけセンサーの接続を切断する
     */
    private void disconnectFromOkudakeSensor() {
        if (okudakeManager.isConnected()) {
            okudakeManager.disconnect();
        }
    }

    /**
     * ビーコンデータを画面に表示する
     * @param data 表示するビーコンデータ
     */
    private void setBeaconData(BeaconData data) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss\n");
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(new Date(data.dateTime)));
        sb.append(String.format("TxPower %ddBm\n", data.rssi));
        sb.append(String.format("温度 %.2f℃\n", data.temperature));
        sb.append(String.format("湿度 %.2f％\n", data.humidity));
        if (data.over24voltage) {
            sb.append(String.format("電池電圧2.4V以上 %.0f％\n", data.batteryLevel));
        } else {
            sb.append(String.format("電池電圧2.4V未満 %.0f％\n", data.batteryLevel));
        }
        if (data.magnetic) {
            sb.append("磁気検出あり\n");
        } else {
            sb.append("磁気検出なし\n");
        }
        beaconInfoTextView.setText(sb.toString());
    }

    /**
     * ListViewの縦幅を変更
     * @param listView 縦幅を変更するListView
     */
    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            listView.setLayoutParams(params);
        } else {
            int itemHeight = (int)Math.round(
                    50.0 * getApplicationContext().getResources().getDisplayMetrics().scaledDensity
            );
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (itemHeight * listAdapter.getCount())
                    + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
        }
    }

    /**
     * IFTTTでアップロードする値の変更
     * @param iftttSpinnerIdx 値の種類
     * @param value アップロードする値
     */
    private void setIftttValue(int iftttSpinnerIdx, String value) {
        if (AppSettings.iftttValue1 == iftttSpinnerIdx) {
            iftttManager.setValue(IFTTTManager.IFTTT_VALUE1_INDEX, value);
        } else if (AppSettings.iftttValue2 == iftttSpinnerIdx) {
            iftttManager.setValue(IFTTTManager.IFTTT_VALUE2_INDEX, value);
        } else if (AppSettings.iftttValue3 == iftttSpinnerIdx) {
            iftttManager.setValue(IFTTTManager.IFTTT_VALUE3_INDEX, value);
        }
    }

    /**
     * 定期的にセンサーデータをReadするためのHandler
     */
    private Handler readDataHandler = new Handler();

    /**
     * 定期的にセンサーデータをReadするためのRunnable
     */
    private Runnable readDataRunnable = new Runnable() {
        @Override
        public void run() {
            readDataHandler.removeCallbacks(readDataRunnable);

            if ((readDataWaySetting == ReadDataWay.READ) && (readDataIntervalSetting > 0)) {
                long elapsedTime = System.currentTimeMillis() - readDataTime;
                if ((readDataTime == 0)
                        || (elapsedTime >= readDataIntervalSetting * 1000)) {
                    readDataTime = System.currentTimeMillis();
                    okudakeManager.getThermohygrometerValue();
                    okudakeManager.getIlluminometerValue();
                    okudakeManager.getAccelerometerValue();
                    okudakeManager.getMagnetometerValue();
                    okudakeManager.getBatteryValue();
                    okudakeManager.getBatteryUsbPlugged();
                    okudakeManager.getLedStatus();
                }
                readDataHandler.postDelayed(readDataRunnable, 200);
            }

        }
    };

    /**
     * OkudakeManagerのコールバック
     */
    private OkudakeManagerListener okudakeManagerListener = new OkudakeManagerListener() {
        /**
         * おくだけセンサーのAdvertiseData取得時のコールバック
         * @param data　検出したビーコンデータ
         */
        @Override
        public void onScannedDevice(final BeaconData data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (data.serialNumber.equals(serialNumber)) {
                        setBeaconData(data);
                    }
                }
            });
        }

        /**
         * おくだけセンサーのスキャン結果を定期的に返す時のコールバック
         * @param datas 検出したビーコンデータ
         */
        @Override
        public void onScannedDevices(List<BeaconData> datas) {

        }

        /**
         * おくだけセンサーに接続された時のコールバック
         */
        @Override
        public void onConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectStatusTextView.setText("接続済み");

                    beaconInfoTextView.setText("");
                    beaconInfoTextView.setVisibility(View.INVISIBLE);

                    readDataWaySetting = ReadDataWay.READ;
                    readDataIntervalSetting = READ_DATA_INTERVAL_DEF;
                    setNotification(false);
                    createDevicePreferenceList();
                    devicePreferenceListView.setVisibility(View.VISIBLE);

                    createSensorInfoList();
                    sensorInfoListView.setVisibility(View.VISIBLE);

                    createIftttSettingList();
                    iftttValuesListView.setVisibility(View.VISIBLE);

                    setIftttValue(
                            SpinnerValues.IFTTT_DEVICE_NAME_INDEX,
                            device.getName()
                    );

                    setListViewHeight(devicePreferenceListView);
                    setListViewHeight(sensorInfoListView);
                    setListViewHeight(iftttValuesListView);

                    connectBleSwitch.setChecked(true);

                    okudakeManager.getBeaconTransmittingInterval();
                    okudakeManager.getBeaconUpdatingInterval();
                    okudakeManager.getTxPowerLevel();
                    okudakeManager.getIdleTimeout();

                    okudakeManager.setThermohygrometerAvailable(true);
                    okudakeManager.setIlluminometerAvailable(true);
                    okudakeManager.setAccelerometerAvailable(true);
                    okudakeManager.setMagnetometerAvailable(true);
                    okudakeManager.setBatteryAvailable(true);

                    okudakeManager.getThermohygrometerPeriod();
                    okudakeManager.getIlluminometerPeriod();
                    okudakeManager.getAccelerometerPeriod();

                    readDataHandler.postDelayed(readDataRunnable, 100);
                }
            });
        }

        /**
         * おくだけセンサーとの接続切断時のコールバック
         */
        @Override
        public void onDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectStatusTextView.setText("未接続");

                    beaconInfoTextView.setVisibility(View.VISIBLE);

                    devicePreferenceListView.setAdapter(null);
                    devicePreferenceListView.setVisibility(View.INVISIBLE);

                    sensorInfoListView.setAdapter((ExpandableListAdapter) null);
                    sensorInfoListView.setVisibility(View.INVISIBLE);

                    iftttValuesListView.setAdapter(null);
                    iftttValuesListView.setVisibility(View.INVISIBLE);
                    iftttManager.stopPosting();
                    iftttEnableSwitch.setChecked(false);

                    setListViewHeight(devicePreferenceListView);
                    setListViewHeight(sensorInfoListView);
                    setListViewHeight(iftttValuesListView);

                    connectBleSwitch.setChecked(false);
                    okudakeManager.startScanning();
                }
            });
        }

        /**
         * 端末設定データ取得時のコールバック
         * @param value 端末設定データ
         */
        @Override
        public void onReceivedDevicePreferences(final DevicePreferenceValue value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    devicePreferenceListAdapter.setSetting(
                            PREFERENCE_LIST_BEACON_TRANSMITTING_INDEX,
                            getSettingIndex(
                                    SpinnerValues.BEACON_TRANSMITTING_INTERVAL_VALUES,
                                    value.beaconTransmittingInterval,
                                    BEACON_TRANSMITTING_INTERVAL_DEF)
                    );

                    devicePreferenceListAdapter.setSetting(
                            PREFERENCE_LIST_BEACON_UPDATE_INDEX,
                            getSettingIndex(
                                    SpinnerValues.BEACON_UPDATE_INTERVAL_VALUES,
                                    value.beaconUpdatingInterval,
                                    BEACON_UPDATE_INTERVAL_DEF)
                    );

                    devicePreferenceListAdapter.setSetting(
                            PREFERENCE_LIST_TX_POWER_LEVEL_INDEX,
                            getSettingIndex(
                                    SpinnerValues.TX_POWER_LEVEL_VALUES,
                                    value.txPowerLevel,
                                    TX_POWER_LEVEL_DEF)
                    );

                    devicePreferenceListAdapter.setSetting(
                            PREFERENCE_LIST_IDLE_TIMEOUT_INDEX,
                            getSettingIndex(
                                    SpinnerValues.IDLE_TIMEOUT_VALUES,
                                    value.idleTimeout,
                                    IDLE_TIMEOUT_DEF)
                    );

                    devicePreferenceListAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * 温湿度センサーデータ取得時のコールバック
         * @param value 温湿度センサーデータ
         */
        @Override
        public void onReceivedThermohygrometer(final ThermohygrometerValue value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (value.enable) {
                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_THERMOHYGROMETER_INDEX,
                                String.format("%.2f℃　 %.2f％", value.temperature, value.humidity)
                        );
                        setIftttValue(
                                SpinnerValues.IFTTT_TEMPERATURE_INDEX,
                                String.format("%.2f", value.temperature)
                        );
                        setIftttValue(
                                SpinnerValues.IFTTT_HUMIDITY_INDEX,
                                String.format("%.2f", value.humidity)
                        );
                    } else {
                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_THERMOHYGROMETER_INDEX,
                                "センサーが無効です"
                        );
                        setIftttValue(SpinnerValues.IFTTT_TEMPERATURE_INDEX, "");
                        setIftttValue(SpinnerValues.IFTTT_HUMIDITY_INDEX, "");
                    }

                    sensorListAdapter.setSettingValue(
                            SENSOR_LIST_THERMOHYGROMETER_INDEX,
                            0,
                            getSettingIndex(
                                    SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                                    value.period,
                                    NOTIFICATION_THERMOHYGROMETER_PERIOD_DEF)
                    );

                    sensorListAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * 照度センサーデータ取得時のコールバック
         * @param value 照度センサーデータ
         */
        @Override
        public void onReceivedIlluminometer(final IlluminometerValue value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (value.enable) {
                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_ILLUMINOMETER_INDEX,
                                String.format("%.2flux", value.illuminance)
                        );
                        setIftttValue(
                                SpinnerValues.IFTTT_ILLUMINANCE_INDEX,
                                String.format("%.2f", value.illuminance)
                        );
                    } else {
                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_ILLUMINOMETER_INDEX,
                                "センサーが無効です"
                        );
                        setIftttValue(SpinnerValues.IFTTT_ILLUMINANCE_INDEX, "");
                    }

                    sensorListAdapter.setSettingValue(
                            SENSOR_LIST_ILLUMINOMETER_INDEX,
                            0,
                            getSettingIndex(
                                    SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                                    value.period,
                                    NOTIFICATION_ILLUMINOMETER_PERIOD_DEF)
                    );

                    sensorListAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * 加速度センサーデータ取得時のコールバック
         * @param value 加速度センサーデータ
         */
        @Override
        public void onReceivedAccelerometer(final AccelerometerValue value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (value.enable) {
                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_ACCELEROMETER_INDEX,
                                String.format("(x,y,z) = (%.2f,%.2f,%.2f)m/s^2",
                                        value.acceleration[0],
                                        value.acceleration[1],
                                        value.acceleration[2])
                        );
                        setIftttValue(
                                SpinnerValues.IFTTT_ACCELERATION_INDEX,
                                String.format("%.2f,%.2f,%.2f",
                                        value.acceleration[0],
                                        value.acceleration[1],
                                        value.acceleration[2])
                        );
                    } else {
                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_ACCELEROMETER_INDEX,
                                "センサーが無効です"
                        );
                        setIftttValue(SpinnerValues.IFTTT_ACCELERATION_INDEX, "");
                    }

                    sensorListAdapter.setSettingValue(
                            SENSOR_LIST_ACCELEROMETER_INDEX,
                            0,
                            getSettingIndex(
                                    SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                                    value.period,
                                    NOTIFICATION_ACCELEROMETER_PERIOD_DEF)
                    );

                    sensorListAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * 磁気センサーデータ取得時のコールバック
         * @param value 磁気センサーデータ
         */
        @Override
        public void onReceivedMagnetometer(final MagnetometerValue value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (value.enable) {
                        String valueStr = value.magnetic ? "検出" : "未検出";

                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_MAGNETOMETER_INDEX,
                                String.format("%s", valueStr)
                        );
                    } else {
                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_MAGNETOMETER_INDEX,
                                "センサーが無効です"
                        );
                    }
                    sensorListAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * バッテリーデータ取得時のコールバック
         * @param value バッテリーデータ
         */
        @Override
        public void onReceivedBattery(final BatteryValue value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (value.enable) {
                        String valueStr1 = value.usbPlugged ? "USB接続" : "電池";
                        String valueStr2 = value.over24voltage ? "2.4V以上" : "2.4V未満";

                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_BATTERY_INDEX,
                                String.format("%s　%s", valueStr1, valueStr2)
                        );
                    } else {
                        sensorListAdapter.setSensorValue(
                                SENSOR_LIST_BATTERY_INDEX,
                                "センサーが無効です"
                        );
                    }
                    sensorListAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * LED状態取得時のコールバック
         * @param value LED状態
         */
        @Override
        public void onReceivedLed(final LedValue value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String valueStr = "";
                    switch (value.status) {
                        case LedValue.LED_STATUS_OFF:
                            valueStr = "消灯";
                            break;
                        case LedValue.LED_STATUS_FLUSH:
                            valueStr = "点滅";
                            break;
                        case LedValue.LED_STATUS_ON:
                            valueStr = "点灯";
                            break;
                    }

                    sensorListAdapter.setSensorValue(
                            SENSOR_LIST_LED_INDEX,
                            String.format("%s", valueStr)
                    );

                    sensorListAdapter.setSettingValue(
                            SENSOR_LIST_LED_INDEX,
                            0,
                            getSettingIndex(
                                    SpinnerValues.LED_STATUS_VALUES,
                                    value.status,
                                    0)
                    );

                    sensorListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    /**
     * 設定リスト項目の生成
     */
    private void createDevicePreferenceList() {
        ArrayList<SettingValue> settingValues = new ArrayList<>();
        DevicePreferenceValue devicePreferenceValue
                = okudakeManager.getLatestDevicePreferenceValue();

        settingValues.add(
                new SettingValue(
                        "ビーコン送信間隔",
                        "秒",
                        SpinnerValues.BEACON_TRANSMITTING_INTERVALS,
                        getSettingIndex(
                                SpinnerValues.BEACON_TRANSMITTING_INTERVAL_VALUES,
                                devicePreferenceValue.beaconTransmittingInterval,
                                BEACON_TRANSMITTING_INTERVAL_DEF),
                        new DevicePreferenceListener(PREFERENCE_LIST_BEACON_TRANSMITTING_INDEX))
        );

        settingValues.add(
                new SettingValue(
                        "ビーコン更新間隔",
                        "秒",
                        SpinnerValues.BEACON_UPDATE_INTERVALS,
                        getSettingIndex(
                                SpinnerValues.BEACON_UPDATE_INTERVAL_VALUES,
                                devicePreferenceValue.beaconUpdatingInterval,
                                BEACON_UPDATE_INTERVAL_DEF),
                        new DevicePreferenceListener(PREFERENCE_LIST_BEACON_UPDATE_INDEX))
        );

        settingValues.add(
                new SettingValue(
                        "データ取得方法",
                        "",
                        SpinnerValues.READ_DATA_WAYS,
                        readDataWaySetting == ReadDataWay.READ ? 0 : 1,
                        new DevicePreferenceListener(PREFERENCE_LIST_READ_DATA_WAY_INDEX))
        );

        if (readDataWaySetting == ReadDataWay.READ) {
            settingValues.add(
                    new SettingValue(
                            "データ取得間隔",
                            "秒",
                            SpinnerValues.READ_DATA_INTERVALS,
                            getSettingIndex(
                                    SpinnerValues.READ_DATA_INTERVAL_VALUES,
                                    readDataIntervalSetting,
                                    READ_DATA_INTERVAL_DEF),
                            new DevicePreferenceListener(PREFERENCE_LIST_READ_DATA_UPDATE_INDEX))
            );
        }

        settingValues.add(
                new SettingValue(
                        "送信電力",
                        "dBm",
                        SpinnerValues.TX_POWER_LEVELS,
                        getSettingIndex(
                                SpinnerValues.TX_POWER_LEVEL_VALUES,
                                devicePreferenceValue.txPowerLevel,
                                TX_POWER_LEVEL_DEF),
                        new DevicePreferenceListener(PREFERENCE_LIST_TX_POWER_LEVEL_INDEX))
        );

        settingValues.add(
                new SettingValue(
                        "自動切断",
                        "分",
                        SpinnerValues.IDLE_TIMEOUTS,
                        getSettingIndex(
                                SpinnerValues.IDLE_TIMEOUT_VALUES,
                                devicePreferenceValue.idleTimeout,
                                IDLE_TIMEOUT_DEF),
                        new DevicePreferenceListener(PREFERENCE_LIST_IDLE_TIMEOUT_INDEX))
        );

        devicePreferenceListAdapter
                = new SettingListAdapter(getApplicationContext(), settingValues);
        devicePreferenceListView.setAdapter(devicePreferenceListAdapter);
    }

    /**
     * センサーリスト項目の生成
     */
    private void createSensorInfoList() {
        ArrayList<SensorValue> sensorValues = new ArrayList<>();
        ArrayList<List<SettingValue>> settingValues = new ArrayList<>();

        sensorValues.add(new SensorValue("温湿度", ""));
        settingValues.add(Arrays.asList(new SettingValue(
                "センサー更新間隔",
                "秒",
                SpinnerValues.SENSOR_UPDATE_INTERVALS,
                getSettingIndex(SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                        NOTIFICATION_THERMOHYGROMETER_PERIOD_DEF,
                        NOTIFICATION_THERMOHYGROMETER_PERIOD_DEF),
                new SensorSettingListener(SENSOR_LIST_THERMOHYGROMETER_INDEX)
        )));

        sensorValues.add(new SensorValue("照度", ""));
        settingValues.add(Arrays.asList(new SettingValue(
                "センサー更新間隔",
                "秒",
                SpinnerValues.SENSOR_UPDATE_INTERVALS,
                getSettingIndex(SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                        NOTIFICATION_ILLUMINOMETER_PERIOD_DEF,
                        NOTIFICATION_ILLUMINOMETER_PERIOD_DEF),
                new SensorSettingListener(SENSOR_LIST_ILLUMINOMETER_INDEX)
        )));

        sensorValues.add(new SensorValue("加速度", ""));
        settingValues.add(Arrays.asList(new SettingValue(
                "センサー更新間隔",
                "秒",
                SpinnerValues.SENSOR_UPDATE_INTERVALS,
                getSettingIndex(SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                        NOTIFICATION_ACCELEROMETER_PERIOD_DEF,
                        NOTIFICATION_ACCELEROMETER_PERIOD_DEF),
                new SensorSettingListener(SENSOR_LIST_ACCELEROMETER_INDEX)
        )));

        sensorValues.add(new SensorValue("磁気", ""));
        settingValues.add(Arrays.asList(new SettingValue(
                "",
                "",
                SpinnerValues.SENSOR_UPDATE_INTERVALS,
                0,
                new SensorSettingListener(SENSOR_LIST_MAGNETOMETER_INDEX)
        )));

        sensorValues.add(new SensorValue("給電状態", ""));
        settingValues.add(Arrays.asList(new SettingValue(
                "",
                "",
                SpinnerValues.SENSOR_UPDATE_INTERVALS,
                0,
                new SensorSettingListener(SENSOR_LIST_BATTERY_INDEX)
        )));

        sensorValues.add(new SensorValue("LED", ""));
        settingValues.add(Arrays.asList(new SettingValue(
                "LED点灯設定",
                "",
                SpinnerValues.LED_STATUS,
                0,
                new SensorSettingListener(SENSOR_LIST_LED_INDEX)
        )));

        sensorListAdapter
                = new SensorListAdapter(getApplicationContext(), sensorValues, settingValues);
        sensorInfoListView.setAdapter(sensorListAdapter);
    }

    /**
     * IFTTT設定リスト項目の生成
     */
    private void createIftttSettingList() {
        ArrayList<SettingValue> settingValues = new ArrayList<>();

        settingValues.add(
                new SettingValue(
                        "Value1",
                        "",
                        SpinnerValues.IFTTT_TEXTS,
                        AppSettings.iftttValue1,
                        new IftttSettingListener(IFTTT_LIST_VALUE1_INDEX))
        );

        settingValues.add(
                new SettingValue(
                        "Value2",
                        "",
                        SpinnerValues.IFTTT_TEXTS,
                        AppSettings.iftttValue2,
                        new IftttSettingListener(IFTTT_LIST_VALUE2_INDEX))
        );

        settingValues.add(
                new SettingValue(
                        "Value3",
                        "",
                        SpinnerValues.IFTTT_TEXTS,
                        AppSettings.iftttValue3,
                        new IftttSettingListener(IFTTT_LIST_VALUE3_INDEX))
        );

        iftttSettingListAdapter
                = new SettingListAdapter(getApplicationContext(), settingValues);
        iftttValuesListView.setAdapter(iftttSettingListAdapter);
    }

    /**
     * 設定値から配列でのindexを取得する
     * @param settings 設定値の配列
     * @param value 設定値
     * @param defaultValue 設定値が配列になかった場合に使用する設定値
     * @return 配列のindex
     */
    private int getSettingIndex(int[] settings, int value, int defaultValue) {
        int index = -1;
        int defIndex = -1;

        for (int i=0; i<settings.length; i++) {
            if (settings[i] == value) {
                index = i;
                break;
            }
            if (settings[i] == defaultValue) {
                defIndex = i;
            }
        }

        if (index < 0) {
            index = defIndex;
            if (index < 0) {
                index = 0;
            }
        }

        return index;
    }

    /**
     * 配列のindexの値をint型で取得する
     * @param values 値の配列
     * @param index 値の配列のindex
     * @param defaultValue indexが配列の範囲外だった場合に返す値
     * @return 配列の値
     */
    private int getSettingValue(int[] values, int index, int defaultValue) {
        int value = defaultValue;

        if ((index >= 0) && (index < values.length)) {
            value = values[index];
        }

        return value;
    }

    /**
     * おくだけセンサーの各センサーのNotification有効／無効を切り替える
     * @param enable Notificationの有効／無効
     */
    private void setNotification(boolean enable) {
        okudakeManager.setThermohygrometerNotification(enable);
        okudakeManager.setIlluminometerNotification(enable);
        okudakeManager.setAccelerometerNotification(enable);
        okudakeManager.setMagnetometerNotification(enable);
        okudakeManager.setBatteryNotification(enable);
    }

    /**
     * 設定リストのドロップダウンが変更された時のコールバック
     */
    private class DevicePreferenceListener extends ListViewSpinnerListener {

        /**
         * コンストラクタ
         * @param listIdx ListViewの行番号
         */
        public DevicePreferenceListener(int listIdx) {
            super(listIdx);
        }

        /**
         * Spinnerの項目が選択された時のコールバック
         * @param adapterView Spinner
         * @param view 選択された項目のView
         * @param position Spinnerで選択された項目のIndex
         * @param id 選択された項目のID
         */
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView, view, position, id);

            if (devicePreferenceListAdapter.getSetting(listIdx) != position) {
                devicePreferenceListAdapter.setSetting(listIdx, position);

                switch (listIdx) {
                    case PREFERENCE_LIST_BEACON_TRANSMITTING_INDEX:
                        int beaconTransmitting = getSettingValue(
                                SpinnerValues.BEACON_TRANSMITTING_INTERVAL_VALUES,
                                position,
                                BEACON_TRANSMITTING_INTERVAL_DEF
                        );
                        okudakeManager.setBeaconTransmittingInterval(beaconTransmitting);
                        break;
                    case PREFERENCE_LIST_BEACON_UPDATE_INDEX:
                        int beaconUpdate = getSettingValue(
                                SpinnerValues.BEACON_UPDATE_INTERVAL_VALUES,
                                position,
                                BEACON_UPDATE_INTERVAL_DEF
                        );
                        okudakeManager.setBeaconUpdatingInterval(beaconUpdate);
                        break;
                    case PREFERENCE_LIST_READ_DATA_WAY_INDEX:
                        ReadDataWay newReadDataWaySetting = ReadDataWay.READ;
                        if ((position >= 0)
                                && (position < SpinnerValues.READ_DATA_WAY_VALUES.length)) {
                            newReadDataWaySetting = SpinnerValues.READ_DATA_WAY_VALUES[position];
                        }
                        if (readDataWaySetting != newReadDataWaySetting) {
                            readDataWaySetting = newReadDataWaySetting;
                            createDevicePreferenceList();
                            if (readDataWaySetting == ReadDataWay.READ) {
                                setNotification(false);
                                readDataHandler.postDelayed(readDataRunnable, 100);
                            } else {
                                setNotification(true);
                            }
                        }
                        break;
                    case PREFERENCE_LIST_READ_DATA_UPDATE_INDEX:
                        readDataIntervalSetting = getSettingValue(
                                SpinnerValues.READ_DATA_INTERVAL_VALUES,
                                position,
                                READ_DATA_INTERVAL_DEF
                        );
                        if (readDataIntervalSetting == 0) {
                            readDataHandler.removeCallbacks(readDataRunnable);
                        } else {
                            readDataHandler.postDelayed(readDataRunnable, 100);
                        }
                        break;
                    case PREFERENCE_LIST_TX_POWER_LEVEL_INDEX:
                        int txPowerLevel = getSettingValue(
                                SpinnerValues.TX_POWER_LEVEL_VALUES,
                                position,
                                TX_POWER_LEVEL_DEF
                        );
                        okudakeManager.setTxPowerLevel(txPowerLevel);
                        break;
                    case PREFERENCE_LIST_IDLE_TIMEOUT_INDEX:
                        int idleTimeout = getSettingValue(
                                SpinnerValues.IDLE_TIMEOUT_VALUES,
                                position,
                                IDLE_TIMEOUT_DEF
                        );
                        okudakeManager.setIdleTimeout(idleTimeout);
                        break;
                }
            }
        }

        /**
         * Spinnerから選択した項目が消えた時のコールバック
         * @param adapterView Spinner
         */
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            super.onNothingSelected(adapterView);
        }
    }

    /**
     * センサー設定リストのドロップダウンが変更された時のコールバック
     */
    private class SensorSettingListener extends ListViewSpinnerListener {

        /**
         * コンストラクタ
         * @param listIdx ListViewの行番号
         */
        public SensorSettingListener(int listIdx) {
            super(listIdx);
        }

        /**
         * Spinnerの項目が選択された時のコールバック
         * @param adapterView Spinner
         * @param view 選択された項目のView
         * @param position Spinnerで選択された項目のIndex
         * @param id 選択された項目のID
         */
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView, view, position, id);

            if (sensorListAdapter.getSettingValue(listIdx, 0) != position) {
                sensorListAdapter.setSettingValue(listIdx, 0, position);

                switch (listIdx) {
                    case SENSOR_LIST_THERMOHYGROMETER_INDEX:
                        okudakeManager.setThermohygrometerPeriod(getSettingValue(
                                SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                                position,
                                NOTIFICATION_THERMOHYGROMETER_PERIOD_DEF
                        ));
                        break;
                    case SENSOR_LIST_ILLUMINOMETER_INDEX:
                        okudakeManager.setIlluminometerPeriod(getSettingValue(
                                SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                                position,
                                NOTIFICATION_ILLUMINOMETER_PERIOD_DEF
                        ));
                        break;
                    case SENSOR_LIST_ACCELEROMETER_INDEX:
                        okudakeManager.setAccelerometerPeriod(getSettingValue(
                                SpinnerValues.SENSOR_UPDATE_INTERVAL_VALUES,
                                position,
                                NOTIFICATION_ACCELEROMETER_PERIOD_DEF
                        ));
                        break;
                    case SENSOR_LIST_MAGNETOMETER_INDEX:
                        break;
                    case SENSOR_LIST_BATTERY_INDEX:
                        break;
                    case SENSOR_LIST_LED_INDEX:
                        byte ledValue = (byte) getSettingValue(
                                SpinnerValues.LED_STATUS_VALUES,
                                position,
                                LedValue.LED_STATUS_OFF
                        );
                        okudakeManager.setLedStatus(ledValue);
                        break;
                }
            }
        }

        /**
         * Spinnerから選択した項目が消えた時のコールバック
         * @param adapterView Spinner
         */
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            super.onNothingSelected(adapterView);
        }
    }

    /**
     * IFTTT設定リストのドロップダウンが変更された時のコールバック
     */
    private class IftttSettingListener extends ListViewSpinnerListener {

        /**
         * コンストラクタ
         * @param listIdx ListViewの行番号
         */
        public IftttSettingListener(int listIdx) {
            super(listIdx);
        }

        /**
         * Spinnerの項目が選択された時のコールバック
         * @param adapterView Spinner
         * @param view 選択された項目のView
         * @param position Spinnerで選択された項目のIndex
         * @param id 選択された項目のID
         */
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView, view, position, id);

            if (iftttSettingListAdapter.getSetting(listIdx) != position) {
                iftttSettingListAdapter.setSetting(listIdx, position);

                switch (listIdx) {
                    case IFTTT_LIST_VALUE1_INDEX:
                        AppSettings.iftttValue1 = getSettingValue(
                                SpinnerValues.IFTTT_VALUES,
                                position,
                                SpinnerValues.IFTTT_TEMPERATURE_INDEX
                        );
                        if (AppSettings.iftttValue1 == SpinnerValues.IFTTT_NOTHING_INDEX) {
                            iftttManager.setValue(IFTTTManager.IFTTT_VALUE1_INDEX, "");
                        }
                        break;
                    case IFTTT_LIST_VALUE2_INDEX:
                        AppSettings.iftttValue2 = getSettingValue(
                                SpinnerValues.IFTTT_VALUES,
                                position,
                                SpinnerValues.IFTTT_HUMIDITY_INDEX
                        );
                        if (AppSettings.iftttValue2 == SpinnerValues.IFTTT_NOTHING_INDEX) {
                            iftttManager.setValue(IFTTTManager.IFTTT_VALUE2_INDEX, "");
                        }
                        break;
                    case IFTTT_LIST_VALUE3_INDEX:
                        AppSettings.iftttValue3 = getSettingValue(
                                SpinnerValues.IFTTT_VALUES,
                                position,
                                SpinnerValues.IFTTT_ILLUMINANCE_INDEX
                        );
                        if (AppSettings.iftttValue3 == SpinnerValues.IFTTT_NOTHING_INDEX) {
                            iftttManager.setValue(IFTTTManager.IFTTT_VALUE3_INDEX, "");
                        }
                        break;
                }

                setIftttValue(
                        SpinnerValues.IFTTT_DEVICE_NAME_INDEX,
                        device.getName()
                );
            }

            AppSettings.saveSettings(getApplicationContext());
        }

        /**
         * Spinnerから選択した項目が消えた時のコールバック
         * @param adapterView Spinner
         */
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            super.onNothingSelected(adapterView);
        }
    }

    /**
     * Toastを表示
     * @param message
     */
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

}
