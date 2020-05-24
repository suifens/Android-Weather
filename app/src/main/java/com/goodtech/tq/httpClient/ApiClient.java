/*
 * Copyright (C) 2015-2016  GenGee Technology Co. Ltd. All rights reserved.
 *
 *                           http://www.GenGee.cn
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user
 * who downloaded the software, his/her employer (which must be your employer)
 * and GenGee Technology Co. Ltd (the "License").  You may not use this
 * Software unless you agree to abide by the terms of the License. The License
 * limits your use, and you acknowledge, that the Software may not be modified,
 * copied or distributed unless embedded on a GenGee Technology intelligent
 * device or system. Other than for the foregoing purpose, you may not use,
 * reproduce, copy, prepare derivative works of, modify, distribute, perform,
 * display or sell this Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * GENGEE TECHNOLOGY CO. LTD OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER
 * CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY,
 * OR OTHER LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
 * INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
 * OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
 * OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
 * (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact GenGee Technology Co. Ltd at www.GenGee.cn.
 */

package com.goodtech.tq.httpClient;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 跟网络有关的工作在这个类中处理
 * 
 * @author baoxiaofeng
 * @date 2016-1-21 上午10:55:25更新
 */
public class ApiClient {

	private static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded"); //普通表单
	private static final MediaType JSON_TYPE = MediaType.parse("application/json");
	private static final MediaType FORM_TYPE = MediaType.parse("multipart/form-data");

	private final int CONNCTION_TIME_OUT = 10; // 连接超时时间
	private final int CONNCTION_MAX = 10; // 最大连接数
	private static OkHttpClient okHttpClient;
	private static ApiClient mApiClient = new ApiClient();
	private String mTokenType = null;
	private String mToken = null;

	private ApiClient() {

		OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
		okHttpClientBuilder.connectTimeout(CONNCTION_TIME_OUT, TimeUnit.SECONDS);
		okHttpClientBuilder.readTimeout(CONNCTION_TIME_OUT, TimeUnit.SECONDS);
		okHttpClientBuilder.writeTimeout(CONNCTION_TIME_OUT, TimeUnit.SECONDS);

		//添加https支持
		okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String s, SSLSession sslSession) {
				return true;
			}
		});
