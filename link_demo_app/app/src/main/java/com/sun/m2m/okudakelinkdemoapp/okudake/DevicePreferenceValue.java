/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

/**
 * 機器設定
 */
public class DevicePreferenceValue {
    /** 無通信タイムアウト（分）　最小値 */
    public static final int MIN_IDLE_TIMEOUT = 0;
    /** 無通信タイムアウト（分）　最大値 */
    public static final int MAX_IDLE_TIMEOUT = 1440;
    /** ビーコン送信間隔（ミリ秒×1.6）　最小値 */
    public static final int MIN_BEACON_TRANSMITTING_INTERVAL = 160;
    /** ビーコン送信間隔（ミリ秒×1.6）　最大値 */
    public static final int MAX_BEACON_TRANSMITTING_INTERVAL = 4096;
    /** ビーコンデータ更新間隔（秒）　最小値 */
    public static final int MIN_BEACON_UPDATING_INTERVAL = 1;
    /** ビーコンデータ更新間隔（秒）　最大値 */
    public static final int MAX_BEACON_UPDATING_INTERVAL = 60;
    /** 送信出力（dBm×10）　最小値 */
    public static final int MIN_TX_POWER_LEVEL = -200;
    /** 送信出力（dBm×10）　最大値 */
    public static final int MAX_TX_POWER_LEVEL = 70;

    /** 無通信タイムアウト（分） */
    public int idleTimeout;
    /** ビーコン送信間隔（ミリ秒×1.6） */
    public int beaconTransmittingInterval;
    /** ビーコンデータ更新間隔（秒） */
    public int beaconUpdatingInterval;
    /** 送信出力（dBm×10） */
    public int txPowerLevel;

    /**
     * コンストラクタ
     */
    public DevicePreferenceValue() {
        resetValue();
    }

    /**
     * 保持しているデータのリセット
     */
    public void resetValue() {
        idleTimeout = 0;
        beaconTransmittingInterval = 1280;
        beaconUpdatingInterval = 4;
        txPowerLevel = 0;
    }

    /**
     * オブジェクトのコピー
     * @return コピーしたオブジェクト
     */
    public DevicePreferenceValue clone() {
        DevicePreferenceValue cloneObj = new DevicePreferenceValue();
        cloneObj.idleTimeout = idleTimeout;
        cloneObj.beaconTransmittingInterval = beaconTransmittingInterval;
        cloneObj.beaconUpdatingInterval = beaconUpdatingInterval;
        cloneObj.txPowerLevel = txPowerLevel;
        return cloneObj;
    }

    /**
     * 受信した無通信タイムアウトをセット
     * @param data　受信したデータ
     */
    public void setIdleTimeout(byte[] data) {
        if (data.length == 2) {
            int val = Utility.bytesToInt(data);
            if ((val >= MIN_IDLE_TIMEOUT) && (val <= MAX_IDLE_TIMEOUT)) {
                idleTimeout = val;
            }
        }
    }

    /**
     * 受信したビーコン送信間隔をセット
     * @param data　受信したデータ
     */
    public void setBeaconTransmittingInterval(byte[] data) {
        if (data.length == 2) {
            int val = Utility.bytesToInt(data);
            if ((val >= MIN_BEACON_TRANSMITTING_INTERVAL)
                    && (val <= MAX_BEACON_TRANSMITTING_INTERVAL)) {
                beaconTransmittingInterval = val;
            }
        }
    }

    /**
     * 受信したビーコンデータ更新時間をセット
     * @param data　受信したデータ
     */
    public void setBeaconUpdatingInterval(byte[] data) {
        if (data.length == 1) {
            if ((data[0] >= MIN_BEACON_UPDATING_INTERVAL)
                    && (data[0] <= MAX_BEACON_UPDATING_INTERVAL)) {
                beaconUpdatingInterval = data[0];
            }
        }
    }

    /**
     * 受信した送信出力をセット
     * @param data　受信したデータ
     */
    public void setTxPowerLevel(byte[] data) {
        if (data.length == 2) {
            int val = Utility.bytesToShort(data);
            if ((val >= MIN_TX_POWER_LEVEL) && (val <= MAX_TX_POWER_LEVEL)) {
                txPowerLevel = val;
            }
        }
    }

}
