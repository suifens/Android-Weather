package com.goodtech.tq.models.constellation;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * com.goodtech.tq.models
 */
public class ConsDayMode implements Parcelable {

    public String name;
    public String datetime;
    public String date;
    public String all;
    public String color;
    public String health;
    public String love;
    public String money;
    public int number;
    public String QFriend;
    public String summary;
    public String work;

    public ConsDayMode() {}

    protected ConsDayMode(Parcel in) {
        name = in.readString();
        datetime = in.readString();
        date = in.readString();
        all = in.readString();
        color = in.readString();
        health = in.readString();
        love = in.readString();
        money = in.readString();
        number = in.readInt();
        QFriend = in.readString();
        summary = in.readString();
        work = in.readString();
    }

    public static final Creator<ConsDayMode> CREATOR = new Creator<ConsDayMode>() {
        @Override
        public ConsDayMode createFromParcel(Parcel in) {
            return new ConsDayMode(in);
        }

        @Override
        public ConsDayMode[] newArray(int size) {
            return new ConsDayMode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(datetime);
        dest.writeString(date);
        dest.writeString(all);
        dest.writeString(color);
        dest.writeString(health);
        dest.writeString(love);
        dest.writeString(money);
        dest.writeInt(number);
        dest.writeString(QFriend);
        dest.writeString(summary);
        dest.writeString(work);
    }
}
