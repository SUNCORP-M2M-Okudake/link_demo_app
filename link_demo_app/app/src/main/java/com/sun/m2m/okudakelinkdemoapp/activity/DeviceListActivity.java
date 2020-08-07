/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.sun.m2m.okudakelinkdemoapp.R;
import com.sun.m2m.okudakelinkdemoapp.common.Common;
import com.sun.m2m.okudakelinkdemoapp.okudake.BeaconData;
import com.sun.m2m.okudakelinkdemoapp.okudake.DevicePreferenceValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.AccelerometerValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.BatteryValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.IlluminometerValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.LedValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.MagnetometerValue;
import com.sun.m2m.okudakelinkdemoapp.okudake.OkudakeManager;
import com.sun.m2m.okudakelinkdemoapp.okudake.OkudakeManagerListener;
import com.sun.m2m.okudakelinkdemoapp.okudake.ThermohygrometerValue;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * デバイス一覧画面
 */
public class DeviceListActivity extends AppCompatActivity {
    /** パーミッション要求時のCODE */
    private final int REQUEST_PERMISSION_CODE = 11;
    /** Bluetooth有効要求時のCODE */
    private final int REQUEST_ENABLE_BT = 22;

    /** IFTTT設定のButton */
    private Button iftttSettingButton;
    /** デバイス未検出時のTextView */
    private TextView notFoundTextView;
    /** デバイス一覧のListView */
    private ListView scannedDevicesListView;

    /** OkudakeManager */
    private OkudakeManager okudakeManager;
    /** 検出したデバイスリスト */
    private ArrayList<BeaconData> scannedDevices;
    /** ListViewに表示するデバイス名リスト */
    private ArrayList<String> scannedDeviceNames;
    /** ListViewのAdapter */
    private ArrayAdapter<String> scannedDeviceListAdapter;
    /** パーミッションが許可されているかどうか */
    private boolean permissionGranted;
    /** 遅延終了待ちかどうか */
    private boolean exiting;

    /**
     * onCreate
     * @param savedInstanceState SavedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);

        iftttSettingButton = findViewById(R.id.ifttt_setting_button);
        notFoundTextView = findViewById(R.id.not_found_textview);
        scannedDevicesListView = findViewById(R.id.scanneddevices_listview);

        iftttSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), IftttSettingActivity.class);
                startActivity(intent);
            }
        });

        scannedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Common.selectedOkudakeDevice = scannedDevices.get(i);
                Intent intent = new Intent(getApplication(), DeviceDetailActivity.class);
                startActivity(intent);
            }
        });

        scannedDevices = new ArrayList<>();
        scannedDeviceNames = new ArrayList<>();
        scannedDeviceListAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.listview_text_item,
                scannedDeviceNames
        );
        scannedDevicesListView.setAdapter(scannedDeviceListAdapter);

        ArrayList<String> permissionStrs = new ArrayList<>();
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissionStrs.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionStrs.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionStrs.size() > 0) {
            permissionGranted = false;
            String[] permissions = new String[permissionStrs.size()];
            permissionStrs.toArray(permissions);
            requestPermissions(permissions, REQUEST_PERMISSION_CODE);
        } else {
            permissionGranted = true;
        }

        exiting = false;
    }

    /**
     * onRequestPermissionsResult
     * パーミッション要求の結果取得時のコールバック
     * @param requestCode RequestCode
     * @param permissions 要求したパーミッション
     * @param grantResults パーミッション許可／不許可の結果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE)
        {
            boolean granted = true;
            for (int i=0; i<permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                }
            }
            if (granted) {
                permissionGranted = true;
            }

            initialize();
        }
    }

    /**
     * onActivityResult
     * Bluetooth許可要求の結果取得時のコールバック
     * @param requestCode RequestCode
     * @param resultCode Result
     * @param data Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                initialize();
            } else {
                showToast("could not change bluetooth to available.\nexit app.");
                exitDelay();
            }
        }
    }

    /**
     * onResume
     * Activity表示時のコールバック
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (!exiting) {
            if (permissionGranted) {
                initialize();
            }

            if (scannedDeviceListAdapter != null) {
                scannedDeviceListAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * onPause
     * Activity非表示時のコールバック
     */
    @Override
    protected void onPause() {
        startScanningHandler.removeCallbacks(startScanningRunnable);

        if (okudakeManager != null) {
            okudakeManager.stopScanning();
        }
        okudakeManager = null;

        super.onPause();
    }

    /**
     * onDestroy
     * Activity破棄時のコールバック
     */
    @Override
    protected void onDestroy() {
        startScanningHandler.removeCallbacks(startScanningRunnable);

        if (okudakeManager != null) {
            okudakeManager.stopScanning();
        }
        okudakeManager = null;

        super.onDestroy();
    }

