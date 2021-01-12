package com.goodtech.tq.news;

/**
 * com.goodtech.tq.news
 */
public enum NewsType {

    TOP("top", "头条"),
    SHE_HUI("shehui", "社会"),
    GUO_NEI("guonei", "国内"),
    GUO_JI("guoji", "国际"),
    YU_LE("yule", "娱乐"),
    TI_YU("tiyu", "体育"),
    JUN_SHI("junshi", "军事"),
    KE_JI("keji", "科技"),
    CAI_JING("caijing", "财经"),
    SHI_SHANG("shishang", "时尚");

    public String enKey;
    public String cnKey;

    NewsType(String en, String cn) {
        this.enKey = en;
        this.cnKey = cn;
    }

    public static NewsType getType(String typeString) {
        NewsType[] allTypes = NewsType.values();
        for (NewsType type : allTypes) {
            if (type.toString().equals(typeString)) {
                return type;
            }
        }
        return TOP;
    }

}