//		okHttpClientBuilder.sslSocketFactory()
		okHttpClient = okHttpClientBuilder.build();

	}

	public static ApiClient getInstance() {
		return mApiClient;
	}

	public void setToken(String tokenType, String token) {
		mTokenType = tokenType;
		mToken = token;
	}

	public void formGet(String url, ApiResponseHandler handler) {
		String reqUrl = getCompleteUrl(url);

		Request.Builder reqBuilder =  new Request.Builder()
				.url(reqUrl)
				.get();

		checkToken(reqBuilder);
		okHttpClient.newCall(reqBuilder.build()).enqueue(handler);
	}

	public void get(String url, JSONObject params, ApiResponseHandler handler) {

		String reqUrl = getCompleteUrl(url);
		HttpUrl.Builder urlBuilder = HttpUrl.parse(reqUrl).newBuilder();
		if (params != null) {
			Iterator keys = params.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				try {
					if (params.get(key) != null) {
						urlBuilder.setQueryParameter(key, (String) params.get(key));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		Request.Builder reqBuilder =  new Request.Builder()
				.url(urlBuilder.build())
				.get();

		checkToken(reqBuilder);
		okHttpClient.newCall(reqBuilder.build())
				.enqueue(handler);
	}

	public void formPost(String url, JSONObject params, ApiResponseHandler handler) {
		String reqUrl = getCompleteUrl(url);

		FormBody.Builder formBuilder = new FormBody.Builder();
		if (params != null) {
			Iterator keys = params.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				try {
					if (params.get(key) != null) {
						formBuilder.add(key, String.valueOf(params.get(key)));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		Request.Builder reqBuilder = new Request.Builder()
				.url(reqUrl)
				.post(formBuilder.build());
		checkToken(reqBuilder);

		okHttpClient.newCall(reqBuilder.build()).enqueue(handler);
	}

	public void post(Context context, String url, JSONObject params, ApiResponseHandler handler) {
		url = getCompleteUrl(url);

		RequestBody requestBody = RequestBody.create(params.toString(), JSON_TYPE);
		Request.Builder reqBuilder = new Request.Builder()
				.url(url)
				.post(requestBody);
		checkToken(reqBuilder);

		okHttpClient.newCall(reqBuilder.build()).enqueue(handler);
	}


	/**
	 * 上传图片
	 */
	public void imagePost(String url, File imageFile, ApiResponseHandler handler) {
		String reqUrl = getCompleteUrl(url);

		MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
		if (imageFile != null) {

			RequestBody body = RequestBody.create(imageFile, MediaType.parse("image/"));
			String fileName = imageFile.getName();

			requestBody.addFormDataPart("file", fileName, body);
		}

		Request.Builder reqBuilder = new Request.Builder()
				.url(reqUrl)
				.post(requestBody.build());
		checkToken(reqBuilder);

		okHttpClient.newCall(reqBuilder.build()).enqueue(handler);

	}


	public void formPut(String url, JSONObject params, ApiResponseHandler handler) {
		String reqUrl = getCompleteUrl(url);

		FormBody.Builder formBuilder = new FormBody.Builder();
		try {
			if (params != null) {
				Iterator keys = params.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (params.get(key) != null) {
						formBuilder.add(key, String.valueOf(params.get(key)));
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Request.Builder reqBuilder =  new Request.Builder()
				.url(reqUrl)
				.put(formBuilder.build());
		checkToken(reqBuilder);

		okHttpClient.newCall(reqBuilder.build())
				.enqueue(handler);
	}

	public void put(Context context, String url, JSONObject params, ApiResponseHandler handler) {
		url = getCompleteUrl(url);

		RequestBody requestBody = RequestBody.create(params.toString(), JSON_TYPE);
		Request.Builder reqBuilder = new Request.Builder()
				.url(url)
				.put(requestBody);
		checkToken(reqBuilder);

		okHttpClient.newCall(reqBuilder.build()).enqueue(handler);
	}

	public void patch(String url, JSONObject params, ApiResponseHandler handler) {
		url = getCompleteUrl(url);
		FormBody.Builder formBuilder = new FormBody.Builder();
		try {
			if (params != null) {
				Iterator keys = params.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (params.get(key) != null) {
						formBuilder.add(key, String.valueOf(params.get(key)));
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Request.Builder reqBuilder =  new Request.Builder()
				.url(url)
				.patch(formBuilder.build());
		checkToken(reqBuilder);
		okHttpClient.newCall(reqBuilder.build())
				.enqueue(handler);
	}

	/**
	 * 取消请求
	 */
	public void cancelRequest(Object tag){
		if (tag == null) return;
		for (Call call : okHttpClient.dispatcher().queuedCalls()) {
			if (tag.equals(call.request().tag())) {
				call.cancel();
			}
		}
		for (Call call : okHttpClient.dispatcher().runningCalls()) {
			if (tag.equals(call.request().tag())) {
				call.cancel();
			}
		}
	}

	/**
	 * 拼接完整的Url
	 * 
	 * @param url
	 *            ApiClient类的常量，如：URL_VERIFICATION，URL_AUTH 等
	 * @author baoxiaofeng
	 */
	public static String getCompleteUrl(String url) {
//		return ApiBaseUrl.getBaseUrl() + url;
		return url;
	}

	private void checkToken(Request.Builder reqBuilder) {
		if (mToken != null && mTokenType != null) {
			HashMap headers = new HashMap();
			headers.put("Access-Token", mToken);
			reqBuilder.headers(Headers.of(headers));
		}
	}

}
