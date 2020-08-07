/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;

import java.util.List;
import java.util.UUID;

/**
 * BleManagerから呼ばれるコールバック
 */
public interface BleManagerListener {
    /**
     * BLE機器のAdvertiseData取得時のコールバック
     * @param scanResult スキャン結果
     */
    void onScannedDevice(ScanResult scanResult);

    /**
     * BLE機器のスキャン結果を定期的に返す時のコールバック
     * @param devices 検出したBLE機器の一覧
     */
    void onScannedDevices(List<BleDeviceInfo> devices);

    /**
     * BLE機器との接続時のコールバック
     * @param device 接続したBLE機器
     */
    void onConnected(BluetoothDevice device);

    /**
     * BLE機器との接続切断時のコールバック
     */
    void onDisconnected();

    /**
     * GATT Service検出時のコールバック
     * @param services 検出したGATT Service
     */
    void onServicesDiscovered(List<BluetoothGattService> services);

    /**
     * Notificationデータ受信時のコールバック
     * @param characteristicUuid データ受信したCharacteristic
     * @param data　受信したデータ
     */
    void onReceivedNotificationData(UUID characteristicUuid, byte[] data);

    /**
     * Characteristicのデータ受信時のコールバック
     * @param status 受信ステータス
     * @param characteristicUuid データ受信したCharacteristic
     * @param data　受信したデータ
     */
    void onReceivedCharacteristicData(int status, UUID characteristicUuid, byte[] data);

    /**
     * Descriptorのデータ受信時のコールバック
     * @param status 受信ステータス
     * @param descriptorUuid　データ受信したDescriptor
     * @param data　受信したデータ
     */
    void onReceivedDescriptorData(int status, UUID descriptorUuid, byte[] data);
}
