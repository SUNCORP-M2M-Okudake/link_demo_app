/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import com.sun.m2m.okudakelinkdemoapp.okudake.ble.BleDeviceInfo;
import com.sun.m2m.okudakelinkdemoapp.okudake.ble.BleManager;
import com.sun.m2m.okudakelinkdemoapp.okudake.ble.BleManagerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * おくだけセンサーのセンサーデータ受信及び設定送信を行うためのクラス
 */
public class OkudakeManager {
    /** ログ出力のTAG */
    private final String TAG = "OkudakeManager";

    /** デバイス設定　ServiceのUUID */
    private static final UUID DEVICE_PREFERENCES_SERVICE_UUID = UUID.fromString("4867e90c-6366-4bbf-5e4a-abb82b515446");
    /** デバイス設定　無通信タイムアウト　CharacteristicのUUID */
    private static final UUID DEVICE_CHARACTERISTIC_IDLE_TIMEOUT_UUID = UUID.fromString("49cbdc62-5f37-4412-80f6-27890adaa2d4");
    /** デバイス設定　ビーコン送信間隔　CharacteristicのUUID */
    private static final UUID DEVICE_CHARACTERISTIC_BEACON_TANSMITTING_INTERVAL_UUID = UUID.fromString("d8351cbe-ba95-4066-8423-9eee6c71472e");
    /** デバイス設定　ビーコンデータ更新間隔　CharacteristicのUUID */
    private static final UUID DEVICE_CHARACTERISTIC_BEACON_UPDATING_INTERVAL_UUID = UUID.fromString("53a54d50-7baf-4027-8f41-39c92fd89358");
    /** デバイス設定　送信出力　CharacteristicのUUID */
    private static final UUID DEVICE_CHARACTERISTIC_TX_POWER_LEVEL_UUID = UUID.fromString("109cf8a7-863e-4123-9d34-b462ace512d8");

    /** 温湿度センサー　ServiceのUUID */
    private static final UUID THERMOHYGROMETER_SERVICE_UUID = UUID.fromString("4ea70e4e-f107-4428-9863-c2ccd3d41bb0");
    /** 温湿度センサー　データ　CharacteristicのUUID */
    private static final UUID THERMOHYGROMETER_CHARACTERISTIC_DATA_UUID = UUID.fromString("f94517ff-aa55-427c-ab19-33ca5dfec192");
    /** 温湿度センサー　有効／無効　CharacteristicのUUID */
    private static final UUID THERMOHYGROMETER_CHARACTERISTIC_ENABLE_UUID = UUID.fromString("b0cc0a99-a8b2-4f80-8095-472d7234bfc8");
    /** 温湿度センサー　更新間隔　CharacteristicのUUID */
    private static final UUID THERMOHYGROMETER_CHARACTERISTIC_PERIOD_UUID = UUID.fromString("a8914c08-f8d1-4152-8b7d-18429226d6c0");

    /** 照度センサー　ServiceのUUID */
    private static final UUID ILLUMINOMETER_SERVICE_UUID = UUID.fromString("dc209ab9-f531-4171-b7bc-7473b0ddd600");
    /** 照度センサー　データ　CharacteristicのUUID */
    private static final UUID ILLUMINOMETER_CHARACTERISTIC_DATA_UUID = UUID.fromString("64315206-83f8-4d36-893a-ba458f4eb76e");
    /** 照度センサー　有効／無効　CharacteristicのUUID */
    private static final UUID ILLUMINOMETER_CHARACTERISTIC_ENABLE_UUID = UUID.fromString("021c84ca-7e21-47ba-a778-50a92e18b91a");
    /** 照度センサー　更新間隔　CharacteristicのUUID */
    private static final UUID ILLUMINOMETER_CHARACTERISTIC_PERIOD_UUID = UUID.fromString("fa90a747-267d-47f6-8ecd-2bf586d4467f");

    /** 加速度センサー　ServiceのUUID */
    private static final UUID ACCELEROMETER_SERVICE_UUID = UUID.fromString("24eaebfe-d7fe-425a-8e7f-3cadbb1d1879");
    /** 加速度センサー　データ　CharacteristicのUUID */
    private static final UUID ACCELEROMETER_CHARACTERISTIC_DATA_UUID = UUID.fromString("57cc3b5c-b5ac-4d3d-ad6a-36ec1392502a");
    /** 加速度センサー　有効／無効　CharacteristicのUUID */
    private static final UUID ACCELEROMETER_CHARACTERISTIC_ENABLE_UUID = UUID.fromString("ee7edab2-da00-4545-8ede-b85713dc55d6");
    /** 加速度センサー　更新間隔　CharacteristicのUUID */
    private static final UUID ACCELEROMETER_CHARACTERISTIC_PERIOD_UUID = UUID.fromString("7cf84ebf-d8d9-42f0-9d89-711d9c919a1b");

    /** 磁気センサー　ServiceのUUID */
    private static final UUID MAGNETOMETER_SERVICE_UUID = UUID.fromString("c324e6a8-feb8-472e-a202-eecf7bc8c14c");
    /** 磁気センサー　データ　CharacteristicのUUID */
    private static final UUID MAGNETOMETER_CHARACTERISTIC_DATA_UUID = UUID.fromString("84d3d46f-c936-4edb-8b5d-e10124e04f28");
    /** 磁気センサー　有効／無効　CharacteristicのUUID */
    private static final UUID MAGNETOMETER_CHARACTERISTIC_ENABLE_UUID = UUID.fromString("77b89044-5c7c-4fb5-a3d9-11c745537a9a");

