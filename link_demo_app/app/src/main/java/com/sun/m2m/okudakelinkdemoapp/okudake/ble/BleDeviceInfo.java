/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake.ble;

import android.bluetooth.BluetoothDevice;
import android.util.SparseArray;

import java.io.Serializable;

/**
 * BLE機器情報
 */
public class BleDeviceInfo implements Serializable {
    /** BluetoothDevice */
    public BluetoothDevice device;
    /** Advertising SID */
    public int advertisingSid;
    /** データ受信日時 */
    public long dateTime;
    /** RSSI（dBm） */
    public int rssi;
    /** Tx Power（dBm） */
    public int txPower;
    /** 距離 */
    public double distance;
    /** Manufacture Specific Data */
    public SparseArray<byte[]> manufactureSpecificData;

    /**
     * コンストラクタ
     * @param device BLE機器
     * @param advertisingSid Advertising SID
     * @param dateTime データ受信日時
     * @param rssi RSSI
     * @param txPower Tx Power
     * @param datas Manufacturer Specific Data
     */
    public BleDeviceInfo(BluetoothDevice device, int advertisingSid, long dateTime, int rssi,
                         int txPower, SparseArray<byte[]> datas) {
        this.device = device;
        this.advertisingSid = advertisingSid;
        this.dateTime = dateTime;
        this.rssi = rssi;
        this.txPower = txPower;
        this.distance = Math.pow(10.0, ((double)this.txPower - (double)this.rssi) / 20.0);
        this.manufactureSpecificData = datas;
    }
}
