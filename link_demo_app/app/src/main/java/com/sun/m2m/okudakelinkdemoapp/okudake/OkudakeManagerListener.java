/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

import java.util.List;

/**
 * OkudakeManagerから呼ばれるコールバック
 */
public interface OkudakeManagerListener {
    /**
     * おくだけセンサーのAdvertiseData取得時のコールバック
     * @param data　検出したビーコンデータ
     */
    void onScannedDevice(BeaconData data);

    /**
     * おくだけセンサーのスキャン結果を定期的に返す時のコールバック
     * @param datas 検出したビーコンデータ
     */
    void onScannedDevices(List<BeaconData> datas);

    /**
     * おくだけセンサーに接続された時のコールバック
     */
    void onConnected();

    /**
     * おくだけセンサーとの接続切断時のコールバック
     */
    void onDisconnected();

    /**
     * 端末設定データ取得時のコールバック
     * @param value 端末設定データ
     */
    void onReceivedDevicePreferences(DevicePreferenceValue value);

    /**
     * 温湿度センサーデータ取得時のコールバック
     * @param value 温湿度センサーデータ
     */
    void onReceivedThermohygrometer(ThermohygrometerValue value);

    /**
     * 照度センサーデータ取得時のコールバック
     * @param value 照度センサーデータ
     */
    void onReceivedIlluminometer(IlluminometerValue value);

    /**
     * 加速度センサーデータ取得時のコールバック
     * @param value 加速度センサーデータ
     */
    void onReceivedAccelerometer(AccelerometerValue value);

    /**
     * 磁気センサーデータ取得時のコールバック
     * @param value 磁気センサーデータ
     */
    void onReceivedMagnetometer(MagnetometerValue value);

    /**
     * バッテリーデータ取得時のコールバック
     * @param value バッテリーデータ
     */
    void onReceivedBattery(BatteryValue value);

    /**
     * LED状態取得時のコールバック
     * @param value LED状態
     */
    void onReceivedLed(LedValue value);
}
