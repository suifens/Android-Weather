package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * com.goodtech.tq.models
 */
public class JuheLifeModel implements Parcelable {

    private LifeItemBean kongtiao;
    private LifeItemBean guomin;
    private LifeItemBean shushidu;
    private LifeItemBean chuanyi;
    private LifeItemBean diaoyu;
    private LifeItemBean ganmao;
    private LifeItemBean ziwaixian;
    private LifeItemBean xiche;
    private LifeItemBean yundong;
    private LifeItemBean daisan;

    public LifeItemBean getKongtiao() {
        return kongtiao;
    }

    public void setKongtiao(LifeItemBean kongtiao) {
        this.kongtiao = kongtiao;
    }

    public LifeItemBean getGuomin() {
        return guomin;
    }

    public void setGuomin(LifeItemBean guomin) {
        this.guomin = guomin;
    }

    public LifeItemBean getShushidu() {
        return shushidu;
    }

    public void setShushidu(LifeItemBean shushidu) {
        this.shushidu = shushidu;
    }

    public LifeItemBean getChuanyi() {
        return chuanyi;
    }

    public void setChuanyi(LifeItemBean chuanyi) {
        this.chuanyi = chuanyi;
    }

    public LifeItemBean getDiaoyu() {
        return diaoyu;
    }

    public void setDiaoyu(LifeItemBean diaoyu) {
        this.diaoyu = diaoyu;
    }

    public LifeItemBean getGanmao() {
        return ganmao;
    }

    public void setGanmao(LifeItemBean ganmao) {
        this.ganmao = ganmao;
    }

    public LifeItemBean getZiwaixian() {
        return ziwaixian;
    }

    public void setZiwaixian(LifeItemBean ziwaixian) {
        this.ziwaixian = ziwaixian;
    }

    public LifeItemBean getXiche() {
        return xiche;
    }

    public void setXiche(LifeItemBean xiche) {
        this.xiche = xiche;
    }

    public LifeItemBean getYundong() {
        return yundong;
    }

    public void setYundong(LifeItemBean yundong) {
        this.yundong = yundong;
    }

    public LifeItemBean getDaisan() {
        return daisan;
    }

    public void setDaisan(LifeItemBean daisan) {
        this.daisan = daisan;
    }

    public static class LifeItemBean implements Parcelable {
        private String v;
        private String des;

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }

        public String getDes() {
            return des;
        }

        public void setDes(String des) {
            this.des = des;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.v);
            dest.writeString(this.des);
        }

        public void readFromParcel(Parcel source) {
            this.v = source.readString();
            this.des = source.readString();
        }

        public LifeItemBean() {
        }

        protected LifeItemBean(Parcel in) {
            this.v = in.readString();
            this.des = in.readString();
        }

        public static final Creator<LifeItemBean> CREATOR = new Creator<LifeItemBean>() {
            @Override
            public LifeItemBean createFromParcel(Parcel source) {
                return new LifeItemBean(source);
            }

            @Override
            public LifeItemBean[] newArray(int size) {
                return new LifeItemBean[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.kongtiao, flags);
        dest.writeParcelable(this.guomin, flags);
        dest.writeParcelable(this.shushidu, flags);
        dest.writeParcelable(this.chuanyi, flags);
        dest.writeParcelable(this.diaoyu, flags);
        dest.writeParcelable(this.ganmao, flags);
        dest.writeParcelable(this.ziwaixian, flags);
        dest.writeParcelable(this.xiche, flags);
        dest.writeParcelable(this.yundong, flags);
        dest.writeParcelable(this.daisan, flags);
    }

    public void readFromParcel(Parcel source) {
        this.kongtiao = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.guomin = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.shushidu = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.chuanyi = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.diaoyu = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.ganmao = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.ziwaixian = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.xiche = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.yundong = source.readParcelable(LifeItemBean.class.getClassLoader());
        this.daisan = source.readParcelable(LifeItemBean.class.getClassLoader());
    }

    public JuheLifeModel() {
    }

    protected JuheLifeModel(Parcel in) {
        this.kongtiao = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.guomin = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.shushidu = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.chuanyi = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.diaoyu = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.ganmao = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.ziwaixian = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.xiche = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.yundong = in.readParcelable(LifeItemBean.class.getClassLoader());
        this.daisan = in.readParcelable(LifeItemBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<JuheLifeModel> CREATOR = new Parcelable.Creator<JuheLifeModel>() {
        @Override
        public JuheLifeModel createFromParcel(Parcel source) {
            return new JuheLifeModel(source);
        }

        @Override
        public JuheLifeModel[] newArray(int size) {
            return new JuheLifeModel[size];
        }
    };
}
