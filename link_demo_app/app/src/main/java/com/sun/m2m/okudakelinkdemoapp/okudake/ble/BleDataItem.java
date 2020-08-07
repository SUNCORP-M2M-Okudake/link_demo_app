/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * Read/Writeの待ちリストに入れる要素
 */
public class BleDataItem {
    /**
     * Read/Writeの種類
     */
    public enum CommunicationType {
        READ_CHARACTERISTIC_DATA,
        WRITE_CHARACTERISTIC_DATA,
        READ_DESCRIPTOR_DATA,
        WRITE_DESCRIPTOR_DATA
    }

    /** Read/Writeの種類 */
    public CommunicationType communicationType;

    /** CharacteristicのRead/Writeを行う場合のCharacteristic */
    public BluetoothGattCharacteristic characteristic;

    /** DescriptorのRead/Writeを行う場合のDescriptor */
    public BluetoothGattDescriptor descriptor;

    /** Writeするデータ */
    public byte[] communicationData;

    /** Read/Writeの開始時刻（タイムアウトチェック用） */
    public long startTime;

    /**
     * コンストラクタ
     * CharacteristicのRead/Write
     * @param type Read/Writeの種別
     * @param characteristic Read/Writeを行うBluetoothGattCharacteristic
     * @param data Writeデータ
     */
    public BleDataItem(CommunicationType type, BluetoothGattCharacteristic characteristic,
                       byte[] data) {
        this.communicationType = type;
        this.characteristic = characteristic;
        this.descriptor = null;
        if (data == null) {
            this.communicationData = null;
        } else {
            this.communicationData = new byte[data.length];
            System.arraycopy(data, 0, this.communicationData, 0, data.length);
        }
        this.startTime = 0;
    }

    /**
     * コンストラクタ
     * DescriptorのRead/Write
     * @param type Read/Writeの種別
     * @param descriptor Read/Writeを行うBluetoothGattDescriptor
     * @param data Writeデータ
     */
    public BleDataItem(CommunicationType type, BluetoothGattDescriptor descriptor, byte[] data) {
        this.communicationType = type;
        this.characteristic = null;
        this.descriptor = descriptor;
        if (data == null) {
            this.communicationData = null;
        } else {
            this.communicationData = new byte[data.length];
            System.arraycopy(data, 0, this.communicationData, 0, data.length);
        }
        this.startTime = 0;
    }
}
