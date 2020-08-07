/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.sun.m2m.okudakelinkdemoapp.okudake.ble.BleDataItem.CommunicationType;

/**
 * BLE機器管理
 */
public class BleManager {
    /** ログ出力時のTAG */
    private final String TAG = "BleManager";

    /** Client Characteristic Configuration DescriptorのUUID */
    public static final UUID UUID_CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /** Read/Writeのタイムアウト時間（ミリ秒） */
    private final int COM_TIMEOUT = 2000;

    /** BleManagerのインスタンス */
    private static BleManager instance;

    /** アプリのContext */
    private Context context;
    /** BluetoothAdapter */
    private BluetoothAdapter bluetoothAdapter;
    /** BluetoothLeScanner */
    private BluetoothLeScanner bluetoothLeScanner;
    /** 検出中のBLEのリスト */
    private HashMap<String, BleDeviceInfo> scanningDeviceList;
    /** 検出したBLEのリスト（startScanにintervalを指定した場合に使用） */
    private ArrayList<BleDeviceInfo> deviceList;
    /** 接続しているBLE機器 */
    private BluetoothDevice bluetoothDevice;
    /** BluetoothGatt */
    private BluetoothGatt bluetoothGatt;
    /** BleManagerのコールバック */
    private BleManagerListener listener;
    /** Read/Writeの待ちリスト */
    private ArrayList<BleDataItem> communicationDataList;
    /** 現在Read/Write中のデータ */
    private BleDataItem communicatingData;
    /** 接続しているBLE機器の信号強度 */
    private int connectedDeviceRssi;
    /** スキャン中一定間隔でonScannedDevicesを呼ぶ間隔（startScanにintervalを指定した場合に使用） */
    private int scannedInterval;
    /** スキャン中かどうか */
    private boolean scanning;

