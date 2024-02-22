package com.gengee.insaitlib.ble.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import com.gengee.insaitlib.ble.util.BleUtil;
import com.gengee.insaitlib.ble.util.ByteUtil;
import com.gengee.insaitlib.ble.util.LogUtil;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * 护腿板设备信息
 */
@Keep
public class ScanBleDevice implements Comparable<ScanBleDevice>, Parcelable {

    private BluetoothDevice bluetoothDevice;
    private String deviceAddress;
    private String pairAddress;
    private String firmwareVersion;
    private String hardwareVersion;
    private String deviceName;
    private String userId;
    private BatteryInfo batteryInfo;
    private int rssi;//信号强度，值为负数，越接近0信号越强
    private final ArrayList<String> rssArray = new ArrayList<>();
    public byte[] scanRecord;
//    public int state;//01:已配对，03：已绑定
    public long lastAdvertisingTime;//上次获取时间
    private boolean isBinded;
    private boolean isPaired;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }
    
    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;

        if (bluetoothDevice != null) {
            this.deviceAddress = bluetoothDevice.getAddress();
            this.deviceName = bluetoothDevice.getName();
        }
    }
    
    public int getRssi() {
        return rssi;
    }

    public int getAvgRssi() {
        int length = rssArray.size();
        int avgCount = Math.min(length, 3);

        int count = 0;
        for (int i = 0; i < avgCount; i++) {
            count += Integer.parseInt(rssArray.get(length - i - 1));
        }

        return avgCount > 0 ? (count / avgCount) : 0;
    }
    
    public void setRssi(int rssi) {
        this.rssi = rssi;

        rssArray.add(String.valueOf(rssi));
    }
    
    public String getDeviceName() {
        String name = deviceName;
        if(TextUtils.isEmpty(name)&&scanRecord!=null){
            final BleAdvertisedData badata = BleUtil.parseAdertisedData(scanRecord);
            name = badata.getName();
        }
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BatteryInfo getBatteryInfo() {
        return batteryInfo;
    }

    public void setBatteryInfo(BatteryInfo batteryInfo) {
        this.batteryInfo = batteryInfo;
    }

    public String getAddress() {
        return deviceAddress;
    }

    public void setPairAddress(String address) {
        this.pairAddress = address;
    }

    public String getPairAddress() {
        return pairAddress;
    }
    
    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
        if (scanRecord.length >= 17 && ByteUtil.getBit(scanRecord[17], 7) == 1) {
            String address = ByteUtil.getMacAddress(scanRecord);
            // LogUtil.e("TAG", "setScanRecord: " + address + " mac = " + bluetoothDevice.getAddress());
            if (!TextUtils.isEmpty(address)) {
                setPairAddress(address);
            }
        }
    }
    
//    public int getState() {
//        return state;
//    }
//
//    public void setState(int state) {
//        this.state = state;
//    }

    public long getLastAdvertisingTime() {
        return lastAdvertisingTime;
    }

    public void setLastAdvertisingTime(long lastTime) {
        this.lastAdvertisingTime = lastTime;
    }

    public void setBinded(boolean binded) {
        this.isBinded = binded;
    }

    public boolean getBinded() {
        return isBinded;
    }

    public void setPaired(boolean paired) {
        this.isPaired = paired;
    }

    public boolean getPaired() {
        return isPaired;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (!(obj instanceof ScanBleDevice)) {
            return false;
        }
        ScanBleDevice another = (ScanBleDevice) obj;
        String anotherAddreee = another.getAddress();
        return !TextUtils.isEmpty(anotherAddreee) && another.getAddress().equals(getAddress());
    }

    @Override
    public int compareTo(ScanBleDevice o) {
        return (int) (o.getAvgRssi() - this.getAvgRssi());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.bluetoothDevice, flags);
        dest.writeString(this.deviceAddress);
        dest.writeString(this.pairAddress);
        dest.writeString(this.firmwareVersion);
        dest.writeString(this.hardwareVersion);
        dest.writeString(this.deviceName);
        dest.writeString(this.userId);
        dest.writeParcelable((Parcelable) this.batteryInfo, flags);
        dest.writeInt(this.rssi);
        dest.writeList(this.rssArray);
        dest.writeByteArray(this.scanRecord);
//        dest.writeInt(this.state);
        dest.writeLong(this.lastAdvertisingTime);
        dest.writeByte(this.isBinded ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isPaired ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.bluetoothDevice = source.readParcelable(BluetoothDevice.class.getClassLoader());
        this.deviceAddress = source.readString();
        this.pairAddress = source.readString();
        this.firmwareVersion = source.readString();
        this.hardwareVersion = source.readString();
        this.deviceName = source.readString();
        this.userId = source.readString();
        this.batteryInfo = source.readParcelable(BatteryInfo.class.getClassLoader());
        this.rssi = source.readInt();
        source.readList(this.rssArray, String.class.getClassLoader());
        this.scanRecord = source.createByteArray();
//        this.state = source.readInt();
        this.lastAdvertisingTime = source.readLong();
        this.isBinded = source.readByte() != 0;
        this.isPaired = source.readByte() != 0;
    }

    public ScanBleDevice() {
    }

    protected ScanBleDevice(Parcel in) {
        this.bluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.deviceAddress = in.readString();
        this.pairAddress = in.readString();
        this.firmwareVersion = in.readString();
        this.hardwareVersion = in.readString();
        this.deviceName = in.readString();
        this.userId = in.readString();
        this.batteryInfo = in.readParcelable(BatteryInfo.class.getClassLoader());
        this.rssi = in.readInt();
        in.readList(this.rssArray, String.class.getClassLoader());
        this.scanRecord = in.createByteArray();
//        this.state = in.readInt();
        this.lastAdvertisingTime = in.readLong();
        this.isBinded = in.readByte() != 0;
        this.isPaired = in.readByte() != 0;
    }

    public static final Parcelable.Creator<ScanBleDevice> CREATOR = new Parcelable.Creator<ScanBleDevice>() {
        @Override
        public ScanBleDevice createFromParcel(Parcel source) {
            return new ScanBleDevice(source);
        }

        @Override
        public ScanBleDevice[] newArray(int size) {
            return new ScanBleDevice[size];
        }
    };
}
