package com.goodtech.tq.others.calendar;

import java.io.Serializable;
import java.util.List;

/**
 * 一个简单的bean
 */
@SuppressWarnings("all")
public class Holiday implements Serializable {

    /**
     * name : 中秋节
     * festival : 2018-9-24
     * desc : 9月24日放假，与周末连休。
     * rest : 拼假建议：2018年9月25日（周二）~2018年9月30日（周日）请假6天，与国庆节衔接，拼16天小长假
     * list : [{"date":"2018-9-22","status":"1"},{"date":"2018-9-23","status":"1"},{"date":"2018-9-24","status":"1"}]
     * list_num : 3
     */

    private String name;
    private String festival;
    private String desc;
    private String rest;
    private int list_num;
    private List<ListDay> list;

    public String getTimeSpan() {
        if (list != null && list.size() > 0) {
            if (list.size() > 1) {
                String first = list.get(0).getDate();
                String last = list.get(list.size() - 1).getDate();

                String firstYear = getYear(first);
                String lastYear = getYear(last);
                if (firstYear.equals(lastYear)) {
                    return getMonthDay(first) + "-" + getMonthDay(last);
                } else {
                    return getData(first) + "-" + getData(last);
                }
            } else {
                return getMonthDay(list.get(0).getDate());
            }
        }
        return "";
    }

    public String getDay() {
        return getMonthDay(festival);
    }

    private String getYear(String time) {
        String[] strings = time.split("-");
        if (strings.length > 0) {
            return strings[0];
        }
        return "";
    }

    private String getMonthDay(String time) {
        String[] strings = time.split("-");
        if (strings.length > 0) {
            return strings[1] + "月" + strings[2] + "日";
        }
        return "";
    }

    private String getData(String time) {
        String[] strings = time.split("-");
        if (strings.length > 0) {
            return strings[0] + "年" + strings[1] + "月" + strings[2] + "日";
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFestival() {
        return festival;
    }

    public void setFestival(String festival) {
        this.festival = festival;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRest() {
        return rest;
    }

    public void setRest(String rest) {
        this.rest = rest;
    }

    public int getList_num() {
        return list_num;
    }

    public void setList_num(int list_num) {
        this.list_num = list_num;
    }

    public List<ListDay> getList() {
        return list;
    }

    public void setList(List<ListDay> list) {
        this.list = list;
    }

    public static class ListDay {
        /**
         * date : 2018-9-22
         * status : 1
         */

        private String date;
        private String status;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
