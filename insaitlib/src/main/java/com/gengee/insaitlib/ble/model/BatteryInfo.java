package com.gengee.insaitlib.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

/**
 * 电池信息
 * 
 */
@Keep
public class BatteryInfo implements Parcelable {
	public static final int STATE_CHARGING = 1; // 电池状态，正在充电中
	public static final int STATE_NORMAL = 0; // 电池状态，未充电
	private int state; // 充电状态
	private int volume; // 电量值，范围0-100

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public BatteryInfo() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.state);
		dest.writeInt(this.volume);
	}

	public void readFromParcel(Parcel source) {
		this.state = source.readInt();
		this.volume = source.readInt();
	}

	protected BatteryInfo(Parcel in) {
		this.state = in.readInt();
		this.volume = in.readInt();
	}

	public static final Creator<BatteryInfo> CREATOR = new Creator<BatteryInfo>() {
		@Override
		public BatteryInfo createFromParcel(Parcel source) {
			return new BatteryInfo(source);
		}

		@Override
		public BatteryInfo[] newArray(int size) {
			return new BatteryInfo[size];
		}
	};
}
