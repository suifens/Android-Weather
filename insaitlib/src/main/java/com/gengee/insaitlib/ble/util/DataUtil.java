package com.gengee.insaitlib.ble.util;

import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.gengee.insaitlib.ble.model.NineAxisData;

public class DataUtil {
    
    // For converting
    private static final String STRING_HEX_NUM = "0123456789ABCDEF";
    private static final char[] CHAR_ARRAY_HEX_NUM = STRING_HEX_NUM.toCharArray();
    
    // Regular expression
    private static final String UUID_PATTERN = "[a-fA-F[0-9]]{32}";
    private static final String UUID_PATTERN_SP = "[a-fA-F[0-9]]{8}-[a-fA-F[0-9]]{4}-[a-fA-F[0-9]]{4}-[a-fA-F[0-9]]{4}-[a-fA-F[0-9]]{12}";
    private static final String TAG = "Util";
    
    /*
     * Thanks for maybeWeCouldStealAVan from StackOverflow forum
     */
    static public String convertByteToHexString(byte[] bytes) {
        
        if (null == bytes) {
            return null;
        }
        
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            // >>> Unsigned right shift
            hexChars[i * 2] = CHAR_ARRAY_HEX_NUM[v >>> 4];
            hexChars[i * 2 + 1] = CHAR_ARRAY_HEX_NUM[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    static public byte[] convertHexStringToByte(String str) {
        
        if (null == str) {
            return null;
        }
        
        int originalLength = str.length();
        String newStr = str.toUpperCase();
        
        if (0 != originalLength % 2) {
            newStr = "0" + str.toUpperCase();
        }
        
        Log.d(TAG, "convertHexStringToByte() : Original input string = " + str + " New = " + newStr);
        
        char[] charBuffer = newStr.toCharArray();
        int length = charBuffer.length;
        
        Log.d(TAG, "convertHexStringToByte() : charBuffer size = " + length);
        
        byte[] byteBuffer = new byte[length >> 1];
        length = byteBuffer.length;
        
        Log.d(TAG, "convertHexStringToByte() : byteBuffer size = " + length);
        
        for (int i = 0; i < length; i++) {
            int lV = STRING_HEX_NUM.indexOf((int) charBuffer[i * 2]);
            int rV = STRING_HEX_NUM.indexOf((int) charBuffer[i * 2 + 1]);
            
            Log.d(TAG, "lV = " + lV + " rV = " + rV);
            
            byteBuffer[i] = (byte) (((lV << 4) & 0x00F0) | (rV & 0x000F));
        }
        return byteBuffer;
    }
    
    static public int getTwoByteValueLittleEndian(byte[] bytes, int offset) {
        return (((int) bytes[offset + 1]) << 8) | (bytes[offset] & 0xFF);
    }
    
    static public int getFourByteValueLittleEndian(byte[] bytes, int offset) {
        return (bytes[offset + 3] << 24) + (bytes[offset + 2] << 16) + (bytes[offset + 1] << 8)
                + (bytes[offset] & 0xFF);
    }
    
    /*
     * Convert 32 bytes UUID to format
     * "8_BYTES - 4_BYTES - 4_BYTES - 4_BYTES - 12_BYTES"
     */
    static public String convertUuidFormatToHumanFriendly(String str) {
        
        int originalLength = str.length();
        String tmp = str;
        
        if (32 == originalLength) {
            tmp = str.substring(0, 8) + "-" + str.substring(8, 12) + "-" + str.substring(12, 16)
                    + "-" + str.substring(16, 20) + "-" + str.substring(20, 32);
        } else {
            Log.e(TAG, "convertUuidFormatToHumanFriendly() : Input length is invalid");
        }
        return tmp;
    }
    
    /*
     * Check if input string is a valid UUID
     */
    static public byte[] checkAndConvertUuid(String str) {
        
        if (null == str)
            return null;
        
        int length = str.length();
        String longStr = "";
        Pattern pattern;
        Matcher matcher;
        
        if (32 == length) {
            pattern = Pattern.compile(UUID_PATTERN_SP);
            matcher = pattern.matcher(str);
            
            if (matcher.matches()) {
                longStr = str;
            } else {
                Log.e(TAG, "checkAndConvertUuid() : Invalid UUID format ! Length = " + length);
                return null;
            }
        } else if (36 == length) {
            pattern = Pattern.compile(UUID_PATTERN_SP);
            matcher = pattern.matcher(str);
            
            if (matcher.matches()) {
                String[] tmp = str.split("-");
                
                for (int i = 0; i < tmp.length; i++) {
                    longStr = longStr.concat(tmp[i]);
                }
            } else {
                Log.e(TAG, "checkAndConvertUuid() : Invalid UUID format ! Length = " + length);
                return null;
            }
        } else {
            Log.e(TAG, "checkAndConvertUuid() : Invalid UUID format !");
            return null;
        }
        
        Log.d(TAG, "checkAndConvertUuid() : longStr = " + longStr);
        
        return convertHexStringToByte(longStr);
    }
    
    static public int strToInt(String input) {
        
        return (null != input) ? Integer.valueOf(input) : 0;
    }
    
    static public String intToDecString(int input) {
        return Integer.toString(input, 10);
    }
    
    static public String intToHexString(int input) {
        return Integer.toString(input, 16);
    }
    
    static public String longToHexString(long input) {
        return Long.toString(input, 16);
    }
    
    static public String longToDecString(long input) {
        return Long.toString(input, 10);
    }
    
    /*
     * https://gist.github.com/dealforest/1949873
     */
    static public byte[] aesEncrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }
    
    static public byte[] aesDecrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }
    
