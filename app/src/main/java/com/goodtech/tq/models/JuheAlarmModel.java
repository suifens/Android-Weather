package com.goodtech.tq.models;

/**
 * com.goodtech.tq.models
 */
public class JuheAlarmModel {
    /**
     * "id": "41164",
     * "title": "江苏省张家港市发布雷暴大风黄色预警",
     * "level": "黄色",
     * "type": "雷暴大风",
     * "time": "2021-09-01 14:15:19",
     * "province": "江苏省",
     * "city": "苏州市",
     * "district": "张家港市",
     * "content": "张家港市气象台2021年09月01日14时12分发布雷暴大风黄色预警信号：受对流云团影响，预计今天下午到夜里我市部分地区将出现雷电（尤其是南丰、常阴沙、塘桥等地区），并可能伴有7-9级雷暴大风、20毫米/小时以上短时强降水等强对流天气，请加强防范。（预警信息来源：国家预警信息发布中心）"
     *
     */
    private String id;
    private String title;
    private String level;
    private String type;
    private String time;
    private String province;
    private String city;
    private String district;
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
