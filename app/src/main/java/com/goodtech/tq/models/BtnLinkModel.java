package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * com.goodtech.tq.models
 */
public class BtnLinkModel implements Serializable, Parcelable {

    /**
     * tempType : 免费打车券
     * usingType : AD_1
     * imgPath : https://app.yiguxm.com/pic/yztqyhq/pic_dache216 _76.png
     * H5link : https://kzurl10.cn/Z9Nks
     */
    private String tempType;
    private String usingType;
    private String imgPath;
    private String H5link;

    public String getTempType() {
        return tempType;
    }

    public void setTempType(String tempType) {
        this.tempType = tempType;
    }

    public String getUsingType() {
        return usingType;
    }

    public void setUsingType(String usingType) {
        this.usingType = usingType;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getH5link() {
        return H5link;
    }

    public void setH5link(String h5link) {
        H5link = h5link;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tempType);
        dest.writeString(this.usingType);
        dest.writeString(this.imgPath);
        dest.writeString(this.H5link);
    }

    public void readFromParcel(Parcel source) {
        this.tempType = source.readString();
        this.usingType = source.readString();
        this.imgPath = source.readString();
        this.H5link = source.readString();
    }

    public BtnLinkModel() {
    }

    protected BtnLinkModel(Parcel in) {
        this.tempType = in.readString();
        this.usingType = in.readString();
        this.imgPath = in.readString();
        this.H5link = in.readString();
    }

    public static final Parcelable.Creator<BtnLinkModel> CREATOR = new Parcelable.Creator<BtnLinkModel>() {
        @Override
        public BtnLinkModel createFromParcel(Parcel source) {
            return new BtnLinkModel(source);
        }

        @Override
        public BtnLinkModel[] newArray(int size) {
            return new BtnLinkModel[size];
        }
    };
}
