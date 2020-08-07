/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

/**
 * バッテリーデータ
 */
public class BatteryValue {
    /** データ取得時刻 */
    public long latestDataTime;
    /** 2.4V以上／以下 */
    public boolean over24voltage;
    /** USB接続あり／なし */
    public boolean usbPlugged;
    /** センサーの有効／無効 */
    public boolean enable;

    /**
     * コンストラクタ
     */
    public BatteryValue() {
        resetValue();
    }

    /**
     * 保持しているデータのリセット
     */
    public void resetValue() {
        latestDataTime = 0;
        over24voltage = false;
        usbPlugged = false;
        enable = false;
    }

    /**
     * オブジェクトのコピー
     * @return コピーしたオブジェクト
     */
    public BatteryValue clone() {
        BatteryValue cloneObj = new BatteryValue();
        cloneObj.latestDataTime = latestDataTime;
        cloneObj.over24voltage = over24voltage;
        cloneObj.usbPlugged = usbPlugged;
        cloneObj.enable = enable;
        return cloneObj;
    }

    /**
     * 受信したバッテリーレベルのセット
     * @param data　受信したデータ
     */
    public void setLevel(byte[] data) {
        if (data.length == 1) {
            latestDataTime = System.currentTimeMillis();

            if (data[0] == 0) {
                over24voltage = false;
            } else if (data[0] == 1) {
                over24voltage = true;
            }
        }
    }

    /**
     * 受信したUSB接続状態のセット
     * @param data　受信したデータ
     */
    public void setUsbPlugged(byte[] data) {
        if (data.length == 1) {
            latestDataTime = System.currentTimeMillis();

            if (data[0] == 0) {
                usbPlugged = false;
            } else if (data[0] == 1) {
                usbPlugged = true;
            }
        }
    }

    /**
     * 受信したセンサーの有効／無効状態をセット
     * @param data　受信したデータ
     */
    public void setEnable(byte[] data) {
        if (data.length == 1) {
            if (data[0] == 0) {
                enable = false;
            } else if (data[0] == 1) {
                enable = true;
            }
        }
    }
}