    static public long getUnixTime(String time, int timeZoneOffset) {
        
        if (null != time) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            // Currently, only support GMT+0800
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0800"));
            try {
                Date date = sdf.parse(time);
                // Seconds
                return (date.getTime() / 1000);
            } catch (ParseException e) {
                Log.e(TAG, "getUnixTime() : ParseException !!!");
            }
            
        }
        return 0;
    }
    
    static public String getUnixCurrentTimeDayPrecision(int base) {
        
        long currentTime = System.currentTimeMillis();
        String str = null;
        
        switch (base) {
            default:
                Log.e(TAG, "Unknown base = " + base);
            case 10:
                str = longToDecString(currentTime);
                break;
            case 16:
                str = longToHexString(currentTime);
                break;
        }
        
        return str.substring(str.length() - 8);
    }

    /**
     * 计算结果,格式:[timeStamp, a_x, a_y, a_z, gyo_x, gyo_y, gyo_z, hg_x, hg_y, hg_z]
     *
     * @param payload
     *            原始字节数据
     * @param data
     *            放置计算结果的数组，长度为 10
     * @return
     */
    private static final int OFFSET_DATA_PAYLOAD_ACC = 0;
    private static final int OFFSET_DATA_PAYLOAD_GYRO = 6;
    private static final int OFFSET_DATA_PAYLOAD_MAG = 12;
    private static final int OFFSET_DATA_PAYLOAD_TIMESTAMP = 12;

    public static NineAxisData getHighGData(byte[] payload, NineAxisData data) {
        data.timestamp = getTimeStamp(payload);
        data.ax = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_ACC);
        data.ay = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_ACC + 2);
        data.az = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_ACC + 4);
        data.gx = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_GYRO);
        data.gy = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_GYRO + 2);
        data.gz = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_GYRO + 4);

        if (payload.length > OFFSET_DATA_PAYLOAD_MAG + 6) {
            data.cx = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_MAG);
            data.cy = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_MAG + 2);
            data.cz = getTwoByteValueLittleEndian(payload, OFFSET_DATA_PAYLOAD_MAG + 4);
        }
        return data;
    }

    public static int getTimeStamp(byte[] payload) {
        int timestamp = getTwoByteValueLittleEndian(payload, payload.length - 2);
        timestamp = 0x0000FFFF & timestamp;
        return timestamp;
    }
    
    /**
     * 生成写入Characteristic的command
     *
     * @param commandType 命令类型 sub command
     * @param payload     要写入的值
     * @return command
     */
    public static byte[] makeCommand(byte commandType, byte[] payload) {
        byte[] value = new byte[payload.length + 2];
        value[0] = commandType;
        value[1] = (byte) payload.length;
        for (int i = 0; i < payload.length; i++) {
            value[i + 2] = payload[i];
        }
        return value;
    }
    
    /**
     * 将长度为 4 的字节数组转为float。低位在前。
     *
     * @param payload 长度 >= 4 的字节数组
     * @param start   开始的下标
     * @return
     */
    public static float byte2Float(byte[] payload, int start) {
        int i = (0xFF000000 & payload[start + 3] << 24)
                | (0xFF0000 & payload[start + 2] << 16)
                | (0xFF00 & payload[start + 1] << 8)
                | (0xFF & payload[start]);
        return Float.intBitsToFloat(i);
    }
    
    /**
     * 将长度为 4 的字节数组转为long。高位在前。
     *
     * @param payload 长度 >= 4 的字节数组
     * @param start   开始的下标
     * @return
     */
    public static long resolveTimeStamp(byte[] payload, int start) {
        return (0xFF000000 & payload[start + 3] << 24)
                | (0xFF0000 & payload[start + 2] << 16)
                | (0xFF00 & payload[start + 1] << 8)
                | (0xFF & payload[start]);
    }
    
    
    /**
     * 低位开始
     *
     * @param s
     * @return
     */
    public static byte[] shortToByte(short s) {
        byte[] shortBuf = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = i * 8;
            shortBuf[i] = (byte) ((s >>> offset) & 0xff);
        }
        return shortBuf;
    }
    
    public static short bytesToShort(byte[] byteNum) {
        short num = 0;
        for (int ix = 0; ix < 2; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }
    
    public static byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[4];
        for (int ix = 0; ix < 4; ++ix) {
            int offset = 32 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }
    
    public static int bytes2Int(byte[] byteNum) {
        int num = 0;
        for (int ix = 0; ix < 4; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }
    
    public static byte int2OneByte(int num) {
        return (byte) (num & 0x000000ff);
    }
    
    public static int oneByte2Int(byte byteNum) {
        return byteNum > 0 ? byteNum : (128 + (128 + byteNum));
    }
    
    public static byte[] timeMills2Bytes(long num) {
        byte[] byteNum = new byte[6];
        for (int ix = 0; ix < 6; ++ix) {
            int offset = ix * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xFF);
        }
        return byteNum;
    }
    
    /**
     * 解析时间戳，6字节
     *
     * @param data
     * @param start
     * @return
     */
    public static long resolveTimeMillsBy6Byte(byte[] data, int start) {
        long x = ((long) data[start] & 0xFFL)
                | ((long) data[start + 1] << 8 & 0xFF00L)
                | ((long) data[start + 2] << 16 & 0xFF0000L)
                | ((long) data[start + 3] << 24 & 0xFF000000L)
                | ((long) data[start + 4] << 32 & 0xFF00000000L)
                | ((long) data[start + 5] << 40 & 0xFF0000000000L);
        return x;
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
    
}
