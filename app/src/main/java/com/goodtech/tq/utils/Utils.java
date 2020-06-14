package com.goodtech.tq.utils;

import com.goodtech.tq.httpClient.ErrorCode;

/**
 * com.goodtech.tq.utils
 *
 * @author: mac
 * @date: 2020/5/23
 */
public class Utils {

    /**
     * 获取ErrorCode类型
     * @param errorCodeStr
     * @return
     */
    public static ErrorCode resolveErrorCode(String errorCodeStr) {
        ErrorCode err= ErrorCode.UNKNOWN;
        try {
            err = ErrorCode.valueOf(errorCodeStr);
        } catch (IllegalArgumentException i) {
            //查询不到类型，采用默认ErrorCode.UNKNOWN
        }
        return err;
    }

}