    /** バッテリー　ServiceのUUID */
    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("7b420b63-9c23-4047-922f-e8caeb27273f");
    /** バッテリー　電池残量　CharacteristicのUUID */
    private static final UUID BATTERY_CHARACTERISTIC_BATTERY_LEVEL_UUID = UUID.fromString("98da9d54-ce70-4718-841b-e8f1196d6b17");
    /** バッテリー　電池残量監視状態　CharacteristicのUUID */
    private static final UUID BATTERY_CHARACTERISTIC_ENABLE_UUID = UUID.fromString("949724da-0d2d-4f2e-88a4-75b7a7341c3d");
    /** バッテリー　USB接続状態　CharacteristicのUUID */
    private static final UUID BATTERY_CHARACTERISTIC_USB_PLUGGED_UUID = UUID.fromString("406b724e-3176-425f-9a68-8532e4c3e0c8");

    /** LED　ServiceのUUID */
    private static final UUID LED_SERVICE_UUID = UUID.fromString("96ef744d-3075-43ce-92b6-42b1745ac4d6");
    /** LEDステータス　CharacteristicのUUID */
    private static final UUID LED_CHARACTERISTIC_STATUS_UUID = UUID.fromString("9b93e645-7b89-4c97-9852-a406762203af");

    /** センサーが有効な場合のStatus */
    private static final int SENSOR_STATUS_AVAILABLE = 0x00;
    /** センサーが無効な場合のStatus */
    private static final int SENSOR_STATUS_NOT_AVAILABLE = 0x02;

    /** ペアリングのタイムアウト時間（ミリ秒） */
    private final int PAIRING_TIMEOUT = 5000;
    /** ペアリングに使用するPIN */
    private final byte[] PAIRING_PIN = {0, 0, 0, 0};

    /** OkudakeManagerのインスタンス */
    private static OkudakeManager instance;

    /** BleManager */
    private BleManager bleManager;
    /** 接続中デバイスがペアリング済みか */
    private boolean connectingDeviceBonded;
    /** 接続中のデバイス */
    private BluetoothDevice connectingDevice;
    /** OkudakeManagerから呼ばれるコールバック */
    private OkudakeManagerListener okudakeManagerListener;
    /** BLE機器のスキャン結果を返す間隔 */
    private int scanInterval;

    /** 最新の端末設定情報 */
    private DevicePreferenceValue currentDevicePreferenceValue;
    /** 最新の温湿度センサー情報 */
    private ThermohygrometerValue currentThermohygrometerValue;
    /** 最新の照度センサー情報 */
    private IlluminometerValue currentIlluminometerValue;
    /** 最新の加速度センサー情報 */
    private AccelerometerValue currentAccelerometerValue;
    /** 最新の磁気センサー情報 */
    private MagnetometerValue currentMagnetometerValue;
    /** 最新のバッテリー情報 */
    private BatteryValue currentBatteryValue;
    /** 最新のLED情報 */
    private LedValue currentLedValue;

    /**
     * OkudakeManagerのインスタンスを返す
     * OkudakeManagerのインスタンスを作成する場合は必ずgetInstanceメソッドを使用する
     * @param context　アプリのContext
     * @param listener　OkudakeManagerのコールバック
     * @return OkudakeManagerのインスタンス
     */
    public static OkudakeManager getInstance(Context context, OkudakeManagerListener listener) {
        if (instance == null) {
            instance = new OkudakeManager(context);
        }
        instance.okudakeManagerListener = listener;
        return instance;
    }

