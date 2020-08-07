package com.sun.m2m.okudakelinkdemoapp.okudake;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * おくだけセンサーの処理で共通で使用する変数・関数
 */
public class Utility {
    /**
     * 2バイトもしくは4バイトのbyte配列（Little Endian）をint変数に変換
     * 2バイトの場合は符号なし整数とする
     * @param bytes intもしくはunsigned shortに変換するバイト配列
     * @return バイト配列から変換したint値
     */
    public static int bytesToInt(byte[] bytes) {
        if ((bytes.length == 2) || (bytes.length == 4)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.put(bytes);
            byteBuffer.position(0);
            int value = byteBuffer.getInt();
            return value;
        } else {
            return 0;
        }
    }

    /**
     * 2バイトのbyte配列（Little Endian）をshort変数に変換
     * @param bytes signed shortに変換するバイト配列
     * @return バイト配列から変換したshort値
     */
    public static short bytesToShort(byte[] bytes) {
        if (bytes.length == 2) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(2);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.put(bytes);
            byteBuffer.position(0);
            short value = byteBuffer.getShort();
            return value;
        } else {
            return 0;
        }
    }

    /**
     * unsigned shortの値を2バイト配列（Little Endian）に変換
     * @param value バイト配列に変換するunsigned short値
     * @return 変換したバイト配列（Little Endian）
     */
    public static byte[] intToUnsignedShortBytes(int value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(value);
        byteBuffer.position(0);
        byte[] bytes = new byte[2];
        byteBuffer.get(bytes);
        return bytes;
    }

    /**
     * shortの値を2バイト配列（Little Endian）に変換
     * @param value バイト配列に変換するshort値
     * @return 変換したバイト配列（Little Endian）
     */
    public static byte[] intToShortBytes(short value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putShort(value);
        byteBuffer.position(0);
        byte[] bytes = new byte[2];
        byteBuffer.get(bytes);
        return bytes;
    }
}
