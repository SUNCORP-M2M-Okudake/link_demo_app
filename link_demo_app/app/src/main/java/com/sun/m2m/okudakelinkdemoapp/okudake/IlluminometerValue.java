/**
 * Copyright © 2020 SUNCORPORATION All rights reserved.
 *
 * Creation Date : July 14th, 2020
 */

package com.sun.m2m.okudakelinkdemoapp.okudake;

/**
 * 照度センサーデータ
 */
public class IlluminometerValue {
    /** データ更新間隔の下限値（ミリ秒） */
    public static final int MIN_PERIOD = 200;
    /** データ更新間隔の上限値（ミリ秒） */
    public static final int MAX_PERIOD = 60000;
    /** 照度の上限値（LUX） */
    public static final double ILLUMINANCE_MAX = 83865.60;

    /**
     * 受信したセンサーデータから照度（LUX）を算出するために使用する値
     * [0] ... 分解能（lux per LSB）
     * [1] ... 最大計測照度
     */
    private double[][] conversionValues = {
            {0.01	, 40.95		},
            {0.02	, 81.90		},
            {0.04	, 163.80	},
            {0.08	, 327.60	},
            {0.16	, 655.20	},
            {0.32	, 1310.40	},
            {0.64	, 2620.80	},
            {1.28	, 5241.60	},
            {2.56	, 10483.20	},
            {5.12	, 20866.40	},
            {10.14	, 41932.80	},
            {20.48	, 83865.60	}
    };

    /** データ取得時刻 */
    public long latestDataTime;
    /** 照度（LUX） */
    public double illuminance;
    /** センサーの有効／無効 */
    public boolean enable;
    /** データ更新間隔 */
    public int period;

    /**
     * コンストラクタ
     */
    public IlluminometerValue() {
        resetValue();
    }

    /**
     * 保持しているデータのリセット
     */
    public void resetValue() {
        latestDataTime = 0;
        illuminance = 0;
        enable = false;
        period = 200;
    }

    /**
     * オブジェクトのコピー
     * @return コピーしたデータ
     */
    public IlluminometerValue clone() {
        IlluminometerValue cloneObj = new IlluminometerValue();
        cloneObj.latestDataTime = latestDataTime;
        cloneObj.illuminance = illuminance;
        cloneObj.enable = enable;
        cloneObj.period = period;
        return cloneObj;
    }

    /**
     * 受信した照度データをセット
     * @param data　受信したデータ
     */
    public void setValue(byte[] data) {
        if (data.length == 2) {
            latestDataTime = System.currentTimeMillis();

            byte val = (byte)((data[1] >> 4) & 0x0F);
            data[1] = (byte)(data[1] & 0x0f);
            if (val > conversionValues.length) {
                illuminance = ILLUMINANCE_MAX;
            } else {
                illuminance = conversionValues[val][0] * Utility.bytesToInt(data);
                if (illuminance > conversionValues[val][1]) {
                    illuminance = conversionValues[val][1];
                }
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

    /**
     * 受信したデータ更新間隔をセット
     * @param data　受信したデータ
     */
    public void setPeriod(byte[] data) {
        if (data.length == 2) {
            int newPeriod = Utility.bytesToInt(data);
            if ((newPeriod >= MIN_PERIOD) && (newPeriod <= MAX_PERIOD)) {
                period = newPeriod;
            }
        }
    }
}
