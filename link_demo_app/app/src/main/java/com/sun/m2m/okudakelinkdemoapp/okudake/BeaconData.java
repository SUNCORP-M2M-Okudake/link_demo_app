/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * おくだけセンサーのビーコンデータ
 */
public class BeaconData {
    /** おくだけセンサーのデバイス */
    public BluetoothDevice device;
    /** シリアル番号 */
    public String serialNumber;
    /** データ受信日時 */
    public long dateTime;
    /** RSSI（dBm） */
    public int rssi;
    /** Tx Power（dBm） */
    public int txPower;
    /** 距離 */
    public double distance;
    /** 温度（℃） */
    public double temperature;
    /** 湿度（％） */
    public double humidity;
    /** 電池電圧2.4V以上かどうか */
    public boolean over24voltage;
    /** 電池残量（％） */
    public double batteryLevel;
    /** 磁気センサー開閉状態（True=磁気検出あり／False=磁気検出なし */
    public boolean magnetic;
    /** 磁気センサー開閉回数（0～2047） */
    public int magneticCount;

    /**
     * コンストラクタ
     * @param result BLE機器スキャンした時のScanResult
     */
    public BeaconData(ScanResult result) {
        initialize(
                result.getDevice(),
                System.currentTimeMillis(),
                result.getRssi(),
                result.getScanRecord().getTxPowerLevel(),
                result.getScanRecord().getManufacturerSpecificData()
        );
    }

    /**
     * コンストラクタ
     * @param device　BLE機器
     * @param dateTime 受信日時
     * @param rssi　RSSI
     * @param txPower　Tx Power
     * @param datas　Manufacture Specific Data
     */
    public BeaconData(BluetoothDevice device, long dateTime, int rssi, int txPower,
                      SparseArray<byte[]> datas) {
        initialize(
                device,
                dateTime,
                rssi,
                txPower,
                datas
        );
    }

    /**
     * 初期化処理
     * @param device　BLE機器
     * @param dateTime 受信日時
     * @param rssi　RSSI
     * @param txPower　Tx Power
     * @param datas　Manufacture Specific Data
     */
    private void initialize(BluetoothDevice device, long dateTime, int rssi, int txPower,
                            SparseArray<byte[]> datas) {
        this.device = device;
        this.dateTime = dateTime;
        this.rssi = rssi;
        this.txPower = txPower;
        if (this.txPower == Integer.MIN_VALUE) {
            this.distance = 0;
        } else {
            this.distance = Math.pow(10.0, ((double) this.txPower - (double) this.rssi) / 20.0);
        }

        int key = 0x02E2;
        if (datas.indexOfKey(key) >= 0) {
            byte[] data = datas.get(key);
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);

            try {
                // company=4bits, vendor=8bits, serial=20bits
                byte[] headerData = new byte[4];
                byteBuffer.get(headerData);
                serialNumber = Integer.toHexString(0x0f & headerData[1])
                        + Integer.toHexString(0x0f & (headerData[2] >>> 4))
                        + Integer.toHexString(0x0f & headerData[2])
                        + Integer.toHexString(0x0f & (headerData[3] >>> 4))
                        + Integer.toHexString(0x0f & headerData[3]);

                // key=4bits, value=12bits
                byte exp;
                byte[] tmpByte = {0, 0, 0, 0};
                byte[] serviceData = new byte[2];
                while (byteBuffer.position() < byteBuffer.capacity()) {
                    byteBuffer.get(serviceData);
                    byte serviceId = (byte)(serviceData[0] >>> 4);
                    switch (serviceId) {
                        case 0x01:
                            // serviceData[] = xxxxabbb bccccccc
                            // xxxx = サービスID
                            // a = 符号
                            // bbbb = 指数部
                            // ccccccc = 仮数部
                            // 4ビット指数部を8ビット指数部に変換
                            exp = (byte)(((serviceData[0] & 0x07) << 1)
                                    + ((serviceData[1] >>> 7) & 0x01));
                            exp = (byte)(exp - (Math.pow(2, 4) / 2 - 1)
                                    + (Math.pow(2, 8) / 2 - 1));
                            // IEEE754のFloat型のビット列に符号、指数部、仮数部のビット列を入れる
                            // Float = abbbbbbb bccccccc 00000000 00000000
                            tmpByte[0] = (byte)(((serviceData[0] & 0x08) << 4)
                                    + ((exp >>> 1) & 0x7f));
                            tmpByte[1] = (byte)(((exp & 0x01) << 7) + (serviceData[1] & 0x7f));
                            tmpByte[2] = 0;
                            tmpByte[3] = 0;
                            temperature = ByteBuffer.wrap(tmpByte).getFloat();
                            if (temperature < -255.0) {
                                temperature = -255.0;
                            } else if (temperature > 255.0) {
                                temperature = 255.0;
                            }
                            break;
                        case 0x02:
                            // serviceData[] = xxxxbbbb cccccccc
                            // xxxx = サービスID
                            // bbbb = 指数部
                            // cccccccc = 仮数部
                            // 4ビット指数部を8ビット指数部に変換
                            exp = (byte)(serviceData[0] & 0x0f);
                            exp = (byte)(exp - (Math.pow(2, 4) / 2 - 1) + (Math.pow(2, 8) / 2 - 1));
                            // IEEE754のFloat型のビット列に指数部、仮数部のビット列を入れる
                            // Float = 0bbbbbbb bccccccc c0000000 00000000
                            tmpByte[0] = (byte)((exp >>> 1) & 0x7f);
                            tmpByte[1] = (byte)((exp << 7) + ((serviceData[1] >>> 1) & 0x7f));
                            tmpByte[2] = (byte)(serviceData[1] << 7);
                            tmpByte[3] = 0;
                            humidity = ByteBuffer.wrap(tmpByte).getFloat();
                            if (humidity < 0) humidity = 0;
                            else if (humidity > 255.5) humidity = 255.5;
                            break;
                        case 0x04:
                            // serviceData[] = xxxxabbb bbbbbbbb
                            // xxxx = サービスID
                            // a = 充電要否（0=充電不要／1=順電必要）
                            // bbbbbbbbbbb = 電池残量×10
                            over24voltage = (0x01 & (serviceData[0] >>> 3)) == 1 ? false: true;
                            byte[] batteryLevelBytes = {
                                    serviceData[1], (byte)(0x07 & serviceData[0])
                            };
                            ByteBuffer batteryLevelByteBuffer = ByteBuffer.wrap(batteryLevelBytes);
                            batteryLevelByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                            batteryLevel = (double)(batteryLevelByteBuffer.getShort()) / 10.0;
                            break;
                        case 0x06:
                            // serviceData[] = xxxxabbb bbbbbbbb
                            // xxxx = サービスID
                            // a = 開閉状態（0=閉／1=開）
                            // bbbbbbbbbbb = 開閉回数（0～2047）
                            magnetic = (0x01 & (serviceData[0] >>> 3)) == 1 ? false: true;
                            byte[] magneticCountBytes = {
                                    serviceData[1], (byte)(0x07 & serviceData[0]), 0, 0
                            };
                            ByteBuffer magneticCountByteBuffer =
                                    ByteBuffer.wrap(magneticCountBytes);
                            magneticCountByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                            magneticCount = magneticCountByteBuffer.getInt();
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            temperature = 0;
            humidity = 0;
            over24voltage = false;
            batteryLevel = 0;
            magnetic = false;
            magneticCount = 0;
        }
    }
}