    /**
     * Activity起動時の初期化処理
     */
    private void initialize() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("cannot use ble on this device.\nexit app.");
            exitDelay();
        }

        if (permissionGranted) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                showToast("this device is not supported bluetooth.\nnexit app.");
                exitDelay();
            } else {
                if (bluetoothAdapter.isEnabled()) {
                    if (okudakeManager == null) {
                        okudakeManager = OkudakeManager.getInstance(
                                getApplicationContext(),
                                okudakeManagerListener
                        );

                        startScanningHandler.postDelayed(startScanningRunnable, 1000);
                    }
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        } else {
            showToast("permission is not granted.\nexit app.");
            exitDelay();
        }
    }

    /**
     * ListViewに表示する文字列の生成
     * @param data ListViewに表示するBeaconData
     * @return ListViewに表示する文字列
     */
    private String getDeviceListText(BeaconData data) {
        return String.format("%s   TxPower:%ddBm", data.device.getName(), data.rssi);
    }

    /**
     * スキャンを開始するためのHandler
     */
    private Handler startScanningHandler = new Handler();

    /**
     * スキャンを開始するためのRunnable
     * 1秒毎にスキャンが開始されているかチェックしてスキャンがされていない場合はスキャン開始する
     */
    private Runnable startScanningRunnable = new Runnable() {
        @Override
        public void run() {
            startScanningHandler.removeCallbacks(startScanningRunnable);
            if (okudakeManager != null) {
                if (!okudakeManager.isScanning()) {
                    okudakeManager.startScanning();
                }
                startScanningHandler.postDelayed(startScanningRunnable, 1000);
            }
        }
    };

    /**
     * OkudakeManagerのコールバック
     */
    private OkudakeManagerListener okudakeManagerListener = new OkudakeManagerListener() {
        /**
         * おくだけセンサーのAdvertiseData取得時のコールバック
         * おくだけセンサーを検出したら随時ListViewへ追加する
         * @param data　検出したビーコンデータ
         */
        @Override
        public void onScannedDevice(final BeaconData data) {
            int idx = -1;
            for (int i=0; i<scannedDevices.size(); i++) {
                if (scannedDevices.get(i).serialNumber.equals(data.serialNumber)) {
                    idx = i;
                    break;
                }
            }

            if (idx >= 0) {
                scannedDeviceNames.set(idx, getDeviceListText(data));
                scannedDevices.set(idx, data);
            } else {
                scannedDeviceNames.add(getDeviceListText(data));
                scannedDevices.add(data);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scannedDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         * おくだけセンサーのスキャン結果を定期的に返す時のコールバック
         * ListView全体を更新することにより、検出できなくなったおくだけセンサーをListViewから除外する
         * @param datas 検出したビーコンデータ
         */
        @Override
        public void onScannedDevices(List<BeaconData> datas) {
            updateList(datas);
        }

        /**
         * おくだけセンサーに接続された時のコールバック
         */
        @Override
        public void onConnected() {

        }

        /**
         * おくだけセンサーとの接続切断時のコールバック
         */
        @Override
        public void onDisconnected() {

        }

        /**
         * 端末設定データ取得時のコールバック
         * @param value 端末設定データ
         */
        @Override
        public void onReceivedDevicePreferences(DevicePreferenceValue value) {

        }

        /**
         * 温湿度センサーデータ取得時のコールバック
         * @param value 温湿度センサーデータ
         */
        @Override
        public void onReceivedThermohygrometer(ThermohygrometerValue value) {

        }

        /**
         * 照度センサーデータ取得時のコールバック
         * @param value 照度センサーデータ
         */
        @Override
        public void onReceivedIlluminometer(IlluminometerValue value) {

        }

        /**
         * 加速度センサーデータ取得時のコールバック
         * @param value 加速度センサーデータ
         */
        @Override
        public void onReceivedAccelerometer(AccelerometerValue value) {

        }

        /**
         * 磁気センサーデータ取得時のコールバック
         * @param value 磁気センサーデータ
         */
        @Override
        public void onReceivedMagnetometer(MagnetometerValue value) {

        }

        /**
         * バッテリーデータ取得時のコールバック
         * @param value バッテリーデータ
         */
        @Override
        public void onReceivedBattery(BatteryValue value) {

        }

        /**
         * LED状態取得時のコールバック
         * @param value LED状態
         */
        @Override
        public void onReceivedLed(LedValue value) {

        }
    };

    /**
     * ListViewの表示を更新
     * @param devices 表示するBeaconData
     */
    private void updateList(final List<BeaconData> devices) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scannedDeviceNames.clear();
                scannedDevices.clear();
                scannedDeviceListAdapter.notifyDataSetChanged();

                if (devices != null) {
                    scannedDevicesListView.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);

                    for (final BeaconData deviceInfo : devices) {
                        scannedDeviceNames.add(getDeviceListText(deviceInfo));
                        scannedDevices.add(deviceInfo);
                        scannedDeviceListAdapter.notifyDataSetChanged();
                    }

                    if (scannedDevices.size() > 0) {
                        scannedDevicesListView.setVisibility(View.VISIBLE);
                        notFoundTextView.setVisibility(View.INVISIBLE);
                    } else {
                        scannedDevicesListView.setVisibility(View.INVISIBLE);
                        notFoundTextView.setVisibility(View.VISIBLE);
                    }

                } else {
                    scannedDevicesListView.setVisibility(View.INVISIBLE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 2秒後にアプリを終了
     */
    private void exitDelay() {
        exiting = true;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
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
