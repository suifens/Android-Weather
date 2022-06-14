package com.goodtech.tq.utils;

import com.bytedance.msdk.adapter.gdt.GdtNetworkRequestInfo;
import com.bytedance.msdk.adapter.pangle.PangleNetworkRequestInfo;
import com.bytedance.msdk.api.v2.GMNetworkRequestInfo;

public class SplashUtils {

    public static GMNetworkRequestInfo getGMNetworkRequestInfo(int type) {
        GMNetworkRequestInfo networkRequestInfo;
        switch (type) {
            case 1:
                //gdt兜底
                networkRequestInfo = new GdtNetworkRequestInfo("1101152570", "8863364436303842593");
                break;
            case 2:
            default:
                //穿山甲兜底，参数分别是appId和adn代码位。注意第二个参数是代码位，而不是广告位。
                // networkRequestInfo = new PangleNetworkRequestInfo(Constants.PGE_APP_ID, Constants.SPLASH_POS_ADN);
                networkRequestInfo = new PangleNetworkRequestInfo(Constants.PGE_APP_ID, "887802618");
                break;
        }
//        //ks兜底
//        networkRequestInfo = new KsNetworkRequestInfo("90009", "4000000042");
//        //百度兜底
//        networkRequestInfo = new BaiduNetworkRequestInfo("e866cfb0", "2058622");
//        //Sigmob兜底
//        networkRequestInfo = new SigmobNetworkRequestInfo("6878", "8ebc1fd1c27e650c", "ea1f8f21300");
//        // mintegral兜底
//         networkRequestInfo = new MintegralNetworkRequestInfo("118690", "7c22942b749fe6a6e361b675e96b3ee9", "209547");

        return networkRequestInfo;
    }
}
