package com.goodtech.tq.utils;

import com.bytedance.msdk.adapter.pangle.PangleNetworkRequestInfo;
import com.bytedance.msdk.api.v2.GMNetworkRequestInfo;

public class SplashUtils {

    public static GMNetworkRequestInfo getGMNetworkRequestInfo() {
        GMNetworkRequestInfo networkRequestInfo;
        networkRequestInfo = new PangleNetworkRequestInfo(Constants.PGE_APP_ID, "887802618");
        return networkRequestInfo;
    }
}
