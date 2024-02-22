package com.gengee.insaitlib.ble.util;

import android.util.Log;

import java.util.Formatter;

public class ByteUtil {

    /**
     * 解析个数，2个字节，低位开始。
     */
    public static int getIntBy4Byte(byte[] data, int start) {
        return ((0xFF & data[start])
                | (0xFF00 & data[start + 1] << 8)
                | (0xFF0000 & data[start + 2] << 16)
                | (0xFF000000 & data[start + 3] << 24));
    }

    public static float getFloat1Byte(byte[] data, int start) {
        float frequency = (0xFF & data[start]);

        return (float) (frequency / 10.0);
    }

    /**
     * 解析个数，2个字节，低位开始。
     */
    public static int getShortBy2Byte(byte[] data, int start) {
        short value = (short) ((0xFF & data[start]) | (0xFF00 & data[start + 1] << 8));
        return (int) value;
    }

    /**
     * 解析个数，2个字节，低位开始。
     */
    public static int getShortBy1Byte(byte[] data, int start) {
        short value = (short) ((0xFF & data[start]));
        return (int) value;
    }

    public static float getFloatBy2Byte(byte[] data, int start) {
        float value = ((0xFF & data[start]) | (0xFF00 & data[start + 1] << 8));
        return (float) (value / 10.0);
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static int getBit(byte b, int index) {
//        StringBuilder string = new StringBuilder();
//        for (int i = 0; i < 8; i++) {
//            string.append((b >> i) & 0x01);
//        }
//        Log.e("getBit", "byte = " + Integer.toHexString(b & 0xFF) + " getBit: " + string.toString());
        return (b >> index) & 0x01;
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 将一byte数组插入到dest数组从dest的startIndex位置开始
     *
     * @param values     要插入的数据
     * @param dest       插入的目标数组
     * @param startIndex 插入位置
     * @return
     */
    public static byte[] copyBytesToBytes(byte[] values, byte[] dest, int startIndex) {
        if (values == null) {
            return null;
        }
        if (dest == null) {
            return null;
        }
        for (int index = 0; index < values.length; ++index) {
            if ((startIndex + index) < dest.length) {
                dest[startIndex + index] = values[index];
            }
        }
        return dest;
    }

    public static String getMacAddress(byte[] bytes) {
        // Byte 11 - 16
        int startIndex = 11;
        char[] hexChars = new char[17];
        for (int i = 0; i < 6; i++) {
            int v = bytes[i + startIndex] & 0xFF;
            hexChars[i * 3] = hexArray[v >>> 4];
            hexChars[i * 3 + 1] = hexArray[v & 0x0F];
            if (i != 5) hexChars[i * 3 + 2] = ":".toCharArray()[0];
        }
        return (new String(hexChars)).toUpperCase();
    }

    /** 获取固件版本 */
    public static String getFirmwareVersion(byte[] data, int start) {
        int length = data.length;
        byte[] bytes = new byte[length];
        System.arraycopy(data, start, bytes, 0, length);
        return new String(bytes);
    }

    /** 获取硬件版本 */
    public static String getHardwareVersion(byte[] data, int start) {

        byte[] bytes = new byte[data.length];
        System.arraycopy(data, start, bytes, 0, data.length);
        String version = new String(bytes);
        return strLast(version, "-");
    }

    public static String getVersion(byte[] data, int start) {

        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = data[i + start];
        }
        String res = new String(bytes);
        return res;
    }

    public static String strLast(String str, String delimiter) {
        if(str!="" && str!=null){
            int lastIndex = str.lastIndexOf(delimiter);
            String newStr=str;  //默认字符串是str
            if(lastIndex>-1){  //如果找到了最后一个字符
                newStr = str.substring(0, lastIndex);
            }
            return newStr;
        }
        return str;
    }

    /**
     * 解析时间戳，6字节
     */
    public static long resolveTimeMillsBy6Byte(byte[] data, int start) {
        return ((long) data[start] & 0xFFL)
                | ((long) data[start + 1] << 8 & 0xFF00L)
                | ((long) data[start + 2] << 16 & 0xFF0000L)
                | ((long) data[start + 3] << 24 & 0xFF000000L)
                | ((long) data[start + 4] << 32 & 0xFF00000000L)
                | ((long) data[start + 5] << 40 & 0xFF0000000000L);
    }

    /**
     * 这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String bytesToHex(byte[] bytes, int startIndex) {
        int maxLength = bytes.length - startIndex;
        char[] hexChars = new char[maxLength * 2];
        for (int j = 0; j < maxLength; j++) {
            int v = bytes[j + startIndex] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getPairMacAddress(byte[] bytes) {
        if (bytes.length < 22) {
            return null;
        }
        // Byte 17 - 22
        int startIndex = 17;
        char[] hexChars = new char[22];
        for (int i = 0; i < 6; i++) {
            int v = bytes[i + startIndex] & 0xFF;
            hexChars[i * 3] = hexArray[v >>> 4];
            hexChars[i * 3 + 1] = hexArray[v & 0x0F];
            if (i != 5) hexChars[i * 3 + 2] = ":".toCharArray()[0];
        }
        return new String(hexChars);
    }


    public static byte loUint16(short v) {
        return (byte) (v & 0xFF);
    }

    public static byte hiUint16(short v) {
        return (byte) (v >> 8);
    }

    public static short buildUint16(byte hi, byte lo) {
        return (short) ((hi << 8) + (lo & 0xff));
    }

    /**
     * 将二进制流转成十六进制字符串
     */
    public static String BytetohexString(byte[] b, int len) {
        StringBuilder sb = new StringBuilder(b.length * (2 + 1));
        Formatter formatter = new Formatter(sb);

        for (int i = 0; i < len; i++) {
            if (i < len - 1)
                formatter.format("%02X:", b[i]);
            else
                formatter.format("%02X", b[i]);

        }
        formatter.close();

        return sb.toString();
    }

    /**
     * 将二进制流转成十六进制字符串
     */
    static String BytetohexString(byte[] b, boolean reverse) {
        StringBuilder sb = new StringBuilder(b.length * (2 + 1));
        Formatter formatter = new Formatter(sb);

        if (!reverse) {
            for (int i = 0; i < b.length; i++) {
                if (i < b.length - 1)
                    formatter.format("%02X:", b[i]);
                else
                    formatter.format("%02X", b[i]);

            }
        } else {
            for (int i = (b.length - 1); i >= 0; i--) {
                if (i > 0)
                    formatter.format("%02X:", b[i]);
                else
                    formatter.format("%02X", b[i]);

            }
        }
        formatter.close();

        return sb.toString();
    }

    /**
     * Convert hex String to Byte
     * 将十六进制字符串转换为字节
     */
    public static int hexStringtoByte(String sb, byte[] results) {

        int i = 0;
        boolean j = false;

        if (sb != null) {
            for (int k = 0; k < sb.length(); k++) {
                if (((sb.charAt(k)) >= '0' && (sb.charAt(k) <= '9')) || ((sb.charAt(k)) >= 'a' && (sb.charAt(k) <= 'f'))
                        || ((sb.charAt(k)) >= 'A' && (sb.charAt(k) <= 'F'))) {
                    if (j) {
                        results[i] += (byte) (Character.digit(sb.charAt(k), 16));
                        i++;
                    } else {
                        results[i] = (byte) (Character.digit(sb.charAt(k), 16) << 4);
                    }
                    j = !j;
                }
            }
        }
        return i;
    }

    public static boolean isAsciiPrintable(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (isAsciiPrintable(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

}
