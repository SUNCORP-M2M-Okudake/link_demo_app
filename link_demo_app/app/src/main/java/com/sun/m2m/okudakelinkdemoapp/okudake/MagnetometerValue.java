/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

/**
 * 磁気センサーデータ
 */
public class MagnetometerValue {

    /** データ取得時刻 */
    public long latestDataTime;
    /** 磁気の検出あり／なし */
    public boolean magnetic;
    /** センサーの有効／無効 */
    public boolean enable;

    /**
     * コンストラクタ
     */
    public MagnetometerValue() {
        resetValue();
    }

    /**
     * 保持しているデータのリセット
     */
    public void resetValue() {
        latestDataTime = 0;
        magnetic = false;
        enable = false;
    }

    /**
     * オブジェクトのコピー
     * @return コピーしたオブジェクト
     */
    public MagnetometerValue clone() {
        MagnetometerValue cloneObj = new MagnetometerValue();
        cloneObj.latestDataTime = latestDataTime;
        cloneObj.magnetic = magnetic;
        cloneObj.enable = enable;
        return cloneObj;
    }

    /**
     * 受信した時期の検出有無をセット
     * @param data　受信したデータ
     */
    public void setValue(byte[] data) {
        if (data.length == 1) {
            latestDataTime = System.currentTimeMillis();

            if (data[0] == 0) {
                magnetic = true;
            } else if (data[0] == 1) {
                magnetic = false;
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
