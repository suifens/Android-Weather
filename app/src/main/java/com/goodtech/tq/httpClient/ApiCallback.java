
package com.goodtech.tq.httpClient;

import com.goodtech.tq.models.WeatherModel;

import java.util.List;

/**
 * api请求的回调接口
 * 
 * @author baoxiaofeng
 * @date 2015-6-8
 */
public class ApiCallback {

	public void onResponse(boolean success, ErrorCode errCode) {
	}
	
	public void onResponse(boolean success, WeatherModel weather, ErrorCode errCode) {
	}

	public void onResponse(boolean success, String imageUrl, ErrorCode errCode) {
	}

}