    /**
     * コンストラクタ
     * @param context　アプリのContext
     */
    public OkudakeManager(Context context) {
        this.bleManager = BleManager.getInstance(context, bleManagerListener);

        // おくだけセンサーのビーコン送信間隔が最大4秒くらいなので、スキャン結果リストの更新は5秒毎に行う
        this.scanInterval = 5000;

        this.currentDevicePreferenceValue = new DevicePreferenceValue();
        this.currentThermohygrometerValue = new ThermohygrometerValue();
        this.currentIlluminometerValue = new IlluminometerValue();
        this.currentAccelerometerValue = new AccelerometerValue();
        this.currentMagnetometerValue = new MagnetometerValue();
        this.currentBatteryValue = new BatteryValue();
        this.currentLedValue = new LedValue();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * おくだけセンサーに接続中かどうか
     * @return 接続中の場合はTrue
     */
    public boolean isConnected() {
        return bleManager.isConnected();
    }

    /**
     * おくだけセンサーのスキャン中かどうか
     * @return スキャン中の場合はTrue
     */
    public boolean isScanning() {
        return bleManager.isScanning();
    }

    /**
     * 周辺のおくだけセンサーのスキャンを開始する
     */
    public void startScanning() {
        if (!bleManager.isScanning()) {
            bleManager.startScan(scanInterval);
        }
    }

    /**
     * おくだけセンサーのスキャンを終了する
     */
    public void stopScanning() {
        if (bleManager.isScanning()) {
            bleManager.stopScan();
        }
    }

    /**
     * スキャンして検出したおくだけセンサーのリストを返す
     * @return 検出したおくだけセンサーのリスト
     */
    public List<BeaconData> getScannedDevices() {
        List<BleDeviceInfo> devices = bleManager.getDevices();
        ArrayList<BeaconData> okudakeDevices = new ArrayList<>();

        for(BleDeviceInfo deviceInfo : devices) {
            if (isOkudakeSensor(deviceInfo.manufactureSpecificData)) {
                okudakeDevices.add(new BeaconData(
                        deviceInfo.device,
                        deviceInfo.dateTime,
                        deviceInfo.rssi,
                        deviceInfo.txPower,
                        deviceInfo.manufactureSpecificData
                ));
            }
        }

        if (okudakeDevices.size() == 0) {
            return null;
        } else {
            return okudakeDevices;
        }
    }

    /**
     * おくだけセンサーから取得した値をリセットする
     */
    public void resetValues() {
        currentDevicePreferenceValue.resetValue();
        currentThermohygrometerValue.resetValue();
        currentIlluminometerValue.resetValue();
        currentAccelerometerValue.resetValue();
        currentMagnetometerValue.resetValue();
        currentBatteryValue.resetValue();
        currentLedValue.resetValue();
    }

    /**
     * おくだけセンサーに接続する
     * 既に他の端末と接続中、もしくは、ペアリングの設定がされていない場合は接続できない
     * @param device　接続する端末
     * @return 接続の開始に成功した場合はTrue
     */
    public boolean connect(BluetoothDevice device) {
        if ((device != null) && !bleManager.isConnected()) {
            connectingDevice = device;
            resetValues();

            connectingDeviceBonded = isBonded(device);
            if (connectingDeviceBonded) {
                addLog("connect to device");
                bleManager.connect(device);
            } else {
                addLog("create bond before connecting device");
                pairingTimeoutHandler.postDelayed(pairingTimeoutRunnable, PAIRING_TIMEOUT);
                device.createBond();
            }

            return true;
        }
        return false;
    }

    /**
     * おくだけセンサーとの接続を切断する
     */
    public void disconnect() {
        if (bleManager.isConnected()) {
            bleManager.disconnect();
        }
        resetValues();
    }

    /**
     * 接続中のおくだけセンサーの信号強度（RSSI）を返す
     * @return RSSI
     */
    public int getConnectedRssi() {
        if (bleManager.isConnected()) {
            return bleManager.getConnectedDeviceRssi();
        } else {
            return 0;
        }
    }

    /**
     * おくだけセンサーから取得した最新の設定情報を返す
     * @return 設定情報
     */
    public DevicePreferenceValue getLatestDevicePreferenceValue() {
        return currentDevicePreferenceValue.clone();
    }

    /**
     * おくだけセンサーから取得した最新の温湿度データを返す
     * @return 温湿度データ
     */
    public ThermohygrometerValue getLatestThermohygrometerValue() {
        return currentThermohygrometerValue.clone();
    }

    /**
     * おくだけセンサーから取得した最新の照度データを返す
     * @return 照度データ
     */
    public IlluminometerValue getLatestIlluminometerValue() {
        return currentIlluminometerValue.clone();
    }

    /**
     * おくだけセンサーから取得した最新の加速度データを返す
     * @return 加速度データ
     */
    public AccelerometerValue getLatestAccelerometerValue() {
        return currentAccelerometerValue.clone();
    }

    /**
     * おくだけセンサーから取得した最新の磁気データを返す
     * @return 磁気データ
     */
    public MagnetometerValue getLatestMagnetometerValue() {
        return currentMagnetometerValue.clone();
    }

    /**
     * おくだけセンサーから取得した最新の電池データを返す
     * @return 電池データ
     */
    public BatteryValue getLatestBatteryValue() {
        return currentBatteryValue.clone();
    }

    /**
     * おくだけセンサーから取得した最新のLEDの状態を返す
     * @return LEDの状態
     */
    public LedValue getLatestLedValue() {
        return currentLedValue.clone();
    }

    /**
     * おくだけセンサーの無通信時に自動切断するまでの時間（分）を設定する
     * 設定できる値は0～1440
     * 0の場合は自動切断しない
     * @param value 自動切断するまでの時間（分）
     */
    public void setIdleTimeout(int value) {
        if (bleManager.isConnected()) {
            if ((value >= DevicePreferenceValue.MIN_IDLE_TIMEOUT)
                    && (value <= DevicePreferenceValue.MAX_IDLE_TIMEOUT)) {
                byte[] data = Utility.intToUnsignedShortBytes(value);
                bleManager.writeData(
                        DEVICE_PREFERENCES_SERVICE_UUID,
                        DEVICE_CHARACTERISTIC_IDLE_TIMEOUT_UUID,
                        data
                );
            }
        }
    }

    /**
     * おくだけセンサーから無通信タイムアウト（分）の設定情報を取得する
     * 結果はonReceivedDevicePreferencesで取得する
     */
    public void getIdleTimeout() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    DEVICE_PREFERENCES_SERVICE_UUID,
                    DEVICE_CHARACTERISTIC_IDLE_TIMEOUT_UUID
            );
        }
    }

    /**
     * おくだけセンサーのビーコン送信間隔（ミリ秒）を設定する
     * 設定できる値は160～4096
     * @param value　ビーコン送信間隔（ミリ秒）
     */
    public void setBeaconTransmittingInterval(int value) {
        if (bleManager.isConnected()) {
            if ((value >= DevicePreferenceValue.MIN_BEACON_TRANSMITTING_INTERVAL)
                    && (value <= DevicePreferenceValue.MAX_BEACON_TRANSMITTING_INTERVAL)) {
                byte[] data = Utility.intToUnsignedShortBytes(value);
                bleManager.writeData(
                        DEVICE_PREFERENCES_SERVICE_UUID,
                        DEVICE_CHARACTERISTIC_BEACON_TANSMITTING_INTERVAL_UUID,
                        data
                );
            }
        }
    }

    /**
     * おくだけセンサーのビーコン送信間隔（ミリ秒）の設定情報を取得する
     * 結果はonReceivedDevicePreferencesで取得する
     */
    public void getBeaconTransmittingInterval() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    DEVICE_PREFERENCES_SERVICE_UUID,
                    DEVICE_CHARACTERISTIC_BEACON_TANSMITTING_INTERVAL_UUID
            );
        }
    }

    /**
     * おくだけセンサーのビーコン更新間隔（秒）を設定する
     * 設定できる値は1～60
     * @param value　ビーコン更新間隔（秒）
     */
    public void setBeaconUpdatingInterval(int value) {
        if (bleManager.isConnected()) {
            if ((value >= DevicePreferenceValue.MIN_BEACON_UPDATING_INTERVAL)
                    && (value <= DevicePreferenceValue.MAX_BEACON_UPDATING_INTERVAL)) {
                byte[] data = new byte[1];
                data[0] = (byte)value;
                bleManager.writeData(
                        DEVICE_PREFERENCES_SERVICE_UUID,
                        DEVICE_CHARACTERISTIC_BEACON_UPDATING_INTERVAL_UUID,
                        data
                );
            }
        }
    }

    /**
     * おくだけセンサーのビーコン更新間隔（秒）の設定情報を取得する
     * 結果はonReceivedDevicePreferencesで取得する
     */
    public void getBeaconUpdatingInterval() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    DEVICE_PREFERENCES_SERVICE_UUID,
                    DEVICE_CHARACTERISTIC_BEACON_UPDATING_INTERVAL_UUID
            );
        }
    }

    /**
     * おくだけセンサーの送信出力（dBm）を設定する
     * 設定できる値は-200～70
     * @param value 送信出力（dBm）
     */
    public void setTxPowerLevel(int value) {
        if (bleManager.isConnected()) {
            if ((value >= DevicePreferenceValue.MIN_TX_POWER_LEVEL)
                    && (value <= DevicePreferenceValue.MAX_TX_POWER_LEVEL)) {
                addLog(String.format("set tx power %d", value));
                byte[] data = Utility.intToShortBytes((short)value);
                bleManager.writeData(
                        DEVICE_PREFERENCES_SERVICE_UUID,
                        DEVICE_CHARACTERISTIC_TX_POWER_LEVEL_UUID,
                        data
                );
            }
        }
    }

    /**
     * おくだけセンサーの送信出力（dBm）の設定情報を取得する
     * 結果はonReceivedDevicePreferencesで取得する
     */
    public void getTxPowerLevel() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    DEVICE_PREFERENCES_SERVICE_UUID,
                    DEVICE_CHARACTERISTIC_TX_POWER_LEVEL_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに加速度センサーのNotification有効／無効を設定する
     * Notificationを有効にするとsetAccelerometerPeriodで設定した更新間隔ごとに
     * onReceivedAccelerometerで加速度データが取得できる
     * @param enable　Notificationの有効／無効
     */
    public void setAccelerometerNotification(boolean enable) {
        if (bleManager.isConnected()) {
            bleManager.setNotification(
                    ACCELEROMETER_SERVICE_UUID,
                    ACCELEROMETER_CHARACTERISTIC_DATA_UUID,
                    enable
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに加速度センサーのデータを要求する
     * 結果はonReceivedAccelerometerで返ってくる
     */
    public void getAccelerometerValue() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    ACCELEROMETER_SERVICE_UUID,
                    ACCELEROMETER_CHARACTERISTIC_DATA_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに加速度センサーの有効／無効を設定する
     * @param enable　加速度センサーの有効／無効
     */
    public void setAccelerometerAvailable(boolean enable) {
        if (bleManager.isConnected()) {
            byte[] data = new byte[1];
            if (enable) data[0] = 1;
            else data[0] = 0;
            bleManager.writeData(
                    ACCELEROMETER_SERVICE_UUID,
                    ACCELEROMETER_CHARACTERISTIC_ENABLE_UUID,
                    data
            );

            currentAccelerometerValue.enable = enable;
        }
    }

    /**
     * 接続中のおくだけセンサーに加速度センサーの有効／無効の状態取得を要求する
     * 結果はonReceivedAccelerometerで返ってくる
     */
    public void getAccelerometerAvailable() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    ACCELEROMETER_SERVICE_UUID,
                    ACCELEROMETER_CHARACTERISTIC_ENABLE_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに加速度センサーのデータ更新間隔（ミリ秒）を設定する
     * Notificationを有効にした時のデータ更新間隔
     * 設定できる範囲は100～60000
     * @param milliSec　データ更新間隔（ミリ秒）
     */
    public void setAccelerometerPeriod(int milliSec) {
        if ((milliSec < AccelerometerValue.MIN_PERIOD)
                || (milliSec > AccelerometerValue.MAX_PERIOD)) {
            return;
        }

        if (bleManager.isConnected()) {
            byte[] data = Utility.intToUnsignedShortBytes(milliSec);
            bleManager.writeData(
                    ACCELEROMETER_SERVICE_UUID,
                    ACCELEROMETER_CHARACTERISTIC_PERIOD_UUID,
                    data
            );

            currentAccelerometerValue.period = milliSec;
        }
    }

    /**
     * 接続中のおくだけセンサーに加速度センサーのデータ更新間隔の設定値取得を要求する
     * Notificationを有効にした時のデータ更新間隔
     * 結果はonReceivedAccelerometerで返ってくる
     */
    public void getAccelerometerPeriod() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    ACCELEROMETER_SERVICE_UUID,
                    ACCELEROMETER_CHARACTERISTIC_PERIOD_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに温湿度センサーのNotification有効／無効を設定する
     * Notificationを有効にするとsetThermohygrometerPeriodで設定した更新間隔ごとに
     * onReceivedThermohygrometerで温湿度データが取得できる
     * @param enable　Notificationの有効／無効
     */
    public void setThermohygrometerNotification(boolean enable) {
        if (bleManager.isConnected()) {
            bleManager.setNotification(
                    THERMOHYGROMETER_SERVICE_UUID,
                    THERMOHYGROMETER_CHARACTERISTIC_DATA_UUID,
                    enable
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに温湿度センサーのデータを要求する
     * 結果はonReceivedThermohygrometerで返ってくる
     */
    public void getThermohygrometerValue() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    THERMOHYGROMETER_SERVICE_UUID,
                    THERMOHYGROMETER_CHARACTERISTIC_DATA_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに温湿度センサーの有効／無効を設定する
     * @param enable　温湿度センサーの有効／無効
     */
    public void setThermohygrometerAvailable(boolean enable) {
        if (bleManager.isConnected()) {
            byte[] data = new byte[1];
            if (enable) data[0] = 1;
            else data[0] = 0;
            bleManager.writeData(
                    THERMOHYGROMETER_SERVICE_UUID,
                    THERMOHYGROMETER_CHARACTERISTIC_ENABLE_UUID,
                    data
            );

            currentThermohygrometerValue.enable = enable;
        }
    }

    /**
     * 接続中のおくだけセンサーに温湿度センサーの有効／無効の状態取得を要求する
     * 結果はonReceivedThermohygrometerで返ってくる
     */
    public void getThermohygrometerAvailable() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    THERMOHYGROMETER_SERVICE_UUID,
                    THERMOHYGROMETER_CHARACTERISTIC_ENABLE_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに温湿度センサーのデータ更新間隔（ミリ秒）を設定する
     * Notificationを有効にした時のデータ更新間隔
     * 設定できる範囲は100～60000
     * @param milliSec　データ更新間隔（ミリ秒）
     */
    public void setThermohygrometerPeriod(int milliSec) {
        if ((milliSec < ThermohygrometerValue.MIN_PERIOD)
                || (milliSec > ThermohygrometerValue.MAX_PERIOD)) {
            return;
        }

        if (bleManager.isConnected()) {
            byte[] data = Utility.intToUnsignedShortBytes(milliSec);
            bleManager.writeData(
                    THERMOHYGROMETER_SERVICE_UUID,
                    THERMOHYGROMETER_CHARACTERISTIC_PERIOD_UUID,
                    data
            );

            currentThermohygrometerValue.period = milliSec;
        }
    }

    /**
     * 接続中のおくだけセンサーに温湿度センサーのデータ更新間隔の設定値取得を要求する
     * Notificationを有効にした時のデータ更新間隔
     * 結果はonReceivedThermohygrometerで返ってくる
     */
    public void getThermohygrometerPeriod() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    THERMOHYGROMETER_SERVICE_UUID,
                    THERMOHYGROMETER_CHARACTERISTIC_PERIOD_UUID
            );
        }
    }


    /**
     * 接続中のおくだけセンサーに照度センサーのNotification有効／無効を設定する
     * Notificationを有効にするとsetIlluminometerPeriodで設定した更新間隔ごとに
     * onReceivedIlluminometerで照度データが取得できる
     * @param enable Notification有効／無効
     */
    public void setIlluminometerNotification(boolean enable) {
        if (bleManager.isConnected()) {
            bleManager.setNotification(
                    ILLUMINOMETER_SERVICE_UUID,
                    ILLUMINOMETER_CHARACTERISTIC_DATA_UUID,
                    enable
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに照度センサーのデータを要求する
     * 結果はonReceivedIlluminometerで返ってくる
     */
    public void getIlluminometerValue() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    ILLUMINOMETER_SERVICE_UUID,
                    ILLUMINOMETER_CHARACTERISTIC_DATA_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに照度センサーの有効／無効を設定する
     * @param enable 照度センサーの有効／無効
     */
    public void setIlluminometerAvailable(boolean enable) {
        if (bleManager.isConnected()) {
            byte[] data = new byte[1];
            if (enable) data[0] = 1;
            else data[0] = 0;
            bleManager.writeData(
                    ILLUMINOMETER_SERVICE_UUID,
                    ILLUMINOMETER_CHARACTERISTIC_ENABLE_UUID,
                    data
            );

            currentIlluminometerValue.enable = enable;
        }
    }

    /**
     * 接続中のおくだけセンサーに照度センサーの有効／無効の状態取得を要求する
     * 結果はonReceivedIlluminometerで返ってくる
     */
    public void getIlluminometerAvailable() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    ILLUMINOMETER_SERVICE_UUID,
                    ILLUMINOMETER_CHARACTERISTIC_ENABLE_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに照度センサーのデータ更新間隔（ミリ秒）を設定する
     * Notificationを有効にした時のデータ更新間隔
     * 設定できる範囲は200～60000
     * @param milliSec データ更新間隔（ミリ秒）
     */
    public void setIlluminometerPeriod(int milliSec) {
        if ((milliSec < IlluminometerValue.MIN_PERIOD)
                || (milliSec > IlluminometerValue.MAX_PERIOD)) {
            return;
        }

        if (bleManager.isConnected()) {
            byte[] data = Utility.intToUnsignedShortBytes(milliSec);
            bleManager.writeData(
                    ILLUMINOMETER_SERVICE_UUID,
                    ILLUMINOMETER_CHARACTERISTIC_PERIOD_UUID,
                    data
            );

            currentIlluminometerValue.period = milliSec;
        }
    }

    /**
     * 接続中のおくだけセンサーに照度センサーのデータ更新間隔の設定値取得を要求する
     * Notificationを有効にした時のデータ更新間隔
     * 結果はonReceivedIlluminometerで返ってくる
     */
    public void getIlluminometerPeriod() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    ILLUMINOMETER_SERVICE_UUID,
                    ILLUMINOMETER_CHARACTERISTIC_PERIOD_UUID
            );
        }
    }


    /**
     * 接続中のおくだけセンサーに磁気センサーのNotification有効／無効を設定する
     * onReceivedMagnetometerで磁気データが取得できる
     * @param enable Notification有効／無効
     */
    public void setMagnetometerNotification(boolean enable) {
        if (bleManager.isConnected()) {
            bleManager.setNotification(
                    MAGNETOMETER_SERVICE_UUID,
                    MAGNETOMETER_CHARACTERISTIC_DATA_UUID,
                    enable
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに磁気センサーのデータを要求する
     * 結果はonReceivedMagnetometerで返ってくる
     */
    public void getMagnetometerValue() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    MAGNETOMETER_SERVICE_UUID,
                    MAGNETOMETER_CHARACTERISTIC_DATA_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに磁気センサーの有効／無効を設定する
     * @param enable 磁気センサーの有効／無効
     */
    public void setMagnetometerAvailable(boolean enable) {
        if (bleManager.isConnected()) {
            byte[] data = new byte[1];
            if (enable) data[0] = 1;
            else data[0] = 0;
            bleManager.writeData(
                    MAGNETOMETER_SERVICE_UUID,
                    MAGNETOMETER_CHARACTERISTIC_ENABLE_UUID,
                    data
            );

            currentMagnetometerValue.enable = enable;
        }
    }

    /**
     * 接続中のおくだけセンサーに磁気センサーの有効／無効の状態取得を要求する
     * 結果はonReceivedMagnetometerで返ってくる
     */
    public void getMagnetometerAvailable() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    MAGNETOMETER_SERVICE_UUID,
                    MAGNETOMETER_CHARACTERISTIC_ENABLE_UUID
            );
        }
    }


    /**
     * 接続中のおくだけセンサーにバッテリーのNotification有効／無効を設定する
     * onReceivedBatteryでバッテリーデータが取得できる
     * @param enable Notification有効／無効
     */
    public void setBatteryNotification(boolean enable) {
        if (bleManager.isConnected()) {
            bleManager.setNotification(
                    BATTERY_SERVICE_UUID,
                    BATTERY_CHARACTERISTIC_BATTERY_LEVEL_UUID,
                    enable
            );
        }
    }

    /**
     * 接続中のおくだけセンサーにバッテリーのデータを要求する
     * 結果はonReceivedBatteryで返ってくる
     */
    public void getBatteryValue() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    BATTERY_SERVICE_UUID,
                    BATTERY_CHARACTERISTIC_BATTERY_LEVEL_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーにUSB接続状態を要求する
     * 結果はonReceivedBatteryで返ってくる
     */
    public void getBatteryUsbPlugged() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    BATTERY_SERVICE_UUID,
                    BATTERY_CHARACTERISTIC_USB_PLUGGED_UUID
            );
        }
    }

    /**
     * 接続中のおくだけセンサーに電池残量監視の有効／無効を設定する
     * @param enable 池残量監視の有効／無効
     */
    public void setBatteryAvailable(boolean enable) {
        if (bleManager.isConnected()) {
            byte[] data = new byte[1];
            if (enable) data[0] = 1;
            else data[0] = 0;
            bleManager.writeData(
                    BATTERY_SERVICE_UUID,
                    BATTERY_CHARACTERISTIC_ENABLE_UUID,
                    data
            );

            currentIlluminometerValue.enable = enable;
        }
    }

    /**
     * 接続中のおくだけセンサーに電池残量監視の有効／無効の状態取得を要求する
     * 結果はonReceivedBatteryで返ってくる
     */
    public void getBatteryAvailable() {
        if (bleManager.isConnected()) {
            bleManager.readData(
                    BATTERY_SERVICE_UUID,
                    BATTERY_CHARACTERISTIC_ENABLE_UUID
            );
        }
    }


    /**
     * 接続中のおくだけセンサーにLED状態（消灯／点滅／点灯）を設定する
     * @param status LED状態
     */
    public void setLedStatus(byte status) {
        if ((status != LedValue.LED_STATUS_OFF)
                && (status != LedValue.LED_STATUS_FLUSH)
                && (status != LedValue.LED_STATUS_ON)) {
            return;
        }

        if (bleManager.isConnected()) {
            byte[] data = new byte[]{status};
            bleManager.writeData(LED_SERVICE_UUID, LED_CHARACTERISTIC_STATUS_UUID, data);

            currentLedValue.status = status;
        }
    }

    /**
     * 接続中のおくだけセンサーにLED状態の取得を要求する
     * 結果はonReceivedLedで返ってくる
     */
    public void getLedStatus() {
        if (bleManager.isConnected()) {
            bleManager.readData(LED_SERVICE_UUID, LED_CHARACTERISTIC_STATUS_UUID);
        }
    }

    /**
     * AdvertiseData内のManufactureSpecificDataに含まれる企業識別子、ベンダ識別子からおくだけセンサーかどうか判定する
     * @param manufactureSpecificDatas AdvertiseDataのManufactureSpecificData
     * @return おくだけセンサーの場合はTrue
     */
    private boolean isOkudakeSensor(SparseArray<byte[]> manufactureSpecificDatas) {
        boolean result = false;
        int key = 0x02E2;
        if (manufactureSpecificDatas.indexOfKey(key) >= 0) {
            byte[] manufactureSpecificData = manufactureSpecificDatas.get(key);
            if (manufactureSpecificData.length > 2) {
                byte vendor = (byte) ((byte) (0xf0 & (manufactureSpecificData[0] << 4))
                        + (byte) (0x0f & (manufactureSpecificData[1] >> 4)));
                if (vendor == 0x06) result = true;
            }
        }
        return result;
    }

    /**
     * おくだけセンサーがペアリング済みか確認する
     * ペアリング済み機器の中に対象の機器が含まれているかどうかで判断する
     * @param device BluetoothDevice
     * @return ペアリング済みの場合True
     */
    private boolean isBonded(BluetoothDevice device) {
        Set<BluetoothDevice> bondedDevices =
                BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        boolean bonded = false;
        for (BluetoothDevice bondedDevice : bondedDevices) {
            if (device.getAddress().equals(bondedDevice.getAddress())) {
                bonded = true;
                break;
            }
        }
        return bonded;
    }

    /**
     * ペアリングのタイムアウトチェックのためのHandler
     */
    private Handler pairingTimeoutHandler = new Handler();

    /**
     * ペアリングのタイムアウトチェックのためのRunnable
     */
    private Runnable pairingTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            pairingTimeoutHandler.removeCallbacks(pairingTimeoutRunnable);

            if (!connectingDeviceBonded && (connectingDevice != null)) {
                addLog("bluetooth pairing timeout  " + getDeviceName(connectingDevice));
                connectingDevice = null;

                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onDisconnected();
                }
            }
        }
    };

    /**
     * BroadcastReceiver
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        /**
         * ACTION_PAIRING_REQUEST、ACTION_BOND_STATE_CHANGEを受け取る
         * ACTION_BOND_STATE_CHANGEでBONDEDになったら接続する
         * @param context Context
         * @param intent Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                    BluetoothDevice device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE
                    );
                    device.setPin(PAIRING_PIN);
                    addLog("received pairing request from " + getDeviceName(device));

                } else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    BluetoothDevice device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int bondState = intent.getIntExtra(
                            BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE
                    );

                    if (bondState == BluetoothDevice.BOND_NONE) {
                        addLog("bond state changed ... BOND_NONE " + getDeviceName(device));
                    } else if (bondState == BluetoothDevice.BOND_BONDING) {
                        addLog("bond state changed ... BOND_BONDING " + getDeviceName(device));
                    } else if (bondState == BluetoothDevice.BOND_BONDED) {
                        addLog("bond state changed ... BOND_BONDED " + getDeviceName(device));
                    }

                    if ((bondState == BluetoothDevice.BOND_BONDED)
                            && (connectingDevice != null)
                            && (device.getAddress().equals(connectingDevice.getAddress()))) {
                        pairingTimeoutHandler.removeCallbacks(pairingTimeoutRunnable);
                        connectingDeviceBonded = true;
                        bleManager.connect(device);
                    }
                }
            }
        }
    };

    /**
     * BLE機器のコールバック
     */
    private BleManagerListener bleManagerListener = new BleManagerListener() {
        /**
         * BLE機器のAdvertiseDataを受信したときのコールバック
         * @param scanResult スキャン結果
         */
        @Override
        public void onScannedDevice(ScanResult scanResult) {
            if (isOkudakeSensor(scanResult.getScanRecord().getManufacturerSpecificData())) {
                BeaconData data = new BeaconData(scanResult);

                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onScannedDevice(data);
                }
            }
        }

        /**
         * BLE機器のスキャン結果を定期的に返す時のコールバック
         * スキャン中は5秒ごとに呼ばれる
         * @param devices 検出したBLE機器の一覧
         */
        @Override
        public void onScannedDevices(List<BleDeviceInfo> devices) {
            if (okudakeManagerListener != null) {
                okudakeManagerListener.onScannedDevices(getScannedDevices());
            }
        }

        /**
         * BLE機器に接続した時のコールバック
         * @param device 接続したBLE機器
         */
        @Override
        public void onConnected(BluetoothDevice device) {
            currentDevicePreferenceValue.resetValue();
            currentThermohygrometerValue.resetValue();
            currentIlluminometerValue.resetValue();
            currentAccelerometerValue.resetValue();
            currentMagnetometerValue.resetValue();
            currentBatteryValue.resetValue();
            currentLedValue.resetValue();
        }

        /**
         * BLE機器の接続を切断した時のコールバック
         */
        @Override
        public void onDisconnected() {
            connectingDevice = null;

            if (okudakeManagerListener != null) {
                okudakeManagerListener.onDisconnected();
            }
        }

        /**
         * BLE機器のService検出時のコールバック
         * @param services BluetoothGattService
         */
        @Override
        public void onServicesDiscovered(List<BluetoothGattService> services) {
            if (okudakeManagerListener != null) {
                okudakeManagerListener.onConnected();
            }
        }

        /**
         * BLE機器のNotifyデータ受信時のコールバック
         * @param characteristicUuid データ受信したCharacteristic
         * @param data　受信したデータ
         */
        @Override
        public void onReceivedNotificationData(UUID characteristicUuid, byte[] data) {
            if (characteristicUuid.equals(THERMOHYGROMETER_CHARACTERISTIC_DATA_UUID)) {
                currentThermohygrometerValue.setValue(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedThermohygrometer(currentThermohygrometerValue);
                }
            } else if (characteristicUuid.equals(ILLUMINOMETER_CHARACTERISTIC_DATA_UUID)) {
                currentIlluminometerValue.setValue(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedIlluminometer(currentIlluminometerValue);
                }
            } else if (characteristicUuid.equals(ACCELEROMETER_CHARACTERISTIC_DATA_UUID)) {
                currentAccelerometerValue.setValue(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedAccelerometer(currentAccelerometerValue);
                }
            } else if (characteristicUuid.equals(MAGNETOMETER_CHARACTERISTIC_DATA_UUID)) {
                currentMagnetometerValue.setValue(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedMagnetometer(currentMagnetometerValue);
                }
            } else if (characteristicUuid.equals(BATTERY_CHARACTERISTIC_BATTERY_LEVEL_UUID)) {
                currentBatteryValue.setLevel(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedBattery(currentBatteryValue);
                }
            }
        }

        /**
         * BLE機器のCharacteristicからのReadデータ受信時のコールバック
         * @param characteristicUuid CharacteristicのUUID
         * @param data 受信したデータ
         */
        @Override
        public void onReceivedCharacteristicData(int status, UUID characteristicUuid, byte[] data) {
            if (characteristicUuid.equals(DEVICE_CHARACTERISTIC_IDLE_TIMEOUT_UUID)) {
                currentDevicePreferenceValue.setIdleTimeout(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedDevicePreferences(currentDevicePreferenceValue);
                }
            } else if (characteristicUuid.equals(DEVICE_CHARACTERISTIC_BEACON_TANSMITTING_INTERVAL_UUID)) {
                currentDevicePreferenceValue.setBeaconTransmittingInterval(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedDevicePreferences(currentDevicePreferenceValue);
                }
            } else if (characteristicUuid.equals(DEVICE_CHARACTERISTIC_BEACON_UPDATING_INTERVAL_UUID)) {
                currentDevicePreferenceValue.setBeaconUpdatingInterval(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedDevicePreferences(currentDevicePreferenceValue);
                }
            } else if (characteristicUuid.equals(DEVICE_CHARACTERISTIC_TX_POWER_LEVEL_UUID)) {
                currentDevicePreferenceValue.setTxPowerLevel(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedDevicePreferences(currentDevicePreferenceValue);
                }
            } else if (characteristicUuid.equals(THERMOHYGROMETER_CHARACTERISTIC_DATA_UUID)) {
                currentThermohygrometerValue.setValue(data);
                currentThermohygrometerValue.enable =
                        (status == SENSOR_STATUS_AVAILABLE) ? true : false;
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedThermohygrometer(currentThermohygrometerValue);
                }
            } else if (characteristicUuid.equals(THERMOHYGROMETER_CHARACTERISTIC_ENABLE_UUID)) {
                currentThermohygrometerValue.setEnable(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedThermohygrometer(currentThermohygrometerValue);
                }
            } else if (characteristicUuid.equals(THERMOHYGROMETER_CHARACTERISTIC_PERIOD_UUID)) {
                currentThermohygrometerValue.setPeriod(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedThermohygrometer(currentThermohygrometerValue);
                }
            } else if (characteristicUuid.equals(ILLUMINOMETER_CHARACTERISTIC_DATA_UUID)) {
                currentIlluminometerValue.setValue(data);
                currentIlluminometerValue.enable =
                        (status == SENSOR_STATUS_AVAILABLE) ? true : false;
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedIlluminometer(currentIlluminometerValue);
                }
            } else if (characteristicUuid.equals(ILLUMINOMETER_CHARACTERISTIC_ENABLE_UUID)) {
                currentIlluminometerValue.setEnable(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedIlluminometer(currentIlluminometerValue);
                }
            } else if (characteristicUuid.equals(ILLUMINOMETER_CHARACTERISTIC_PERIOD_UUID)) {
                currentIlluminometerValue.setPeriod(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedIlluminometer(currentIlluminometerValue);
                }
            } else if (characteristicUuid.equals(ACCELEROMETER_CHARACTERISTIC_DATA_UUID)) {
                currentAccelerometerValue.setValue(data);
                currentAccelerometerValue.enable =
                        (status == SENSOR_STATUS_AVAILABLE) ? true : false;
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedAccelerometer(currentAccelerometerValue);
                }
            } else if (characteristicUuid.equals(ACCELEROMETER_CHARACTERISTIC_ENABLE_UUID)) {
                currentAccelerometerValue.setEnable(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedAccelerometer(currentAccelerometerValue);
                }
            } else if (characteristicUuid.equals(ACCELEROMETER_CHARACTERISTIC_PERIOD_UUID)) {
                currentAccelerometerValue.setPeriod(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedAccelerometer(currentAccelerometerValue);
                }
            } else if (characteristicUuid.equals(MAGNETOMETER_CHARACTERISTIC_DATA_UUID)) {
                currentMagnetometerValue.setValue(data);
                currentMagnetometerValue.enable =
                        (status == SENSOR_STATUS_AVAILABLE) ? true : false;
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedMagnetometer(currentMagnetometerValue);
                }
            } else if (characteristicUuid.equals(MAGNETOMETER_CHARACTERISTIC_ENABLE_UUID)) {
                currentMagnetometerValue.setEnable(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedMagnetometer(currentMagnetometerValue);
                }
            } else if (characteristicUuid.equals(BATTERY_CHARACTERISTIC_BATTERY_LEVEL_UUID)) {
                currentBatteryValue.setLevel(data);
                currentBatteryValue.enable =
                        (status == SENSOR_STATUS_AVAILABLE) ? true : false;
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedBattery(currentBatteryValue);
                }
            } else if (characteristicUuid.equals(BATTERY_CHARACTERISTIC_USB_PLUGGED_UUID)) {
                currentBatteryValue.setUsbPlugged(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedBattery(currentBatteryValue);
                }
            } else if (characteristicUuid.equals(BATTERY_CHARACTERISTIC_ENABLE_UUID)) {
                currentBatteryValue.setEnable(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedBattery(currentBatteryValue);
                }
            } else if (characteristicUuid.equals(LED_CHARACTERISTIC_STATUS_UUID)) {
                currentLedValue.setValue(data);
                if (okudakeManagerListener != null) {
                    okudakeManagerListener.onReceivedLed(currentLedValue);
                }
            }
        }

        /**
         * BLE機器のDescriptorからのReadデータ受信時のコールバック
         * @param descriptorUuid DescriptorのUUID
         * @param data 受信したデータ
         */
        @Override
        public void onReceivedDescriptorData(int status, UUID descriptorUuid, byte[] data) {

        }
    };

    /**
     * BluetoothDeviceの名称を取得する
     * @param device 名称を取得するBluetoothDevice
     * @return デバイス名称
     */
    private String getDeviceName(BluetoothDevice device) {
        String name;
        if (device.getName() != null) {
            name = device.getName();
        } else {
            name = "address=" + device.getAddress();
        }
        return name;
    }

    /**
     * デバッグログ出力
     * @param log　出力するログ
     */
    private void addLog(String log) {
        Log.d(TAG, log);
    }
}
