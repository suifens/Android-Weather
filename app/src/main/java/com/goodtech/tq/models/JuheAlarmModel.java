package com.goodtech.tq.models;

/**
 * 聚合数据天气预警模型类
 * 包含天气预警的详细信息，如预警类型、级别、内容等
 * 用于显示和管理天气预警信息
 */
public class JuheAlarmModel {
    /**
     * 预警信息示例：
     * "id": "41164",
     * "title": "江苏省张家港市发布雷暴大风黄色预警",
     * "level": "黄色",
     * "type": "雷暴大风",
     * "time": "2021-09-01 14:15:19",
     * "province": "江苏省",
     * "city": "苏州市",
     * "district": "张家港市",
     * "content": "张家港市气象台2021年09月01日14时12分发布雷暴大风黄色预警信号：受对流云团影响，预计今天下午到夜里我市部分地区将出现雷电（尤其是南丰、常阴沙、塘桥等地区），并可能伴有7-9级雷暴大风、20毫米/小时以上短时强降水等强对流天气，请加强防范。（预警信息来源：国家预警信息发布中心）"
     */
    
    /** 预警信息唯一标识ID */
    private String id;
    
    /** 预警标题，包含地区、预警类型和级别 */
    private String title;
    
    /** 预警级别，如"蓝色"、"黄色"、"橙色"、"红色" */
    private String level;
    
    /** 预警类型，如"雷暴大风"、"暴雨"、"台风"等 */
    private String type;
    
    /** 预警发布时间，格式为"yyyy-MM-dd HH:mm:ss" */
    private String time;
    
    /** 省份名称 */
    private String province;
    
    /** 城市名称 */
    private String city;
    
    /** 区县名称 */
    private String district;
    
    /** 预警详细内容，包含具体影响和防范建议 */
    private String content;

    /**
     * 获取预警信息ID
     * @return 预警信息唯一标识
     */
    public String getId() {
        return id;
    }

    /**
     * 设置预警信息ID
     * @param id 预警信息唯一标识
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取预警标题
     * @return 预警标题字符串
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置预警标题
     * @param title 预警标题字符串
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取预警级别
     * @return 预警级别字符串
     */
    public String getLevel() {
        return level;
    }

    /**
     * 设置预警级别
     * @param level 预警级别字符串
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * 获取预警类型
     * @return 预警类型字符串
     */
    public String getType() {
        return type;
    }

    /**
     * 设置预警类型
     * @param type 预警类型字符串
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取预警发布时间
     * @return 预警发布时间字符串
     */
    public String getTime() {
        return time;
    }

    /**
     * 设置预警发布时间
     * @param time 预警发布时间字符串
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * 获取省份名称
     * @return 省份名称字符串
     */
    public String getProvince() {
        return province;
    }

    /**
     * 设置省份名称
     * @param province 省份名称字符串
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 获取城市名称
     * @return 城市名称字符串
     */
    public String getCity() {
        return city;
    }

    /**
     * 设置城市名称
     * @param city 城市名称字符串
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取区县名称
     * @return 区县名称字符串
     */
    public String getDistrict() {
        return district;
    }

    /**
     * 设置区县名称
     * @param district 区县名称字符串
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * 获取预警详细内容
     * @return 预警详细内容字符串
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置预警详细内容
     * @param content 预警详细内容字符串
     */
    public void setContent(String content) {
        this.content = content;
    }
}