    /**
     * BleManagerのインスタンスを返す
     * BleManagerのインスタンスを作成する場合は必ずgetInstanceメソッドを使用する
     * @param context アプリのContext
     * @param listener BleManagerのコールバック
     * @return BleManagerのインスタンス
     */
    public static BleManager getInstance(Context context, BleManagerListener listener) {
        if (instance == null) {
            instance = new BleManager(context, listener);
        } else {
            instance.setEventListener(listener);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param context アプリのContext
     */
    public BleManager(Context context, BleManagerListener listener) {
        this.context = context;
        this.listener = listener;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        this.scanningDeviceList = new HashMap<>();
        this.deviceList = new ArrayList<>();
        this.bluetoothDevice = null;
        this.bluetoothGatt = null;
        this.communicationDataList = new ArrayList<>();
        this.communicatingData = null;
        this.scannedInterval = 0;
        this.scanning = false;
    }


    /**
     * スキャン中かどうか
     * @return スキャン中の場合はTrue
     */
    public boolean isScanning() {
        return scanning;
    }


    /**
     * BLE接続中かどうか
     * @return BLE接続している場合はTrue
     */
    public boolean isConnected() {
        if (bluetoothGatt == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 接続中のBLE機器の信号強度を返す
     * @return RSSI
     */
    public int getConnectedDeviceRssi() {
        if (isConnected()) {
            return connectedDeviceRssi;
        } else {
            return 0;
        }
    }

    /**
     * コールバックの設定
     * @param listener BleManagerのコールバック
     */
    public void setEventListener(BleManagerListener listener) {
        this.listener = listener;
    }


    /**
     * スキャンで検出したBLE機器リストを返す
     * @return 検出したBLE機器
     */
    public List<BleDeviceInfo> getDevices() {
        if ((deviceList.size() == 0) && (scanningDeviceList.size() > 0)) {
            return new ArrayList<>(scanningDeviceList.values());
        } else {
            return new ArrayList<>(deviceList);
        }
    }


    /**
     * 接続中のBLE機器のGATT Serviceを返す
     * @return 接続したBLE機器のGATT Service
     */
    public List<BluetoothGattService> getServices() {
        if (isConnected()) {
            return new ArrayList<>(bluetoothGatt.getServices());
        } else {
            return null;
        }
    }


    /**
     * BLE機器のスキャンを開始する
     * intervalに500～60000（ミリ秒）の値を指定した場合、指定した時間ごとにonCompletedScanningDeviceを呼び出します
     * スキャン中にBLE機器からAdvertiseDataを受け取った場合はonScannedDeviceを呼び出します
     * @param interval 定期的にスキャン結果を取得する時間（ミリ秒）
     */
    public void startScan(int interval) {
        if (!scanning) {
            try {
                scanning = true;
                scanningDeviceList.clear();
                ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .build();
                bluetoothLeScanner.startScan(null, scanSettings, bleScanCallback);
                if ((interval > 500) && (interval <= 60000)) {
                    scannedInterval = interval;
                    bleScanHandler.postDelayed(bleScanRunnable, scannedInterval);
                } else {
                    scannedInterval = 0;
                }
            } catch(Exception e) {
                addErrorLog("start scan ble error : " + e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * BLE機器のスキャンを停止する
     */
    public void stopScan() {
        if (scanning) {
            scanning = false;
            bluetoothLeScanner.stopScan(bleScanCallback);
        }
    }

    /**
     * BLE機器に接続する
     * @param device 接続するBLE機器
     */
    public void connect(BluetoothDevice device) {
        if (!isConnected()) {
            bluetoothDevice = device;
            if (bluetoothDevice != null) {

                if (scanning) {
                    stopScan();
                }

                addLog(String.format("connect to ble device %s", device.getAddress()));
                BluetoothGatt gatt =
                        bluetoothDevice.connectGatt(context, false, gattCallback);
                gatt.connect();
            }
        } else {
            addLog("connect() : already connected");
        }
    }

    /**
     * 接続中のBLE機器との接続を切断する
     */
    public void disconnect() {
        if (isConnected()) {
            bluetoothGatt.disconnect();
            addLog("disconnected ble device");
        }
    }


    /**
     * Notificationの有効／無効を切り替える
     * @param characteristic Notificationを切り替えるCharacteristic
     * @param enable Trueの場合Notificationを有効にする
     */
    public void setNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (isConnected() && (characteristic != null)) {
            bluetoothGatt.setCharacteristicNotification(characteristic, enable);
            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            for (BluetoothGattDescriptor descriptor : descriptors) {
                if (descriptor.getUuid().equals(UUID_CCCD)) {
                    byte[] data;
                    if (enable) {
                        data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    } else {
                        data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    }
                    BleDataItem item = new BleDataItem(
                            CommunicationType.WRITE_DESCRIPTOR_DATA,
                            descriptor,
                            data
                    );
                    communicationDataList.add(item);
                }
            }
        }
    }

    /**
     * Service、CharacteristicのUUIDを指定してNotificationの有効／無効を切り替える
     * @param serviceUuid CharacteristicがあるServiceのUUID
     * @param characteristicUuid CharacteristicのUUID
     * @param enable Trueの場合Notificationを有効にする
     */
    public void setNotification(UUID serviceUuid, UUID characteristicUuid, boolean enable) {
        if (isConnected() && (serviceUuid != null) && (characteristicUuid != null)) {
            List<BluetoothGattService> services = bluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
                if (service.getUuid().equals(serviceUuid)) {
                    List<BluetoothGattCharacteristic> characteristics =
                            service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        if (characteristic.getUuid().equals(characteristicUuid)) {
                            setNotification(characteristic, enable);
                        }
                    }
                }
            }
        }
    }

    /**
     * Characteristicにデータを書き込む
     * @param characteristic データを書き込むCharacteristic
     * @param data 書き込むデータ
     */
    public void writeData(BluetoothGattCharacteristic characteristic, byte[] data) {
        if (isConnected()) {
            BleDataItem item = new BleDataItem(
                    CommunicationType.WRITE_CHARACTERISTIC_DATA,
                    characteristic,
                    data
            );
            communicationDataList.add(item);
        }
    }

    /**
     * CharacteristicのUUIDを指定してデータを書き込む
     * @param serviceUuid CharacteristicがあるServiceのUUID
     * @param characteristicUuid CharacteristicのUUID
     * @param data 書き込むデータ
     */
    public void writeData(UUID serviceUuid, UUID characteristicUuid, byte[] data) {
        if (isConnected() && (serviceUuid != null) && (characteristicUuid != null)) {
            List<BluetoothGattService> services = bluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
                if (service.getUuid().equals(serviceUuid)) {
                    List<BluetoothGattCharacteristic> characteristics =
                            service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        if (characteristic.getUuid().equals(characteristicUuid)) {
                            writeData(characteristic, data);
                        }
                    }
                }
            }
        }
    }

    /**
     * Descriptorにデータを書き込む
     * @param descriptor データを書き込むDescriptor
     * @param data 書き込むデータ
     */
    public void writeData(BluetoothGattDescriptor descriptor, byte[] data) {
        if (isConnected()) {
            BleDataItem item = new BleDataItem(
                    CommunicationType.WRITE_DESCRIPTOR_DATA,
                    descriptor,
                    data
            );
            communicationDataList.add(item);
        }
    }

    /**
     * DescriptorのUUIDを指定してデータを書き込む
     * @param serviceUuid DescriptorがあるServiceのUUID
     * @param characteristicUuid DescriptorがあるCharacteristicのUUID
     * @param descriptorUuid DescriptorのUUID
     * @param data 書き込むデータ
     */
    public void writeData(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid,
                          byte[] data) {
        if (isConnected()
                && (serviceUuid != null)
                && (characteristicUuid != null)
                && (descriptorUuid != null)) {
            List<BluetoothGattService> services = bluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
                if (service.getUuid().equals(serviceUuid)) {
                    List<BluetoothGattCharacteristic> characteristics =
                            service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        if (characteristic.getUuid().equals(characteristicUuid)) {
                            List<BluetoothGattDescriptor> descriptors =
                                    characteristic.getDescriptors();
                            for (BluetoothGattDescriptor descriptor : descriptors) {
                                if (descriptor.getUuid().equals(descriptorUuid)) {
                                    writeData(descriptor, data);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Characteristicのデータを読み込む
     * 結果はonCharacteristicReadに返ってくる
     * @param characteristic データを読み込むCharacteristic
     */
    public void readData(BluetoothGattCharacteristic characteristic) {
        if (isConnected()) {
            BleDataItem item = new BleDataItem(
                    CommunicationType.READ_CHARACTERISTIC_DATA,
                    characteristic,
                    null
            );
            communicationDataList.add(item);
        }
    }

    /**
     * CharacteristicのUUIDを指定してデータを読み込む
     * 結果はonCharacteristicReadに返ってくる
     * @param serviceUuid CharacteristicがあるServiceのUUID
     * @param characteristicUuid CharacteristicのUUID
     */
    public void readData(UUID serviceUuid, UUID characteristicUuid) {
        if (isConnected() && (serviceUuid != null) && (characteristicUuid != null)) {
            List<BluetoothGattService> services = bluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
                if (service.getUuid().equals(serviceUuid)) {
                    List<BluetoothGattCharacteristic> characteristics =
                            service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        if (characteristic.getUuid().equals(characteristicUuid)) {
                            readData(characteristic);
                        }
                    }
                }
            }
        }
    }

    /**
     * Descriptorのデータを読み込む
     * 結果はonDescriptorReadに返ってくる
     * @param descriptor　データを読み込むDescriptor
     */
    public void readData(BluetoothGattDescriptor descriptor) {
        if (isConnected()) {
            BleDataItem item = new BleDataItem(
                    CommunicationType.READ_DESCRIPTOR_DATA,
                    descriptor,
                    null
            );
            communicationDataList.add(item);
        }
    }

    /**
     * DescriptorのUUIDを指定してデータを読み込む
     * 結果はonDescriptorReadに返ってくる
     * @param serviceUuid　DescriptorがあるServiceのUUID
     * @param characteristicUuid　DescriptorがあるCharacteristicのUUID
     * @param descriptorUuid　DescriptorのUUID
     */
    public void readData(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid) {
        if (isConnected()
                && (serviceUuid != null)
                && (characteristicUuid != null)
                && (descriptorUuid != null)) {
            List<BluetoothGattService> services = bluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
                if (service.getUuid().equals(serviceUuid)) {
                    List<BluetoothGattCharacteristic> characteristics =
                            service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        if (characteristic.getUuid().equals(characteristicUuid)) {
                            List<BluetoothGattDescriptor> descriptors =
                                    characteristic.getDescriptors();
                            for (BluetoothGattDescriptor descriptor : descriptors) {
                                if (descriptor.getUuid().equals(descriptorUuid)) {
                                    readData(descriptor);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Characteristic、DescriptorのRead/Writeを行うためのHandler
     */
    private Handler dataCommunicationHandler = new Handler();

    /**
     * Characteristic、DescriptorのRead/Writeを行うためのRunnable
     * communicationDataListに入っている要素を先頭から順にRead/Writeしていく
     * Read/Write後、Read/WriteしたCharacteristic/DescriptorのRead/Writeコールバックが呼ばれたら次のRead/Writeを行う
     * Read/Write後、タイムアウト時間が経過したらコールバックが呼ばれていなくても次のRead/Writeを行う
     */
    private Runnable dataCommunicationRunnable = new Runnable() {
        @Override
        public void run() {
            dataCommunicationHandler.removeCallbacks(dataCommunicationRunnable);

            if (isConnected()) {
                if ((communicatingData == null) && (communicationDataList.size() > 0)) {
                    communicatingData = communicationDataList.get(0);
                    communicationDataList.remove(0);

                    if (communicatingData.communicationType
                            == CommunicationType.READ_CHARACTERISTIC_DATA) {
                        if (communicatingData.characteristic == null) {
                            communicatingData = null;
                        } else {
                            bluetoothGatt.readCharacteristic(communicatingData.characteristic);
                            communicatingData.startTime = System.currentTimeMillis();
                            addLog("read characteristic  "
                                    + communicatingData.characteristic.getUuid().toString());
                        }

                    } else if (communicatingData.communicationType
                            == CommunicationType.WRITE_CHARACTERISTIC_DATA) {
                        if ((communicatingData.characteristic == null)
                                || (communicatingData.communicationData == null)) {
                            communicatingData = null;
                        } else {
                            communicatingData.characteristic.setValue(
                                    communicatingData.communicationData
                            );
                            bluetoothGatt.writeCharacteristic(communicatingData.characteristic);
                            communicatingData.startTime = System.currentTimeMillis();
                            addLog("write characteristic  "
                                    + communicatingData.characteristic.getUuid().toString()
                                    + "  "
                                    + bytesToHex(communicatingData.communicationData)
                            );
                        }

                    } else if (communicatingData.communicationType
                            == CommunicationType.READ_DESCRIPTOR_DATA) {
                        if (communicatingData.descriptor == null) {
                            communicatingData = null;
                        } else {
                            bluetoothGatt.readDescriptor(communicatingData.descriptor);
                            communicatingData.startTime = System.currentTimeMillis();
                            addLog("read descriptor  "
                                    + communicatingData.descriptor.getUuid().toString()
                            );
                        }

                    } else if (communicatingData.communicationType
                            == CommunicationType.WRITE_DESCRIPTOR_DATA) {
                        if ((communicatingData.descriptor == null)
                                || (communicatingData.communicationData == null)) {
                            communicatingData = null;
                        } else {
                            communicatingData.descriptor.setValue(
                                    communicatingData.communicationData
                            );
                            bluetoothGatt.writeDescriptor(communicatingData.descriptor);
                            communicatingData.startTime = System.currentTimeMillis();
                            addLog("write descriptor  "
                                    + communicatingData.descriptor.getUuid().toString()
                            );
                        }

                    } else {
                        communicatingData = null;
                    }
                } else {
                    if ((communicatingData != null)
                            && (System.currentTimeMillis() - communicatingData.startTime
                                >= COM_TIMEOUT)) {
                        communicatingData = null;
                    }
                }

                dataCommunicationHandler.postDelayed(dataCommunicationRunnable, 100);
            }
        }
    };

    /**
     * BLE機器スキャンのコールバック
     */
    private ScanCallback bleScanCallback = new ScanCallback() {
        /**
         * BLE機器検出時のコールバック
         * @param callbackType　Callback type
         * @param result　Scan result
         */
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (result != null && result.getDevice() != null) {
                BleDeviceInfo info = new BleDeviceInfo(
                        result.getDevice(),
                        result.getAdvertisingSid(),
                        System.currentTimeMillis(),
                        result.getRssi(),
                        result.getScanRecord().getTxPowerLevel(),
                        result.getScanRecord().getManufacturerSpecificData()
                );

                if (scanningDeviceList.containsKey(info.device.getAddress())) {
                    BleDeviceInfo infoData = scanningDeviceList.get(info.device.getAddress());
                    infoData.advertisingSid = info.advertisingSid;
                    infoData.dateTime = System.currentTimeMillis();
                    infoData.txPower = info.txPower;
                    infoData.rssi = info.rssi;
                    infoData.distance = info.distance;
                    infoData.manufactureSpecificData = info.manufactureSpecificData;
                } else {
                    scanningDeviceList.put(info.device.getAddress(), info);
                }

                if (listener != null) {
                    listener.onScannedDevice(result);
                }
            }
        }

        /**
         * スキャン結果取得時のコールバック
         * @param results Scan results
         */
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        /**
         * スキャン失敗時のコールバック
         * @param errorCode Error code
         */
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    addErrorLog("already scanning ble device");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    addErrorLog("cannot start ble scan");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    addErrorLog("ble scan is not supported");
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    addErrorLog("ble scan internal error");
                    break;
            }
        }
    };

    /**
     * スキャン結果を定期的に返すためのHandler
     */
    private Handler bleScanHandler = new Handler();

    /**
     * スキャン結果を定期的に返すためのRunnable
     */
    private Runnable bleScanRunnable = new Runnable() {
        @Override
        public void run() {
            bleScanHandler.removeCallbacks(bleScanRunnable);

            if (isScanning()) {
                if (scannedInterval > 0) {
                    bluetoothLeScanner.stopScan(bleScanCallback);

                    deviceList = new ArrayList<>(scanningDeviceList.values());
                    scanningDeviceList.clear();

                    ScanSettings scanSettings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                            .build();
                    bluetoothLeScanner.startScan(null, scanSettings, bleScanCallback);
                    bleScanHandler.postDelayed(bleScanRunnable, scannedInterval);

                    if (listener != null) {
                        listener.onScannedDevices(new ArrayList<>(deviceList));
                    }
                }
            }
        }
    };

    /**
     * Bluetooth GATT のコールバック
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        /**
         * 接続状態変更時のコールバック
         * @param gatt Bluetooth GATT
         * @param status 変更前のStatus
         * @param newState 変更後のStatus
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothGatt.STATE_CONNECTED) {

                addLog("gatt discoverServices");
                connectedDeviceRssi = 0;
                gatt.discoverServices();

                if (listener != null) {
                    listener.onConnected(gatt.getDevice());
                }

            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {

                String deviceAddress = "";
                if (bluetoothGatt != null) {
                    deviceAddress = bluetoothGatt.getDevice().getAddress();
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
                addLog(String.format("disconnected ble : %s", deviceAddress));

                dataCommunicationHandler.removeCallbacks(dataCommunicationRunnable);
                bluetoothDevice = null;
                connectedDeviceRssi = 0;

                if (listener != null) {
                    listener.onDisconnected();
                }

            }
        }

        /**
         * GATT Service 検出時のコールバック
         * @param gatt　BluetoothGatt
         * @param status　Status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            bluetoothGatt = gatt;

            communicatingData = null;
            communicationDataList.clear();
            dataCommunicationHandler.post(dataCommunicationRunnable);

            if (listener != null) {
                listener.onServicesDiscovered(gatt.getServices());
            }
        }

        /**
         * 接続中デバイスのRSSIを取得
         * @param gatt BluetoothGatt
         * @param rssi RSSI
         * @param status GATT Status
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                connectedDeviceRssi = rssi;
            }
        }

        /**
         * Characteristicの状態変更時のコールバック
         * @param gatt　BluetoothGatt
         * @param characteristic　BluetoothGattCharacteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            UUID uuid = characteristic.getUuid();
            byte[] data = characteristic.getValue();

            addLog(String.format("onCharacteristicChanged  %s", uuid.toString()));
            if (listener != null) {
                listener.onReceivedNotificationData(uuid, data);
            }
        }

        /**
         * Characteristic Read のコールバック
         * @param gatt　BluetoothGatt
         * @param characteristic　BluetoothGattCharacteristic
         * @param status　Status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            addLog(String.format("onCharacteristicRead  status=%d  %s", status, uuid.toString()));

            byte[] data = characteristic.getValue();
            if (listener != null) {
                listener.onReceivedCharacteristicData(status, uuid, data);
            }

            if ((communicatingData.characteristic != null)
                    && (communicatingData.characteristic.getUuid().equals(uuid))) {
                communicatingData = null;
                dataCommunicationHandler.post(dataCommunicationRunnable);
            }
        }

        /**
         * Characteristic Write のコールバック
         * @param gatt　BluetoothGatt
         * @param characteristic　BluetoothGattCharacteristic
         * @param status　Status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            addLog(String.format("onCharacteristicWrite  status=%d  %s", status, uuid.toString()));
            if ((communicatingData.characteristic != null)
                    && (communicatingData.characteristic.getUuid().equals(uuid))) {
                communicatingData = null;
                dataCommunicationHandler.post(dataCommunicationRunnable);
            }
        }

        /**
         * Descriptor Read のコールバック
         * @param gatt　BluetoothGatt
         * @param descriptor　BluetoothGattDescriptor
         * @param status　Status
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            addLog(String.format("onDescriptorRead  status=%d  %s", status, uuid.toString()));

            byte[] data = descriptor.getValue();
            if (listener != null) {
                listener.onReceivedDescriptorData(status, uuid, data);
            }

            if ((communicatingData.descriptor != null)
                    && (communicatingData.descriptor.getUuid().equals(uuid))) {
                communicatingData = null;
                dataCommunicationHandler.post(dataCommunicationRunnable);
            }
        }

        /**
         * Descriptor Write のコールバック
         * @param gatt BluetoothGatt
         * @param descriptor BluetoothGattDescriptor
         * @param status Status
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            addLog(String.format("onDescriptorWrite  status=%d  %s", status, uuid.toString()));
            if ((communicatingData.descriptor != null)
                    && (communicatingData.descriptor.getUuid().equals(uuid))) {
                communicatingData = null;
                dataCommunicationHandler.post(dataCommunicationRunnable);
            }
        }
    };

    /**
     * バイト配列を16進文字列に変換
     * @param data 16進文字列に変換するbyte配列
     * @return 16進文字列
     */
    private String bytesToHex(byte[] data){
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) sb.append("0");
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * デバッグログ出力
     * @param log　出力するログ
     */
    private void addLog(String log) {
        Log.d(TAG, log);
    }

    /**
     * エラーログ出力
     * @param log　出力するログ
     */
    private void addErrorLog(String log) {
        Log.e(TAG, log);
    }
}
